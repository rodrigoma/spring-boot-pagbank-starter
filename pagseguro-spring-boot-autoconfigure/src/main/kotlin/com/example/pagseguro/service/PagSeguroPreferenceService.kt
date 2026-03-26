package com.example.pagseguro.service

import com.example.pagseguro.model.preference.*
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagSeguroPreferenceService(private val restClient: RestClient) {

    fun get(): PreferenceResponse =
        restClient.get()
            .uri("/preferences")
            .retrieve()
            .body<PreferenceResponse>()!!

    fun update(request: UpdatePreferenceRequest): PreferenceResponse =
        restClient.put()
            .uri("/preferences")
            .body(request)
            .retrieve()
            .body<PreferenceResponse>()!!
}
