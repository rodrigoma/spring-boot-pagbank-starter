package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.invoice.InvoiceStatus
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

class PagBankInvoiceServiceTest {
    private lateinit var service: PagBankInvoiceService
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
        service = PagBankInvoiceService(restClient)
    }

    private fun invoiceMap(id: String = "INVO_123") =
        mapOf(
            "id" to id,
            "amount" to mapOf("value" to 2990, "currency" to "BRL"),
            "status" to "PAID",
            "occurrence" to 1,
            "plan" to mapOf("id" to "PLAN_001", "name" to "Basic"),
            "items" to
                listOf(
                    mapOf("amount" to mapOf("value" to 2990, "currency" to "BRL"), "type" to "SUBSCRIPTION_AMOUNT"),
                ),
            "subscription" to mapOf("id" to "SUBS_001"),
            "customer" to mapOf("id" to "CUST_001", "name" to "Maria Silva", "email" to "maria@example.com"),
            "created_at" to "2026-01-01T00:00:00Z",
            "updated_at" to "2026-01-01T00:00:00Z",
        )

    private fun paymentMap(id: String = "PAYM_001") =
        mapOf(
            "id" to id,
            "status" to "APPROVED",
            "invoice" to mapOf("id" to "INVO_123"),
        )

    @Test
    fun `get should return InvoiceResponse by id`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(invoiceMap())
        val response = service.get("INVO_123")
        assertThat(response.id).isEqualTo("INVO_123")
        assertThat(response.amount.value).isEqualTo(2990)
        assertThat(response.status).isEqualTo(InvoiceStatus.PAID)
    }

    @Test
    fun `listPayments should GET payments for invoice`() {
        mockFactory.nextBody =
            mapper.writeValueAsBytes(
                mapOf("result_set" to mapOf("total" to 1), "payments" to listOf(paymentMap())),
            )
        val response = service.listPayments("INVO_123")
        assertThat(response.payments).hasSize(1)
        assertThat(response.payments[0].id).isEqualTo("PAYM_001")
        assertThat(response.resultSet.total).isEqualTo(1)
        assertThat(mockFactory.lastUri!!.path).contains("/invoices/INVO_123/payments")
    }

    @Test
    fun `listPayments with filters should encode query params in URI`() {
        mockFactory.nextBody =
            mapper.writeValueAsBytes(
                mapOf("result_set" to mapOf("total" to 0), "payments" to emptyList<Any>()),
            )
        service.listPayments("INVO_123", offset = 5, limit = 20)
        val query = mockFactory.lastUri!!.query
        assertThat(query).contains("offset=5").contains("limit=20")
    }
}
