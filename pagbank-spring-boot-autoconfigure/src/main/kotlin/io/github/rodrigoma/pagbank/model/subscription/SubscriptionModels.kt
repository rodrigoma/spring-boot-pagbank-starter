package io.github.rodrigoma.pagbank.model.subscription

import io.github.rodrigoma.pagbank.model.common.Currency
import io.github.rodrigoma.pagbank.model.common.Currency.BRL
import io.github.rodrigoma.pagbank.model.common.PagBankLink
import io.github.rodrigoma.pagbank.model.invoice.InvoiceResponse
import io.github.rodrigoma.pagbank.model.plan.IntervalUnit
import io.github.rodrigoma.pagbank.model.plan.PaymentMethod

enum class SubscriptionStatus { ACTIVE, EXPIRED, CANCELED, SUSPENDED, OVERDUE, TRIAL, PENDING, PENDING_ACTION }

enum class RetryAttempt { FIRST, SECOND, THIRD }

enum class RetryStatus { SCHEDULED, EXECUTING, MANUALLY_EXECUTED, AUTOMATICALLY_EXECUTED, CANCELED }

enum class SplitMethod { FIXED, PERCENTAGE }

data class SubscriptionPlanRef(
    val id: String,
)

data class SubscriptionCustomerRef(
    val id: String,
)

data class SubscriptionAmount(
    val value: Int,
    val currency: Currency = BRL,
)

data class SubscriptionCardHolder(
    val name: String,
    val taxId: String? = null,
    val birthDate: String? = null,
)

data class SubscriptionCard(
    val token: String? = null,
    val securityCode: String? = null,
    val brand: String? = null,
    val firstDigits: String? = null,
    val lastDigits: String? = null,
    val expMonth: String? = null,
    val expYear: String? = null,
    val holder: SubscriptionCardHolder? = null,
)

data class Boleto(
    val id: String,
    val barcode: String,
    val formattedBarcode: String,
    val dueAt: String,
)

data class SubscriptionPaymentMethod(
    val type: PaymentMethod,
    val card: SubscriptionCard? = null,
    val boleto: Boleto? = null,
)

data class SubscriptionPlan(
    val id: String,
    val name: String? = null,
    val interval: SubscriptionPlanInterval? = null,
)

data class SubscriptionPlanInterval(
    val length: Int,
    val unit: IntervalUnit,
)

data class SubscriptionCustomer(
    val id: String,
    val name: String? = null,
    val email: String? = null,
    val address: SubscriptionCustomerAddress? = null,
)

data class SubscriptionCustomerAddress(
    val street: String,
    val number: String,
    val locality: String,
    val city: String,
    val regionCode: String,
    val country: String,
    val postalCode: String,
    val complement: String? = null,
)

data class SubscriptionTrial(
    val startAt: String? = null,
    val endAt: String? = null,
)

data class BillingCycle(
    val occurrence: Int,
    val total: Int? = null,
)

data class BestInvoiceDate(
    val day: Int? = null,
    val month: Int? = null,
)

data class SubscriptionCoupon(
    val id: String,
    val name: String? = null,
)

data class Retry(
    val attempt: RetryAttempt,
    val retriedAt: String? = null,
    val status: RetryStatus? = null,
)

data class SplitAccount(
    val id: String,
)

data class SplitReceiverAmount(
    val value: Int,
)

data class SplitReceiver(
    val account: SplitAccount,
    val amount: SplitReceiverAmount,
)

data class Splits(
    val method: SplitMethod,
    val receivers: List<SplitReceiver>,
)

data class CreateSubscriptionRequest(
    val plan: SubscriptionPlanRef,
    val customer: SubscriptionCustomerRef,
    val paymentMethod: List<SubscriptionPaymentMethod>,
    val referenceId: String? = null,
    val amount: SubscriptionAmount? = null,
    val proRata: Boolean? = null,
    val splitEnabled: Boolean? = null,
    val splits: Splits? = null,
)

data class SubscriptionResponse(
    val id: String,
    val status: SubscriptionStatus,
    val plan: SubscriptionPlan? = null,
    val customer: SubscriptionCustomer? = null,
    val amount: SubscriptionAmount? = null,
    val paymentMethod: List<SubscriptionPaymentMethod>? = null,
    val referenceId: String? = null,
    val trial: SubscriptionTrial? = null,
    val coupon: SubscriptionCoupon? = null,
    val billingCycle: BillingCycle? = null,
    val bestInvoiceDate: BestInvoiceDate? = null,
    val nextInvoiceAt: String? = null,
    val proRata: Boolean? = null,
    val retries: List<Retry>? = null,
    val expAt: String? = null,
    val splitEnabled: Boolean? = null,
    val splits: Splits? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val links: List<PagBankLink>? = null,
)

data class SubscriptionResultSet(
    val total: Int,
    val offset: Int? = null,
    val limit: Int? = null,
    val status: List<String>? = null,
    val referenceId: String? = null,
)

data class SubscriptionListResponse(
    val resultSet: SubscriptionResultSet,
    val subscriptions: List<SubscriptionResponse>,
)

data class UpdateSubscriptionRequest(
    val plan: SubscriptionPlanRef? = null,
    val amount: SubscriptionAmount? = null,
    val nextInvoiceAt: String? = null,
    val proRata: Boolean? = null,
    val bestInvoiceDate: BestInvoiceDate? = null,
    val coupon: SubscriptionCoupon? = null,
)

data class SubscriptionInvoiceResultSet(
    val total: Int,
)

data class SubscriptionInvoiceListResponse(
    val resultSet: SubscriptionInvoiceResultSet,
    val invoices: List<InvoiceResponse>,
)
