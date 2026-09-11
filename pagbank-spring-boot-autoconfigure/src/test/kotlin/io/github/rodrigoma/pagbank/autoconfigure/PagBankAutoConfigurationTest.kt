package io.github.rodrigoma.pagbank.autoconfigure

import io.github.rodrigoma.pagbank.service.PagBankCouponService
import io.github.rodrigoma.pagbank.service.PagBankCustomerService
import io.github.rodrigoma.pagbank.service.PagBankInvoiceService
import io.github.rodrigoma.pagbank.service.PagBankPaymentService
import io.github.rodrigoma.pagbank.service.PagBankPlanService
import io.github.rodrigoma.pagbank.service.PagBankPreferenceService
import io.github.rodrigoma.pagbank.service.PagBankRefundService
import io.github.rodrigoma.pagbank.service.PagBankSubscriptionService
import io.github.rodrigoma.pagbank.service.PagBankWebhookParser
import io.github.rodrigoma.pagbank.service.PagBankWebhookVerifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

@ExtendWith(OutputCaptureExtension::class)
class PagBankAutoConfigurationTest {
    private val contextRunner =
        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PagBankAutoConfiguration::class.java))

    @Test
    fun `should fail to start without token`() {
        contextRunner.run { context ->
            assertThat(context).hasFailed()
        }
    }

    @Test
    fun `should create all service beans when token is configured`() {
        contextRunner.withPropertyValues("pagbank.token=TEST_TOKEN").run { context ->
            assertThat(context).hasNotFailed()
            assertThat(context).hasSingleBean(PagBankPlanService::class.java)
            assertThat(context).hasSingleBean(PagBankCustomerService::class.java)
            assertThat(context).hasSingleBean(PagBankSubscriptionService::class.java)
            assertThat(context).hasSingleBean(PagBankCouponService::class.java)
            assertThat(context).hasSingleBean(PagBankInvoiceService::class.java)
            assertThat(context).hasSingleBean(PagBankPaymentService::class.java)
            assertThat(context).hasSingleBean(PagBankRefundService::class.java)
            assertThat(context).hasSingleBean(PagBankPreferenceService::class.java)
            assertThat(context).hasSingleBean(PagBankWebhookParser::class.java)
        }
    }

    @Test
    fun `RestClient bean should not be exposed for general injection`() {
        contextRunner.withPropertyValues("pagbank.token=TEST_TOKEN").run { context ->
            // The named bean exists but consumers should use service beans instead
            assertThat(context.getBeanNamesForType(RestClient::class.java)).contains("pagBankRestClient")
        }
    }

    @Test
    fun `RestClient should NOT have logging interceptor when logRequests is false`() {
        contextRunner
            .withPropertyValues("pagbank.token=TEST_TOKEN", "pagbank.log-requests=false")
            .run { context ->
                assertThat(context).hasNotFailed()
                assertThat(context.getBeanNamesForType(RestClient::class.java)).contains("pagBankRestClient")
            }
    }

    @Test
    fun `RestClient should have logging interceptor when logRequests is true`() {
        contextRunner
            .withPropertyValues("pagbank.token=TEST_TOKEN", "pagbank.log-requests=true")
            .run { context ->
                assertThat(context).hasNotFailed()
                assertThat(context.getBeanNamesForType(RestClient::class.java)).contains("pagBankRestClient")
            }
    }

    @Test
    fun `RestClient should omit null fields when serializing requests`() {
        contextRunner.withPropertyValues("pagbank.token=TEST_TOKEN").run { context ->
            val builder = context.getBean("pagBankRestClient", RestClient::class.java).mutate()
            val server = MockRestServiceServer.bindTo(builder).build()
            server
                .expect(requestTo("https://sandbox.api.assinaturas.pagseguro.com/plans"))
                .andExpect(content().json("""{"name":"test"}""", true))
                .andRespond(withSuccess())

            builder
                .build()
                .post()
                .uri("/plans")
                .body(mapOf("name" to "test", "absent" to null))
                .retrieve()
                .toBodilessEntity()
            server.verify()
        }
    }

    @Test
    fun `should register webhook verifier and warn when signature verification is disabled`(output: CapturedOutput) {
        contextRunner.withPropertyValues("pagbank.token=TEST_TOKEN").run { context ->
            assertThat(context).hasSingleBean(PagBankWebhookVerifier::class.java)
            assertThat(output).contains("pagbank.webhook.verify-signature")
        }
    }

    @Test
    fun `should not warn when signature verification is enabled`(output: CapturedOutput) {
        contextRunner
            .withPropertyValues("pagbank.token=TEST_TOKEN", "pagbank.webhook.verify-signature=true")
            .run { context ->
                assertThat(context).hasNotFailed()
                assertThat(output).doesNotContain("Webhook signature verification is disabled")
            }
    }

    @Test
    fun `RestClient should target pagbank base-url when configured`() {
        contextRunner
            .withPropertyValues("pagbank.token=TEST_TOKEN", "pagbank.base-url=http://localhost:1234")
            .run { context ->
                val builder = context.getBean("pagBankRestClient", RestClient::class.java).mutate()
                val server = MockRestServiceServer.bindTo(builder).build()
                server.expect(requestTo("http://localhost:1234/plans")).andRespond(withSuccess())

                builder
                    .build()
                    .get()
                    .uri("/plans")
                    .retrieve()
                    .toBodilessEntity()
                server.verify()
            }
    }

    @Test
    fun `RestClient should apply PagBankRestClientCustomizer beans`() {
        contextRunner
            .withPropertyValues("pagbank.token=TEST_TOKEN")
            .withUserConfiguration(CustomizerConfig::class.java)
            .run { context ->
                val builder = context.getBean("pagBankRestClient", RestClient::class.java).mutate()
                val server = MockRestServiceServer.bindTo(builder).build()
                server
                    .expect(requestTo("https://sandbox.api.assinaturas.pagseguro.com/plans"))
                    .andExpect(header("X-Custom", "yes"))
                    .andExpect(header("Authorization", "Bearer TEST_TOKEN"))
                    .andRespond(withSuccess())

                builder
                    .build()
                    .get()
                    .uri("/plans")
                    .retrieve()
                    .toBodilessEntity()
                server.verify()
            }
    }

    @Configuration(proxyBeanMethods = false)
    class CustomizerConfig {
        @Bean
        fun customHeader() = PagBankRestClientCustomizer { it.defaultHeader("X-Custom", "yes") }
    }
}
