package com.example.pagseguro.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.FilteredClassLoader
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class PagSeguroHealthIndicatorAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                PagSeguroAutoConfiguration::class.java,
                PagSeguroHealthIndicatorAutoConfiguration::class.java
            )
        )
        .withPropertyValues("pagseguro.token=TEST_TOKEN")

    @Test
    fun `health indicator should not be created when disabled`() {
        contextRunner
            .withPropertyValues("pagseguro.health-indicator-enabled=false")
            .run { context ->
                assertThat(context).doesNotHaveBean(HealthIndicator::class.java)
            }
    }

    @Test
    fun `health indicator should not be created when actuator is absent`() {
        contextRunner
            .withPropertyValues("pagseguro.health-indicator-enabled=true")
            .withClassLoader(FilteredClassLoader(HealthIndicator::class.java))
            .run { context ->
                assertThat(context).doesNotHaveBean("pagSeguroHealthIndicator")
            }
    }

    @Test
    fun `health indicator should be created when enabled and actuator is present`() {
        contextRunner
            .withPropertyValues("pagseguro.health-indicator-enabled=true")
            .run { context ->
                assertThat(context).hasSingleBean(HealthIndicator::class.java)
            }
    }
}
