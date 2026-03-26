package com.example.pagbank.http

import com.example.pagbank.exception.PagBankException
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.mock.http.client.MockClientHttpResponse

class PagBankErrorHandlerTest {

    private val mapper = ObjectMapper()
    private val handler = PagBankErrorHandler(mapper)

    @Test
    fun `401 throws Unauthorized`() {
        val body = """{"message":"Unauthorized"}""".toByteArray()
        val response = MockClientHttpResponse(body, HttpStatus.UNAUTHORIZED)
        assertThatThrownBy { handler.handle(response) }
            .isInstanceOf(PagBankException.Unauthorized::class.java)
    }

    @Test
    fun `404 throws NotFound`() {
        val response = MockClientHttpResponse(ByteArray(0), HttpStatus.NOT_FOUND)
        assertThatThrownBy { handler.handle(response) }
            .isInstanceOf(PagBankException.NotFound::class.java)
    }

    @Test
    fun `400 with valid JSON throws ValidationError`() {
        val body = """[{"code":"40001","message":"amount is required"}]""".toByteArray()
        val response = MockClientHttpResponse(body, HttpStatus.BAD_REQUEST)
        val ex = assertThrows<PagBankException.ValidationError> { handler.handle(response) }
        assertThat(ex.errors).hasSize(1)
        assertThat(ex.errors[0].code).isEqualTo("40001")
    }

    @Test
    fun `400 with non-JSON body falls back to ServerError`() {
        val body = "<html>Bad Request</html>".toByteArray()
        val response = MockClientHttpResponse(body, HttpStatus.BAD_REQUEST)
        assertThatThrownBy { handler.handle(response) }
            .isInstanceOf(PagBankException.ServerError::class.java)
    }

    @Test
    fun `500 throws ServerError with status code`() {
        val response = MockClientHttpResponse(ByteArray(0), HttpStatus.INTERNAL_SERVER_ERROR)
        val ex = assertThrows<PagBankException.ServerError> { handler.handle(response) }
        assertThat(ex.statusCode).isEqualTo(500)
    }
}
