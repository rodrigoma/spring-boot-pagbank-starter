package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.refund.RefundRequest
import io.github.rodrigoma.pagbank.model.refund.RefundResponse
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagBankRefundService(
    private val restClient: RestClient,
) {
    fun create(
        paymentId: String,
        request: RefundRequest = RefundRequest(),
    ): RefundResponse =
        restClient
            .post()
            .uri("/payments/{paymentId}/refunds", paymentId)
            .body(request)
            .retrieve()
            .body<RefundResponse>()!!

    fun get(id: String): RefundResponse =
        restClient
            .get()
            .uri("/refunds/{id}", id)
            .retrieve()
            .body<RefundResponse>()!!
}
