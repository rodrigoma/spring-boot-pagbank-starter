package io.github.rodrigoma.pagbank.exception

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Duration

data class ApiError(
    val error: String,
    val description: String,
    @field:JsonProperty("parameter_name") val parameterName: String? = null,
)

data class ApiErrorResponse(
    @field:JsonProperty("error_messages") val errorMessages: List<ApiError>,
)

sealed class PagBankException(
    message: String,
) : RuntimeException(message) {
    class Unauthorized(
        message: String,
        val httpStatus: Int = 401,
    ) : PagBankException(message)

    class NotFound(
        message: String,
    ) : PagBankException(message)

    class ValidationError(
        val errors: List<ApiError>,
        val httpStatus: Int = 422,
    ) : PagBankException("Validation failed")

    /** 5xx or any status the starter does not map explicitly — PagBank itself is failing. */
    class ServerError(
        val statusCode: Int,
    ) : PagBankException("Server error: $statusCode")

    /**
     * HTTP 429 — you are calling too fast, not PagBank failing. Back off and retry;
     * [retryAfter] carries the `Retry-After` header when PagBank sent one.
     */
    class RateLimited(
        val retryAfter: Duration? = null,
    ) : PagBankException("Rate limited" + (retryAfter?.let { ", retry after ${it.seconds}s" } ?: ""))

    /** Thrown by `PagBankWebhookParser.parseVerified` when verification is on and the header does not match. */
    class InvalidSignature : PagBankException("Webhook signature is missing or invalid")
}
