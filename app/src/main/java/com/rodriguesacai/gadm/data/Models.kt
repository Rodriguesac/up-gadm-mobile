package com.rodriguesacai.gadm.data

import com.google.firebase.Timestamp

enum class UserRole { ADMIN, GESTOR }

data class GadmUser(
    val uid: String,
    val name: String,
    val role: UserRole,
    val email: String
)

enum class OrderStatus(val label: String) {
    RECEBIDO("Recebido"),
    CONFIRMADO("Confirmado"),
    EM_PREPARO("Em preparo"),
    PRONTO("Pronto"),
    AGUARDANDO_ENTREGADOR("Aguardando entregador"),
    EM_ROTA("Em rota"),
    ENTREGUE("Entregue"),
    CANCELADO("Cancelado");

    companion object {
        fun normalize(value: String?): OrderStatus {
            return when (value?.trim()?.uppercase()?.replace(" ", "_")?.replace("Á", "A")?.replace("É", "E")) {
                "NOVO", "PENDENTE", "RECEBIDO" -> RECEBIDO
                "ACEITO", "CONFIRMADO", "APROVADO" -> CONFIRMADO
                "EM_PREPARO", "PREPARANDO", "EM_PRODUCAO", "EM_PRODUÇÃO" -> EM_PREPARO
                "PRONTO", "FINALIZADO_COZINHA" -> PRONTO
                "AGUARDANDO_ENTREGADOR", "AGUARDANDO_MOTOBOY", "DESPACHO" -> AGUARDANDO_ENTREGADOR
                "EM_ROTA", "SAIU_PARA_ENTREGA" -> EM_ROTA
                "ENTREGUE", "CONCLUIDO", "CONCLUÍDO", "FINALIZADO" -> ENTREGUE
                "CANCELADO", "CANCELADA" -> CANCELADO
                else -> RECEBIDO
            }
        }
    }
}

data class OrderItem(
    val quantity: Int,
    val name: String,
    val details: String = ""
)

data class OperationalOrder(
    val id: String,
    val code: String,
    val status: OrderStatus,
    val customerName: String,
    val customerPhone: String,
    val address: String,
    val reference: String,
    val items: List<OrderItem>,
    val total: Double,
    val deliveryFee: Double,
    val payment: String,
    val changeFor: String,
    val notes: String,
    val createdAt: Timestamp?,
    val assignedDriverName: String = "",
    val assignedDriverId: String = "",
    val priority: Boolean = false
)

data class GadmUiState(
    val loading: Boolean = true,
    val busy: Boolean = false,
    val currentUser: GadmUser? = null,
    val orders: List<OperationalOrder> = emptyList(),
    val error: String? = null,
    val info: String? = null
)
