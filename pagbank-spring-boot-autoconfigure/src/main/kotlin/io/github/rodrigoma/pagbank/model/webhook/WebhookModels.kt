package io.github.rodrigoma.pagbank.model.webhook

import com.fasterxml.jackson.annotation.JsonProperty

// Values match PagBank webhook "event" field exactly (dot notation) via @JsonProperty
enum class WebhookEventType {
    // Plan events
    @JsonProperty("plan.created")
    PLAN_CREATED,

    @JsonProperty("plan.updated")
    PLAN_UPDATED,

    @JsonProperty("plan.activated")
    PLAN_ACTIVATED,

    @JsonProperty("plan.inactivated")
    PLAN_INACTIVATED,

    @JsonProperty("plan.suspended")
    PLAN_SUSPENDED,

    @JsonProperty("plan.canceled")
    PLAN_CANCELED,

    // Coupon events
    @JsonProperty("coupon.created")
    COUPON_CREATED,

    @JsonProperty("coupon.updated")
    COUPON_UPDATED,

    @JsonProperty("coupon.activated")
    COUPON_ACTIVATED,

    @JsonProperty("coupon.inactivated")
    COUPON_INACTIVATED,

    @JsonProperty("coupon.suspended")
    COUPON_SUSPENDED,

    @JsonProperty("coupon.canceled")
    COUPON_CANCELED,

    // Customer events
    @JsonProperty("customer.created")
    CUSTOMER_CREATED,

    @JsonProperty("customer.updated")
    CUSTOMER_UPDATED,

    @JsonProperty("customer.billing_info.updated")
    CUSTOMER_BILLING_INFO_UPDATED,

    // Subscription events
    @JsonProperty("subscription.initial")
    SUBSCRIPTION_INITIAL,

    @JsonProperty("subscription.updated")
    SUBSCRIPTION_UPDATED,

    @JsonProperty("subscription.activated")
    SUBSCRIPTION_ACTIVATED,

    @JsonProperty("subscription.inactivated")
    SUBSCRIPTION_INACTIVATED,

    @JsonProperty("subscription.suspended")
    SUBSCRIPTION_SUSPENDED,

    @JsonProperty("subscription.recurrence")
    SUBSCRIPTION_RECURRENCE,

    @JsonProperty("subscription.expired")
    SUBSCRIPTION_EXPIRED,

    @JsonProperty("subscription.canceled")
    SUBSCRIPTION_CANCELED,

    @JsonProperty("subscription.migrated")
    SUBSCRIPTION_MIGRATED,

    // Payment refund events
    @JsonProperty("payment.refund.activated")
    PAYMENT_REFUND_ACTIVATED,

    @JsonProperty("payment.refund.inactivated")
    PAYMENT_REFUND_INACTIVATED,

    @JsonProperty("payment.refund.updated")
    PAYMENT_REFUND_UPDATED,

    @JsonProperty("payment.refund.suspended")
    PAYMENT_REFUND_SUSPENDED,

    @JsonProperty("payment.refund.canceled")
    PAYMENT_REFUND_CANCELED,
}

enum class WebhookEnv {
    @JsonProperty("sandbox")
    SANDBOX,

    @JsonProperty("production")
    PRODUCTION,
}

data class WebhookPayload(
    val env: WebhookEnv,
    val event: WebhookEventType,
    val resource: Map<String, Any>,
    val date: String? = null,
)
