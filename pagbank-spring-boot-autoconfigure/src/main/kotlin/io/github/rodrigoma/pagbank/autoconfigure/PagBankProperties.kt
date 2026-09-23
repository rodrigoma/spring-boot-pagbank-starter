package io.github.rodrigoma.pagbank.autoconfigure

import io.github.rodrigoma.pagbank.autoconfigure.PagBankEnvironment.SANDBOX
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI
import java.time.Duration

enum class PagBankEnvironment {
    SANDBOX,
    PRODUCTION,
    ;

    fun baseUrl(): String =
        when (this) {
            SANDBOX -> "https://sandbox.api.assinaturas.pagseguro.com"
            PRODUCTION -> "https://api.assinaturas.pagseguro.com"
        }
}

@ConfigurationProperties(prefix = "pagbank")
data class PagBankProperties(
    val token: String = "",
    val environment: PagBankEnvironment = SANDBOX,
    /** Absolute URL that overrides [environment]'s base URL — e.g. a local stub when testing. */
    val baseUrl: String? = null,
    val healthIndicatorEnabled: Boolean = false,
    val logRequests: Boolean = false,
    /**
     * How long to wait for the TCP connection to PagBank. `0` disables the timeout (waits forever).
     *
     * A request that times out while connecting never reached PagBank — see `PagBankException.Timeout`.
     */
    val connectTimeout: Duration = Duration.ofSeconds(DEFAULT_CONNECT_TIMEOUT_SECONDS),
    /**
     * How long to wait for PagBank's response once connected. `0` disables the timeout (waits forever).
     *
     * Keep it **below** the timeout of whatever sits in front of your application (Heroku's router cuts at
     * 30s, nginx defaults to 60s): the starter should fail first, with a typed exception, instead of the
     * proxy returning an opaque 5xx. The Subscriptions API answers in under 2s in normal operation.
     */
    val readTimeout: Duration = Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS),
    val webhook: Webhook = Webhook(),
) : InitializingBean {
    /**
     * Webhook options.
     *
     * @property verifySignature When `true`, `PagBankWebhookParser.parseVerified` requires a valid
     * `x-authenticity-token` header (`hex(SHA-256(token + "-" + rawBody))`). Defaults to `false` because the
     * PagBank Subscriptions API is not documented to send that header; enable it once you confirm it arrives.
     */
    data class Webhook(
        val verifySignature: Boolean = false,
    )

    /** The base URL the client actually uses: [baseUrl] when set, otherwise the one implied by [environment]. */
    fun resolvedBaseUrl(): String = baseUrl ?: environment.baseUrl()

    private companion object {
        const val DEFAULT_CONNECT_TIMEOUT_SECONDS = 5L
        const val DEFAULT_READ_TIMEOUT_SECONDS = 20L
    }

    /** Never prints the token — property objects end up in logs and exception messages more often than expected. */
    override fun toString(): String =
        "PagBankProperties(token=<hidden>, environment=$environment, baseUrl=$baseUrl, " +
            "healthIndicatorEnabled=$healthIndicatorEnabled, logRequests=$logRequests, " +
            "connectTimeout=$connectTimeout, readTimeout=$readTimeout, webhook=$webhook)"

    override fun afterPropertiesSet() {
        require(token.isNotBlank()) {
            "pagbank.token must be configured — set it as an environment variable or in application.properties/.yml"
        }
        require(!connectTimeout.isNegative) { "pagbank.connect-timeout must not be negative, got '$connectTimeout'" }
        require(!readTimeout.isNegative) { "pagbank.read-timeout must not be negative, got '$readTimeout'" }
        baseUrl?.let {
            require(runCatching { URI(it).isAbsolute }.getOrDefault(false)) {
                "pagbank.base-url must be an absolute URL (e.g. http://localhost:8080), got '$it'"
            }
        }
    }
}
