package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.common.ListParams
import io.github.rodrigoma.pagbank.model.payment.PaymentListResponse
import io.github.rodrigoma.pagbank.model.payment.PaymentResponse
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagBankPaymentService(
    private val restClient: RestClient,
) {
    fun get(id: String): PaymentResponse =
        restClient
            .get()
            .uri("/payments/{id}", id)
            .retrieve()
            .body<PaymentResponse>()!!

    fun list(params: ListParams = ListParams()): PaymentListResponse =
        restClient
            .get()
            .uri("/payments?limit={limit}&offset={offset}", params.limit, params.offset)
            .retrieve()
            .body<PaymentListResponse>()!!
}
