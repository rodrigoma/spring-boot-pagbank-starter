package io.github.rodrigoma.pagbank.sample

import io.github.rodrigoma.pagbank.exception.ApiError
import io.github.rodrigoma.pagbank.exception.PagBankException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class PagBankExceptionHandler {
    @ExceptionHandler(PagBankException.ValidationError::class)
    fun handleValidationError(ex: PagBankException.ValidationError): ResponseEntity<Map<String, List<ApiError>>> =
        ResponseEntity.status(ex.httpStatus).body(mapOf("error_messages" to ex.errors))

    @ExceptionHandler(PagBankException.NotFound::class)
    fun handleNotFound(ex: PagBankException.NotFound): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to ex.message.orEmpty()))

    @ExceptionHandler(PagBankException.Unauthorized::class)
    fun handleUnauthorized(ex: PagBankException.Unauthorized): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(ex.httpStatus).body(mapOf("error" to ex.message.orEmpty()))

    @ExceptionHandler(PagBankException.RateLimited::class)
    fun handleRateLimited(ex: PagBankException.RateLimited): ResponseEntity<Map<String, String>> =
        ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .apply { ex.retryAfter?.let { header(HttpHeaders.RETRY_AFTER, it.seconds.toString()) } }
            .body(mapOf("error" to ex.message.orEmpty()))

    @ExceptionHandler(PagBankException.Timeout::class)
    fun handleTimeout(ex: PagBankException.Timeout): ResponseEntity<Map<String, String>> =
        // READ means the request reached PagBank and its outcome is unknown — a real application should
        // re-read the resource here instead of reporting a plain failure.
        ResponseEntity
            .status(HttpStatus.GATEWAY_TIMEOUT)
            .body(mapOf("error" to ex.message.orEmpty(), "phase" to ex.phase.name))

    @ExceptionHandler(PagBankException.ServerError::class)
    fun handleServerError(ex: PagBankException.ServerError): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(mapOf("error" to "PagBank error: ${ex.statusCode}"))
}
