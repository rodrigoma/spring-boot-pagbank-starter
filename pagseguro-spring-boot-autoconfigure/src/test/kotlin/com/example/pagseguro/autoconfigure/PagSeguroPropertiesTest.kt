package com.example.pagseguro.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class PagSeguroPropertiesTest {

    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(PropertiesTestConfig::class.java)

    @Test
    fun `should bind token and environment`() {
        contextRunner
            .withPropertyValues("pagseguro.token=MY_TOKEN", "pagseguro.environment=production")
            .run { context ->
                assertThat(context).hasNotFailed()
                val props = context.getBean(PagSeguroProperties::class.java)
                assertThat(props.token).isEqualTo("MY_TOKEN")
                assertThat(props.environment).isEqualTo(PagSeguroEnvironment.PRODUCTION)
            }
    }

    @Test
    fun `should default environment to sandbox`() {
        contextRunner
            .withPropertyValues("pagseguro.token=MY_TOKEN")
            .run { context ->
                val props = context.getBean(PagSeguroProperties::class.java)
                assertThat(props.environment).isEqualTo(PagSeguroEnvironment.SANDBOX)
            }
    }

    @Test
    fun `should fail when token is blank`() {
        contextRunner
            .withPropertyValues("pagseguro.token=")
            .run { context ->
                assertThat(context).hasFailed()
                assertThat(context.startupFailure).hasMessageContaining("pagseguro.token")
            }
    }

    @Test
    fun `should fail when token is absent`() {
        contextRunner.run { context ->
            assertThat(context).hasFailed()
        }
    }

    @EnableConfigurationProperties(PagSeguroProperties::class)
    class PropertiesTestConfig
}
