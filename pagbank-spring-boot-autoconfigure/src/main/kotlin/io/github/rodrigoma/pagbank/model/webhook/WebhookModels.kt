package io.github.rodrigoma.pagbank.model.webhook

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import tools.jackson.core.JacksonException
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JavaType
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.annotation.JsonDeserialize

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

/**
 * A PagBank webhook notification.
 *
 * Unknown top-level fields are ignored so a new field on PagBank's side never breaks parsing.
 * [resource] tolerates the three shapes the PagBank sandbox has been observed to send — see
 * [WebhookResourceDeserializer].
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class WebhookPayload(
    val env: WebhookEnv,
    val event: WebhookEventType,
    @JsonDeserialize(using = WebhookResourceDeserializer::class)
    val resource: Map<String, Any> = emptyMap(),
    val date: String? = null,
)

/**
 * Reads `resource` from any of the shapes the PagBank sandbox sends:
 *
 * - a JSON object (the documented form) → the map;
 * - a JSON **string** containing a serialized object → parsed, then the map;
 * - `null` or absent → an empty map.
 *
 * Anything else (a number, an array, a boolean, or a string that does not hold a JSON object) is
 * reported as an input mismatch with a message naming the field, instead of Jackson's generic
 * "cannot deserialize LinkedHashMap" error.
 */
class WebhookResourceDeserializer : ValueDeserializer<Map<String, Any>>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): Map<String, Any> = toMap(ctxt.readTree(p), ctxt)

    override fun getNullValue(ctxt: DeserializationContext): Map<String, Any> = emptyMap()

    private fun toMap(
        node: JsonNode,
        ctxt: DeserializationContext,
    ): Map<String, Any> =
        when {
            node.isNull -> emptyMap()
            node.isObject -> ctxt.readTreeAsValue(node, mapType(ctxt))
            node.isString -> fromString(node.asString(), ctxt)
            else ->
                ctxt.reportInputMismatch(
                    WebhookPayload::class.java,
                    "Webhook field 'resource' must be a JSON object, a string containing a JSON object, " +
                        "or null; got %s",
                    node.nodeType,
                )
        }

    private fun fromString(
        text: String,
        ctxt: DeserializationContext,
    ): Map<String, Any> {
        val nested =
            try {
                ctxt.tokenStreamFactory().createParser(ctxt, text).use { ctxt.readTree(it) }
            } catch (e: JacksonException) {
                return ctxt.reportInputMismatch(
                    WebhookPayload::class.java,
                    "Webhook field 'resource' is a string that does not contain a JSON object: %s",
                    e.originalMessage,
                )
            }
        return if (nested.isObject) {
            ctxt.readTreeAsValue(nested, mapType(ctxt))
        } else {
            ctxt.reportInputMismatch(
                WebhookPayload::class.java,
                "Webhook field 'resource' is a string that does not contain a JSON object (found %s)",
                nested.nodeType,
            )
        }
    }

    private fun mapType(ctxt: DeserializationContext): JavaType =
        ctxt.typeFactory.constructMapType(Map::class.java, String::class.java, Any::class.java)
}
