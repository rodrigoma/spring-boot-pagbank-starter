package io.github.rodrigoma.pagbank.http

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.rodrigoma.pagbank.exception.ApiError
import io.github.rodrigoma.pagbank.exception.PagBankException
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpResponse

// PagBankErrorHandler is registered on the RestClient builder via:
//   .defaultStatusHandler({ it.isError }, handler::handle)
// It does NOT implement ResponseErrorHandler (a RestTemplate interface).
// The handle() method is called by RestClient for any 4xx/5xx response.
class PagBankErrorHandler(
    objectMapper: ObjectMapper,
) {
    companion object {
        private const val HTTP_UNAUTHORIZED = 401
        private const val HTTP_FORBIDDEN = 403
        private const val HTTP_NOT_FOUND = 404
        private const val HTTP_BAD_REQUEST = 400
        private const val HTTP_UNPROCESSABLE = 422
    }

    private val objectMapper: ObjectMapper =
        objectMapper.copy().apply {
            if (!registeredModuleIds.contains(KotlinModule::class.java.name)) {
                registerModule(KotlinModule.Builder().build())
            }
        }

    @Suppress("UnusedParameter")
    fun handle(
        statusCode: HttpStatusCode,
        response: ClientHttpResponse,
    ) {
        handle(response)
    }

    fun handle(response: ClientHttpResponse) {
        val statusCode = response.statusCode.value()
        val body = runCatching { response.body.readBytes() }.getOrDefault(ByteArray(0))

        throw when (statusCode) {
            HTTP_UNAUTHORIZED, HTTP_FORBIDDEN -> PagBankException.Unauthorized(bodyAsString(body))
            HTTP_NOT_FOUND -> PagBankException.NotFound("Resource not found")
            HTTP_BAD_REQUEST, HTTP_UNPROCESSABLE -> parseValidationError(body, statusCode)
            else -> PagBankException.ServerError(statusCode)
        }
    }

    @Suppress("SwallowedException")
    private fun parseValidationError(
        body: ByteArray,
        statusCode: Int,
    ): PagBankException =
        try {
            val errors: List<ApiError> = objectMapper.readValue(body, object : TypeReference<List<ApiError>>() {})
            PagBankException.ValidationError(errors)
        } catch (e: JsonProcessingException) {
            PagBankException.ServerError(statusCode)
        }

    private fun bodyAsString(body: ByteArray): String = if (body.isEmpty()) "Unauthorized" else body.decodeToString()
}
