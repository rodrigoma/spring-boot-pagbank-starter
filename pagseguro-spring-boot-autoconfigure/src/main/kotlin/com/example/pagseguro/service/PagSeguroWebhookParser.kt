package com.example.pagseguro.service

import com.example.pagseguro.autoconfigure.PagSeguroProperties
import com.example.pagseguro.model.webhook.WebhookPayload
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class PagSeguroWebhookParser(private val properties: PagSeguroProperties) {

    private val mapper: ObjectMapper = jacksonObjectMapper().apply {
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }

    fun parse(rawBody: String): WebhookPayload = mapper.readValue(rawBody)

    fun verify(rawBody: String, signature: String): Boolean {
        check(properties.webhookSecret != null) {
            "pagseguro.webhook-secret must be configured to verify webhook signatures"
        }
        return computeHmac(rawBody, properties.webhookSecret) == signature
    }

    // Internal: exposed for testing only
    internal fun computeSignature(rawBody: String): String {
        val secret = requireNotNull(properties.webhookSecret) { "webhookSecret is required" }
        return computeHmac(rawBody, secret)
    }

    private fun computeHmac(data: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(data.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
