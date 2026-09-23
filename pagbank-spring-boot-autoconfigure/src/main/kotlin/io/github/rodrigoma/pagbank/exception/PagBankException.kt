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

/** Where a request stopped when it timed out — see [PagBankException.Timeout]. */
enum class TimeoutPhase {
    /**
     * The connection to PagBank was never established, so the request **did not reach** PagBank.
     * Safe to retry.
     */
    CONNECT,

    /**
     * The request was sent and no response arrived in time. **The outcome is unknown**: PagBank may
     * have processed it. See [PagBankException.Timeout].
     */
    READ,
}

sealed class PagBankException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
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

    /**
     * No response arrived within the configured timeout (`pagbank.connect-timeout` /
     * `pagbank.read-timeout`).
     *
     * **Read [phase] before deciding what to do.** With [TimeoutPhase.CONNECT] the request never left,
     * so retrying is safe. With [TimeoutPhase.READ] the request *was* sent and the outcome is
     * **indeterminate** — PagBank may have created the subscription, charged the card or issued the
     * refund. Do not report failure and do not blindly retry a write: re-read the resource (or retry
     * with the same `idempotencyKey`) and decide from its actual state.
     *
     * @property phase where the request stopped.
     * @property method HTTP method of the request that timed out.
     * @property path path of the request that timed out (no query string).
     */
    class Timeout(
        val phase: TimeoutPhase,
        val method: String,
        val path: String,
        cause: Throwable,
    ) : PagBankException(
            "No response from PagBank within the configured timeout ($phase) for $method $path" +
                if (phase == TimeoutPhase.READ) " — the request was sent, its outcome is unknown" else "",
            cause,
        )

    /** Thrown by `PagBankWebhookParser.parseVerified` when verification is on and the header does not match. */
    class InvalidSignature : PagBankException("Webhook signature is missing or invalid")
}
