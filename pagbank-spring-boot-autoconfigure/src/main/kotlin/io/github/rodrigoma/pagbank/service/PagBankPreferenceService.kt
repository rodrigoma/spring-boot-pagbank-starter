package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.preference.NotificationPreferences
import io.github.rodrigoma.pagbank.model.preference.PublicKeyResponse
import io.github.rodrigoma.pagbank.model.preference.RetryPreferences
import io.github.rodrigoma.pagbank.service.PagBankHeaders.IDEMPOTENCY_KEY
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagBankPreferenceService(
    private val restClient: RestClient,
) {
    fun getNotifications(): NotificationPreferences =
        restClient
            .get()
            .uri("/preferences/notifications")
            .retrieve()
            .body<NotificationPreferences>()!!

    fun updateNotifications(
        request: NotificationPreferences,
        idempotencyKey: String? = null,
    ): NotificationPreferences =
        restClient
            .put()
            .uri("/preferences/notifications")
            .body(request)
            .apply { idempotencyKey?.let { header(IDEMPOTENCY_KEY, it) } }
            .retrieve()
            .body<NotificationPreferences>()!!

    fun getRetries(): RetryPreferences =
        restClient
            .get()
            .uri("/preferences/retries")
            .retrieve()
            .body<RetryPreferences>()!!

    fun updateRetries(
        request: RetryPreferences,
        idempotencyKey: String? = null,
    ): RetryPreferences =
        restClient
            .put()
            .uri("/preferences/retries")
            .body(request)
            .apply { idempotencyKey?.let { header(IDEMPOTENCY_KEY, it) } }
            .retrieve()
            .body<RetryPreferences>()!!

    fun getPublicKey(): PublicKeyResponse =
        restClient
            .get()
            .uri("/public-keys")
            .retrieve()
            .body<PublicKeyResponse>()!!

    fun rotatePublicKey(idempotencyKey: String? = null): PublicKeyResponse =
        restClient
            .put()
            .uri("/public-keys")
            .apply { idempotencyKey?.let { header(IDEMPOTENCY_KEY, it) } }
            .retrieve()
            .body<PublicKeyResponse>()!!
}
