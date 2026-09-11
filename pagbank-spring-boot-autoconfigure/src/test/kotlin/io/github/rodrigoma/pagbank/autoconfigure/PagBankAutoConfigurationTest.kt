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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

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
}
