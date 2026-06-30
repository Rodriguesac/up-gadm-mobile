package com.rodriguesacai.gadm.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun DocumentSnapshot.text(vararg keys: String): String {
    keys.forEach { key ->
        val value = get(key)
        if (value != null) return value.toString()
    }
    return ""
}


private fun Any?.asText(): String = when (this) {
    null -> ""
    is String -> trim()
    is Number -> toString()
    else -> toString().trim()
}

private fun Map<*, *>.valueOf(vararg keys: String): String {
    keys.forEach { key ->
        val value = this[key].asText()
        if (value.isNotBlank()) return value
    }
    return ""
}

private fun addressFromMap(map: Map<*, *>): String {
    val street = map.valueOf("logradouro", "rua", "street", "endereco", "address")
    val number = map.valueOf("numero", "número", "number")
    val complement = map.valueOf("complemento", "complement", "referencia", "referência")
    val neighborhood = map.valueOf("bairro", "neighborhood")
    val city = map.valueOf("cidade", "city")
    val state = map.valueOf("uf", "estado", "state")
    val zip = map.valueOf("cep", "zipCode", "postalCode")

    val firstLine = listOfNotNull(
        street.takeIf { it.isNotBlank() },
        number.takeIf { it.isNotBlank() }
    ).joinToString(", ")
    val secondLine = listOfNotNull(
        complement.takeIf { it.isNotBlank() },
        neighborhood.takeIf { it.isNotBlank() },
        listOf(city.takeIf { it.isNotBlank() }, state.takeIf { it.isNotBlank() })
            .filterNotNull().joinToString(" - ").takeIf { it.isNotBlank() },
        zip.takeIf { it.isNotBlank() }
    ).joinToString(" • ")

    return listOf(firstLine, secondLine).filter { it.isNotBlank() }.joinToString(" • ")
}

private fun DocumentSnapshot.addressText(): String {
    val keys = listOf("enderecoCompleto", "entregaEndereco", "address", "endereco")
    keys.forEach { key ->
        when (val value = get(key)) {
            is String -> if (value.isNotBlank()) return value.trim()
            is Map<*, *> -> addressFromMap(value).takeIf { it.isNotBlank() }?.let { return it }
        }
    }

    val direct = mapOf(
        "logradouro" to get("logradouro"),
        "numero" to get("numero"),
        "complemento" to get("complemento"),
        "bairro" to get("bairro"),
        "cidade" to get("cidade"),
        "uf" to get("uf"),
        "cep" to get("cep")
    )
    return addressFromMap(direct)
}

private fun DocumentSnapshot.itemsText(): String {
    text("resumoItens", "itensResumo", "itemsLabel", "itensTexto").takeIf { it.isNotBlank() }?.let { return it }
    val items = get("itens") as? List<*> ?: get("items") as? List<*> ?: return ""
    return items.mapNotNull { entry ->
        when (entry) {
            is String -> entry.trim().takeIf { it.isNotBlank() }
            is Map<*, *> -> {
                val name = entry.valueOf("nome", "name", "produto", "titulo", "title")
                val quantity = entry.valueOf("quantidade", "qtd", "quantity")
                when {
                    name.isBlank() -> null
                    quantity.isBlank() || quantity == "1" -> name
                    else -> "$quantity× $name"
                }
            }
            else -> null
        }
    }.joinToString(" • ")
}

private fun DocumentSnapshot.bool(vararg keys: String): Boolean {
    keys.forEach { key ->
        when (val value = get(key)) {
            is Boolean -> return value
            is String -> if (value.equals("true", true) || value == "1") return true
            is Number -> return value.toInt() != 0
        }
    }
    return false
}

private fun DocumentSnapshot.amount(vararg keys: String): Double {
    keys.forEach { key ->
        when (val value = get(key)) {
            is Number -> return value.toDouble()
            is String -> value.replace("R$", "").replace(".", "").replace(",", ".").trim().toDoubleOrNull()?.let { return it }
        }
    }
    return 0.0
}

private fun DocumentSnapshot.time(vararg keys: String): Long {
    keys.forEach { key ->
        when (val value = get(key)) {
            is Timestamp -> return value.toDate().time
            is Date -> return value.time
            is Number -> return value.toLong()
            is String -> value.toLongOrNull()?.let { return it }
        }
    }
    return 0L
}

