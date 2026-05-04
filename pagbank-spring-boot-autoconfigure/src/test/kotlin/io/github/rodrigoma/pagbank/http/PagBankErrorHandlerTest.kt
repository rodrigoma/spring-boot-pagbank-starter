package io.github.rodrigoma.pagbank.http

import io.github.rodrigoma.pagbank.exception.PagBankException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.mock.http.client.MockClientHttpResponse
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jacksonMapperBuilder

class PagBankErrorHandlerTest {
    private val mapper = jacksonMapperBuilder().build()
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
        val body = """{"error_messages":[{"error":"40001","description":"amount is required"}]}""".toByteArray()
        val response = MockClientHttpResponse(body, HttpStatus.BAD_REQUEST)
        val ex = assertThrows<PagBankException.ValidationError> { handler.handle(response) }
        assertThat(ex.errors).hasSize(1)
        assertThat(ex.errors[0].code).isEqualTo("40001")
        assertThat(ex.httpStatus).isEqualTo(400)
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

    @Test
    fun `400 with valid JSON deserializes correctly even when mapper has no KotlinModule`() {
        val bareMapper = JsonMapper.builder().build()
        val bareHandler = PagBankErrorHandler(bareMapper)
        val body = """{"error_messages":[{"error":"40001","description":"amount is required"}]}""".toByteArray()
        val response = MockClientHttpResponse(body, HttpStatus.BAD_REQUEST)
        val ex = assertThrows<PagBankException.ValidationError> { bareHandler.handle(response) }
        assertThat(ex.errors).hasSize(1)
        assertThat(ex.errors[0].code).isEqualTo("40001")
    }

    @Test
    fun `409 throws ValidationError with httpStatus 409`() {
        val body = """{"error_messages":[{"error":"40901","description":"conflict"}]}""".toByteArray()
        val response = MockClientHttpResponse(body, HttpStatus.CONFLICT)
        val ex = assertThrows<PagBankException.ValidationError> { handler.handle(response) }
        assertThat(ex.httpStatus).isEqualTo(409)
    }
}
