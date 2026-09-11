package io.github.rodrigoma.pagbank.autoconfigure

import io.github.rodrigoma.pagbank.autoconfigure.PagBankEnvironment.SANDBOX
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI

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

    override fun afterPropertiesSet() {
        require(token.isNotBlank()) {
            "pagbank.token must be configured — set it as an environment variable or in application.properties/.yml"
        }
        baseUrl?.let {
            require(runCatching { URI(it).isAbsolute }.getOrDefault(false)) {
                "pagbank.base-url must be an absolute URL (e.g. http://localhost:8080), got '$it'"
            }
        }
    }
}
