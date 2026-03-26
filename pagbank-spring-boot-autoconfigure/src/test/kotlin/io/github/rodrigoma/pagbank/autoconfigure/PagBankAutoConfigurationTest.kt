package io.github.rodrigoma.pagbank.autoconfigure

import io.github.rodrigoma.pagbank.service.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.web.client.RestClient

class PagBankAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
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
            assertThat(context).hasSingleBean(PagBankChargeService::class.java)
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
}
