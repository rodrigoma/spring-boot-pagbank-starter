package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.invoice.InvoiceStatus
import io.github.rodrigoma.pagbank.model.subscription.BestInvoiceDate
import io.github.rodrigoma.pagbank.model.subscription.CreateSubscriptionRequest
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionCard
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionCoupon
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionCustomerRef
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionPaymentMethod
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionPlanRef
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionStatus
import io.github.rodrigoma.pagbank.model.subscription.UpdateSubscriptionRequest
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

class PagBankSubscriptionServiceTest {
    private lateinit var service: PagBankSubscriptionService
    private val mapper =
        jacksonMapperBuilder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build()

    private val mockFactory =
        object : org.springframework.http.client.ClientHttpRequestFactory {
            var nextBody: ByteArray = ByteArray(0)
            var nextStatus: HttpStatus = HttpStatus.OK
            var nextContentType: MediaType = MediaType.APPLICATION_JSON
            var lastRequest: MockClientHttpRequest? = null
            var lastUri: java.net.URI? = null

            override fun createRequest(
                uri: java.net.URI,
                httpMethod: HttpMethod,
            ): org.springframework.http.client.ClientHttpRequest {
                lastUri = uri
                val response = MockClientHttpResponse(nextBody, nextStatus)
                response.headers.contentType = nextContentType
                return MockClientHttpRequest(httpMethod, uri).also {
                    it.setResponse(response)
                    lastRequest = it
                }
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

    private fun invoiceListMap() =
        mapOf(
            "result_set" to mapOf("total" to 2),
            "invoices" to
                listOf(
                    mapOf(
                        "id" to "INVO_001",
                        "status" to "PAID",
                        "amount" to mapOf("value" to 1990, "currency" to "BRL"),
                    ),
                ),
        )

    @Test
    fun `create should POST and return SubscriptionResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(subscriptionMap())
        val response =
            service.create(
                CreateSubscriptionRequest(
                    plan = SubscriptionPlanRef("PLAN_001"),
                    customer = SubscriptionCustomerRef("CUST_001"),
                    paymentMethod =
                        listOf(
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
    fun `update should PUT and return SubscriptionResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(subscriptionMap())
        val response =
            service.update(
                "SUBS_123",
                UpdateSubscriptionRequest(
                    plan = SubscriptionPlanRef("PLAN_002"),
                    bestInvoiceDate = BestInvoiceDate(day = 1, month = 12),
                    coupon = SubscriptionCoupon(id = "COUP_001"),
                ),
            )
        assertThat(response.id).isEqualTo("SUBS_123")
        val body = mockFactory.lastRequest!!.bodyAsString
        assertThat(body).contains("plan").contains("best_invoice_date")
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
    fun `activate should PUT without a response body`() {
        mockFactory.nextBody = ByteArray(0)
        mockFactory.nextStatus = HttpStatus.NO_CONTENT
        service.activate("SUBS_123")
        assertThat(mockFactory.lastUri!!.path).contains("/activate")
    }

    @Test
    fun `retry should PUT without a response body`() {
        mockFactory.nextBody = ByteArray(0)
        mockFactory.nextStatus = HttpStatus.NO_CONTENT
        service.retry("SUBS_123")
        assertThat(mockFactory.lastUri!!.path).contains("/retry")
    }

    @Test
    fun `removeCoupon should DELETE without a response body`() {
        mockFactory.nextBody = ByteArray(0)
        mockFactory.nextStatus = HttpStatus.NO_CONTENT
        service.removeCoupon("SUBS_123")
        assertThat(mockFactory.lastUri!!.path).contains("/coupons")
        assertThat(mockFactory.lastRequest!!.method).isEqualTo(HttpMethod.DELETE)
    }

    @Test
    fun `list should return SubscriptionListResponse with default params`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(mapOf("subscriptions" to listOf(subscriptionMap())))
        val response = service.list()
        assertThat(response.subscriptions).hasSize(1)
        assertThat(response.subscriptions[0].id).isEqualTo("SUBS_123")
    }

    @Test
    fun `list with filters should encode query params in URI`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(mapOf("subscriptions" to emptyList<Any>()))
        service.list(offset = 10, limit = 25, referenceId = "ref-abc", status = SubscriptionStatus.ACTIVE)
        val query = mockFactory.lastUri!!.query
        assertThat(query).contains("offset=10").contains("limit=25")
        assertThat(query).contains("reference_id=ref-abc").contains("status=ACTIVE")
    }

    @Test
    fun `listInvoices should GET invoices for a subscription`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(invoiceListMap())
        val response = service.listInvoices("SUBS_123")
        assertThat(response.invoices).hasSize(1)
        assertThat(response.invoices[0].id).isEqualTo("INVO_001")
        assertThat(response.resultSet.total).isEqualTo(2)
        assertThat(mockFactory.lastUri!!.path).contains("/subscriptions/SUBS_123/invoices")
    }

    @Test
    fun `listInvoices with status filter should encode status in URI`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(invoiceListMap())
        service.listInvoices("SUBS_123", status = InvoiceStatus.PAID)
        assertThat(mockFactory.lastUri!!.query).contains("status=PAID")
    }
}
