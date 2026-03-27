package io.github.rodrigoma.pagbank.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.rodrigoma.pagbank.model.common.ListParams
import io.github.rodrigoma.pagbank.model.subscription.CreateSubscriptionRequest
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionStatus
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

class PagBankSubscriptionServiceTest {
    private lateinit var service: PagBankSubscriptionService
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
        service = PagBankSubscriptionService(restClient)
    }

    private fun subscriptionMap(id: String = "SUB_123") =
        mapOf(
            "id" to id,
            "plan_id" to "PLAN_001",
            "customer_id" to "CUST_001",
            "status" to "ACTIVE",
            "created_at" to "2026-01-01T00:00:00Z",
        )

    @Test
    fun `create should POST and return SubscriptionResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(subscriptionMap())
        val response =
            service.create(
                CreateSubscriptionRequest(planId = "PLAN_001", customerId = "CUST_001"),
            )
        assertThat(response.id).isEqualTo("SUB_123")
        assertThat(response.status).isEqualTo(SubscriptionStatus.ACTIVE)
    }

    @Test
    fun `get should return SubscriptionResponse by id`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(subscriptionMap("SUB_456"))
        val response = service.get("SUB_456")
        assertThat(response.id).isEqualTo("SUB_456")
    }

    @Test
    fun `cancel should PUT without a response body`() {
        mockFactory.nextBody = ByteArray(0)
        mockFactory.nextStatus = HttpStatus.NO_CONTENT
        service.cancel("SUB_123")
    }

    @Test
    fun `suspend should PUT without a response body`() {
        mockFactory.nextBody = ByteArray(0)
        mockFactory.nextStatus = HttpStatus.NO_CONTENT
        service.suspend("SUB_123")
    }

    @Test
    fun `reactivate should PUT without a response body`() {
        mockFactory.nextBody = ByteArray(0)
        mockFactory.nextStatus = HttpStatus.NO_CONTENT
        service.reactivate("SUB_123")
    }

    @Test
    fun `list should return SubscriptionListResponse with default params`() {
        mockFactory.nextBody =
            mapper.writeValueAsBytes(
                mapOf("subscriptions" to listOf(subscriptionMap())),
            )
        val response = service.list()
        assertThat(response.subscriptions).hasSize(1)
        assertThat(response.subscriptions[0].id).isEqualTo("SUB_123")
    }

    @Test
    fun `list should forward limit and offset params`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(mapOf("subscriptions" to emptyList<Any>()))
        val response = service.list(ListParams(limit = 25, offset = 50))
        assertThat(response.subscriptions).isEmpty()
    }
}
