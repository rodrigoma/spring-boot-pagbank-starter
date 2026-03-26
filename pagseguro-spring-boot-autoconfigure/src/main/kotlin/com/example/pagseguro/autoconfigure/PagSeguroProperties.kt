package com.example.pagseguro.autoconfigure

import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.context.properties.ConfigurationProperties

enum class PagSeguroEnvironment {
    SANDBOX, PRODUCTION;

    fun baseUrl(): String = when (this) {
        SANDBOX -> "https://sandbox.assinaturas.pagseguro.uol.com.br"
        PRODUCTION -> "https://assinaturas.pagseguro.uol.com.br"
    }
}

@ConfigurationProperties(prefix = "pagseguro")
data class PagSeguroProperties(
    val token: String = "",
    val environment: PagSeguroEnvironment = PagSeguroEnvironment.SANDBOX,
    val webhookSecret: String? = null,
    val healthIndicatorEnabled: Boolean = false
) : InitializingBean {

    override fun afterPropertiesSet() {
        require(token.isNotBlank()) {
            "pagseguro.token must be configured — set it in your application.yml"
        }
    }
}
