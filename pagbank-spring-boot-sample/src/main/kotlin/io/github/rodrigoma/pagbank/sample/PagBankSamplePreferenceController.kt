package io.github.rodrigoma.pagbank.sample

import io.github.rodrigoma.pagbank.model.preference.NotificationPreferences
import io.github.rodrigoma.pagbank.model.preference.PublicKeyResponse
import io.github.rodrigoma.pagbank.model.preference.RetryPreferences
import io.github.rodrigoma.pagbank.service.PagBankPreferenceService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sample")
class PagBankSamplePreferenceController(
    private val preferenceService: PagBankPreferenceService,
) {
    @GetMapping("/preferences/notifications")
    fun getNotifications(): NotificationPreferences = preferenceService.getNotifications()

    @PutMapping("/preferences/notifications")
    fun updateNotifications(
        @RequestBody request: NotificationPreferences,
    ): NotificationPreferences = preferenceService.updateNotifications(request)

    @GetMapping("/preferences/retries")
    fun getRetries(): RetryPreferences = preferenceService.getRetries()

    @PutMapping("/preferences/retries")
    fun updateRetries(
        @RequestBody request: RetryPreferences,
    ): RetryPreferences = preferenceService.updateRetries(request)

    @GetMapping("/public-keys")
    fun getPublicKey(): PublicKeyResponse = preferenceService.getPublicKey()

    @PutMapping("/public-keys")
    fun rotatePublicKey(): PublicKeyResponse = preferenceService.rotatePublicKey()
}
