package io.github.rodrigoma.pagbank.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.rodrigoma.pagbank.http.PagBankErrorHandler
import io.github.rodrigoma.pagbank.http.PagBankLoggingInterceptor
import io.github.rodrigoma.pagbank.service.PagBankChargeService
import io.github.rodrigoma.pagbank.service.PagBankCouponService
import io.github.rodrigoma.pagbank.service.PagBankCustomerService
import io.github.rodrigoma.pagbank.service.PagBankInvoiceService
import io.github.rodrigoma.pagbank.service.PagBankPaymentService
import io.github.rodrigoma.pagbank.service.PagBankPlanService
import io.github.rodrigoma.pagbank.service.PagBankPreferenceService
import io.github.rodrigoma.pagbank.service.PagBankRefundService
import io.github.rodrigoma.pagbank.service.PagBankSubscriptionService
import io.github.rodrigoma.pagbank.service.PagBankWebhookParser
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestClient

@AutoConfiguration
@EnableConfigurationProperties(PagBankProperties::class)
class PagBankAutoConfiguration(
    private val properties: PagBankProperties,
) {
    @Bean(name = ["pagBankObjectMapper"])
    fun pagBankObjectMapper(): ObjectMapper =
        jacksonObjectMapper().apply {
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        }

    @Bean(name = ["pagBankRestClient"])
    fun pagBankRestClient(
        @Qualifier("pagBankObjectMapper")
        objectMapper: ObjectMapper,
    ): RestClient {
        val errorHandler = PagBankErrorHandler(objectMapper)
        val builder =
            RestClient
                .builder()
                .baseUrl(properties.environment.baseUrl())
                .defaultHeader("Authorization", "Bearer ${properties.token}")
                .messageConverters { converters ->
                    converters.removeIf { it is MappingJackson2HttpMessageConverter }
                    converters.add(0, MappingJackson2HttpMessageConverter(objectMapper))
                }.defaultStatusHandler({ it.isError }) { _, response -> errorHandler.handle(response) }
        if (properties.logRequests) {
            builder.requestInterceptor(PagBankLoggingInterceptor())
        }
        return builder.build()
    }

    @Bean fun pagBankPlanService(
        @Qualifier("pagBankRestClient") rc: RestClient,
    ) = PagBankPlanService(rc)

    @Bean fun pagBankCustomerService(
        @Qualifier("pagBankRestClient") rc: RestClient,
    ) = PagBankCustomerService(rc)

    @Bean fun pagBankSubscriptionService(
        @Qualifier("pagBankRestClient") rc: RestClient,
    ) = PagBankSubscriptionService(rc)

    @Bean fun pagBankCouponService(
        @Qualifier("pagBankRestClient") rc: RestClient,
    ) = PagBankCouponService(rc)

    @Bean fun pagBankInvoiceService(
        @Qualifier("pagBankRestClient") rc: RestClient,
    ) = PagBankInvoiceService(rc)

    @Bean fun pagBankPaymentService(
        @Qualifier("pagBankRestClient") rc: RestClient,
    ) = PagBankPaymentService(rc)

    @Bean fun pagBankChargeService(
        @Qualifier("pagBankRestClient") rc: RestClient,
    ) = PagBankChargeService(rc)

    @Bean fun pagBankRefundService(
        @Qualifier("pagBankRestClient") rc: RestClient,
    ) = PagBankRefundService(rc)

    @Bean fun pagBankPreferenceService(
        @Qualifier("pagBankRestClient") rc: RestClient,
    ) = PagBankPreferenceService(rc)

    @Bean fun pagBankWebhookParser() = PagBankWebhookParser(properties)
}
