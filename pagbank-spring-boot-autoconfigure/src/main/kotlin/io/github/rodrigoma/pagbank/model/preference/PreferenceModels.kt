package io.github.rodrigoma.pagbank.model.preference

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.rodrigoma.pagbank.model.common.PagBankLink

// --- Notifications (GET/PUT /preferences/notifications) ---

data class NotificationChannel(
    val enabled: Boolean,
)

data class NotificationEmail(
    val merchant: NotificationChannel,
    val customer: NotificationChannel,
)

data class NotificationPreferences(
    val email: NotificationEmail,
    val urls: List<String>? = null,
)

// --- Retries (GET/PUT /preferences/retries) ---

enum class FinalAction { SUSPEND, CANCEL }

data class RetryPreferences(
    val firstTry: Int,
    val secondTry: Int? = null,
    val thirdTry: Int? = null,
    @JsonProperty("finally")
    val finalAction: FinalAction? = null,
)

// --- Public Keys (GET/PUT /public-keys) ---

data class PublicKeyResponse(
    val publicKey: String,
    val links: List<PagBankLink>? = null,
)
