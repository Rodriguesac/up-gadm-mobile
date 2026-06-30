package com.rodriguesacai.gadm.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

/**
 * Fonte única de comandos do UP GADM Mobile.
 *
 * Regras importantes:
 * - Uma oferta enviada ainda NÃO é corrida ativa.
 * - pedidoAtualId/corridaAtualId só pertencem ao entregador depois que o app dele aceitar a missão.
 * - Destravar sempre limpa entregador + pedido + ride + rota relacionada.
 * - Taxa cobrada do cliente nunca é usada como repasse do entregador.
 */
class GadmRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeOrders(): Flow<List<GadmOrder>> = observeCollection("pedidos") { it.toGadmOrder() }
    fun observeDrivers(): Flow<List<GadmDriver>> = observeCollection("entregadores") { it.toGadmDriver() }
    fun observeCustomers(): Flow<List<GadmCustomer>> = observeCollection("clientes") { it.toGadmCustomer() }
    fun observeIncidents(): Flow<List<GadmIncident>> = observeCollection("ocorrencias") { it.toGadmIncident() }
    fun observeProducts(): Flow<List<GadmProduct>> = observeCollection("produtos") { it.toGadmProduct() }
    fun observeFinance(): Flow<List<GadmFinanceEntry>> = observeCollection("financeiro_movimentos") { it.toGadmFinanceEntry() }

    fun observeStoreOperation(): Flow<StoreOperation> = callbackFlow {
        val registration = db.collection("configuracoes_loja").document("operacao")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(StoreOperation())
                    return@addSnapshotListener
                }
                val data = snapshot?.data ?: emptyMap()
                trySend(
                    StoreOperation(
                        open = data["lojaAberta"] as? Boolean ?: true,
                        acceptOrders = data["aceitarPedidos"] as? Boolean ?: true,
                        maintenance = data["manutencao"] as? Boolean ?: false,
                        message = data["mensagemOperacao"]?.toString().orEmpty(),
                        estimatedMinutes = (data["tempoEstimadoMin"] as? Number)?.toInt() ?: 45
                    )
                )
            }
        awaitClose { registration.remove() }
    }

    private fun <T> observeCollection(name: String, mapper: (DocumentSnapshot) -> T): Flow<List<T>> = callbackFlow {
        val registration = db.collection(name).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            trySend(snapshot?.documents?.map(mapper).orEmpty())
        }
        awaitClose { registration.remove() }
    }

    suspend fun signIn(pin: String): Result<GadmUser> = runCatching {
        require(pin.length == 5 && pin.all(Char::isDigit)) { "Digite os 5 números do PIN." }
        val hash = sha256(pin)
        val results = db.collection("usuarios_gadm")
            .whereEqualTo("pinHash", hash)
            .whereEqualTo("ativo", true)
            .limit(1)
            .get()
            .await()

        results.documents.firstOrNull()?.let { document ->
            return@runCatching GadmUser(
                id = document.id,
                name = document.getString("nome") ?: "Gestor",
                role = document.getString("perfil") ?: "ADMIN",
                active = true
            )
        }

        if (pin == FIRST_ACCESS_PIN) {
            db.collection("usuarios_gadm").document("master").set(
                mapOf(
                    "nome" to "Administrador",
                    "perfil" to "ADMIN",
                    "ativo" to true,
                    "pinHash" to hash,
                    "criadoEm" to FieldValue.serverTimestamp(),
                    "atualizadoEm" to FieldValue.serverTimestamp(),
                    "origem" to ORIGIN
                ),
                SetOptions.merge()
            ).await()
            GadmUser("master", "Administrador", "ADMIN", true)
        } else {
            error("PIN inválido ou usuário sem permissão.")
        }
    }

    suspend fun changePin(userId: String, newPin: String): Result<Unit> = runCatching {
        require(newPin.length == 5 && newPin.all(Char::isDigit)) { "O novo PIN precisa ter 5 números." }
        db.collection("usuarios_gadm").document(userId).set(
            mapOf("pinHash" to sha256(newPin), "atualizadoEm" to FieldValue.serverTimestamp()),
            SetOptions.merge()
        ).await()
    }

    /**
     * Operação do botão ACEITAR: atualiza o documento real em /pedidos/{order.id}
     * e deixa o pedido imediatamente visível na fila da cozinha.
     */
    suspend fun acceptAndStartPreparation(order: GadmOrder): Result<Unit> = runCatching {
        require(order.id.isNotBlank()) { "Pedido sem identificador do Firestore." }
        db.collection("pedidos").document(order.id).set(
            mapOf(
                "status" to "EM_PREPARO",
                "statusPedido" to "CONFIRMADO",
                "statusProducao" to "EM_PREPARO",
                "aceitoEm" to FieldValue.serverTimestamp(),
                "preparoIniciadoEm" to FieldValue.serverTimestamp(),
                "atualizadoEm" to FieldValue.serverTimestamp(),
                "atualizadoPor" to ORIGIN
            ),
            SetOptions.merge()
        ).await()
        audit("PEDIDO_ACEITO", order.id, "Pedido aceito e enviado ao preparo", "Documento /pedidos/${order.id}")
    }

    suspend fun updateOrderStatus(
        order: GadmOrder,
        statusPedido: String? = null,
        statusProducao: String? = null,
        statusEntrega: String? = null,
        note: String = ""
    ): Result<Unit> = runCatching {
        val changes = mutableMapOf<String, Any>("atualizadoEm" to FieldValue.serverTimestamp())
        statusPedido?.let { changes["statusPedido"] = it }
        statusProducao?.let { changes["statusProducao"] = it }
        statusEntrega?.let { changes["statusEntrega"] = it }
        listOfNotNull(statusProducao, statusPedido, statusEntrega).firstOrNull()?.let { changes["status"] = it }
        db.collection("pedidos").document(order.id).set(changes, SetOptions.merge()).await()
        audit("PEDIDO_STATUS", order.id, listOfNotNull(statusPedido, statusProducao, statusEntrega).joinToString(" "), note)
    }

    suspend fun cancelOrder(order: GadmOrder, reason: String): Result<Unit> = runCatching {
        val related = missionRefs(order.id, order.driverId, "")
        db.runBatch { batch ->
            batch.set(
                db.collection("pedidos").document(order.id),
                mapOf(
                    "status" to "CANCELADO",
                    "statusPedido" to "CANCELADO",
                    "statusProducao" to "CANCELADO",
                    "statusEntrega" to "CANCELADO",
                    "statusCorrida" to "CANCELADA",
                    "motivoCancelamento" to reason,
                    "corridaAtiva" to false,
                    "ocorrenciaAtiva" to false,
                    "liberadoParaEntregador" to false,
                    "atualizadoEm" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            markMissionRefsCancelled(batch, related, reason)
            if (order.driverId.isNotBlank()) {
                writeDriverReleased(batch, order.driverId, reason, online = true)
            }
        }.await()
        audit("PEDIDO_CANCELADO", order.id, "Pedido cancelado", reason)
    }

    /** Envia oferta sem transformar o entregador em "em corrida" antes do aceite. */
    suspend fun assignDriver(order: GadmOrder, driver: GadmDriver): Result<Unit> = runCatching {
        require(driver.approved && !driver.blocked) { "Entregador não está apto para receber corrida." }
        require(driver.currentOrderId.isBlank() && driver.currentRideId.isBlank()) { "Entregador já possui corrida ativa." }
        require(driver.pendingOfferOrderId.isBlank() && driver.pendingOfferRideId.isBlank()) { "Entregador já possui oferta pendente." }

        val rideId = "ride_${order.id}_${driver.id}"
        val orderRef = db.collection("pedidos").document(order.id)
        val rideRef = db.collection("rides").document(rideId)
        val driverRef = db.collection("entregadores").document(driver.id)

        db.runBatch { batch ->
            batch.set(
                orderRef,
                mapOf(
                    "entregadorOfertaId" to driver.id,
                    "entregadorOfertaNome" to driver.name,
                    "statusEntrega" to "AGUARDANDO_ACEITE",
                    "statusCorrida" to "NOVA_OFERTA",
                    "corridaAtiva" to false,
                    "ofertaAtiva" to true,
                    "atualizadoEm" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            batch.set(
                rideRef,
                mapOf(
                    "pedidoId" to order.id,
                    "numeroPedido" to order.code,
                    "entregadorId" to driver.id,
                    "entregadorNome" to driver.name,
                    "status" to "NOVA_OFERTA",
                    "statusCorrida" to "NOVA_OFERTA",
                    "corridaAtiva" to false,
                    "ofertaAtiva" to true,
                    "valorCorrida" to findDriverPay(order.raw),
                    "criadoEm" to FieldValue.serverTimestamp(),
                    "atualizadoEm" to FieldValue.serverTimestamp(),
                    "origem" to ORIGIN
                ),
                SetOptions.merge()
            )
            batch.set(
                driverRef,
                mapOf(
                    "ofertaPedidoId" to order.id,
                    "ofertaCorridaId" to rideId,
                    "statusOperacional" to "OFERTA_ENVIADA",
                    "atualizadoEm" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
        }.await()
        audit("CORRIDA_OFERTADA", order.id, "Oferta enviada para ${driver.name}", "Ride: $rideId")
    }

    suspend fun cancelPendingOffer(driver: GadmDriver, reason: String = "Oferta cancelada pelo gestor"): Result<Unit> = runCatching {
        val orderId = driver.pendingOfferOrderId
        val rideId = driver.pendingOfferRideId
        val related = missionRefs(orderId, driver.id, rideId)
        db.runBatch { batch ->
            markMissionRefsCancelled(batch, related, reason)
            if (orderId.isNotBlank()) {
                batch.set(
                    db.collection("pedidos").document(orderId),
                    mapOf(
                        "entregadorOfertaId" to "",
                        "entregadorOfertaNome" to "",
                        "statusEntrega" to "AGUARDANDO_ENTREGADOR",
                        "statusCorrida" to "CANCELADA",
                        "ofertaAtiva" to false,
                        "corridaAtiva" to false,
                        "atualizadoEm" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
            }
            writeDriverReleased(batch, driver.id, reason, online = driver.online)
        }.await()
        audit("OFERTA_CANCELADA", driver.id, driver.name, reason)
    }

    suspend fun approveDriver(driver: GadmDriver): Result<Unit> = runCatching {
        db.collection("entregadores").document(driver.id).set(
            mapOf(
                "aprovado" to true,
                "bloqueado" to false,
                "statusCadastro" to "APROVADO",
                "status" to "Livre",
                "statusOperacional" to "DISPONIVEL",
                "atualizadoEm" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
        audit("ENTREGADOR_APROVADO", driver.id, driver.name, "Cadastro aprovado")
    }

    suspend fun requestDriverCorrection(driver: GadmDriver, reason: String): Result<Unit> = runCatching {
        db.collection("entregadores").document(driver.id).set(
            mapOf(
                "aprovado" to false,
                "statusCadastro" to "CORRIGIR_DADOS",
                "motivoCorrecao" to reason,
                "atualizadoEm" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
        sendDriverNotice(driver.id, "Corrija seu cadastro", reason, "COMPLETAR_CADASTRO")
        audit("ENTREGADOR_CORRECAO", driver.id, driver.name, reason)
    }

    suspend fun setDriverBlocked(driver: GadmDriver, blocked: Boolean, reason: String = ""): Result<Unit> = runCatching {
        if (blocked) {
            releaseDriver(driver, reason.ifBlank { "Bloqueado pelo gestor" }).getOrThrow()
        }
        db.collection("entregadores").document(driver.id).set(
            mapOf(
                "bloqueado" to blocked,
                "statusCadastro" to if (blocked) "BLOQUEADO" else "APROVADO",
                "status" to if (blocked) "Bloqueado" else "Livre",
                "statusOperacional" to if (blocked) "BLOQUEADO" else "DISPONIVEL",
                "motivoBloqueio" to reason,
                "aceitaNovasOfertas" to !blocked,
                "atualizadoEm" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
        audit(if (blocked) "ENTREGADOR_BLOQUEADO" else "ENTREGADOR_DESBLOQUEADO", driver.id, driver.name, reason)
    }

    /**
     * Botão de segurança do GADM: limpa todos os vínculos que possam prender o entregador.
     */
    suspend fun releaseDriver(driver: GadmDriver, reason: String = "Liberado pelo gestor"): Result<Unit> = runCatching {
        val latest = driverLinks(driver.id)
        val orderId = driver.currentOrderId.ifBlank { driver.pendingOfferOrderId.ifBlank { latest.orderId } }
        val rideId = driver.currentRideId.ifBlank { driver.pendingOfferRideId.ifBlank { latest.rideId } }
        val related = missionRefs(orderId, driver.id, rideId)

        db.runBatch { batch ->
            markMissionRefsCancelled(batch, related, reason)
            if (orderId.isNotBlank()) {
                batch.set(
                    db.collection("pedidos").document(orderId),
                    mapOf(
                        "entregadorId" to "",
                        "entregadorNome" to "",
                        "entregadorOfertaId" to "",
                        "entregadorOfertaNome" to "",
                        "statusEntrega" to "AGUARDANDO_ENTREGADOR",
                        "statusCorrida" to "CANCELADA",
                        "corridaAtiva" to false,
                        "ocorrenciaAtiva" to false,
                        "ofertaAtiva" to false,
                        "liberadoParaEntregador" to true,
                        "motivoDestravamento" to reason,
                        "atualizadoEm" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
            }
            writeDriverReleased(batch, driver.id, reason, online = driver.online)
        }.await()
        audit("ENTREGADOR_LIBERADO", driver.id, driver.name, reason)
    }

    suspend fun resolveIncident(incident: GadmIncident, releaseDriver: Boolean): Result<Unit> = runCatching {
        if (releaseDriver && incident.driverId.isNotBlank()) {
            val live = db.collection("entregadores").document(incident.driverId).get().await().toGadmDriver()
            releaseDriver(live, "Ocorrência ${incident.id} resolvida no GADM").getOrThrow()
        }

        db.runBatch { batch ->
            batch.set(
                db.collection("ocorrencias").document(incident.id),
                mapOf(
                    "status" to "RESOLVIDA",
                    "resolvidaEm" to FieldValue.serverTimestamp(),
                    "atualizadoEm" to FieldValue.serverTimestamp(),
                    "resolvidaPor" to ORIGIN
                ),
                SetOptions.merge()
            )
            if (incident.orderId.isNotBlank()) {
                batch.set(
                    db.collection("pedidos").document(incident.orderId),
                    mapOf(
                        "ocorrenciaAtiva" to false,
                        "statusOcorrencia" to "RESOLVIDA",
                        "atualizadoEm" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
            }
        }.await()
        audit("OCORRENCIA_RESOLVIDA", incident.id, incident.title, if (releaseDriver) "Entregador liberado" else "Mantido em operação")
    }

    suspend fun updateStoreOperation(operation: StoreOperation): Result<Unit> = runCatching {
        db.collection("configuracoes_loja").document("operacao").set(
            mapOf(
                "lojaAberta" to operation.open,
                "aceitarPedidos" to operation.acceptOrders,
                "manutencao" to operation.maintenance,
                "mensagemOperacao" to operation.message,
                "tempoEstimadoMin" to operation.estimatedMinutes,
                "atualizadoEm" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
        audit("OPERACAO_LOJA", "operacao", "Atualização operacional", operation.message)
    }

    suspend fun toggleProduct(product: GadmProduct, paused: Boolean): Result<Unit> = runCatching {
        db.collection("produtos").document(product.id).set(
            mapOf("pausado" to paused, "indisponivel" to paused, "atualizadoEm" to FieldValue.serverTimestamp()),
            SetOptions.merge()
        ).await()
        audit("PRODUTO_${if (paused) "PAUSADO" else "ATIVADO"}", product.id, product.name, product.category)
    }

    suspend fun sendGlobalCommunication(
        title: String,
        message: String,
        type: String,
        action: String,
        active: Boolean
    ): Result<Unit> = runCatching {
        val id = "comunicado_${System.currentTimeMillis()}"
        db.collection("comunicados_entregador").document(id).set(
            mapOf(
                "titulo" to title,
                "mensagem" to message,
                "tipo" to type,
                "acao" to action,
                "ativo" to active,
                "mostrarUmaVez" to true,
                "origem" to ORIGIN,
                "criadoEm" to FieldValue.serverTimestamp(),
                "atualizadoEm" to FieldValue.serverTimestamp()
            )
        ).await()
        audit("COMUNICADO_CRIADO", id, title, "$type / $action")
    }

    private suspend fun missionRefs(orderId: String, driverId: String, rideId: String): List<DocumentReference> {
        val refs = linkedMapOf<String, DocumentReference>()
        fun add(ref: DocumentReference) { refs[ref.path] = ref }

        if (rideId.isNotBlank()) {
            add(db.collection("rides").document(rideId))
            add(db.collection("corridas").document(rideId))
            add(db.collection("rotas_entrega").document(rideId))
        }
        if (orderId.isNotBlank()) {
            listOf("rides", "corridas", "rotas_entrega").forEach { collection ->
                db.collection(collection).whereEqualTo("pedidoId", orderId).get().await().documents.forEach { add(it.reference) }
            }
        }
        return refs.values.toList()
    }

    private fun markMissionRefsCancelled(batch: WriteBatch, refs: List<DocumentReference>, reason: String) {
        refs.forEach { ref ->
            batch.set(
                ref,
                mapOf(
                    "status" to "CANCELADA",
                    "statusCorrida" to "CANCELADA",
                    "corridaAtiva" to false,
                    "ocorrenciaAtiva" to false,
                    "ofertaAtiva" to false,
                    "motivoCancelamento" to reason,
                    "atualizadoEm" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
        }
    }

    private fun writeDriverReleased(batch: WriteBatch, driverId: String, reason: String, online: Boolean) {
        batch.set(
            db.collection("entregadores").document(driverId),
            mapOf(
                "status" to "Livre",
                "statusOperacional" to "DISPONIVEL",
                "online" to online,
                "aceitaNovasOfertas" to online,
                "emCorrida" to false,
                "corridaAtiva" to false,
                "ocorrenciaAtual" to "",
                "pedidoAtualId" to "",
                "corridaAtualId" to "",
                "rideAtualId" to "",
                "rotaAtualId" to "",
                "currentOrderId" to "",
                "currentRideId" to "",
                "ofertaPedidoId" to "",
                "ofertaCorridaId" to "",
                "ultimoDestravamento" to reason,
                "atualizadoEm" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        )
    }

    private data class DriverLinks(val orderId: String = "", val rideId: String = "")

    private suspend fun driverLinks(driverId: String): DriverLinks {
        val snapshot = db.collection("entregadores").document(driverId).get().await()
        val map = snapshot.data.orEmpty()
        fun field(vararg names: String) = names.firstNotNullOfOrNull { map[it]?.toString()?.takeIf(String::isNotBlank) }.orEmpty()
        return DriverLinks(
            orderId = field("pedidoAtualId", "currentOrderId", "ofertaPedidoId", "pendingOfferOrderId"),
            rideId = field("corridaAtualId", "rideAtualId", "currentRideId", "ofertaCorridaId", "pendingOfferRideId")
        )
    }

    private suspend fun sendDriverNotice(driverId: String, title: String, message: String, action: String) {
        db.collection("comunicados_entregador").document("driver_${driverId}_${System.currentTimeMillis()}").set(
            mapOf(
                "entregadorId" to driverId,
                "titulo" to title,
                "mensagem" to message,
                "tipo" to "ALERTA",
                "acao" to action,
                "ativo" to true,
                "mostrarUmaVez" to false,
                "origem" to ORIGIN,
                "criadoEm" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    private suspend fun audit(type: String, targetId: String, title: String, detail: String) {
        db.collection("auditoria").add(
            mapOf(
                "tipo" to type,
                "alvoId" to targetId,
                "titulo" to title,
                "detalhe" to detail,
                "origem" to ORIGIN,
                "criadoEm" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    private fun findDriverPay(raw: Map<String, Any?>): Double {
        val keys = listOf("valorCorrida", "valorRepasseEntregador", "valorRepasseMotoboy", "repasseFrota", "repassePiloto", "valorTotalMotoboy")
        keys.forEach { key ->
            when (val value = raw[key]) {
                is Number -> return value.toDouble()
                is String -> value.replace("R$", "").replace(".", "").replace(",", ".").trim().toDoubleOrNull()?.let { return it }
            }
        }
        return 0.0
    }

    private fun sha256(input: String): String = MessageDigest.getInstance("SHA-256")
        .digest(input.toByteArray())
        .joinToString("") { "%02x".format(it) }

    companion object {
        private const val FIRST_ACCESS_PIN = "12345"
        private const val ORIGIN = "UP GADM Mobile"
    }
}
