package com.example.pagseguro.exception

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PagSeguroExceptionTest {

    @Test
    fun `Unauthorized carries message`() {
        val ex = PagSeguroException.Unauthorized("invalid token")
        assertThat(ex.message).isEqualTo("invalid token")
        assertThat(ex).isInstanceOf(RuntimeException::class.java)
    }

    @Test
    fun `ValidationError carries error list`() {
        val errors = listOf(ApiError("40001", "amount is required"))
        val ex = PagSeguroException.ValidationError(errors)
        assertThat(ex.errors).hasSize(1)
        assertThat(ex.errors[0].code).isEqualTo("40001")
        assertThat(ex.message).isEqualTo("Validation failed")
    }

    @Test
    fun `ServerError carries status code`() {
        val ex = PagSeguroException.ServerError(503)
        assertThat(ex.statusCode).isEqualTo(503)
        assertThat(ex.message).isEqualTo("Server error: 503")
    }

    @Test
    fun `sealed class enables exhaustive when`() {
        val ex: PagSeguroException = PagSeguroException.NotFound("plan not found")
        val result = when (ex) {
            is PagSeguroException.Unauthorized -> "auth"
            is PagSeguroException.NotFound -> "not_found"
            is PagSeguroException.ValidationError -> "validation"
            is PagSeguroException.ServerError -> "server"
        }
        assertThat(result).isEqualTo("not_found")
    }
}
