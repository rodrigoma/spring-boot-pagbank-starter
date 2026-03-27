package io.github.rodrigoma.pagbank.http

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.mock.http.client.MockClientHttpRequest
import org.springframework.mock.http.client.MockClientHttpResponse
import java.net.URI

class PagBankLoggingInterceptorTest {
    private val interceptor = PagBankLoggingInterceptor()

    @Test
    fun `intercept should return the response body intact when DEBUG is disabled`() {
        val request = MockClientHttpRequest(HttpMethod.GET, URI.create("https://sandbox.example.com/plans"))
        val responseBody = """{"plans":[]}""".toByteArray()
        val mockResponse =
            MockClientHttpResponse(responseBody, HttpStatus.OK).also {
                it.headers.contentType = MediaType.APPLICATION_JSON
            }

        val execution = ClientHttpRequestExecution { _, _ -> mockResponse }

        val result = interceptor.intercept(request, ByteArray(0), execution)

        assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `intercept should forward request body to execution`() {
        val request = MockClientHttpRequest(HttpMethod.POST, URI.create("https://sandbox.example.com/plans"))
        val requestBody = """{"name":"Basic"}""".toByteArray()
        val responseBody = """{"id":"PLAN_1"}""".toByteArray()
        val mockResponse =
            MockClientHttpResponse(responseBody, HttpStatus.CREATED).also {
                it.headers.contentType = MediaType.APPLICATION_JSON
            }

        var capturedBody: ByteArray? = null
        val execution =
            ClientHttpRequestExecution { _, body ->
                capturedBody = body
                mockResponse
            }

        interceptor.intercept(request, requestBody, execution)

        assertThat(capturedBody).isEqualTo(requestBody)
    }

    @Test
    fun `intercept should allow response body to be read after interception`() {
        val request =
            MockClientHttpRequest(HttpMethod.GET, URI.create("https://sandbox.example.com/plans/PLAN_1"))
        val responseBody = """{"id":"PLAN_1","name":"Basic"}""".toByteArray()
        val mockResponse =
            MockClientHttpResponse(responseBody, HttpStatus.OK).also {
                it.headers.contentType = MediaType.APPLICATION_JSON
            }

        val execution = ClientHttpRequestExecution { _, _ -> mockResponse }

        val result = interceptor.intercept(request, ByteArray(0), execution)

        val readBody = result.body.readBytes()
        assertThat(readBody).isEqualTo(responseBody)
    }
}
