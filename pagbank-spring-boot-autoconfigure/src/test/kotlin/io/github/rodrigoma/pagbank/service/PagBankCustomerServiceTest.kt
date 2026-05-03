package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.common.ListParams
import io.github.rodrigoma.pagbank.model.customer.CreateCustomerRequest
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

class PagBankCustomerServiceTest {
    private lateinit var service: PagBankCustomerService
    private val mapper =
        jacksonMapperBuilder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build()

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
                .configureMessageConverters { converters ->
                    converters.registerDefaults().withJsonConverter(JacksonJsonHttpMessageConverter(mapper))
                }.build()
        service = PagBankCustomerService(restClient)
    }

    private fun customerMap(id: String = "CUST_123") =
        mapOf(
            "id" to id,
            "name" to "Maria Silva",
            "email" to "maria@example.com",
            "tax_id" to "123.456.789-00",
            "created_at" to "2026-01-01T00:00:00Z",
        )

    @Test
    fun `create should POST and return CustomerResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(customerMap())
        val response =
            service.create(
                CreateCustomerRequest(
                    name = "Maria Silva",
                    email = "maria@example.com",
                    taxId = "123.456.789-00",
                ),
            )
        assertThat(response.id).isEqualTo("CUST_123")
        assertThat(response.email).isEqualTo("maria@example.com")
    }

    @Test
    fun `get should return CustomerResponse by id`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(customerMap("CUST_456"))
        val response = service.get("CUST_456")
        assertThat(response.id).isEqualTo("CUST_456")
    }

    @Test
    fun `update should PUT and return updated CustomerResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(customerMap())
        val response =
            service.update(
                "CUST_123",
                CreateCustomerRequest(
                    name = "Maria Silva",
                    email = "new@example.com",
                    taxId = "123.456.789-00",
                ),
            )
        assertThat(response.id).isEqualTo("CUST_123")
    }

    @Test
    fun `list should return CustomerListResponse with default params`() {
        mockFactory.nextBody =
            mapper.writeValueAsBytes(
                mapOf("customers" to listOf(customerMap())),
            )
        val response = service.list()
        assertThat(response.customers).hasSize(1)
        assertThat(response.customers[0].id).isEqualTo("CUST_123")
    }

    @Test
    fun `list with custom params should return empty response`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(mapOf("customers" to emptyList<Any>()))
        val response = service.list(ListParams(limit = 5, offset = 10))
        assertThat(response.customers).isEmpty()
    }

    @Test
    fun `updateBillingInfo should PUT and return CustomerResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(customerMap())
        val response =
            service.updateBillingInfo(
                "CUST_123",
                io.github.rodrigoma.pagbank.model.customer.UpdateBillingInfoRequest(
                    billingInfo =
                        listOf(
                            io.github.rodrigoma.pagbank.model.customer.BillingInfo(
                                type = io.github.rodrigoma.pagbank.model.customer.BillingInfoType.CREDIT_CARD,
                            ),
                        ),
                ),
            )
        assertThat(response.id).isEqualTo("CUST_123")
    }
}
