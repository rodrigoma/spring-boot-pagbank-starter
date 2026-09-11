package io.github.rodrigoma.pagbank.http

import org.slf4j.LoggerFactory
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * Logs outgoing PagBank requests and responses at `DEBUG` when `pagbank.log-requests=true`.
 *
 * Headers are never logged (so the `Authorization` token stays out of logs) and bodies go through
 * [PagBankBodyMasker], which hides card data, CPF/CNPJ, e-mail and phone numbers.
 */
class PagBankLoggingInterceptor : ClientHttpRequestInterceptor {
    private val log = LoggerFactory.getLogger(PagBankLoggingInterceptor::class.java)

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        if (log.isDebugEnabled) {
            log.debug(
                "--> {} {}{}",
                request.method,
                request.uri,
                if (body.isNotEmpty()) "\n${PagBankBodyMasker.mask(body)}" else "",
            )
        }

        val response = execution.execute(request, body)

        if (log.isDebugEnabled) {
            val responseBody = response.body.readBytes()
            log.debug(
                "<-- {} {}\n{}",
                response.statusCode.value(),
                request.uri,
                PagBankBodyMasker.mask(responseBody),
            )
            return BufferedClientHttpResponse(response, responseBody)
        }

        return response
    }
}

private class BufferedClientHttpResponse(
    private val delegate: ClientHttpResponse,
    private val bodyBytes: ByteArray,
) : ClientHttpResponse by delegate {
    override fun getBody(): InputStream = ByteArrayInputStream(bodyBytes)
}
