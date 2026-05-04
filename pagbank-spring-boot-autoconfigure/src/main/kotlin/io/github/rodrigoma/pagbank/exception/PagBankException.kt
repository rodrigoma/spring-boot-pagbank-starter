package io.github.rodrigoma.pagbank.exception

import com.fasterxml.jackson.annotation.JsonProperty

data class ApiError(
    @JsonProperty("error") val code: String,
    @JsonProperty("description") val message: String,
)

data class ApiErrorResponse(
    @JsonProperty("error_messages") val errorMessages: List<ApiError>,
)

sealed class PagBankException(
    message: String,
) : RuntimeException(message) {
    class Unauthorized(
        message: String,
    ) : PagBankException(message)

    class NotFound(
        message: String,
    ) : PagBankException(message)

    class ValidationError(
        val errors: List<ApiError>,
        val httpStatus: Int = 422,
    ) : PagBankException("Validation failed")

    class ServerError(
        val statusCode: Int,
    ) : PagBankException("Server error: $statusCode")
}
