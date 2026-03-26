package com.example.pagseguro.model.webhook

import com.fasterxml.jackson.annotation.JsonProperty

// Values match PagBank webhook "event" field exactly (dot notation) via @JsonProperty
enum class WebhookEventType {
    @JsonProperty("plan.created")       PLAN_CREATED,
    @JsonProperty("plan.updated")       PLAN_UPDATED,
    @JsonProperty("plan.activated")     PLAN_ACTIVATED,
    @JsonProperty("plan.inactivated")   PLAN_INACTIVATED,
    @JsonProperty("coupon.created")     COUPON_CREATED,
    @JsonProperty("coupon.activated")   COUPON_ACTIVATED,
    @JsonProperty("coupon.inactivated") COUPON_INACTIVATED,
    @JsonProperty("customer.created")   CUSTOMER_CREATED,
    @JsonProperty("customer.updated")   CUSTOMER_UPDATED,
    @JsonProperty("customer.billing_info.updated") CUSTOMER_BILLING_INFO_UPDATED,
    @JsonProperty("subscription.initial")    SUBSCRIPTION_INITIAL,
    @JsonProperty("subscription.updated")    SUBSCRIPTION_UPDATED,
    @JsonProperty("subscription.activated")  SUBSCRIPTION_ACTIVATED,
    @JsonProperty("subscription.suspended")  SUBSCRIPTION_SUSPENDED,
    @JsonProperty("subscription.recurrence") SUBSCRIPTION_RECURRENCE,
    @JsonProperty("subscription.expired")    SUBSCRIPTION_EXPIRED,
    @JsonProperty("subscription.canceled")   SUBSCRIPTION_CANCELED,
    @JsonProperty("subscription.migrated")   SUBSCRIPTION_MIGRATED,
    @JsonProperty("refund.created")     REFUND_CREATED
}

data class WebhookPayload(
    val env: String,
    val event: WebhookEventType,
    val resource: Map<String, Any>,
    val links: List<Map<String, Any>> = emptyList()
)
