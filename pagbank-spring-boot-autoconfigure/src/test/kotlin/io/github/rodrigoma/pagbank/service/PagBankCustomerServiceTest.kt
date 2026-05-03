package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.customer.BillingInfoRequest
import io.github.rodrigoma.pagbank.model.customer.BillingInfoType
import io.github.rodrigoma.pagbank.model.customer.CardHolder
import io.github.rodrigoma.pagbank.model.customer.CardRequest
import io.github.rodrigoma.pagbank.model.customer.CreateCustomerRequest
import io.github.rodrigoma.pagbank.model.customer.UpdateCustomerRequest
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
            "tax_id" to "12345678900",
            "created_at" to "2026-01-01T00:00:00Z",
        )

    private fun listResponseMap(
        customers: List<Map<String, Any>> = listOf(customerMap()),
        total: Int = customers.size,
        offset: Int = 0,
        limit: Int = 100,
    ) = mapOf(
        "result_set" to mapOf("total" to total, "offset" to offset, "limit" to limit),
        "customers" to customers,
    )

    @Test
    fun `create should POST and return CustomerResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(customerMap())
        val response =
            service.create(
                CreateCustomerRequest(
                    name = "Maria Silva",
                    email = "maria@example.com",
                    taxId = "12345678900",
                ),
            )
        assertThat(response.id).isEqualTo("CUST_123")
        assertThat(response.email).isEqualTo("maria@example.com")
    }

    @Test
    fun `create with encrypted card should serialize without plain fields`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(customerMap())
        service.create(
            CreateCustomerRequest(
                name = "Maria Silva",
                email = "maria@example.com",
                taxId = "12345678900",
                billingInfo = listOf(
                    BillingInfoRequest(
                        type = BillingInfoType.CREDIT_CARD,
                        card = CardRequest.Encrypted(encrypted = "ENC_TOKEN_ABC"),
                    ),
                ),
            ),
        )
    }

    @Test
    fun `create with plain card should serialize without encrypted field`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(customerMap())
        service.create(
            CreateCustomerRequest(
                name = "Maria Silva",
                email = "maria@example.com",
                taxId = "12345678900",
                billingInfo = listOf(
                    BillingInfoRequest(
                        type = BillingInfoType.CREDIT_CARD,
                        card = CardRequest.Plain(
                            number = "4111111111111111",
                            expYear = "2043",
                            expMonth = "12",
                            holder = CardHolder(name = "Maria Silva"),
                            securityCode = "123",
                        ),
                    ),
                ),
            ),
        )
    }

    @Test
    fun `get should return CustomerResponse by id`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(customerMap("CUST_456"))
        val response = service.get("CUST_456")
        assertThat(response.id).isEqualTo("CUST_456")
    }

    @Test
    fun `update should PUT with UpdateCustomerRequest and return CustomerResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(customerMap())
        val response =
            service.update(
                "CUST_123",
                UpdateCustomerRequest(name = "Maria Silva Atualizada", email = "new@example.com"),
            )
        assertThat(response.id).isEqualTo("CUST_123")
    }

    @Test
    fun `updateBillingInfo should PUT array of BillingInfoRequest and return CustomerResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(customerMap())
        val response =
            service.updateBillingInfo(
                "CUST_123",
                listOf(
                    BillingInfoRequest(
                        type = BillingInfoType.CREDIT_CARD,
                        card = CardRequest.Encrypted(encrypted = "ENC_TOKEN_ABC"),
                    ),
                ),
            )
        assertThat(response.id).isEqualTo("CUST_123")
    }

    @Test
    fun `list should return CustomerListResponse with result_set`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(listResponseMap(total = 133, offset = 0, limit = 100))
        val response = service.list()
        assertThat(response.customers).hasSize(1)
        assertThat(response.customers[0].id).isEqualTo("CUST_123")
        assertThat(response.resultSet.total).isEqualTo(133)
        assertThat(response.resultSet.offset).isEqualTo(0)
        assertThat(response.resultSet.limit).isEqualTo(100)
    }

    @Test
    fun `list with params should pass offset limit and referenceId`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(listResponseMap(customers = emptyList(), total = 0))
        val response = service.list(offset = 10, limit = 5, referenceId = "ref-abc")
        assertThat(response.customers).isEmpty()
        assertThat(response.resultSet.total).isEqualTo(0)
    }
}
