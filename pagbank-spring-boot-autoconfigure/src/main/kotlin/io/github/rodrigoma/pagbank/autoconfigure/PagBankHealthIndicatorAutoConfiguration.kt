package io.github.rodrigoma.pagbank.autoconfigure

import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestClient

@AutoConfiguration(after = [PagBankAutoConfiguration::class])
@ConditionalOnClass(HealthIndicator::class)
@ConditionalOnProperty(name = ["pagbank.health-indicator-enabled"], havingValue = "true")
@ConditionalOnBean(name = ["pagBankRestClient"])
class PagBankHealthIndicatorAutoConfiguration {
    @Bean("pagBankHealthIndicator")
    fun pagBankHealthIndicator(
        @org.springframework.beans.factory.annotation.Qualifier("pagBankRestClient")
        restClient: RestClient,
    ): HealthIndicator = PagBankHealthIndicator(restClient)
}

class PagBankHealthIndicator(
    private val restClient: RestClient,
) : AbstractHealthIndicator() {
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override fun doHealthCheck(builder: Health.Builder) {
        try {
            restClient
                .get()
                .uri("/plans?limit=1")
                .retrieve()
                .toBodilessEntity()
            builder.up()
        } catch (e: io.github.rodrigoma.pagbank.exception.PagBankException.Unauthorized) {
            builder.outOfService().withDetail("reason", "unauthorized")
        } catch (e: Exception) {
            builder.down(e)
        }
    }
}
