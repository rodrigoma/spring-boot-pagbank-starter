package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.exception.PagBankException
import io.github.rodrigoma.pagbank.model.webhook.WebhookEnv
import io.github.rodrigoma.pagbank.model.webhook.WebhookEventType
import io.github.rodrigoma.pagbank.model.webhook.WebhookPayload
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import tools.jackson.databind.DatabindException
import tools.jackson.databind.PropertyNamingStrategies.SNAKE_CASE
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.readValue
import java.security.MessageDigest
import java.util.HexFormat

class PagBankWebhookParserTest {
    private val parser = PagBankWebhookParser()

    private val rawBody =
        """{"env":"sandbox","event":"subscription.recurrence","resource":{},"date":"2026-03-28T10:05:00Z"}"""

    private fun recurrenceWith(resource: String): String =
        """{"env":"sandbox","event":"subscription.recurrence",""" + """"resource":$resource}"""

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
    fun `parse should read resource when it is a JSON object`() {
        val payload =
            parser.parse(recurrenceWith("""{"id":"SUBS_X","status":"ACTIVE"}"""))
        assertThat(payload.resource["id"]).isEqualTo("SUBS_X")
        assertThat(payload.resource["status"]).isEqualTo("ACTIVE")
    }

    @Test
    fun `parse should read resource when it is a string containing a JSON object`() {
        val payload =
            parser.parse(
                """{"env":"sandbox","event":"subscription.recurrence",""" +
                    """"resource":"{\"id\":\"SUBS_X\",\"status\":\"ACTIVE\"}"}""",
            )
        assertThat(payload.resource["id"]).isEqualTo("SUBS_X")
        assertThat(payload.resource["status"]).isEqualTo("ACTIVE")
        assertThat(payload.event).isEqualTo(WebhookEventType.SUBSCRIPTION_RECURRENCE)
    }

    @Test
    fun `parse should map a null resource to an empty map and keep the other fields`() {
        val payload =
            parser.parse(
                """{"env":"sandbox","event":"subscription.activated","resource":null,"date":"2026-09-14T10:00:00Z"}""",
            )
        assertThat(payload.resource).isEmpty()
        assertThat(payload.event).isEqualTo(WebhookEventType.SUBSCRIPTION_ACTIVATED)
        assertThat(payload.env).isEqualTo(WebhookEnv.SANDBOX)
        assertThat(payload.date).isEqualTo("2026-09-14T10:00:00Z")
    }

    @Test
    fun `parse should map a missing resource to an empty map`() {
        val payload = parser.parse("""{"env":"sandbox","event":"subscription.activated"}""")
        assertThat(payload.resource).isEmpty()
    }

    @Test
    fun `parse should reject a resource string that is not JSON with a clear message`() {
        assertThatThrownBy { parser.parse(recurrenceWith(""""abc"""")) }
            .isInstanceOf(DatabindException::class.java)
            .hasMessageContaining("resource")
            .hasMessageContaining("JSON object")
            .hasMessageNotContaining("LinkedHashMap")
    }

    @Test
    fun `parse should reject a resource string containing a JSON array with a clear message`() {
        assertThatThrownBy { parser.parse(recurrenceWith(""""[1,2]"""")) }
            .isInstanceOf(DatabindException::class.java)
            .hasMessageContaining("resource")
            .hasMessageContaining("JSON object")
            .hasMessageNotContaining("LinkedHashMap")
    }

    @Test
    fun `parse should reject a resource of any other JSON type with a clear message`() {
        for (bad in listOf("42", "[1,2]", "true")) {
            assertThatThrownBy { parser.parse(recurrenceWith(bad)) }
                .isInstanceOf(DatabindException::class.java)
                .hasMessageContaining("resource")
                .hasMessageNotContaining("LinkedHashMap")
        }
    }

    @Test
    fun `parse should ignore unknown top-level fields`() {
        val payload = parser.parse("""{"env":"sandbox","event":"subscription.recurrence","resource":{},"novo":1}""")
        assertThat(payload.event).isEqualTo(WebhookEventType.SUBSCRIPTION_RECURRENCE)
    }

    @Test
    fun `WebhookPayload should ignore unknown fields even with a strict mapper`() {
        val strict = jacksonMapperBuilder().propertyNamingStrategy(SNAKE_CASE).build()
        val payload =
            strict.readValue<WebhookPayload>(
                """{"env":"sandbox","event":"subscription.recurrence","resource":{},"novo":1}""",
            )
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
