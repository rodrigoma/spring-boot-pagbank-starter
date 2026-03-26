package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.coupon.*
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

class PagBankCouponServiceTest {

    private lateinit var service: PagBankCouponService
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
        service = PagBankCouponService(restClient)
    }

    private fun couponMap(id: String = "CPN_123") = mapOf(
        "id" to id,
        "code" to "SAVE10",
        "discount_type" to "PERCENT",
        "discount_value" to 10
    )

    @Test
    fun `create should POST and return CouponResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(couponMap())
        val response = service.create(
            CreateCouponRequest(
                code = "SAVE10",
                discountType = DiscountType.PERCENT,
                discountValue = 10
            )
        )
        assertThat(response.id).isEqualTo("CPN_123")
        assertThat(response.code).isEqualTo("SAVE10")
        assertThat(response.discountType).isEqualTo(DiscountType.PERCENT)
        assertThat(response.discountValue).isEqualTo(10)
    }

    @Test
    fun `get should return CouponResponse by id`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(couponMap("CPN_456"))
        val response = service.get("CPN_456")
        assertThat(response.id).isEqualTo("CPN_456")
    }

    @Test
    fun `delete should DELETE without a response body`() {
        mockFactory.nextBody = ByteArray(0)
        mockFactory.nextStatus = HttpStatus.NO_CONTENT
        // No exception = success
        service.delete("CPN_123")
    }

    @Test
    fun `applyToSubscription should POST without a response body`() {
        mockFactory.nextBody = ByteArray(0)
        mockFactory.nextStatus = HttpStatus.NO_CONTENT
        service.applyToSubscription("SUB_001", "CPN_123")
    }
}
