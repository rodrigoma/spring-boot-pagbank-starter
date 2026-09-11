package io.github.rodrigoma.pagbank.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.security.MessageDigest
import java.util.HexFormat

class PagBankWebhookVerifierTest {
    private val token = "MY_SECRET_TOKEN"
    private val verifier = PagBankWebhookVerifier(token)
    private val body = """{"env":"sandbox","event":"subscription.recurrence","resource":{}}"""

    private fun sign(payload: String): String =
        HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest("$token-$payload".toByteArray()))

    @Test
    fun `verify should accept a correct signature`() {
        assertThat(verifier.verify(body, sign(body))).isTrue()
    }

    @Test
    fun `verify should accept an upper-case hex signature`() {
        assertThat(verifier.verify(body, sign(body).uppercase())).isTrue()
    }

    @Test
    fun `verify should accept raw bytes`() {
        assertThat(verifier.verify(body.toByteArray(), sign(body))).isTrue()
    }

    @Test
    fun `verify should reject a wrong signature`() {
        assertThat(verifier.verify(body, sign("$body "))).isFalse()
    }

    @Test
    fun `verify should reject the same JSON with different whitespace`() {
        val reformatted = body.replace(",", ", ")
        assertThat(verifier.verify(reformatted, sign(body))).isFalse()
    }

    @Test
    fun `verify should reject null or blank signature`() {
        assertThat(verifier.verify(body, null)).isFalse()
        assertThat(verifier.verify(body, "")).isFalse()
        assertThat(verifier.verify(body, "   ")).isFalse()
    }

    @Test
    fun `verify should reject invalid hex without throwing`() {
        assertThat(verifier.verify(body, "not-hex-at-all")).isFalse()
        assertThat(verifier.verify(body, "abc")).isFalse()
    }
}
