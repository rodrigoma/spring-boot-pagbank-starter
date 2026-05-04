package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.payment.PaymentStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.mock.http.client.MockClientHttpRequest
import org.springframework.mock.http.client.MockClientHttpResponse
import org.springframework.web.client.RestClient
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.module.kotlin.jacksonMapperBuilder

class PagBankPaymentServiceTest {
    private lateinit var service: PagBankPaymentService
    private val mapper =
        jacksonMapperBuilder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build()

    private val mockFactory =
        object : org.springframework.http.client.ClientHttpRequestFactory {
            var nextBody: ByteArray = ByteArray(0)
            var nextStatus: HttpStatus = HttpStatus.OK
            var nextContentType: MediaType = MediaType.APPLICATION_JSON
            var lastUri: java.net.URI? = null

            override fun createRequest(
                uri: java.net.URI,
                httpMethod: HttpMethod,
            ): org.springframework.http.client.ClientHttpRequest {
                lastUri = uri
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
                .configureMessageConverters { converters ->
                    converters.registerDefaults().withJsonConverter(JacksonJsonHttpMessageConverter(mapper))
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
        assertThat(response.status).isEqualTo(PaymentStatus.APPROVED)
        assertThat(response.invoice?.id).isEqualTo("INVO_001")
        assertThat(response.provider?.transactionId).isEqualTo("CHAR_123")
    }

    @Test
    fun `list should return PaymentListResponse with default params`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(mapOf("payments" to listOf(paymentMap())))
        val response = service.list()
        assertThat(response.payments).hasSize(1)
        assertThat(response.payments[0].id).isEqualTo("PAYM_123")
    }

    @Test
    fun `list with filters should encode query params in URI`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(mapOf("payments" to emptyList<Any>()))
        service.list(
            offset = 10,
            limit = 25,
            status = PaymentStatus.APPROVED,
            paidAtStart = "2026-01-01",
            paidAtEnd = "2026-01-31",
            paymentMethodType = "CREDIT_CARD",
        )
        val query = mockFactory.lastUri!!.query
        assertThat(query).contains("offset=10").contains("limit=25")
        assertThat(query).contains("status=APPROVED")
        assertThat(query).contains("paid_at_start=2026-01-01").contains("paid_at_end=2026-01-31")
        assertThat(query).contains("payment_method_type=CREDIT_CARD")
    }
}
