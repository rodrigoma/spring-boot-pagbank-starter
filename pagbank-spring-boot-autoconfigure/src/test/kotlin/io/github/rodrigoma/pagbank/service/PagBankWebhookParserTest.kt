package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.autoconfigure.PagBankProperties
import io.github.rodrigoma.pagbank.model.webhook.WebhookEventType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class PagBankWebhookParserTest {
    private val propertiesWithSecret =
        PagBankProperties(
            token = "TOKEN",
            webhookSecret = "SECRET_KEY",
        )
    private val propertiesWithoutSecret = PagBankProperties(token = "TOKEN")
    private val parser = PagBankWebhookParser(propertiesWithSecret)

    private val rawBody =
        """{"env":"sandbox","event":"subscription.recurrence","resource":{},"date":"2026-03-28T10:05:00Z"}"""

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
        val parserNoSecret = PagBankWebhookParser(propertiesWithoutSecret)
        assertThatThrownBy { parserNoSecret.verify(rawBody, "sig") }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("pagbank.webhook-secret")
    }

    @Test
    fun `parse should succeed when payload contains unknown fields like links`() {
        val bodyWithLinks =
            """{"env":"sandbox","event":"subscription.recurrence","resource":{}""" +
                ""","date":"2026-03-28T10:05:00Z","links":[{"rel":"self","href":"https://example.com"}]}"""
        val payload = parser.parse(bodyWithLinks)
        assertThat(payload.event).isEqualTo(WebhookEventType.SUBSCRIPTION_RECURRENCE)
    }
}
