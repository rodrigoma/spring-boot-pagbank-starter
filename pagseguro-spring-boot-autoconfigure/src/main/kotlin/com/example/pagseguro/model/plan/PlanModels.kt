package com.example.pagseguro.model.plan

enum class PlanStatus { ACTIVE, INACTIVE }

data class Money(val value: Int, val currency: String = "BRL")

data class PlanInterval(val length: Int, val unit: String)  // unit: "month", "day", "year"

data class PlanTrial(val days: Int, val enabled: Boolean, val holdSetupFee: Boolean = false)

data class CreatePlanRequest(
    val name: String,
    val amount: Money,
    val interval: PlanInterval,
    val paymentMethod: List<String> = listOf("CARD"),
    val referenceId: String? = null,
    val description: String? = null,
    val setupFee: Int? = null,
    val limitSubscriptions: Int? = null,
    val trial: PlanTrial? = null
)

data class PlanResponse(
    val id: String,
    val name: String,
    val amount: Money,
    val interval: PlanInterval,
    val status: PlanStatus,
    val referenceId: String?,
    val trial: PlanTrial?,
    val createdAt: String?,
    val updatedAt: String?
)

data class PlanListResponse(val plans: List<PlanResponse>)
