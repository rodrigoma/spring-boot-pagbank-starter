package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.webhook.WebhookPayload
import tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import tools.jackson.databind.PropertyNamingStrategies.SNAKE_CASE
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.readValue

class PagBankWebhookParser {
    private val mapper =
        jacksonMapperBuilder()
            .propertyNamingStrategy(SNAKE_CASE)
            .disable(FAIL_ON_UNKNOWN_PROPERTIES)
            .build()

    fun parse(rawBody: String): WebhookPayload = mapper.readValue(rawBody)
}
