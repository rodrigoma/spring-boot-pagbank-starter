package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.preference.*
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

class PagBankPreferenceServiceTest {

    private lateinit var service: PagBankPreferenceService
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
        service = PagBankPreferenceService(restClient)
    }

    private fun preferenceMap(retryDays: Int = 3, email: String? = "ops@example.com") = mapOf(
        "retry_days" to retryDays,
        "notification_email" to email
    )

    @Test
    fun `get should return PreferenceResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(preferenceMap())
        val response = service.get()
        assertThat(response.retryDays).isEqualTo(3)
        assertThat(response.notificationEmail).isEqualTo("ops@example.com")
    }

    @Test
    fun `get should return PreferenceResponse with null notificationEmail`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(preferenceMap(retryDays = 5, email = null))
        val response = service.get()
        assertThat(response.retryDays).isEqualTo(5)
        assertThat(response.notificationEmail).isNull()
    }

    @Test
    fun `update should PUT and return updated PreferenceResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(preferenceMap(retryDays = 7))
        val response = service.update(UpdatePreferenceRequest(retryDays = 7))
        assertThat(response.retryDays).isEqualTo(7)
    }

    @Test
    fun `update should PUT with notificationEmail and return updated PreferenceResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(
            preferenceMap(retryDays = 3, email = "new@example.com")
        )
        val response = service.update(
            UpdatePreferenceRequest(notificationEmail = "new@example.com")
        )
        assertThat(response.notificationEmail).isEqualTo("new@example.com")
    }
}
