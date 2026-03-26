package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.common.ListParams
import io.github.rodrigoma.pagbank.model.plan.*
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

class PagBankPlanServiceTest {

    private lateinit var service: PagBankPlanService
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
        service = PagBankPlanService(restClient)
    }

    @Test
    fun `create should POST and return PlanResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(
            mapOf("id" to "PLAN_123", "name" to "Basic",
                  "amount" to mapOf("value" to 999, "currency" to "BRL"),
                  "interval" to mapOf("length" to 1, "unit" to "month"),
                  "status" to "ACTIVE", "created_at" to "2026-01-01T00:00:00Z",
                  "reference_id" to null, "trial" to null, "updated_at" to null)
        )
        val response = service.create(
            CreatePlanRequest("Basic", Money(999), PlanInterval(1, "month"))
        )
        assertThat(response.id).isEqualTo("PLAN_123")
        assertThat(response.amount.value).isEqualTo(999)
    }

    @Test
    fun `get should return plan by id`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(
            mapOf("id" to "PLAN_123", "name" to "Basic",
                  "amount" to mapOf("value" to 999, "currency" to "BRL"),
                  "interval" to mapOf("length" to 1, "unit" to "month"),
                  "status" to "ACTIVE", "created_at" to "2026-01-01T00:00:00Z",
                  "reference_id" to null, "trial" to null, "updated_at" to null)
        )
        val response = service.get("PLAN_123")
        assertThat(response.id).isEqualTo("PLAN_123")
    }

    @Test
    fun `list should return PlanListResponse with default params`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(
            mapOf("plans" to listOf(
                mapOf("id" to "PLAN_001", "name" to "Basic",
                      "amount" to mapOf("value" to 999, "currency" to "BRL"),
                      "interval" to mapOf("length" to 1, "unit" to "month"),
                      "status" to "ACTIVE", "created_at" to "2026-01-01T00:00:00Z",
                      "reference_id" to null, "trial" to null, "updated_at" to null)
            ))
        )
        val response = service.list()
        assertThat(response.plans).hasSize(1)
        assertThat(response.plans[0].id).isEqualTo("PLAN_001")
    }

    @Test
    fun `list should forward limit and offset params`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(mapOf("plans" to emptyList<Any>()))
        val response = service.list(ListParams(limit = 5, offset = 20))
        assertThat(response.plans).isEmpty()
    }
}
