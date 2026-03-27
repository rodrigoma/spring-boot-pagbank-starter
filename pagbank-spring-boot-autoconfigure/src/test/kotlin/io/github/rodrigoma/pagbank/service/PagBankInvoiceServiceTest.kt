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

class PagBankInvoiceServiceTest {
    private lateinit var service: PagBankInvoiceService
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
        service = PagBankInvoiceService(restClient)
    }

    private fun invoiceMap(id: String = "INV_123") =
        mapOf(
            "id" to id,
            "subscription_id" to "SUB_001",
            "amount" to 2990,
            "status" to "PAID",
            "due_date" to "2026-02-01",
        )

    @Test
    fun `get should return InvoiceResponse by id`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(invoiceMap())
        val response = service.get("INV_123")
        assertThat(response.id).isEqualTo("INV_123")
        assertThat(response.amount).isEqualTo(2990)
        assertThat(response.status).isEqualTo("PAID")
    }

    @Test
    fun `listBySubscription should return InvoiceListResponse with default params`() {
        mockFactory.nextBody =
            mapper.writeValueAsBytes(
                mapOf("invoices" to listOf(invoiceMap())),
            )
        val response = service.listBySubscription("SUB_001")
        assertThat(response.invoices).hasSize(1)
        assertThat(response.invoices[0].subscriptionId).isEqualTo("SUB_001")
    }

    @Test
    fun `listBySubscription should forward limit and offset params`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(mapOf("invoices" to emptyList<Any>()))
        val response = service.listBySubscription("SUB_001", ListParams(limit = 3, offset = 6))
        assertThat(response.invoices).isEmpty()
    }
}
