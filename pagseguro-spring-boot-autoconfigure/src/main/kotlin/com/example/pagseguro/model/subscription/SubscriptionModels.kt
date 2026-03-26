package com.example.pagseguro.model.subscription

enum class SubscriptionStatus { ACTIVE, SUSPENDED, CANCELLED, PENDING }

data class CreateSubscriptionRequest(
    val planId: String,
    val customerId: String,
    val startDate: String? = null
)

data class SubscriptionResponse(
    val id: String,
    val planId: String,
    val customerId: String,
    val status: SubscriptionStatus,
    val createdAt: String
)

data class SubscriptionListResponse(val subscriptions: List<SubscriptionResponse>)