data class GadmOrder(
    val id: String,
    val code: String,
    val customerName: String,
    val customerPhone: String,
    val address: String,
    val itemsLabel: String,
    val total: Double,
    val deliveryFee: Double,
    val payment: String,
    val orderStatus: String,
    val kitchenStatus: String,
    val deliveryStatus: String,
    val driverId: String,
    val driverName: String,
    val createdAt: Long,
    val priority: Boolean,
    val raw: Map<String, Any?> = emptyMap()
) {
    val currentStage: String
        get() = when {
            deliveryStatus.uppercase() in setOf("EM_ROTA", "EM_ENTREGA", "ENTREGADOR_NO_LOCAL") -> "Em entrega"
            kitchenStatus.uppercase() in setOf("EM_PREPARO", "PREPARANDO") -> "Em preparo"
            kitchenStatus.uppercase() in setOf("PRONTO", "FINALIZADO") -> "Pronto"
            orderStatus.uppercase() in setOf("CANCELADO", "CANCELADA") -> "Cancelado"
            orderStatus.uppercase() in setOf("FINALIZADO", "CONCLUIDO", "ENTREGUE") -> "Finalizado"
            driverId.isNotBlank() -> "Na torre"
            else -> "Novo"
        }
}

data class GadmDriver(
    val id: String,
    val name: String,
    val phone: String,
    val vehicle: String,
    val plate: String,
    val status: String,
    val operationalStatus: String,
    val approved: Boolean,
    val blocked: Boolean,
    val online: Boolean,
    val pendingDocs: Boolean,
    val currentOrderId: String,
    val currentRideId: String,
    val pendingOfferOrderId: String,
    val pendingOfferRideId: String,
    val pix: String,
    val createdAt: Long,
    val raw: Map<String, Any?> = emptyMap()
) {
    val availabilityLabel: String
        get() = when {
            blocked -> "Bloqueado"
            !approved -> "Em análise"
            currentOrderId.isNotBlank() || currentRideId.isNotBlank() -> "Em corrida"
            pendingOfferOrderId.isNotBlank() || pendingOfferRideId.isNotBlank() -> "Oferta enviada"
            online -> "Disponível"
            else -> "Offline"
        }
}

data class GadmCustomer(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val ordersCount: Int,
    val totalSpent: Double,
    val lastOrderAt: Long,
    val raw: Map<String, Any?> = emptyMap()
)

data class GadmIncident(
    val id: String,
    val title: String,
    val description: String,
    val severity: String,
    val status: String,
    val orderId: String,
    val driverId: String,
    val driverName: String,
    val createdAt: Long,
    val raw: Map<String, Any?> = emptyMap()
)

data class GadmProduct(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val stock: Double,
    val active: Boolean,
    val paused: Boolean,
    val raw: Map<String, Any?> = emptyMap()
)

data class GadmFinanceEntry(
    val id: String,
    val type: String,
    val description: String,
    val amount: Double,
    val status: String,
    val createdAt: Long,
    val raw: Map<String, Any?> = emptyMap()
)

data class GadmUser(
    val id: String,
    val name: String,
    val role: String,
    val active: Boolean
)

data class StoreOperation(
    val open: Boolean = true,
    val acceptOrders: Boolean = true,
    val maintenance: Boolean = false,
    val message: String = "",
    val estimatedMinutes: Int = 45
)

fun DocumentSnapshot.toGadmOrder() = GadmOrder(
    id = id,
    code = text("numeroPedido", "codigoPedido", "codigo", "orderNumber").ifBlank { "#$id" },
    customerName = text("clienteNome", "nomeCliente", "customerName", "cliente.nome").ifBlank { "Cliente não identificado" },
    customerPhone = text("clienteTelefone", "telefone", "customerPhone"),
    address = addressText(),
    itemsLabel = itemsText().ifBlank { "Ver itens no pedido" },
    total = amount("valorTotal", "total", "valorPedido"),
    deliveryFee = amount("taxaEntrega", "frete", "valorEntrega"),
    payment = text("formaPagamento", "pagamento", "paymentMethod").ifBlank { "Não informado" },
    orderStatus = text("statusPedido", "status", "orderStatus").ifBlank { "RECEBIDO" },
    kitchenStatus = text("statusProducao", "statusCozinha", "kitchenStatus").ifBlank { "AGUARDANDO" },
    deliveryStatus = text("statusEntrega", "statusCorrida", "deliveryStatus").ifBlank { "AGUARDANDO" },
    driverId = text("entregadorId", "driverId"),
    driverName = text("entregadorNome", "driverName"),
    createdAt = time("criadoEm", "createdAt", "dataCriacao", "atualizadoEm"),
    priority = bool("prioritario", "prioridade", "isPriority"),
    raw = data ?: emptyMap()
)

