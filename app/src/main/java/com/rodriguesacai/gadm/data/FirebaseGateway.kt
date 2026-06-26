package com.rodriguesacai.gadm.data

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot

class FirebaseGateway {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun restoreSession(onResult: (Result<GadmUser?>) -> Unit) {
        val firebaseUser = try {
            auth.currentUser
        } catch (error: Throwable) {
            onResult(Result.failure(configError(error)))
            return
        }

        if (firebaseUser == null) {
            onResult(Result.success(null))
            return
        }

        loadGadmUser(firebaseUser.uid, firebaseUser.email.orEmpty(), onResult)
    }

    fun signIn(email: String, password: String, onResult: (Result<GadmUser>) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onResult(Result.failure(IllegalArgumentException("Informe o e-mail e a senha do gestor.")))
            return
        }

        try {
            auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener { result ->
                    val firebaseUser = result.user
                    if (firebaseUser == null) {
                        onResult(Result.failure(IllegalStateException("Login concluído sem usuário válido.")))
                    } else {
                        loadGadmUser(firebaseUser.uid, firebaseUser.email.orEmpty()) { profile ->
                            profile.fold(
                                onSuccess = { user ->
                                    if (user == null) {
                                        auth.signOut()
                                        onResult(Result.failure(IllegalStateException("Este usuário não possui perfil GADM.")))
                                    } else onResult(Result.success(user))
                                },
                                onFailure = { onResult(Result.failure(it)) }
                            )
                        }
                    }
                }
                .addOnFailureListener { onResult(Result.failure(it)) }
        } catch (error: Throwable) {
            onResult(Result.failure(configError(error)))
        }
    }

    fun signOut() {
        try { auth.signOut() } catch (_: Throwable) { }
    }

    fun listenOrders(
        onOrders: (List<OperationalOrder>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration? {
        return try {
            db.collection("pedidos").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                onOrders(snapshot?.toOrders().orEmpty())
            }
        } catch (error: Throwable) {
            onError(configError(error))
            null
        }
    }

    fun updateStatus(
        order: OperationalOrder,
        target: OrderStatus,
        actor: GadmUser,
        note: String = "",
        onResult: (Result<Unit>) -> Unit
    ) {
        val updates = hashMapOf<String, Any>(
            "status" to target.name,
            "updatedAt" to FieldValue.serverTimestamp(),
            "ultimoAtualizador" to actor.name,
            "timeline" to FieldValue.arrayUnion(
                hashMapOf(
                    "status" to target.name,
                    "label" to target.label,
                    "at" to Timestamp.now(),
                    "by" to actor.name,
                    "role" to actor.role.name,
                    "note" to note.trim()
                )
            )
        )

        if (target == OrderStatus.CANCELADO) {
            updates["canceladoEm"] = Timestamp.now()
            updates["canceladoPor"] = actor.name
            updates["motivoCancelamento"] = note.trim()
        }
        if (target == OrderStatus.EM_PREPARO) updates["preparoIniciadoEm"] = Timestamp.now()
        if (target == OrderStatus.PRONTO) updates["prontoEm"] = Timestamp.now()
        if (target == OrderStatus.EM_ROTA) updates["saiuParaEntregaEm"] = Timestamp.now()
        if (target == OrderStatus.ENTREGUE) updates["entregueEm"] = Timestamp.now()

        updateDocument(order.id, updates, onResult)
    }

    fun assignDriver(
        order: OperationalOrder,
        driverName: String,
        driverId: String,
        actor: GadmUser,
        onResult: (Result<Unit>) -> Unit
    ) {
        val name = driverName.trim()
        if (name.isBlank()) {
            onResult(Result.failure(IllegalArgumentException("Informe ao menos o nome do entregador.")))
            return
        }

        val updates = hashMapOf<String, Any>(
            "entregadorNome" to name,
            "entregadorId" to driverId.trim(),
            "status" to OrderStatus.AGUARDANDO_ENTREGADOR.name,
            "updatedAt" to FieldValue.serverTimestamp(),
            "ultimoAtualizador" to actor.name,
            "timeline" to FieldValue.arrayUnion(
                hashMapOf(
                    "status" to OrderStatus.AGUARDANDO_ENTREGADOR.name,
                    "label" to "Entregador definido",
                    "at" to Timestamp.now(),
                    "by" to actor.name,
                    "role" to actor.role.name,
                    "note" to "Entregador: $name"
                )
            )
        )
        updateDocument(order.id, updates, onResult)
    }

    fun setPriority(
        order: OperationalOrder,
        priority: Boolean,
        actor: GadmUser,
        onResult: (Result<Unit>) -> Unit
    ) {
        val updates = hashMapOf<String, Any>(
            "prioridade" to priority,
            "updatedAt" to FieldValue.serverTimestamp(),
            "ultimoAtualizador" to actor.name,
            "timeline" to FieldValue.arrayUnion(
                hashMapOf(
                    "status" to order.status.name,
                    "label" to if (priority) "Prioridade ativada" else "Prioridade removida",
                    "at" to Timestamp.now(),
                    "by" to actor.name,
                    "role" to actor.role.name,
                    "note" to ""
                )
            )
        )
        updateDocument(order.id, updates, onResult)
    }

    private fun updateDocument(id: String, updates: Map<String, Any>, onResult: (Result<Unit>) -> Unit) {
        try {
            db.collection("pedidos").document(id).update(updates)
                .addOnSuccessListener { onResult(Result.success(Unit)) }
                .addOnFailureListener { onResult(Result.failure(it)) }
        } catch (error: Throwable) {
            onResult(Result.failure(configError(error)))
        }
    }

    private fun loadGadmUser(uid: String, email: String, onResult: (Result<GadmUser?>) -> Unit) {
        try {
            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    if (!document.exists()) {
                        onResult(Result.success(null))
                        return@addOnSuccessListener
                    }
                    val roleText = document.getString("role")?.trim()?.uppercase().orEmpty()
                    val role = when (roleText) {
                        "ADMIN" -> UserRole.ADMIN
                        "GESTOR" -> UserRole.GESTOR
                        else -> null
                    }
                    if (role == null) {
                        onResult(Result.success(null))
                    } else {
                        onResult(Result.success(
                            GadmUser(
                                uid = uid,
                                name = document.getString("nome")?.takeIf { it.isNotBlank() }
                                    ?: document.getString("displayName")?.takeIf { it.isNotBlank() }
                                    ?: email.substringBefore("@"),
                                role = role,
                                email = email
                            )
                        ))
                    }
                }
                .addOnFailureListener { onResult(Result.failure(it)) }
        } catch (error: Throwable) {
            onResult(Result.failure(configError(error)))
        }
    }

    private fun QuerySnapshot.toOrders(): List<OperationalOrder> {
        return documents.mapNotNull { snapshot -> snapshot.toOperationalOrderOrNull() }
            .sortedWith(
                compareByDescending<OperationalOrder> { it.priority }
                    .thenByDescending { it.createdAt?.seconds ?: 0L }
            )
    }

    private fun DocumentSnapshot.toOperationalOrderOrNull(): OperationalOrder? {
        return try {
            val customer = getMap("cliente").orEmpty()
            val addressMap = getMap("endereco").orEmpty()
            val paymentMap = getMap("pagamento").orEmpty()
            val rawItems = get("itens") as? List<*> ?: get("produtos") as? List<*> ?: emptyList<Any>()
            val parsedItems = rawItems.mapNotNull { raw ->
                val item = raw as? Map<*, *> ?: return@mapNotNull null
                val quantity = (item["quantidade"] ?: item["qtd"] ?: 1).asInt()
                val name = (item["nome"] ?: item["titulo"] ?: item["produto"] ?: "Item").toString()
                val details = listOfNotNull(
                    item["descricao"]?.toString(),
                    item["detalhes"]?.toString(),
                    item["observacao"]?.toString(),
                    item["observações"]?.toString()
                ).firstOrNull().orEmpty()
                OrderItem(quantity, name, details)
            }

            val codeRaw = getString("numero") ?: getString("codigo") ?: id.takeLast(5).uppercase()
            val code = if (codeRaw.startsWith("#")) codeRaw else "#$codeRaw"
            val customerName = firstString(customer, listOf("nome", "name"))
                ?: getString("clienteNome")
                ?: "Cliente não identificado"
            val customerPhone = firstString(customer, listOf("telefone", "phone", "whatsapp"))
                ?: getString("telefone")
                ?: ""
            val street = firstString(addressMap, listOf("logradouro", "rua", "endereco", "endereço"))
                ?: getString("endereco")
                ?: "Endereço não informado"
            val number = firstString(addressMap, listOf("numero", "número"))
            val neighborhood = firstString(addressMap, listOf("bairro"))
            val address = listOfNotNull(street, number, neighborhood).filter { it.isNotBlank() }.joinToString(", ")
            val reference = firstString(addressMap, listOf("referencia", "referência", "complemento"))
                ?: getString("referencia")
                ?: ""
            val payment = firstString(paymentMap, listOf("metodo", "método", "tipo", "forma"))
                ?: getString("formaPagamento")
                ?: getString("pagamento")
                ?: "Não informado"
            val changeFor = firstString(paymentMap, listOf("trocoPara", "troco", "changeFor"))
                ?: getString("trocoPara")
                ?: ""

            OperationalOrder(
                id = id,
                code = code,
                status = OrderStatus.normalize(getString("status")),
                customerName = customerName,
                customerPhone = customerPhone,
                address = address,
                reference = reference,
                items = parsedItems,
                total = (get("total") ?: get("valorTotal") ?: 0).asDouble(),
                deliveryFee = (get("taxaEntrega") ?: get("deliveryFee") ?: 0).asDouble(),
                payment = payment,
                changeFor = changeFor,
                notes = getString("observacoes") ?: getString("observações") ?: getString("nota") ?: "",
                createdAt = getTimestamp("createdAt") ?: getTimestamp("criadoEm"),
                assignedDriverName = getString("entregadorNome") ?: getString("driverName") ?: "",
                assignedDriverId = getString("entregadorId") ?: getString("driverId") ?: "",
                priority = getBoolean("prioridade") ?: false
            )
        } catch (_: Throwable) {
            null
        }
    }

    private fun firstString(map: Map<String, Any>, keys: List<String>): String? {
        return keys.firstNotNullOfOrNull { key -> map[key]?.toString()?.takeIf { it.isNotBlank() } }
    }

    private fun Any?.asDouble(): Double = when (this) {
        is Number -> toDouble()
        is String -> replace("R$", "").replace(".", "").replace(",", ".").trim().toDoubleOrNull() ?: 0.0
        else -> 0.0
    }

    private fun Any?.asInt(): Int = when (this) {
        is Number -> toInt().coerceAtLeast(1)
        is String -> toIntOrNull()?.coerceAtLeast(1) ?: 1
        else -> 1
    }

    private fun configError(error: Throwable): Throwable {
        val message = error.message.orEmpty()
        return if (message.contains("Default FirebaseApp", ignoreCase = true)) {
            IllegalStateException("Firebase não está configurado. Copie o google-services.json real para a pasta app/ antes de usar o GADM.", error)
        } else error
    }
}
