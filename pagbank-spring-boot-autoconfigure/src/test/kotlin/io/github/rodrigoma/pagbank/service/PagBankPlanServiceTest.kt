package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.plan.CreatePlanRequest
import io.github.rodrigoma.pagbank.model.plan.IntervalUnit
import io.github.rodrigoma.pagbank.model.plan.Money
import io.github.rodrigoma.pagbank.model.plan.PlanInterval
import io.github.rodrigoma.pagbank.model.plan.PlanStatus
import io.github.rodrigoma.pagbank.model.plan.UpdatePlanRequest
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

class PagBankPlanServiceTest {
    private lateinit var service: PagBankPlanService
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
        service = PagBankPlanService(restClient)
    }

    private fun planMap(
        id: String = "PLAN_123",
        name: String = "Basic",
        status: String = "ACTIVE",
    ) = mapOf(
        "id" to id,
        "name" to name,
        "amount" to mapOf("value" to 999, "currency" to "BRL"),
        "interval" to mapOf("length" to 1, "unit" to "MONTH"),
        "status" to status,
        "created_at" to "2026-01-01T00:00:00Z",
    )

    @Suppress("LongParameterList")
    private fun listResponseMap(
        plans: List<Map<String, Any>> = listOf(planMap()),
        total: Int = plans.size,
        offset: Int = 0,
        limit: Int = 100,
        referenceId: String? = null,
        statusFilter: List<String> = emptyList(),
    ) = mapOf(
        "result_set" to
            mapOf(
                "total" to total,
                "offset" to offset,
                "limit" to limit,
                "reference_id" to referenceId,
                "status" to statusFilter,
            ),
        "plans" to plans,
    )

    @Test
    fun `create should POST and return PlanResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(planMap())
        val response =
            service.create(
                CreatePlanRequest("Basic", Money(999), PlanInterval(1, IntervalUnit.MONTH)),
            )
        assertThat(response.id).isEqualTo("PLAN_123")
        assertThat(response.amount.value).isEqualTo(999)
    }

    @Test
    fun `get should return plan by id`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(planMap("PLAN_456"))
        val response = service.get("PLAN_456")
        assertThat(response.id).isEqualTo("PLAN_456")
    }

    @Test
    fun `update should PUT and return updated PlanResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(planMap(name = "Updated Name"))
        val response = service.update("PLAN_123", UpdatePlanRequest(name = "Updated Name"))
        assertThat(response.name).isEqualTo("Updated Name")
    }

    @Test
    fun `list should return PlanListResponse with result_set`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(listResponseMap(total = 36, offset = 0, limit = 100))
        val response = service.list()
        assertThat(response.plans).hasSize(1)
        assertThat(response.plans[0].id).isEqualTo("PLAN_123")
        assertThat(response.resultSet.total).isEqualTo(36)
        assertThat(response.resultSet.offset).isEqualTo(0)
        assertThat(response.resultSet.limit).isEqualTo(100)
        assertThat(response.resultSet.status).isEmpty()
    }

    @Test
    fun `list with referenceId filter should reflect in result_set`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(listResponseMap(total = 5, referenceId = "plano"))
        val response = service.list(referenceId = "plano")
        assertThat(response.resultSet.referenceId).isEqualTo("plano")
        assertThat(response.resultSet.total).isEqualTo(5)
    }

    @Test
    fun `list with status filter should reflect in result_set`() {
        mockFactory.nextBody =
            mapper.writeValueAsBytes(listResponseMap(statusFilter = listOf("ACTIVE", "INACTIVE")))
        val response = service.list(status = PlanStatus.ACTIVE)
        assertThat(response.resultSet.status).containsExactly("ACTIVE", "INACTIVE")
    }

    @Test
    fun `list with offset and limit should pass params`() {
        mockFactory.nextBody =
            mapper.writeValueAsBytes(listResponseMap(plans = emptyList(), total = 0, offset = 20, limit = 5))
        val response = service.list(offset = 20, limit = 5)
        assertThat(response.plans).isEmpty()
        assertThat(response.resultSet.offset).isEqualTo(20)
        assertThat(response.resultSet.limit).isEqualTo(5)
        val query = mockFactory.lastUri!!.query
        assertThat(query).contains("offset=20").contains("limit=5")
    }

    @Test
    fun `activate should PUT without a response body`() {
        mockFactory.nextBody = ByteArray(0)
        mockFactory.nextStatus = HttpStatus.OK
        service.activate("PLAN_123")
    }

    @Test
    fun `deactivate should PUT without a response body`() {
        mockFactory.nextBody = ByteArray(0)
        mockFactory.nextStatus = HttpStatus.OK
        service.deactivate("PLAN_123")
    }
}
