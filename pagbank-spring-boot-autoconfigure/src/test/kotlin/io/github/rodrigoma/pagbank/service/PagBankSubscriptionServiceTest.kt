package io.github.rodrigoma.pagbank.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.rodrigoma.pagbank.model.common.ListParams
import io.github.rodrigoma.pagbank.model.subscription.CreateSubscriptionRequest
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionCustomerRef
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionCard
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionPaymentMethod
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionPlanRef
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

    private fun subscriptionMap(id: String = "SUBS_123") =
        mapOf(
            "id" to id,
            "plan" to mapOf("id" to "PLAN_001", "name" to "Basic"),
            "customer" to mapOf("id" to "CUST_001", "name" to "Maria Silva", "email" to "maria@example.com"),
            "status" to "ACTIVE",
            "retries" to listOf(mapOf("attempt" to "FIRST", "retried_at" to "2026-03-29", "status" to "SCHEDULED")),
            "created_at" to "2026-01-01T00:00:00Z",
        )

    @Test
    fun `create should POST and return SubscriptionResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(subscriptionMap())
        val response =
            service.create(
                CreateSubscriptionRequest(
                    plan = SubscriptionPlanRef("PLAN_001"),
                    customer = SubscriptionCustomerRef("CUST_001"),
                    paymentMethod = listOf(
                        SubscriptionPaymentMethod(
                            type = "CREDIT_CARD",
                            card = SubscriptionCard(token = "CARD_123", securityCode = "123"),
                        ),
                    ),
                ),
            )
        assertThat(response.id).isEqualTo("SUBS_123")
        assertThat(response.status).isEqualTo(SubscriptionStatus.ACTIVE)
        assertThat(response.plan?.id).isEqualTo("PLAN_001")
        assertThat(response.customer?.id).isEqualTo("CUST_001")
    }

    @Test
    fun `get should return SubscriptionResponse by id`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(subscriptionMap("SUBS_456"))
        val response = service.get("SUBS_456")
        assertThat(response.id).isEqualTo("SUBS_456")
    }

    @Test
    fun `cancel should PUT without a response body`() {
        mockFactory.nextBody = ByteArray(0)
        mockFactory.nextStatus = HttpStatus.NO_CONTENT
        service.cancel("SUBS_123")
    }

    @Test
    fun `suspend should PUT without a response body`() {
        mockFactory.nextBody = ByteArray(0)
        mockFactory.nextStatus = HttpStatus.NO_CONTENT
        service.suspend("SUBS_123")
    }

    @Test
    fun `reactivate should PUT without a response body`() {
        mockFactory.nextBody = ByteArray(0)
        mockFactory.nextStatus = HttpStatus.NO_CONTENT
        service.reactivate("SUBS_123")
    }

    @Test
    fun `list should return SubscriptionListResponse with default params`() {
        mockFactory.nextBody =
            mapper.writeValueAsBytes(
                mapOf("subscriptions" to listOf(subscriptionMap())),
            )
        val response = service.list()
        assertThat(response.subscriptions).hasSize(1)
        assertThat(response.subscriptions[0].id).isEqualTo("SUBS_123")
    }

    @Test
    fun `list should forward limit and offset params`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(mapOf("subscriptions" to emptyList<Any>()))
        val response = service.list(ListParams(limit = 25, offset = 50))
        assertThat(response.subscriptions).isEmpty()
    }
}
