package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.exception.PagBankException
import io.github.rodrigoma.pagbank.model.webhook.WebhookEventType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.security.MessageDigest
import java.util.HexFormat

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
    fun `parse should accept raw bytes`() {
        val payload = parser.parse(rawBody.toByteArray())
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

    @Test
    fun `parseVerified without a verifier should behave like parse`() {
        val payload = parser.parseVerified(rawBody.toByteArray(), null)
        assertThat(payload.event).isEqualTo(WebhookEventType.SUBSCRIPTION_RECURRENCE)
    }

    @Test
    fun `parseVerified with a verifier should accept a valid signature`() {
        val token = "TOKEN"
        val signature =
            HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest("$token-$rawBody".toByteArray()))
        val verifying = PagBankWebhookParser(PagBankWebhookVerifier(token))

        val payload = verifying.parseVerified(rawBody.toByteArray(), signature)
        assertThat(payload.event).isEqualTo(WebhookEventType.SUBSCRIPTION_RECURRENCE)
    }

    @Test
    fun `parseVerified with a verifier should reject a missing or wrong signature`() {
        val verifying = PagBankWebhookParser(PagBankWebhookVerifier("TOKEN"))

        assertThatThrownBy { verifying.parseVerified(rawBody.toByteArray(), null) }
            .isInstanceOf(PagBankException.InvalidSignature::class.java)
        assertThatThrownBy { verifying.parseVerified(rawBody.toByteArray(), "deadbeef") }
            .isInstanceOf(PagBankException.InvalidSignature::class.java)
    }

    @Test
    fun `parseVerified should reject before parsing invalid JSON`() {
        val verifying = PagBankWebhookParser(PagBankWebhookVerifier("TOKEN"))

        assertThatThrownBy { verifying.parseVerified("not json".toByteArray(), "deadbeef") }
            .isInstanceOf(PagBankException.InvalidSignature::class.java)
    }
}
