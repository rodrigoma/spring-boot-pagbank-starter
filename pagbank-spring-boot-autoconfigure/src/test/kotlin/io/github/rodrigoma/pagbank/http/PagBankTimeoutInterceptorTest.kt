package io.github.rodrigoma.pagbank.http

import io.github.rodrigoma.pagbank.exception.PagBankException
import io.github.rodrigoma.pagbank.exception.TimeoutPhase
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.mock.http.client.MockClientHttpRequest
import org.springframework.mock.http.client.MockClientHttpResponse
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URI
import java.net.http.HttpConnectTimeoutException
import java.net.http.HttpTimeoutException

class PagBankTimeoutInterceptorTest {
    private val interceptor = PagBankTimeoutInterceptor()
    private val request = MockClientHttpRequest(HttpMethod.POST, URI.create("https://example.com/subscriptions?x=1"))

    private fun intercept(failure: IOException?) =
        interceptor.intercept(
            request,
            ByteArray(0),
            ClientHttpRequestExecution { _, _ ->
                failure?.let { throw it }
                MockClientHttpResponse(ByteArray(0), HttpStatus.OK)
            },
        )

    private fun phaseOf(failure: IOException): TimeoutPhase {
        val thrown = runCatching { intercept(failure) }.exceptionOrNull()
        assertThat(thrown).isInstanceOf(PagBankException.Timeout::class.java)
        return (thrown as PagBankException.Timeout).phase
    }

    @Test
    fun `passes the response through when nothing fails`() {
        assertThat(intercept(null).statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `maps JDK HttpClient timeouts`() {
        assertThat(phaseOf(HttpConnectTimeoutException("connect"))).isEqualTo(TimeoutPhase.CONNECT)
        assertThat(phaseOf(HttpTimeoutException("request timed out"))).isEqualTo(TimeoutPhase.READ)
    }

    @Test
    fun `maps socket timeouts by message`() {
        assertThat(phaseOf(SocketTimeoutException("connect timed out"))).isEqualTo(TimeoutPhase.CONNECT)
        assertThat(phaseOf(SocketTimeoutException("Read timed out"))).isEqualTo(TimeoutPhase.READ)
        assertThat(phaseOf(SocketTimeoutException())).isEqualTo(TimeoutPhase.READ)
    }

    @Test
    fun `maps a client-specific connect timeout by type name`() {
        class ConnectTimeoutException : IOException("connection lease request time out")
        assertThat(phaseOf(ConnectTimeoutException())).isEqualTo(TimeoutPhase.CONNECT)
        assertThat(phaseOf(ConnectException("Connection timed out"))).isEqualTo(TimeoutPhase.CONNECT)
    }

    @Test
    fun `keeps the request method and path and hides the query string`() {
        val thrown = runCatching { intercept(SocketTimeoutException("Read timed out")) }.exceptionOrNull()
        val timeout = thrown as PagBankException.Timeout
        assertThat(timeout.method).isEqualTo("POST")
        assertThat(timeout.path).isEqualTo("/subscriptions")
        assertThat(timeout.message).doesNotContain("x=1")
    }

    @Test
    fun `rethrows a non-timeout IOException untouched`() {
        val failure = IOException("connection reset")
        assertThatThrownBy { intercept(failure) }.isSameAs(failure)
    }
}
