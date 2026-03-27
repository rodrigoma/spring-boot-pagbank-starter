package io.github.rodrigoma.pagbank.autoconfigure

import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.context.properties.ConfigurationProperties

enum class PagBankEnvironment {
    SANDBOX,
    PRODUCTION,
    ;

    fun baseUrl(): String =
        when (this) {
            SANDBOX -> "https://sandbox.assinaturas.pagseguro.uol.com.br"
            PRODUCTION -> "https://assinaturas.pagseguro.uol.com.br"
        }
}

@ConfigurationProperties(prefix = "pagbank")
data class PagBankProperties(
    val token: String = "",
    val environment: PagBankEnvironment = PagBankEnvironment.SANDBOX,
    val webhookSecret: String? = null,
    val healthIndicatorEnabled: Boolean = false,
    val logRequests: Boolean = false,
) : InitializingBean {
    override fun afterPropertiesSet() {
        require(token.isNotBlank()) {
            "pagbank.token must be configured — set it in your application.yml"
        }
    }
}
