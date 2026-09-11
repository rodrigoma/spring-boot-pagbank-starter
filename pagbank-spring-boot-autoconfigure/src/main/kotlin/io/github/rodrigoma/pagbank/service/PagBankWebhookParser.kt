package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.exception.PagBankException
import io.github.rodrigoma.pagbank.model.webhook.WebhookPayload
import tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import tools.jackson.databind.PropertyNamingStrategies.SNAKE_CASE
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.readValue

/**
 * Parses PagBank webhook notifications into [WebhookPayload].
 *
 * When constructed with a [PagBankWebhookVerifier] (the auto-configuration does this when
 * `pagbank.webhook.verify-signature=true`), [parseVerified] rejects payloads whose
 * `x-authenticity-token` header does not match before touching the JSON. Without a verifier,
 * [parseVerified] is equivalent to [parse].
 */
class PagBankWebhookParser(
    private val verifier: PagBankWebhookVerifier? = null,
) {
    private val mapper =
        jacksonMapperBuilder()
            .propertyNamingStrategy(SNAKE_CASE)
            .disable(FAIL_ON_UNKNOWN_PROPERTIES)
            .build()

    fun parse(rawBody: String): WebhookPayload = mapper.readValue(rawBody)

    fun parse(rawBody: ByteArray): WebhookPayload = mapper.readValue(rawBody)

    /**
     * Verifies [authenticityToken] against [rawBody] (when verification is enabled) and then parses it.
     *
     * @throws PagBankException.InvalidSignature when verification is enabled and the header is missing or wrong.
     */
    fun parseVerified(
        rawBody: ByteArray,
        authenticityToken: String?,
    ): WebhookPayload {
        verifier?.let {
            if (!it.verify(rawBody, authenticityToken)) throw PagBankException.InvalidSignature()
        }
        return parse(rawBody)
    }
}
