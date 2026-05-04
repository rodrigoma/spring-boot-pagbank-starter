package io.github.rodrigoma.pagbank.sample

import io.github.rodrigoma.pagbank.exception.ApiError
import io.github.rodrigoma.pagbank.exception.PagBankException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class PagBankExceptionHandler {
    @ExceptionHandler(PagBankException.ValidationError::class)
    fun handleValidationError(ex: PagBankException.ValidationError): ResponseEntity<Map<String, List<ApiError>>> =
        ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(mapOf("error_messages" to ex.errors))

    @ExceptionHandler(PagBankException.NotFound::class)
    fun handleNotFound(ex: PagBankException.NotFound): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to ex.message.orEmpty()))

    @ExceptionHandler(PagBankException.Unauthorized::class)
    fun handleUnauthorized(ex: PagBankException.Unauthorized): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to ex.message.orEmpty()))

    @ExceptionHandler(PagBankException.ServerError::class)
    fun handleServerError(ex: PagBankException.ServerError): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(mapOf("error" to "PagBank error: ${ex.statusCode}"))
}
