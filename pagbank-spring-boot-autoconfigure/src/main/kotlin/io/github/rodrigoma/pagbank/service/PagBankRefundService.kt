package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.common.ListParams
import io.github.rodrigoma.pagbank.model.refund.RefundListResponse
import io.github.rodrigoma.pagbank.model.refund.RefundRequest
import io.github.rodrigoma.pagbank.model.refund.RefundResponse
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagBankRefundService(
    private val restClient: RestClient,
) {
    fun create(
        paymentId: String,
        request: RefundRequest,
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

    fun listByPayment(
        paymentId: String,
        params: ListParams = ListParams(),
    ): RefundListResponse =
        restClient
            .get()
            .uri("/payments/{paymentId}/refunds?limit={limit}&offset={offset}", paymentId, params.limit, params.offset)
            .retrieve()
            .body<RefundListResponse>()!!

    fun list(params: ListParams = ListParams()): RefundListResponse =
        restClient
            .get()
            .uri("/refunds?limit={limit}&offset={offset}", params.limit, params.offset)
            .retrieve()
            .body<RefundListResponse>()!!
}
