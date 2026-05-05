package io.github.rodrigoma.pagbank.autoconfigure

import io.github.rodrigoma.pagbank.autoconfigure.PagBankEnvironment.SANDBOX
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.context.properties.ConfigurationProperties

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
    val healthIndicatorEnabled: Boolean = false,
    val logRequests: Boolean = false,
) : InitializingBean {
    override fun afterPropertiesSet() {
        require(token.isNotBlank()) {
            "pagbank.token must be configured — set it in your application.yml"
        }
    }
}
