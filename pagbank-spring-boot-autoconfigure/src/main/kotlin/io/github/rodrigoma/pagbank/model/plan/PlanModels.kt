package io.github.rodrigoma.pagbank.model.plan

import io.github.rodrigoma.pagbank.model.common.Currency
import io.github.rodrigoma.pagbank.model.common.PagBankLink

enum class PlanStatus { ACTIVE, INACTIVE }

enum class IntervalUnit { DAY, MONTH, YEAR }

enum class PaymentMethod { CREDIT_CARD, BOLETO }

data class Money(
    val value: Int,
    val currency: Currency = Currency.BRL,
)

data class PlanInterval(
    val length: Int,
    val unit: IntervalUnit,
)

data class PlanTrial(
    val days: Int,
    val enabled: Boolean,
    val holdSetupFee: Boolean = false,
)

data class CreatePlanRequest(
    val name: String,
    val amount: Money,
    val interval: PlanInterval,
    val paymentMethod: List<PaymentMethod> = listOf(PaymentMethod.CREDIT_CARD),
    val referenceId: String? = null,
    val description: String? = null,
    val setupFee: Int? = null,
    val limitSubscriptions: Int? = null,
    val trial: PlanTrial? = null,
)

data class UpdatePlanRequest(
    val name: String? = null,
    val description: String? = null,
    val amount: Money? = null,
    val setupFee: Int? = null,
    val limitSubscriptions: Int? = null,
    val trial: PlanTrial? = null,
    val paymentMethod: List<PaymentMethod>? = null,
)

data class PlanResponse(
    val id: String,
    val name: String,
    val amount: Money,
    val interval: PlanInterval,
    val status: PlanStatus,
    val paymentMethod: List<PaymentMethod>?,
    val referenceId: String? = null,
    val description: String? = null,
    val setupFee: Int? = null,
    val limitSubscriptions: Int? = null,
    val trial: PlanTrial? = null,
    val editable: Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val links: List<PagBankLink>? = null,
)

data class PlanResultSet(
    val total: Int,
    val offset: Int,
    val limit: Int,
    val referenceId: String? = null,
    val status: List<String>? = null,
)

data class PlanListResponse(
    val resultSet: PlanResultSet,
    val plans: List<PlanResponse>,
)
