package io.github.rodrigoma.pagbank.model.payment

enum class PaymentStatus { APPROVED, PENDING, DENIED, REFUNDED, UNPAID, IN_ANALYSIS }

data class PaymentAmount(
    val value: Int,
    val currency: String = "BRL",
)

data class PaymentInvoice(
    val id: String,
    val amount: PaymentAmount? = null,
)

data class PaymentCustomer(
    val id: String,
    val name: String? = null,
    val email: String? = null,
)

data class PaymentCardHolder(
    val name: String,
)

data class PaymentCard(
    val token: String? = null,
    val brand: String? = null,
    val firstDigits: String? = null,
    val lastDigits: String? = null,
    val expMonth: String? = null,
    val expYear: String? = null,
    val holder: PaymentCardHolder? = null,
)

data class PaymentMethodDetails(
    val type: String,
    val card: PaymentCard? = null,
)

data class PaymentProvider(
    val name: String? = null,
    val transactionId: String? = null,
    val code: String? = null,
    val message: String? = null,
    val reference: String? = null,
)

data class PaymentLink(
    val rel: String? = null,
    val href: String? = null,
    val media: String? = null,
    val type: String? = null,
)

data class PaymentResponse(
    val id: String,
    val status: PaymentStatus,
    val invoice: PaymentInvoice? = null,
    val customer: PaymentCustomer? = null,
    val paymentMethod: PaymentMethodDetails? = null,
    val provider: PaymentProvider? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val links: List<PaymentLink>? = null,
)

data class PaymentListResponse(
    val payments: List<PaymentResponse>,
)
