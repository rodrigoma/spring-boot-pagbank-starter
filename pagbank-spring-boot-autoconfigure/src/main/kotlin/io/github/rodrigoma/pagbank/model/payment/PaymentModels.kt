package io.github.rodrigoma.pagbank.model.payment

data class PaymentResponse(
    val id: String,
    val invoiceId: String,
    val amount: Int,
    val status: String,
    val paidAt: String?,
)

data class PaymentListResponse(
    val payments: List<PaymentResponse>,
)
