package com.example.pagseguro.service

import com.example.pagseguro.autoconfigure.PagSeguroProperties
import com.example.pagseguro.autoconfigure.PagSeguroEnvironment
import com.example.pagseguro.model.webhook.WebhookEventType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class PagSeguroWebhookParserTest {

    private val propertiesWithSecret = PagSeguroProperties(
        token = "TOKEN",
        webhookSecret = "SECRET_KEY"
    )
    private val propertiesWithoutSecret = PagSeguroProperties(token = "TOKEN")
    private val parser = PagSeguroWebhookParser(propertiesWithSecret)

    private val rawBody = """{"env":"sandbox","event":"subscription.recurrence","resource":{},"links":[]}"""

    @Test
    fun `parse should deserialize event type`() {
        val payload = parser.parse(rawBody)
        assertThat(payload.event).isEqualTo(WebhookEventType.SUBSCRIPTION_RECURRENCE)
    }

    @Test
    fun `verify should return true for valid HMAC signature`() {
        val signature = parser.computeSignature(rawBody)
        assertThat(parser.verify(rawBody, signature)).isTrue()
    }

    @Test
    fun `verify should return false for tampered body`() {
        val signature = parser.computeSignature(rawBody)
        assertThat(parser.verify("tampered", signature)).isFalse()
    }

    @Test
    fun `verify should throw when webhookSecret is not configured`() {
        val parserNoSecret = PagSeguroWebhookParser(propertiesWithoutSecret)
        assertThatThrownBy { parserNoSecret.verify(rawBody, "sig") }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("pagseguro.webhook-secret")
    }
}
