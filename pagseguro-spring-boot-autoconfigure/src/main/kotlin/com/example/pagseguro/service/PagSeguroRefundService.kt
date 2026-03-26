package com.example.pagseguro.service

import com.example.pagseguro.model.refund.*
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagSeguroRefundService(private val restClient: RestClient) {

    fun create(paymentId: String, request: RefundRequest = RefundRequest()): RefundResponse =
        restClient.post()
            .uri("/payments/{paymentId}/refunds", paymentId)
            .body(request)
            .retrieve()
            .body<RefundResponse>()!!

    fun get(id: String): RefundResponse =
        restClient.get()
            .uri("/refunds/{id}", id)
            .retrieve()
            .body<RefundResponse>()!!
}
