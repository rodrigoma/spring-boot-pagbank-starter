package com.example.pagseguro.exception

data class ApiError(
    val code: String,
    val message: String
)

sealed class PagSeguroException(message: String) : RuntimeException(message) {
    class Unauthorized(message: String) : PagSeguroException(message)
    class NotFound(message: String) : PagSeguroException(message)
    class ValidationError(val errors: List<ApiError>) : PagSeguroException("Validation failed")
    class ServerError(val statusCode: Int) : PagSeguroException("Server error: $statusCode")
}
