package io.github.rodrigoma.pagbank.http

import io.github.rodrigoma.pagbank.exception.ApiError
import io.github.rodrigoma.pagbank.exception.PagBankException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpResponse

// PagBankErrorHandler is registered on the RestClient builder via:
//   .defaultStatusHandler({ it.isError }, handler::handle)
// It does NOT implement ResponseErrorHandler (a RestTemplate interface).
// The handle() method is called by RestClient for any 4xx/5xx response.
class PagBankErrorHandler(objectMapper: ObjectMapper) {

    private val objectMapper: ObjectMapper = objectMapper.copy().apply {
        if (!registeredModuleIds.contains(KotlinModule::class.java.name)) {
            registerModule(KotlinModule.Builder().build())
        }
    }

    fun handle(statusCode: HttpStatusCode, response: ClientHttpResponse) {
        handle(response)
    }

    fun handle(response: ClientHttpResponse) {
        val statusCode = response.statusCode.value()
        val body = runCatching { response.body.readBytes() }.getOrDefault(ByteArray(0))

        throw when (statusCode) {
            401, 403 -> PagBankException.Unauthorized(bodyAsString(body))
            404 -> PagBankException.NotFound("Resource not found")
            400, 422 -> parseValidationError(body, statusCode)
            else -> PagBankException.ServerError(statusCode)
        }
    }

    private fun parseValidationError(body: ByteArray, statusCode: Int): PagBankException =
        try {
            val errors: List<ApiError> = objectMapper.readValue(body, object : TypeReference<List<ApiError>>() {})
            PagBankException.ValidationError(errors)
        } catch (e: Exception) {
            PagBankException.ServerError(statusCode)
        }

    private fun bodyAsString(body: ByteArray): String =
        if (body.isEmpty()) "Unauthorized" else body.decodeToString()
}
