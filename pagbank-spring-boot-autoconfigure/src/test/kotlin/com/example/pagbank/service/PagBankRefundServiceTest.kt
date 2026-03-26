package com.example.pagbank.service

import com.example.pagbank.model.refund.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.http.client.MockClientHttpRequest
import org.springframework.mock.http.client.MockClientHttpResponse
import org.springframework.web.client.RestClient

class PagBankRefundServiceTest {

    private lateinit var service: PagBankRefundService
    private val mapper: ObjectMapper = jacksonObjectMapper().apply {
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }

    private val mockFactory = object : org.springframework.http.client.ClientHttpRequestFactory {
        var nextBody: ByteArray = ByteArray(0)
        var nextStatus: HttpStatus = HttpStatus.OK
        var nextContentType: MediaType = MediaType.APPLICATION_JSON

        override fun createRequest(uri: java.net.URI, httpMethod: HttpMethod): org.springframework.http.client.ClientHttpRequest {
            val response = MockClientHttpResponse(nextBody, nextStatus)
            response.headers.contentType = nextContentType
            return MockClientHttpRequest(httpMethod, uri).also { it.setResponse(response) }
        }
    }

    @BeforeEach
    fun setUp() {
        val restClient = RestClient.builder()
            .requestFactory(mockFactory)
            .messageConverters { converters ->
                converters.removeIf { it is MappingJackson2HttpMessageConverter }
                converters.add(0, MappingJackson2HttpMessageConverter(mapper))
            }
            .build()
        service = PagBankRefundService(restClient)
    }

    @Test
    fun `create should POST to payments refunds and return RefundResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(
            mapOf(
                "id" to "REF_123",
                "payment_id" to "PAY_123",
                "amount" to 999,
                "status" to "COMPLETED",
                "created_at" to "2026-01-15T10:00:00Z"
            )
        )
        val response = service.create("PAY_123", RefundRequest())
        assertThat(response.id).isEqualTo("REF_123")
        assertThat(response.paymentId).isEqualTo("PAY_123")
    }
}
