package io.github.rodrigoma.pagbank.autoconfigure

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonInclude.Value.construct
import io.github.rodrigoma.pagbank.http.PagBankErrorHandler
import io.github.rodrigoma.pagbank.http.PagBankLoggingInterceptor
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
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.web.client.RestClient
import tools.jackson.databind.PropertyNamingStrategies.SNAKE_CASE
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jacksonMapperBuilder

@AutoConfiguration
@EnableConfigurationProperties(PagBankProperties::class)
class PagBankAutoConfiguration(
    private val properties: PagBankProperties,
) {
    @Bean(name = ["pagBankObjectMapper"])
    fun pagBankObjectMapper(): JsonMapper =
        jacksonMapperBuilder()
            .propertyNamingStrategy(SNAKE_CASE)
            .changeDefaultPropertyInclusion { construct(NON_NULL, NON_NULL) }
            .build()

    @Bean(name = ["pagBankRestClient"])
    fun pagBankRestClient(
        @Qualifier("pagBankObjectMapper")
        objectMapper: JsonMapper,
    ): RestClient {
        val errorHandler = PagBankErrorHandler(objectMapper)

        return RestClient
            .builder()
            .baseUrl(properties.environment.baseUrl())
            .defaultHeader(AUTHORIZATION, "Bearer ${properties.token}")
            .configureMessageConverters {
                it.registerDefaults().withJsonConverter(JacksonJsonHttpMessageConverter(objectMapper))
            }.also { if (properties.logRequests) it.requestInterceptor(PagBankLoggingInterceptor()) }
            .defaultStatusHandler({ it.isError }) { _, response -> errorHandler.handle(response) }
            .build()
    }

    @Bean
    fun pagBankPlanService(
        @Qualifier("pagBankRestClient") rc: RestClient,
    ) = PagBankPlanService(rc)

    @Bean
    fun pagBankCustomerService(
        @Qualifier("pagBankRestClient") rc: RestClient,
    ) = PagBankCustomerService(rc)

    @Bean
    fun pagBankSubscriptionService(
        @Qualifier("pagBankRestClient") rc: RestClient,
    ) = PagBankSubscriptionService(rc)

    @Bean
    fun pagBankCouponService(
        @Qualifier("pagBankRestClient") rc: RestClient,
    ) = PagBankCouponService(rc)

    @Bean
    fun pagBankInvoiceService(
        @Qualifier("pagBankRestClient") rc: RestClient,
    ) = PagBankInvoiceService(rc)

    @Bean
    fun pagBankPaymentService(
        @Qualifier("pagBankRestClient") rc: RestClient,
    ) = PagBankPaymentService(rc)

    @Bean
    fun pagBankRefundService(
        @Qualifier("pagBankRestClient") rc: RestClient,
    ) = PagBankRefundService(rc)

    @Bean
    fun pagBankPreferenceService(
        @Qualifier("pagBankRestClient") rc: RestClient,
    ) = PagBankPreferenceService(rc)

    @Bean
    fun pagBankWebhookParser() = PagBankWebhookParser(properties)
}
