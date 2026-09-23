package io.github.rodrigoma.pagbank.autoconfigure

import com.sun.net.httpserver.HttpServer
import io.github.rodrigoma.pagbank.exception.PagBankException
import io.github.rodrigoma.pagbank.exception.TimeoutPhase
import io.github.rodrigoma.pagbank.service.PagBankPlanService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import java.net.InetSocketAddress
import java.time.Duration
import kotlin.system.measureTimeMillis

/**
 * Exercises the real `RestClient` the auto-configuration builds against a local HTTP server that
 * either stalls or answers, so the timeouts and their mapping to [PagBankException.Timeout] are
 * verified end to end rather than by inspecting the builder.
 */
class PagBankTimeoutTest {
    private lateinit var server: HttpServer
    private val slowResponseMillis = 3_000L

    private companion object {
        const val EMPTY_PLAN_LIST = """{"result_set":{"total":0,"limit":100,"offset":0},"plans":[]}"""
    }

    @BeforeEach
    fun startServer() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/plans") { exchange ->
            Thread.sleep(slowResponseMillis)
            val body = EMPTY_PLAN_LIST.toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.createContext("/fast") { exchange ->
            val body = EMPTY_PLAN_LIST.toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.createContext("/boom") { exchange ->
            exchange.sendResponseHeaders(500, -1)
            exchange.close()
        }
        server.start()
    }

    @AfterEach
    fun stopServer() {
        server.stop(0)
    }

    private fun runner(vararg properties: String) =
        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PagBankAutoConfiguration::class.java))
            .withPropertyValues(
                "pagbank.token=TEST_TOKEN",
                "pagbank.base-url=http://127.0.0.1:${server.address.port}",
                *properties,
            )

    @Test
    fun `a slow response fails with Timeout before the read timeout is much exceeded`() {
        runner("pagbank.read-timeout=300ms").run { context ->
            val service = context.getBean(PagBankPlanService::class.java)

            lateinit var thrown: Throwable
            val elapsed = measureTimeMillis { thrown = catchThrowable { service.list() } }

            assertThat(thrown).isInstanceOf(PagBankException.Timeout::class.java)
            val timeout = thrown as PagBankException.Timeout
            assertThat(timeout.phase).isEqualTo(TimeoutPhase.READ)
            assertThat(timeout.method).isEqualTo("GET")
            assertThat(timeout.path).isEqualTo("/plans")
            assertThat(timeout.message)
                .contains("outcome is unknown")
                .contains("GET /plans")
            assertThat(timeout.cause).isNotNull()
            // Proves the timeout actually fired instead of the call completing normally.
            assertThat(elapsed).isLessThan(slowResponseMillis)
        }
    }

    @Test
    fun `the configured read timeout is respected`() {
        runner("pagbank.read-timeout=5s").run { context ->
            val service = context.getBean(PagBankPlanService::class.java)
            val elapsed = measureTimeMillis { service.list() }
            assertThat(elapsed).isGreaterThanOrEqualTo(slowResponseMillis)
        }
    }

    @Test
    fun `defaults apply when no timeout property is set`() {
        runner().run { context ->
            val properties = context.getBean(PagBankProperties::class.java)
            assertThat(properties.connectTimeout).isEqualTo(Duration.ofSeconds(5))
            assertThat(properties.readTimeout).isEqualTo(Duration.ofSeconds(20))
            // 3s < 20s, so the default read timeout must let the slow endpoint through.
            context.getBean(PagBankPlanService::class.java).list()
        }
    }

    @Test
    fun `a real 500 still becomes ServerError, not Timeout`() {
        runner("pagbank.read-timeout=5s", "pagbank.base-url=http://127.0.0.1:${server.address.port}/boom")
            .run { context ->
                assertThatThrownBy { context.getBean(PagBankPlanService::class.java).list() }
                    .isInstanceOf(PagBankException.ServerError::class.java)
            }
    }

    @Test
    fun `a RestClientCustomizer can still override the request factory`() {
        runner("pagbank.read-timeout=300ms")
            .withUserConfiguration(SlowFactoryConfig::class.java)
            .run { context ->
                // The customizer replaces the factory with one that tolerates the slow endpoint.
                context.getBean(PagBankPlanService::class.java).list()
            }
    }

    private fun catchThrowable(action: () -> Unit): Throwable =
        try {
            action()
            error("expected the call to fail")
        } catch (e: Throwable) {
            e
        }

    @Configuration(proxyBeanMethods = false)
    class SlowFactoryConfig {
        @Bean
        fun patientFactory() =
            PagBankRestClientCustomizer { builder ->
                builder.requestFactory(
                    SimpleClientHttpRequestFactory().apply {
                        setConnectTimeout(Duration.ofSeconds(5))
                        setReadTimeout(Duration.ofSeconds(10))
                    },
                )
            }
    }
}
