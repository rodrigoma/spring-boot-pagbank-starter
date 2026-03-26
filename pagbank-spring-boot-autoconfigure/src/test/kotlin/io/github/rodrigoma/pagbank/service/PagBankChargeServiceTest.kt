package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.charge.*
import io.github.rodrigoma.pagbank.model.common.ListParams
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

class PagBankChargeServiceTest {

    private lateinit var service: PagBankChargeService
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
        service = PagBankChargeService(restClient)
    }

    private fun chargeMap(id: String = "CHG_123") = mapOf(
        "id" to id,
        "subscription_id" to "SUB_001",
        "amount" to 2990,
        "status" to "PAID"
    )

    @Test
    fun `get should return ChargeResponse by id`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(chargeMap())
        val response = service.get("CHG_123")
        assertThat(response.id).isEqualTo("CHG_123")
        assertThat(response.amount).isEqualTo(2990)
    }

    @Test
    fun `list should return ChargeListResponse with default params`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(
            mapOf("charges" to listOf(chargeMap()))
        )
        val response = service.list()
        assertThat(response.charges).hasSize(1)
        assertThat(response.charges[0].id).isEqualTo("CHG_123")
    }

    @Test
    fun `list should forward limit and offset params`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(mapOf("charges" to emptyList<Any>()))
        val response = service.list(ListParams(limit = 50, offset = 100))
        assertThat(response.charges).isEmpty()
    }

    @Test
    fun `retry should POST without a response body`() {
        mockFactory.nextBody = ByteArray(0)
        mockFactory.nextStatus = HttpStatus.NO_CONTENT
        service.retry("CHG_123")
    }

    @Test
    fun `retry should POST with paymentMethod`() {
        mockFactory.nextBody = ByteArray(0)
        mockFactory.nextStatus = HttpStatus.NO_CONTENT
        service.retry("CHG_123", RetryChargeRequest(paymentMethod = "CARD"))
    }
}
