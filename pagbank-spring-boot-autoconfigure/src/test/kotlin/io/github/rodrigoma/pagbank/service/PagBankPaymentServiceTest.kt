package io.github.rodrigoma.pagbank.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.rodrigoma.pagbank.model.common.ListParams
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

class PagBankPaymentServiceTest {
    private lateinit var service: PagBankPaymentService
    private val mapper: ObjectMapper =
        jacksonObjectMapper().apply {
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        }

    private val mockFactory =
        object : org.springframework.http.client.ClientHttpRequestFactory {
            var nextBody: ByteArray = ByteArray(0)
            var nextStatus: HttpStatus = HttpStatus.OK
            var nextContentType: MediaType = MediaType.APPLICATION_JSON

            override fun createRequest(
                uri: java.net.URI,
                httpMethod: HttpMethod,
            ): org.springframework.http.client.ClientHttpRequest {
                val response = MockClientHttpResponse(nextBody, nextStatus)
                response.headers.contentType = nextContentType
                return MockClientHttpRequest(httpMethod, uri).also { it.setResponse(response) }
            }
        }

    @BeforeEach
    fun setUp() {
        val restClient =
            RestClient
                .builder()
                .requestFactory(mockFactory)
                .messageConverters { converters ->
                    converters.removeIf { it is MappingJackson2HttpMessageConverter }
                    converters.add(0, MappingJackson2HttpMessageConverter(mapper))
                }.build()
        service = PagBankPaymentService(restClient)
    }

    private fun paymentMap(id: String = "PAYM_123") =
        mapOf(
            "id" to id,
            "status" to "APPROVED",
            "invoice" to
                mapOf(
                    "id" to "INVO_001",
                    "amount" to mapOf("value" to 999, "currency" to "BRL"),
                ),
            "customer" to mapOf("id" to "CUST_001", "name" to "Maria Silva", "email" to "maria@example.com"),
            "payment_method" to
                mapOf(
                    "type" to "CREDIT_CARD",
                    "card" to mapOf("brand" to "visa", "first_digits" to "411111", "last_digits" to "1111"),
                ),
            "provider" to
                mapOf("name" to "PAGSEGURO", "transaction_id" to "CHAR_123", "code" to "00", "message" to "Aprovado"),
            "created_at" to "2026-01-15T10:00:00Z",
            "updated_at" to "2026-01-15T10:00:00Z",
        )

    @Test
    fun `get should return PaymentResponse by id`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(paymentMap())
        val response = service.get("PAYM_123")
        assertThat(response.id).isEqualTo("PAYM_123")
        assertThat(response.status).isEqualTo(io.github.rodrigoma.pagbank.model.payment.PaymentStatus.APPROVED)
        assertThat(response.invoice?.id).isEqualTo("INVO_001")
        assertThat(response.provider?.transactionId).isEqualTo("CHAR_123")
    }

    @Test
    fun `list should return PaymentListResponse with default params`() {
        mockFactory.nextBody =
            mapper.writeValueAsBytes(
                mapOf("payments" to listOf(paymentMap())),
            )
        val response = service.list()
        assertThat(response.payments).hasSize(1)
        assertThat(response.payments[0].id).isEqualTo("PAYM_123")
    }

    @Test
    fun `list should forward limit and offset params`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(mapOf("payments" to emptyList<Any>()))
        val response = service.list(ListParams(limit = 20, offset = 40))
        assertThat(response.payments).isEmpty()
    }
}
