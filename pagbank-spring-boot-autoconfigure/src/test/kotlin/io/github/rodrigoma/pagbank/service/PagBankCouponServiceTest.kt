package io.github.rodrigoma.pagbank.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.http.client.MockClientHttpRequest
import org.springframework.mock.http.client.MockClientHttpResponse
import org.springframework.web.client.RestClient

class PagBankCouponServiceTest {
    private lateinit var service: PagBankCouponService
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
    fun `delete should DELETE without a response body`() {
        mockFactory.nextBody = ByteArray(0)
        mockFactory.nextStatus = HttpStatus.NO_CONTENT
        // No exception = success
        service.delete("COUP_123")
    }

    @Test
    fun `applyToSubscription should POST without a response body`() {
        mockFactory.nextBody = ByteArray(0)
        mockFactory.nextStatus = HttpStatus.NO_CONTENT
        service.applyToSubscription("SUB_001", "COUP_123")
    }
}
