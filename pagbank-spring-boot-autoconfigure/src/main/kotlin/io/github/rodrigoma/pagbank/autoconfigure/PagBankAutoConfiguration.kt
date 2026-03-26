package io.github.rodrigoma.pagbank.autoconfigure

import io.github.rodrigoma.pagbank.http.PagBankErrorHandler
import io.github.rodrigoma.pagbank.service.*
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
@EnableConfigurationProperties(PagBankProperties::class)
class PagBankAutoConfiguration(private val properties: PagBankProperties) {

    @Bean(name = ["pagBankObjectMapper"])
    fun pagBankObjectMapper(): ObjectMapper =
        jacksonObjectMapper().apply {
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        }

    @Bean(name = ["pagBankRestClient"])
    fun pagBankRestClient(
        @Qualifier("pagBankObjectMapper")
        objectMapper: ObjectMapper
    ): RestClient {
        val errorHandler = PagBankErrorHandler(objectMapper)
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

    @Bean fun pagBankPlanService(@Qualifier("pagBankRestClient") rc: RestClient) = PagBankPlanService(rc)
    @Bean fun pagBankCustomerService(@Qualifier("pagBankRestClient") rc: RestClient) = PagBankCustomerService(rc)
    @Bean fun pagBankSubscriptionService(@Qualifier("pagBankRestClient") rc: RestClient) = PagBankSubscriptionService(rc)
    @Bean fun pagBankCouponService(@Qualifier("pagBankRestClient") rc: RestClient) = PagBankCouponService(rc)
    @Bean fun pagBankInvoiceService(@Qualifier("pagBankRestClient") rc: RestClient) = PagBankInvoiceService(rc)
    @Bean fun pagBankPaymentService(@Qualifier("pagBankRestClient") rc: RestClient) = PagBankPaymentService(rc)
    @Bean fun pagBankChargeService(@Qualifier("pagBankRestClient") rc: RestClient) = PagBankChargeService(rc)
    @Bean fun pagBankRefundService(@Qualifier("pagBankRestClient") rc: RestClient) = PagBankRefundService(rc)
    @Bean fun pagBankPreferenceService(@Qualifier("pagBankRestClient") rc: RestClient) = PagBankPreferenceService(rc)
    @Bean fun pagBankWebhookParser() = PagBankWebhookParser(properties)
}
