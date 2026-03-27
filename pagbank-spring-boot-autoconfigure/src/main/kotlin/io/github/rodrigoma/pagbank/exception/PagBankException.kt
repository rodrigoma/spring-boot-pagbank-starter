package io.github.rodrigoma.pagbank.exception

data class ApiError(
    val code: String,
    val message: String,
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
    ) : PagBankException("Validation failed")

    class ServerError(
        val statusCode: Int,
    ) : PagBankException("Server error: $statusCode")
}
