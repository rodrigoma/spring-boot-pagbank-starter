package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.preference.PreferenceResponse
import io.github.rodrigoma.pagbank.model.preference.UpdatePreferenceRequest
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagBankPreferenceService(
    private val restClient: RestClient,
) {
    fun get(): PreferenceResponse =
        restClient
            .get()
            .uri("/preferences")
            .retrieve()
            .body<PreferenceResponse>()!!

    fun update(request: UpdatePreferenceRequest): PreferenceResponse =
        restClient
            .put()
            .uri("/preferences")
            .body(request)
            .retrieve()
            .body<PreferenceResponse>()!!
}
