package io.github.rodrigoma.pagbank.model.preference

import io.github.rodrigoma.pagbank.model.common.PagBankLink

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

enum class Finally { SUSPEND, CANCEL }

data class RetryPreferences(
    val firstTry: Int,
    val secondTry: Int? = null,
    val thirdTry: Int? = null,
    val finally: Finally? = null,
)

data class PublicKeyResponse(
    val publicKey: String,
    val links: List<PagBankLink>? = null,
)
