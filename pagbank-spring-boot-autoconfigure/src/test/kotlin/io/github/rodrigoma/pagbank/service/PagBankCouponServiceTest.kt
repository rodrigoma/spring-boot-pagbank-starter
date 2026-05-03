package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.coupon.CouponStatus
import io.github.rodrigoma.pagbank.model.coupon.CreateCouponRequest
import io.github.rodrigoma.pagbank.model.coupon.Discount
import io.github.rodrigoma.pagbank.model.coupon.DiscountType
import io.github.rodrigoma.pagbank.model.coupon.Duration
import io.github.rodrigoma.pagbank.model.coupon.DurationType
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

class PagBankCouponServiceTest {
    private lateinit var service: PagBankCouponService
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
        service = PagBankCouponService(restClient)
    }

    private fun couponMap(id: String = "COUP_123") =
        mapOf(
            "id" to id,
            "name" to "SAVE10",
            "discount" to mapOf("value" to 10, "type" to "PERCENT"),
            "duration" to mapOf("type" to "FOREVER"),
            "status" to "ACTIVE",
        )

    private fun listResponseMap(
        coupons: List<Map<String, Any>> = listOf(couponMap()),
        total: Int = coupons.size,
        statusFilter: List<String> = emptyList(),
    ) = mapOf(
        "result_set" to mapOf("total" to total, "status" to statusFilter),
        "coupons" to coupons,
    )

    @Test
    fun `create should POST and return CouponResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(couponMap())
        val response =
            service.create(
                CreateCouponRequest(
                    name = "SAVE10",
                    discount = Discount(value = 10, type = DiscountType.PERCENT),
                    duration = Duration(type = DurationType.FOREVER),
                ),
            )
        assertThat(response.id).isEqualTo("COUP_123")
        assertThat(response.name).isEqualTo("SAVE10")
        assertThat(response.discount.type).isEqualTo(DiscountType.PERCENT)
        assertThat(response.discount.value).isEqualTo(10)
    }

    @Test
    fun `get should return CouponResponse by id`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(couponMap("COUP_456"))
        val response = service.get("COUP_456")
        assertThat(response.id).isEqualTo("COUP_456")
    }

    @Test
    fun `list should GET and return CouponListResponse with result_set`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(listResponseMap(total = 5))
        val response = service.list()
        assertThat(response.coupons).hasSize(1)
        assertThat(response.coupons[0].id).isEqualTo("COUP_123")
        assertThat(response.resultSet.total).isEqualTo(5)
        assertThat(response.resultSet.status).isEmpty()
    }

    @Test
    fun `list with params should include query parameters`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(listResponseMap())
        val response = service.list(offset = 0, limit = 10, referenceId = "ref-123")
        assertThat(response.coupons).hasSize(1)
        assertThat(response.resultSet.total).isEqualTo(1)
        val query = mockFactory.lastUri!!.query
        assertThat(query).contains("offset=0").contains("limit=10").contains("reference_id=ref-123")
    }

    @Test
    fun `list with status filter should return result_set with status`() {
        mockFactory.nextBody =
            mapper.writeValueAsBytes(listResponseMap(statusFilter = listOf("ACTIVE")))
        val response = service.list(status = CouponStatus.ACTIVE)
        assertThat(response.coupons).hasSize(1)
        assertThat(response.resultSet.status).containsExactly("ACTIVE")
    }

    @Test
    fun `inactivate should PUT without a response body`() {
        mockFactory.nextBody = ByteArray(0)
        mockFactory.nextStatus = HttpStatus.OK
        service.inactivate("COUP_123")
    }

    @Test
    fun `activate should PUT without a response body`() {
        mockFactory.nextBody = ByteArray(0)
        mockFactory.nextStatus = HttpStatus.OK
        service.activate("COUP_123")
    }
}
