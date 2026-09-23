package io.github.rodrigoma.pagbank.exception

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PagBankExceptionTest {
    @Test
    fun `Unauthorized carries message`() {
        val ex = PagBankException.Unauthorized("invalid token")
        assertThat(ex.message).isEqualTo("invalid token")
        assertThat(ex).isInstanceOf(RuntimeException::class.java)
    }

    @Test
    fun `ValidationError carries error list`() {
        val errors = listOf(ApiError(error = "40001", description = "amount is required"))
        val ex = PagBankException.ValidationError(errors)
        assertThat(ex.errors).hasSize(1)
        assertThat(ex.errors[0].error).isEqualTo("40001")
        assertThat(ex.message).isEqualTo("Validation failed")
    }

    @Test
    fun `ServerError carries status code`() {
        val ex = PagBankException.ServerError(503)
        assertThat(ex.statusCode).isEqualTo(503)
        assertThat(ex.message).isEqualTo("Server error: 503")
    }

    @Test
    fun `InvalidSignature has a descriptive message`() {
        val ex = PagBankException.InvalidSignature()
        assertThat(ex.message).contains("signature")
    }

    @Test
    fun `Timeout keeps the cause and spells out an unknown outcome on READ`() {
        val cause = java.net.SocketTimeoutException("Read timed out")
        val ex = PagBankException.Timeout(TimeoutPhase.READ, "POST", "/subscriptions", cause)
        assertThat(ex.phase).isEqualTo(TimeoutPhase.READ)
        assertThat(ex.cause).isSameAs(cause)
        assertThat(ex.message).contains("POST /subscriptions").contains("outcome is unknown")
    }

    @Test
    fun `Timeout on CONNECT does not claim the request was sent`() {
        val ex =
            PagBankException.Timeout(
                TimeoutPhase.CONNECT,
                "GET",
                "/plans",
                java.net.SocketTimeoutException("connect timed out"),
            )
        assertThat(ex.message).contains("CONNECT").doesNotContain("outcome is unknown")
    }

    @Test
    fun `sealed class enables exhaustive when`() {
        val ex: PagBankException = PagBankException.NotFound("plan not found")
        val result =
            when (ex) {
                is PagBankException.Unauthorized -> "auth"
                is PagBankException.NotFound -> "not_found"
                is PagBankException.ValidationError -> "validation"
                is PagBankException.ServerError -> "server"
                is PagBankException.InvalidSignature -> "signature"
                is PagBankException.RateLimited -> "rate_limited"
                is PagBankException.Timeout -> "timeout"
            }
        assertThat(result).isEqualTo("not_found")
    }
}
