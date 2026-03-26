package com.example.pagseguro.http

import com.example.pagseguro.exception.ApiError
import com.example.pagseguro.exception.PagSeguroException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpResponse

// PagSeguroErrorHandler is registered on the RestClient builder via:
//   .defaultStatusHandler({ it.isError }, handler::handle)
// It does NOT implement ResponseErrorHandler (a RestTemplate interface).
// The handle() method is called by RestClient for any 4xx/5xx response.
class PagSeguroErrorHandler(objectMapper: ObjectMapper) {

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
            401, 403 -> PagSeguroException.Unauthorized(bodyAsString(body))
            404 -> PagSeguroException.NotFound("Resource not found")
            400, 422 -> parseValidationError(body, statusCode)
            else -> PagSeguroException.ServerError(statusCode)
        }
    }

    private fun parseValidationError(body: ByteArray, statusCode: Int): PagSeguroException =
        try {
            val errors: List<ApiError> = objectMapper.readValue(body, object : TypeReference<List<ApiError>>() {})
            PagSeguroException.ValidationError(errors)
        } catch (e: Exception) {
            PagSeguroException.ServerError(statusCode)
        }

    private fun bodyAsString(body: ByteArray): String =
        if (body.isEmpty()) "Unauthorized" else body.decodeToString()
}
