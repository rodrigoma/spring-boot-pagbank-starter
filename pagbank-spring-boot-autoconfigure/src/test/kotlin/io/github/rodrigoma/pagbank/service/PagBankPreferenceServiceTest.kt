package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.preference.NotificationChannel
import io.github.rodrigoma.pagbank.model.preference.NotificationEmail
import io.github.rodrigoma.pagbank.model.preference.NotificationPreferences
import io.github.rodrigoma.pagbank.model.preference.RetryPreferences
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

class PagBankPreferenceServiceTest {
    private lateinit var service: PagBankPreferenceService
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

            override fun createRequest(
                uri: java.net.URI,
                httpMethod: HttpMethod,
            ): org.springframework.http.client.ClientHttpRequest {
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
        service = PagBankPreferenceService(restClient)
    }

    private fun notificationsMap() =
        mapOf(
            "email" to
                mapOf(
                    "merchant" to mapOf("enabled" to true),
                    "customer" to mapOf("enabled" to true),
                ),
        )

    private fun retriesMap() =
        mapOf(
            "first_try" to 1,
            "second_try" to 3,
            "third_try" to 5,
            "finally" to "CANCEL",
        )

    private fun publicKeyMap() =
        mapOf(
            "public_key" to "MIIBIjAN...",
            "links" to emptyList<Any>(),
        )

    @Test
    fun `getNotifications should return NotificationPreferences`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(notificationsMap())
        val response = service.getNotifications()
        assertThat(response.email.merchant.enabled).isTrue()
        assertThat(response.email.customer.enabled).isTrue()
    }

    @Test
    fun `updateNotifications should PUT and return NotificationPreferences`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(notificationsMap())
        val response =
            service.updateNotifications(
                NotificationPreferences(
                    email =
                        NotificationEmail(
                            merchant = NotificationChannel(enabled = true),
                            customer = NotificationChannel(enabled = false),
                        ),
                ),
            )
        assertThat(response.email.merchant.enabled).isTrue()
    }

    @Test
    fun `getRetries should return RetryPreferences`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(retriesMap())
        val response = service.getRetries()
        assertThat(response.firstTry).isEqualTo(1)
        assertThat(response.secondTry).isEqualTo(3)
        assertThat(response.thirdTry).isEqualTo(5)
        assertThat(response.finalAction).isEqualTo(io.github.rodrigoma.pagbank.model.preference.FinalAction.CANCEL)
    }

    @Test
    fun `updateRetries should PUT and return RetryPreferences`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(retriesMap())
        val response =
            service.updateRetries(
                RetryPreferences(
                    firstTry = 1,
                    secondTry = 3,
                    thirdTry = 5,
                    finalAction = io.github.rodrigoma.pagbank.model.preference.FinalAction.CANCEL,
                ),
            )
        assertThat(response.firstTry).isEqualTo(1)
    }

    @Test
    fun `getPublicKey should return PublicKeyResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(publicKeyMap())
        val response = service.getPublicKey()
        assertThat(response.publicKey).isEqualTo("MIIBIjAN...")
    }

    @Test
    fun `rotatePublicKey should PUT without body and return PublicKeyResponse`() {
        mockFactory.nextBody = mapper.writeValueAsBytes(publicKeyMap())
        val response = service.rotatePublicKey()
        assertThat(response.publicKey).isEqualTo("MIIBIjAN...")
        assertThat(mockFactory.lastRequest!!.bodyAsString).isEmpty()
    }
}
