package io.github.rodrigoma.pagbank.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.rodrigoma.pagbank.model.refund.RefundRequest
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
        service = PagBankRefundService(restClient)
    }

    private fun refundMap(id: String = "REF_123") =
        mapOf(
            "id" to id,
            "payment_id" to "PAY_001",
            "amount" to 2990,
            "status" to "COMPLETED",
            "created_at" to "2026-01-20T12:00:00Z",
        )

    @Test
    fun `create should POST full refund and return RefundResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(refundMap())
        val response = service.create("PAY_001")
        assertThat(response.id).isEqualTo("REF_123")
        assertThat(response.paymentId).isEqualTo("PAY_001")
        assertThat(response.amount).isEqualTo(2990)
        assertThat(response.status).isEqualTo("COMPLETED")
    }

    @Test
    fun `create should POST partial refund with amount`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(refundMap())
        val response = service.create("PAY_001", RefundRequest(amount = 1000))
        assertThat(response.id).isEqualTo("REF_123")
    }

    @Test
    fun `get should return RefundResponse by id`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(refundMap("REF_456"))
        val response = service.get("REF_456")
        assertThat(response.id).isEqualTo("REF_456")
    }
}
