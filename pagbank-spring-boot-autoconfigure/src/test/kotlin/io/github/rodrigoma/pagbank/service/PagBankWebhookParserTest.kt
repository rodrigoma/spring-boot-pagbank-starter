package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.webhook.WebhookEventType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PagBankWebhookParserTest {
    private val parser = PagBankWebhookParser()

    private val rawBody =
        """{"env":"sandbox","event":"subscription.recurrence","resource":{},"date":"2026-03-28T10:05:00Z"}"""

    @Test
    fun `parse should deserialize event type`() {
        val payload = parser.parse(rawBody)
        assertThat(payload.event).isEqualTo(WebhookEventType.SUBSCRIPTION_RECURRENCE)
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
