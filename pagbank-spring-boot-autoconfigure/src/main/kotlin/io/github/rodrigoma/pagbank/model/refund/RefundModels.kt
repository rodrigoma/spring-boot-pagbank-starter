package io.github.rodrigoma.pagbank.model.refund

enum class RefundType { FULL, PARTIAL }

enum class RefundStatus { SUCCESS, FAILED }

data class RefundAmount(
    val value: Int,
    val currency: String = "BRL",
)

data class RefundPayment(
    val id: String,
    val amount: RefundAmount? = null,
)

data class RefundRequest(
    val amount: RefundAmount,
)

data class RefundResponse(
    val id: String,
    val amount: RefundAmount,
    val status: RefundStatus,
    val type: RefundType? = null,
    val payment: RefundPayment? = null,
    val createdAt: String? = null,
)

data class RefundListResponse(
    val refunds: List<RefundResponse>,
)
