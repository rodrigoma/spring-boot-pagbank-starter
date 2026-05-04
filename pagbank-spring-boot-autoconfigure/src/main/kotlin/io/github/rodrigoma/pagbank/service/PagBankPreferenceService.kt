package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.preference.NotificationPreferences
import io.github.rodrigoma.pagbank.model.preference.PublicKeyResponse
import io.github.rodrigoma.pagbank.model.preference.RetryPreferences
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

    fun updateNotifications(request: NotificationPreferences): NotificationPreferences =
        restClient
            .put()
            .uri("/preferences/notifications")
            .body(request)
            .retrieve()
            .body<NotificationPreferences>()!!

    fun getRetries(): RetryPreferences =
        restClient
            .get()
            .uri("/preferences/retries")
            .retrieve()
            .body<RetryPreferences>()!!

    fun updateRetries(request: RetryPreferences): RetryPreferences =
        restClient
            .put()
            .uri("/preferences/retries")
            .body(request)
            .retrieve()
            .body<RetryPreferences>()!!

    fun getPublicKey(): PublicKeyResponse =
        restClient
            .get()
            .uri("/public-keys")
            .retrieve()
            .body<PublicKeyResponse>()!!

    fun rotatePublicKey(): PublicKeyResponse =
        restClient
            .put()
            .uri("/public-keys")
            .retrieve()
            .body<PublicKeyResponse>()!!
}
