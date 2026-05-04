package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.refund.RefundListResponse
import io.github.rodrigoma.pagbank.model.refund.RefundRequest
import io.github.rodrigoma.pagbank.model.refund.RefundResponse
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder

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
        offset: Int? = null,
        limit: Int? = null,
    ): RefundListResponse {
        val uri =
            UriComponentsBuilder
                .fromPath("/payments/{paymentId}/refunds")
                .apply {
                    offset?.let { queryParam("offset", it) }
                    limit?.let { queryParam("limit", it) }
                }.build()
                .expand(paymentId)
                .toUriString()
        return restClient
            .get()
            .uri(uri)
            .retrieve()
            .body<RefundListResponse>()!!
    }

    fun list(
        offset: Int? = null,
        limit: Int? = null,
    ): RefundListResponse {
        val uri =
            UriComponentsBuilder
                .fromPath("/refunds")
                .apply {
                    offset?.let { queryParam("offset", it) }
                    limit?.let { queryParam("limit", it) }
                }.build()
                .toUriString()
        return restClient
            .get()
            .uri(uri)
            .retrieve()
            .body<RefundListResponse>()!!
    }
}
