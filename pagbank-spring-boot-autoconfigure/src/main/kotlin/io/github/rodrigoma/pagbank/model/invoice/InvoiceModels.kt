package io.github.rodrigoma.pagbank.model.invoice

enum class InvoiceStatus { OPEN, PAID, WAITING, UNPAID, OVERDUE, CLOSED, PENDING_ACTION }

enum class InvoiceItemType { SUBSCRIPTION_AMOUNT, SETUP_FEE, TRIAL, COUPON }

data class InvoiceAmount(
    val value: Int,
    val currency: String = "BRL",
)

data class InvoicePlan(
    val id: String,
    val name: String? = null,
)

data class InvoiceItem(
    val amount: InvoiceAmount,
    val type: InvoiceItemType,
)

data class InvoiceSubscription(
    val id: String,
)

data class InvoiceCustomer(
    val id: String,
    val name: String? = null,
    val email: String? = null,
)

data class InvoiceBoleto(
    val id: String,
    val barcode: String,
    val formattedBarcode: String,
    val dueAt: String,
)

data class InvoiceLink(
    val rel: String? = null,
    val href: String? = null,
    val media: String? = null,
    val type: String? = null,
)

data class InvoiceResponse(
    val id: String,
    val status: InvoiceStatus,
    val amount: InvoiceAmount,
    val plan: InvoicePlan? = null,
    val items: List<InvoiceItem>? = null,
    val subscription: InvoiceSubscription? = null,
    val occurrence: Int? = null,
    val customer: InvoiceCustomer? = null,
    val boleto: InvoiceBoleto? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val links: List<InvoiceLink>? = null,
)

data class InvoiceListResponse(
    val invoices: List<InvoiceResponse>,
)
