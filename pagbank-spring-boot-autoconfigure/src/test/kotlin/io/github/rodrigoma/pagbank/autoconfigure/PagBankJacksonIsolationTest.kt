package io.github.rodrigoma.pagbank.autoconfigure

import io.github.rodrigoma.pagbank.service.PagBankPlanService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import tools.jackson.databind.json.JsonMapper

/**
 * Guards against the starter leaking its snake_case mapper into the host application.
 * Spring Boot's [JacksonAutoConfiguration] declares its `JsonMapper` with `@ConditionalOnMissingBean`,
 * so any `JsonMapper` bean published by this starter would silently replace the application-wide one.
 */
class PagBankJacksonIsolationTest {
    private val contextRunner =
        ApplicationContextRunner()
            .withConfiguration(
                AutoConfigurations.of(JacksonAutoConfiguration::class.java, PagBankAutoConfiguration::class.java),
            ).withPropertyValues("pagbank.token=TEST_TOKEN")

    data class Sample(
        val refreshToken: String,
        val absent: String? = null,
    )

    @Test
    fun `application JsonMapper should keep Spring Boot defaults`() {
        contextRunner.run { context ->
            assertThat(context).hasNotFailed()
            assertThat(context).hasSingleBean(JsonMapper::class.java)

            val json = context.getBean(JsonMapper::class.java).writeValueAsString(Sample("abc"))
            assertThat(json).contains("\"refreshToken\"").contains("\"absent\":null")
        }
    }

    @Test
    fun `starter should not publish any JsonMapper bean of its own`() {
        contextRunner.run { context ->
            assertThat(context.getBeanNamesForType(JsonMapper::class.java)).doesNotContain("pagBankObjectMapper")
        }
    }

    @Test
    fun `PagBank RestClient should still serialize in snake_case`() {
        contextRunner.run { context ->
            val builder = context.getBean("pagBankRestClient", RestClient::class.java).mutate()
            val server = MockRestServiceServer.bindTo(builder).build()
            val restClient = builder.build()
            server
                .expect(requestTo("https://sandbox.api.assinaturas.pagseguro.com/plans"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""{"refresh_token":"abc"}"""))
                .andRespond(withSuccess("""{"id":"PLAN_1"}""", MediaType.APPLICATION_JSON))

            restClient
                .post()
                .uri("/plans")
                .body(Sample("abc"))
                .retrieve()
                .toBodilessEntity()
            server.verify()
            assertThat(context).hasSingleBean(PagBankPlanService::class.java)
        }
    }
}
