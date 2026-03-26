package com.example.pagseguro.autoconfigure

import com.example.pagseguro.http.PagSeguroErrorHandler
import com.example.pagseguro.service.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestClient

@AutoConfiguration
@EnableConfigurationProperties(PagSeguroProperties::class)
class PagSeguroAutoConfiguration(private val properties: PagSeguroProperties) {

    @Bean(name = ["pagSeguroObjectMapper"])
    fun pagSeguroObjectMapper(): ObjectMapper =
        jacksonObjectMapper().apply {
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        }

    @Bean(name = ["pagSeguroRestClient"])
    fun pagSeguroRestClient(
        @Qualifier("pagSeguroObjectMapper")
        objectMapper: ObjectMapper
    ): RestClient {
        val errorHandler = PagSeguroErrorHandler(objectMapper)
        return RestClient.builder()
            .baseUrl(properties.environment.baseUrl())
            .defaultHeader("Authorization", "Bearer ${properties.token}")
            .messageConverters { converters ->
                converters.removeIf { it is MappingJackson2HttpMessageConverter }
                converters.add(0, MappingJackson2HttpMessageConverter(objectMapper))
            }
            .defaultStatusHandler({ it.isError }) { _, response -> errorHandler.handle(response) }
            .build()
    }

    @Bean fun pagSeguroPlanService(@Qualifier("pagSeguroRestClient") rc: RestClient) = PagSeguroPlanService(rc)
    @Bean fun pagSeguroCustomerService(@Qualifier("pagSeguroRestClient") rc: RestClient) = PagSeguroCustomerService(rc)
    @Bean fun pagSeguroSubscriptionService(@Qualifier("pagSeguroRestClient") rc: RestClient) = PagSeguroSubscriptionService(rc)
    @Bean fun pagSeguroCouponService(@Qualifier("pagSeguroRestClient") rc: RestClient) = PagSeguroCouponService(rc)
    @Bean fun pagSeguroInvoiceService(@Qualifier("pagSeguroRestClient") rc: RestClient) = PagSeguroInvoiceService(rc)
    @Bean fun pagSeguroPaymentService(@Qualifier("pagSeguroRestClient") rc: RestClient) = PagSeguroPaymentService(rc)
    @Bean fun pagSeguroChargeService(@Qualifier("pagSeguroRestClient") rc: RestClient) = PagSeguroChargeService(rc)
    @Bean fun pagSeguroRefundService(@Qualifier("pagSeguroRestClient") rc: RestClient) = PagSeguroRefundService(rc)
    @Bean fun pagSeguroPreferenceService(@Qualifier("pagSeguroRestClient") rc: RestClient) = PagSeguroPreferenceService(rc)
    @Bean fun pagSeguroWebhookParser() = PagSeguroWebhookParser(properties)
}
