package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.autoconfigure.PagBankProperties
import io.github.rodrigoma.pagbank.model.webhook.WebhookPayload
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.readValue
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class PagBankWebhookParser(
    private val properties: PagBankProperties,
) {
    private val mapper =
        jacksonMapperBuilder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build()

    fun parse(rawBody: String): WebhookPayload = mapper.readValue(rawBody)

    fun verify(
        rawBody: String,
        signature: String,
    ): Boolean {
        check(properties.webhookSecret != null) {
            "pagbank.webhook-secret must be configured to verify webhook signatures"
        }
        return computeHmac(rawBody, properties.webhookSecret) == signature
    }

    // Internal: exposed for testing only
    internal fun computeSignature(rawBody: String): String {
        val secret = requireNotNull(properties.webhookSecret) { "webhookSecret is required" }
        return computeHmac(rawBody, secret)
    }

    private fun computeHmac(
        data: String,
        secret: String,
    ): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(data.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
