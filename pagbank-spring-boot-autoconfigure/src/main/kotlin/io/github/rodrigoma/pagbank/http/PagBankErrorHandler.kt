package io.github.rodrigoma.pagbank.http

import io.github.rodrigoma.pagbank.exception.ApiErrorResponse
import io.github.rodrigoma.pagbank.exception.PagBankException
import io.github.rodrigoma.pagbank.exception.PagBankException.NotFound
import io.github.rodrigoma.pagbank.exception.PagBankException.RateLimited
import io.github.rodrigoma.pagbank.exception.PagBankException.ServerError
import io.github.rodrigoma.pagbank.exception.PagBankException.Unauthorized
import org.springframework.http.client.ClientHttpResponse
import tools.jackson.core.JacksonException
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME

class PagBankErrorHandler(
    objectMapper: JsonMapper,
) {
    private val objectMapper: JsonMapper =
        if (objectMapper.registeredModules().none { it is KotlinModule }) {
            objectMapper.rebuild().addModule(KotlinModule.Builder().build()).build()
        } else {
            objectMapper
        }

    companion object {
        private const val HTTP_UNAUTHORIZED = 401
        private const val HTTP_FORBIDDEN = 403
        private const val HTTP_NOT_FOUND = 404
        private const val HTTP_BAD_REQUEST = 400
        private const val HTTP_UNPROCESSABLE = 422
        private const val HTTP_CONFLICT = 409
        private const val HTTP_TOO_MANY_REQUESTS = 429
    }

    fun handle(response: ClientHttpResponse) {
        val statusCode = response.statusCode.value()
        val body = runCatching { response.body.readBytes() }.getOrDefault(ByteArray(0))

        throw when (statusCode) {
            HTTP_UNAUTHORIZED, HTTP_FORBIDDEN -> Unauthorized(bodyAsString(body, statusCode), statusCode)
            HTTP_NOT_FOUND -> NotFound("Resource not found")
            HTTP_BAD_REQUEST, HTTP_UNPROCESSABLE, HTTP_CONFLICT -> parseValidationError(body, statusCode)
            HTTP_TOO_MANY_REQUESTS -> RateLimited(retryAfter(response))
            else -> ServerError(statusCode)
        }
    }

    @Suppress("SwallowedException")
    private fun parseValidationError(
        body: ByteArray,
        statusCode: Int,
    ): PagBankException =
        try {
            val response = objectMapper.readValue(body, ApiErrorResponse::class.java)
            PagBankException.ValidationError(response.errorMessages, statusCode)
        } catch (e: JacksonException) {
            ServerError(statusCode)
        }

    /** `Retry-After` may be a delay in seconds or an HTTP-date; anything else yields `null`. */
    private fun retryAfter(response: ClientHttpResponse): Duration? {
        val header = response.headers.getFirst("Retry-After")?.trim()
        return when {
            header == null -> null
            header.toLongOrNull() != null -> Duration.ofSeconds(header.toLong())
            else ->
                runCatching { ZonedDateTime.parse(header, RFC_1123_DATE_TIME).toInstant() }
                    .map { Duration.between(Instant.now(), it).takeIf { d -> !d.isNegative } ?: Duration.ZERO }
                    .getOrNull()
        }
    }

    private fun bodyAsString(
        body: ByteArray,
        statusCode: Int,
    ): String =
        if (body.isEmpty()) {
            if (statusCode == HTTP_FORBIDDEN) "Forbidden" else "Unauthorized"
        } else {
            body.decodeToString()
        }
}
