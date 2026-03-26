package com.example.pagbank.service

import com.example.pagbank.model.payment.*
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagBankPaymentService(private val restClient: RestClient) {

    fun get(id: String): PaymentResponse =
        restClient.get()
            .uri("/payments/{id}", id)
            .retrieve()
            .body<PaymentResponse>()!!

    fun list(): PaymentListResponse =
        restClient.get()
            .uri("/payments")
            .retrieve()
            .body<PaymentListResponse>()!!
}
