package io.github.rodrigoma.pagbank.service

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

class PagBankRefundServiceTest {
    private lateinit var service: PagBankRefundService
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
        service = PagBankRefundService(restClient)
    }

    private fun refundMap(id: String = "REF_123") =
        mapOf(
            "id" to id,
            "amount" to mapOf("value" to 2990, "currency" to "BRL"),
            "status" to "SUCCESS",
            "type" to "FULL",
            "payment" to
                mapOf(
                    "id" to "PAYM_001",
                    "amount" to mapOf("value" to 2990, "currency" to "BRL"),
                ),
            "created_at" to "2026-01-20T12:00:00Z",
        )

    @Test
    fun `get should return RefundResponse by id`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(refundMap("REF_456"))
        val response = service.get("REF_456")
        assertThat(response.id).isEqualTo("REF_456")
    }

    @Test
    fun `list should return RefundListResponse with default params`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(mapOf("refunds" to listOf(refundMap())))
        val response = service.list()
        assertThat(response.refunds).hasSize(1)
    }

    @Test
    fun `list with filters should encode query params in URI`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(mapOf("refunds" to emptyList<Any>()))
        service.list(offset = 5, limit = 50)
        val query = mockFactory.lastUri!!.query
        assertThat(query).contains("offset=5").contains("limit=50")
    }
}
