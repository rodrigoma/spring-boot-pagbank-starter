package io.github.rodrigoma.pagbank.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.boot.test.context.FilteredClassLoader
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class PagBankHealthIndicatorAutoConfigurationTest {
    private val contextRunner =
        ApplicationContextRunner()
            .withConfiguration(
                AutoConfigurations.of(
                    PagBankAutoConfiguration::class.java,
                    PagBankHealthIndicatorAutoConfiguration::class.java,
                ),
            ).withPropertyValues("pagbank.token=TEST_TOKEN")

    @Test
    fun `health indicator should not be created when disabled`() {
        contextRunner
            .withPropertyValues("pagbank.health-indicator-enabled=false")
            .run { context ->
                assertThat(context).doesNotHaveBean(HealthIndicator::class.java)
            }
    }

    @Test
    fun `health indicator should not be created when actuator is absent`() {
        contextRunner
            .withPropertyValues("pagbank.health-indicator-enabled=true")
            .withClassLoader(FilteredClassLoader(HealthIndicator::class.java))
            .run { context ->
                assertThat(context).doesNotHaveBean("pagBankHealthIndicator")
            }
    }

    @Test
    fun `health indicator should be created when enabled and actuator is present`() {
        contextRunner
            .withPropertyValues("pagbank.health-indicator-enabled=true")
            .run { context ->
                assertThat(context).hasSingleBean(HealthIndicator::class.java)
            }
    }
}
