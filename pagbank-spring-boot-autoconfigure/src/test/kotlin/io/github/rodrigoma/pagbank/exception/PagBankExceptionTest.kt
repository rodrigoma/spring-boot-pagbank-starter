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
        val errors = listOf(ApiError("40001", "amount is required"))
        val ex = PagBankException.ValidationError(errors)
        assertThat(ex.errors).hasSize(1)
        assertThat(ex.errors[0].code).isEqualTo("40001")
        assertThat(ex.message).isEqualTo("Validation failed")
    }

    @Test
    fun `ServerError carries status code`() {
        val ex = PagBankException.ServerError(503)
        assertThat(ex.statusCode).isEqualTo(503)
        assertThat(ex.message).isEqualTo("Server error: 503")
    }

    @Test
    fun `sealed class enables exhaustive when`() {
        val ex: PagBankException = PagBankException.NotFound("plan not found")
        val result = when (ex) {
            is PagBankException.Unauthorized -> "auth"
            is PagBankException.NotFound -> "not_found"
            is PagBankException.ValidationError -> "validation"
            is PagBankException.ServerError -> "server"
        }
        assertThat(result).isEqualTo("not_found")
    }
}
