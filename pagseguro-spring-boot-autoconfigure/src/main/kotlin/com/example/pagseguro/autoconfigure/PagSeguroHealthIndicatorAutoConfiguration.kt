package com.example.pagseguro.autoconfigure

import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestClient

@AutoConfiguration(after = [PagSeguroAutoConfiguration::class])
@ConditionalOnClass(HealthIndicator::class)
@ConditionalOnProperty(name = ["pagseguro.health-indicator-enabled"], havingValue = "true")
@ConditionalOnBean(name = ["pagSeguroRestClient"])
class PagSeguroHealthIndicatorAutoConfiguration {

    @Bean("pagSeguroHealthIndicator")
    fun pagSeguroHealthIndicator(
        @org.springframework.beans.factory.annotation.Qualifier("pagSeguroRestClient")
        restClient: RestClient
    ): HealthIndicator = PagSeguroHealthIndicator(restClient)
}

class PagSeguroHealthIndicator(private val restClient: RestClient) : AbstractHealthIndicator() {

    override fun doHealthCheck(builder: Health.Builder) {
        try {
            restClient.get()
                .uri("/plans?limit=1")
                .retrieve()
                .toBodilessEntity()
            builder.up()
        } catch (e: com.example.pagseguro.exception.PagSeguroException.Unauthorized) {
            builder.outOfService().withDetail("reason", "unauthorized")
        } catch (e: Exception) {
            builder.down(e)
        }
    }
}
