package io.github.rodrigoma.pagbank.model.preference

data class PreferenceResponse(
    val retryDays: Int,
    val notificationEmail: String?
)

data class UpdatePreferenceRequest(
    val retryDays: Int? = null,
    val notificationEmail: String? = null
)