fun DocumentSnapshot.toGadmDriver() = GadmDriver(
    id = id,
    name = text("nomeCompleto", "nome", "name").ifBlank { "Entregador" },
    phone = text("telefone", "phone"),
    vehicle = text("veiculo", "tipoVeiculo", "vehicleType"),
    plate = text("placa", "plate"),
    status = text("status", "situacao"),
    operationalStatus = text("statusOperacional", "operationalStatus"),
    approved = bool("aprovado", "approved") || text("statusCadastro").equals("APROVADO", true),
    blocked = bool("bloqueado", "blocked") || text("statusCadastro").equals("BLOQUEADO", true),
    online = bool("online", "aceitaNovasOfertas") || text("statusOnline").equals("ONLINE", true),
    pendingDocs = text("statusCadastro").equals("EM_ANALISE", true) || bool("documentosPendentes"),
    currentOrderId = text("pedidoAtualId", "currentOrderId"),
    currentRideId = text("corridaAtualId", "currentRideId", "rideAtualId"),
    pendingOfferOrderId = text("ofertaPedidoId", "pendingOfferOrderId"),
    pendingOfferRideId = text("ofertaCorridaId", "pendingOfferRideId"),
    pix = text("pix", "chavePix"),
    createdAt = time("criadoEm", "createdAt"),
    raw = data ?: emptyMap()
)

fun DocumentSnapshot.toGadmCustomer() = GadmCustomer(
    id = id,
    name = text("nome", "nomeCompleto", "name").ifBlank { "Cliente" },
    phone = text("telefone", "phone"),
    email = text("email"),
    address = when (val rawAddress = get("enderecoPrincipal") ?: get("endereco") ?: get("address")) {
        is String -> rawAddress.trim()
        is Map<*, *> -> addressFromMap(rawAddress)
        else -> ""
    },
    ordersCount = amount("quantidadePedidos", "ordersCount", "totalPedidos").toInt(),
    totalSpent = amount("totalGasto", "totalSpent"),
    lastOrderAt = time("ultimoPedidoEm", "lastOrderAt"),
    raw = data ?: emptyMap()
)

fun DocumentSnapshot.toGadmIncident() = GadmIncident(
    id = id,
    title = text("titulo", "title", "tipo").ifBlank { "Ocorrência operacional" },
    description = text("descricao", "description", "mensagem"),
    severity = text("gravidade", "severity").ifBlank { "MÉDIA" },
    status = text("status").ifBlank { "ABERTA" },
    orderId = text("pedidoId", "orderId"),
    driverId = text("entregadorId", "driverId"),
    driverName = text("entregadorNome", "driverName"),
    createdAt = time("criadoEm", "createdAt"),
    raw = data ?: emptyMap()
)

fun DocumentSnapshot.toGadmProduct() = GadmProduct(
    id = id,
    name = text("nome", "name").ifBlank { "Produto" },
    category = text("categoriaNome", "categoria", "category"),
    price = amount("preco", "valor", "price"),
    stock = amount("estoqueAtual", "estoque", "stock"),
    active = !bool("inativo", "archived"),
    paused = bool("pausado", "paused", "indisponivel"),
    raw = data ?: emptyMap()
)

fun DocumentSnapshot.toGadmFinanceEntry() = GadmFinanceEntry(
    id = id,
    type = text("tipo", "type").ifBlank { "MOVIMENTO" },
    description = text("descricao", "description", "titulo").ifBlank { "Movimento financeiro" },
    amount = amount("valor", "amount", "total"),
    status = text("status").ifBlank { "PENDENTE" },
    createdAt = time("criadoEm", "createdAt"),
    raw = data ?: emptyMap()
)

fun money(value: Double): String = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)

fun dateTime(value: Long): String {
    if (value <= 0L) return "Agora"
    return SimpleDateFormat("dd/MM • HH:mm", Locale("pt", "BR")).format(Date(value))
}
