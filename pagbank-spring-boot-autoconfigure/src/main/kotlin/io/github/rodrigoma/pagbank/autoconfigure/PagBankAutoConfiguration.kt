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
import io.github.rodrigoma.pagbank.service.PagBankWebhookVerifier
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
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
    /**
     * Mapper dedicated to the PagBank API (snake_case, nulls omitted).
     *
     * Deliberately **not** a Spring bean: Spring Boot's `JacksonAutoConfiguration` registers its
     * `JsonMapper` with `@ConditionalOnMissingBean`, so publishing this one would silently replace
     * the application-wide mapper and change the JSON contract of every consumer.
     */
    private val objectMapper: JsonMapper =
        jacksonMapperBuilder()
            .propertyNamingStrategy(SNAKE_CASE)
            .changeDefaultPropertyInclusion { construct(NON_NULL, NON_NULL) }
            .build()

    @Bean(name = ["pagBankRestClient"])
    fun pagBankRestClient(customizers: ObjectProvider<PagBankRestClientCustomizer>): RestClient {
        val errorHandler = PagBankErrorHandler(objectMapper)

        val builder =
            RestClient
                .builder()
                .baseUrl(properties.resolvedBaseUrl())
                .defaultHeader(AUTHORIZATION, "Bearer ${properties.token}")
                .configureMessageConverters {
                    it.registerDefaults().withJsonConverter(JacksonJsonHttpMessageConverter(objectMapper))
                }.also { if (properties.logRequests) it.requestInterceptor(PagBankLoggingInterceptor()) }
                .defaultStatusHandler({ it.isError }) { _, response -> errorHandler.handle(response) }

        customizers.orderedStream().forEach { it.customize(builder) }

        return builder.build()
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
    fun pagBankWebhookVerifier() = PagBankWebhookVerifier(properties.token)

    @Bean
    fun pagBankWebhookParser(verifier: PagBankWebhookVerifier): PagBankWebhookParser =
        if (properties.webhook.verifySignature) {
            PagBankWebhookParser(verifier)
        } else {
            log.warn(
                "Webhook signature verification is disabled — PagBankWebhookParser.parseVerified will accept any " +
                    "payload. Set pagbank.webhook.verify-signature=true once PagBank sends the " +
                    "x-authenticity-token header.",
            )
            PagBankWebhookParser()
        }

    private companion object {
        private val log = LoggerFactory.getLogger(PagBankAutoConfiguration::class.java)
    }
}
