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

    @Test
    fun `get should return PaymentResponse by id`() {
        mockFactory.nextBody =
            mapper.writeValueAsBytes(
                mapOf(
                    "id" to "PAY_123",
                    "invoice_id" to "INV_123",
                    "amount" to 999,
                    "status" to "PAID",
                    "paid_at" to "2026-01-15T10:00:00Z",
                ),
            )
        val response = service.get("PAY_123")
        assertThat(response.id).isEqualTo("PAY_123")
        assertThat(response.status).isEqualTo("PAID")
    }

    @Test
    fun `list should return PaymentListResponse with default params`() {
        mockFactory.nextBody =
            mapper.writeValueAsBytes(
                mapOf(
                    "payments" to
                        listOf(
                            mapOf(
                                "id" to "PAY_001",
                                "invoice_id" to "INV_001",
                                "amount" to 2990,
                                "status" to "PAID",
                                "paid_at" to "2026-01-15T10:00:00Z",
                            ),
                        ),
                ),
            )
        val response = service.list()
        assertThat(response.payments).hasSize(1)
        assertThat(response.payments[0].id).isEqualTo("PAY_001")
    }

    @Test
    fun `list should forward limit and offset params`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(mapOf("payments" to emptyList<Any>()))
        val response = service.list(ListParams(limit = 20, offset = 40))
        assertThat(response.payments).isEmpty()
    }
}
