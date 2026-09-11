package io.github.rodrigoma.pagbank.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class PagBankPropertiesTest {
    private val contextRunner =
        ApplicationContextRunner()
            .withUserConfiguration(PropertiesTestConfig::class.java)

    @Test
    fun `should bind token and environment`() {
        contextRunner
            .withPropertyValues("pagbank.token=MY_TOKEN", "pagbank.environment=production")
            .run { context ->
                assertThat(context).hasNotFailed()
                val props = context.getBean(PagBankProperties::class.java)
                assertThat(props.token).isEqualTo("MY_TOKEN")
                assertThat(props.environment).isEqualTo(PagBankEnvironment.PRODUCTION)
            }
    }

    @Test
    fun `should default environment to sandbox`() {
        contextRunner
            .withPropertyValues("pagbank.token=MY_TOKEN")
            .run { context ->
                val props = context.getBean(PagBankProperties::class.java)
                assertThat(props.environment).isEqualTo(PagBankEnvironment.SANDBOX)
            }
    }

    @Test
    fun `should fail when token is blank`() {
        contextRunner
            .withPropertyValues("pagbank.token=")
            .run { context ->
                assertThat(context).hasFailed()
                assertThat(context.startupFailure).hasMessageContaining("pagbank.token")
            }
    }

    @Test
    fun `should fail when token is absent`() {
        contextRunner.run { context ->
            assertThat(context).hasFailed()
        }
    }

    @Test
    fun `should resolve base url from environment when not set`() {
        contextRunner
            .withPropertyValues("pagbank.token=MY_TOKEN", "pagbank.environment=production")
            .run { context ->
                val props = context.getBean(PagBankProperties::class.java)
                assertThat(props.baseUrl).isNull()
                assertThat(props.resolvedBaseUrl()).isEqualTo("https://api.assinaturas.pagseguro.com")
            }
    }

    @Test
    fun `explicit base url should win over environment`() {
        contextRunner
            .withPropertyValues("pagbank.token=MY_TOKEN", "pagbank.base-url=http://localhost:1234")
            .run { context ->
                val props = context.getBean(PagBankProperties::class.java)
                assertThat(props.resolvedBaseUrl()).isEqualTo("http://localhost:1234")
            }
    }

    @Test
    fun `should fail when base url is not absolute`() {
        contextRunner
            .withPropertyValues("pagbank.token=MY_TOKEN", "pagbank.base-url=/relative")
            .run { context ->
                assertThat(context).hasFailed()
                assertThat(context.startupFailure).hasMessageContaining("pagbank.base-url")
            }
    }

    @EnableConfigurationProperties(PagBankProperties::class)
    class PropertiesTestConfig
}
