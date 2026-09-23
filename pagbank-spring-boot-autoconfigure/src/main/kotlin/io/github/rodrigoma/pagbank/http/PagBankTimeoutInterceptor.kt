package io.github.rodrigoma.pagbank.http

import io.github.rodrigoma.pagbank.exception.PagBankException.Timeout
import io.github.rodrigoma.pagbank.exception.TimeoutPhase
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.http.HttpConnectTimeoutException
import java.net.http.HttpTimeoutException

/**
 * Turns the I/O timeouts of whichever HTTP client is in use into [Timeout], so consumers can tell
 * "no answer came" from "an answer came and it was an error".
 *
 * Without it a timeout surfaces as Spring's `ResourceAccessException`, indistinguishable from any
 * other transport failure.
 */
class PagBankTimeoutInterceptor : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse =
        try {
            execution.execute(request, body)
        } catch (e: IOException) {
            throw phaseOf(e)?.let { Timeout(it, request.method.name(), request.uri.path, e) } ?: e
        }

    private companion object {
        /** Returns the phase a timeout happened in, or `null` when [e] is not a timeout at all. */
        fun phaseOf(e: IOException): TimeoutPhase? =
            when {
                // JDK HttpClient
                e is HttpConnectTimeoutException -> TimeoutPhase.CONNECT
                e is HttpTimeoutException -> TimeoutPhase.READ
                // Apache HttpClient 5 (ConnectTimeoutException) and any client naming it the same way
                e::class.java.simpleName.contains("ConnectTimeout") -> TimeoutPhase.CONNECT
                // SimpleClientHttpRequestFactory / JDK URLConnection: same type for both phases
                e is SocketTimeoutException ->
                    if (e.message?.contains("connect", ignoreCase = true) == true) {
                        TimeoutPhase.CONNECT
                    } else {
                        TimeoutPhase.READ
                    }
                e is ConnectException && e.message?.contains("timed out", ignoreCase = true) == true ->
                    TimeoutPhase.CONNECT
                e is InterruptedIOException && e.message?.contains("timed out", ignoreCase = true) == true ->
                    TimeoutPhase.READ
                else -> null
            }
    }
}
