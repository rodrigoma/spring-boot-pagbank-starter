package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.payment.PaymentListResponse
import io.github.rodrigoma.pagbank.model.payment.PaymentResponse
import io.github.rodrigoma.pagbank.model.payment.PaymentStatus
import io.github.rodrigoma.pagbank.model.refund.RefundListResponse
import io.github.rodrigoma.pagbank.model.refund.RefundRequest
import io.github.rodrigoma.pagbank.model.refund.RefundResponse
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder

class PagBankPaymentService(
    private val restClient: RestClient,
) {
    fun get(id: String): PaymentResponse =
        restClient
            .get()
            .uri("/payments/{id}", id)
            .retrieve()
            .body<PaymentResponse>()!!

    fun createRefund(
        id: String,
        request: RefundRequest,
    ): RefundResponse =
        restClient
            .post()
            .uri("/payments/{id}/refunds", id)
            .body(request)
            .retrieve()
            .body<RefundResponse>()!!

    fun listRefunds(
        id: String,
        offset: Int? = null,
        limit: Int? = null,
    ): RefundListResponse {
        val uri =
            UriComponentsBuilder
                .fromPath("/payments/{id}/refunds")
                .apply {
                    offset?.let { queryParam("offset", it) }
                    limit?.let { queryParam("limit", it) }
                }.build()
                .expand(id)
                .toUriString()
        return restClient
            .get()
            .uri(uri)
            .retrieve()
            .body<RefundListResponse>()!!
    }

    @Suppress("LongParameterList")
    fun list(
        offset: Int? = null,
        limit: Int? = null,
        status: PaymentStatus? = null,
        createdAtStart: String? = null,
        createdAtEnd: String? = null,
        paymentMethodType: String? = null,
        q: String? = null,
    ): PaymentListResponse {
        val uri =
            UriComponentsBuilder
                .fromPath("/payments")
                .apply {
                    offset?.let { queryParam("offset", it) }
                    limit?.let { queryParam("limit", it) }
                    status?.let { queryParam("status", it.name) }
                    createdAtStart?.let { queryParam("created_at_start", it) }
                    createdAtEnd?.let { queryParam("created_at_end", it) }
                    paymentMethodType?.let { queryParam("payment_method_type", it) }
                }.build()
                .toUriString()
        return restClient
            .get()
            .uri(uri)
            .apply { q?.let { header("q", it) } }
            .retrieve()
            .body<PaymentListResponse>()!!
    }
}
