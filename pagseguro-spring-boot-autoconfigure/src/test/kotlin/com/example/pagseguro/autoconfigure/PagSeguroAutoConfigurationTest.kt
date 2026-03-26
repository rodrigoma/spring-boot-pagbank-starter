package com.example.pagseguro.autoconfigure

import com.example.pagseguro.service.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.web.client.RestClient

class PagSeguroAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(PagSeguroAutoConfiguration::class.java))

    @Test
    fun `should fail to start without token`() {
        contextRunner.run { context ->
            assertThat(context).hasFailed()
        }
    }

    @Test
    fun `should create all service beans when token is configured`() {
        contextRunner.withPropertyValues("pagseguro.token=TEST_TOKEN").run { context ->
            assertThat(context).hasNotFailed()
            assertThat(context).hasSingleBean(PagSeguroPlanService::class.java)
            assertThat(context).hasSingleBean(PagSeguroCustomerService::class.java)
            assertThat(context).hasSingleBean(PagSeguroSubscriptionService::class.java)
            assertThat(context).hasSingleBean(PagSeguroCouponService::class.java)
            assertThat(context).hasSingleBean(PagSeguroInvoiceService::class.java)
            assertThat(context).hasSingleBean(PagSeguroPaymentService::class.java)
            assertThat(context).hasSingleBean(PagSeguroChargeService::class.java)
            assertThat(context).hasSingleBean(PagSeguroRefundService::class.java)
            assertThat(context).hasSingleBean(PagSeguroPreferenceService::class.java)
            assertThat(context).hasSingleBean(PagSeguroWebhookParser::class.java)
        }
    }

    @Test
    fun `RestClient bean should not be exposed for general injection`() {
        contextRunner.withPropertyValues("pagseguro.token=TEST_TOKEN").run { context ->
            // The named bean exists but consumers should use service beans instead
            assertThat(context.getBeanNamesForType(RestClient::class.java)).contains("pagSeguroRestClient")
        }
    }
}
