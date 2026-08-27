package com.rodriguesacai.gadm.data

data class PixChangeAdminRequest(
    val id: String,
    val orderId: String,
    val orderCode: String,
    val customerName: String,
    val amount: Double,
    val cashExpected: Double,
    val cashReceivedAmount: Double?,
    val cashReceivedAt: String?,
    val status: String,
    val pixKeyType: String,
    val pixKey: String,
    val recipientName: String,
    val bank: String,
    val pixSentAt: String?,
    val pixReference: String,
    val createdAt: String,
    val updatedAt: String
) {
    val isReleased: Boolean
        get() = cashReceivedAt != null && status in setOf("DINHEIRO_RECEBIDO", "PIX_PENDENTE")

    val isSent: Boolean
        get() = status == "PIX_ENVIADO" || status == "CONFIRMADO"
}
