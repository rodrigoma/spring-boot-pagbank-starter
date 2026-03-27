package io.github.rodrigoma.pagbank.http

import org.slf4j.LoggerFactory
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.io.ByteArrayInputStream
import java.io.InputStream

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
                if (body.isNotEmpty()) "\n${body.decodeToString()}" else "",
            )
        }

        val response = execution.execute(request, body)

        if (log.isDebugEnabled) {
            val responseBody = response.body.readBytes()
            log.debug(
                "<-- {} {}\n{}",
                response.statusCode.value(),
                request.uri,
                responseBody.decodeToString(),
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
