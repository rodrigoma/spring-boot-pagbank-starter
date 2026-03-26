package com.example.pagseguro.service

import com.example.pagseguro.model.charge.*
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagSeguroChargeService(private val restClient: RestClient) {

    fun get(id: String): ChargeResponse =
        restClient.get()
            .uri("/charges/{id}", id)
            .retrieve()
            .body<ChargeResponse>()!!

    fun list(): ChargeListResponse =
        restClient.get()
            .uri("/charges")
            .retrieve()
            .body<ChargeListResponse>()!!

    fun retry(id: String, request: RetryChargeRequest = RetryChargeRequest()) {
        restClient.post()
            .uri("/charges/{id}/retry", id)
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }
}
