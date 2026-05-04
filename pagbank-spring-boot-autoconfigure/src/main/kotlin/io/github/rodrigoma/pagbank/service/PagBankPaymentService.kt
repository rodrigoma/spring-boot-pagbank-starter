package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.payment.PaymentListResponse
import io.github.rodrigoma.pagbank.model.payment.PaymentResponse
import io.github.rodrigoma.pagbank.model.payment.PaymentStatus
import io.github.rodrigoma.pagbank.model.refund.RefundListResponse
import io.github.rodrigoma.pagbank.model.refund.RefundRequest
import io.github.rodrigoma.pagbank.model.refund.RefundResponse
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
        offset: Int = 0,
        limit: Int = 100,
    ): RefundListResponse =
        restClient
            .get()
            .uri { builder ->
                builder.path("/payments/{id}/refunds")
                offset.let { builder.queryParam("offset", it) }
                limit.let { builder.queryParam("limit", it) }
                builder.build(id)
            }.retrieve()
            .body<RefundListResponse>()!!

    @Suppress("LongParameterList")
    fun list(
        offset: Int = 0,
        limit: Int = 100,
        status: PaymentStatus? = null,
        createdAtStart: String? = null,
        createdAtEnd: String? = null,
        paymentMethodType: String? = null,
        q: String? = null,
    ): PaymentListResponse =
        restClient
            .get()
            .uri { builder ->
                builder.path("/payments")
                offset.let { builder.queryParam("offset", it) }
                limit.let { builder.queryParam("limit", it) }
                status?.let { builder.queryParam("status", it.name) }
                createdAtStart?.let { builder.queryParam("created_at_start", it) }
                createdAtEnd?.let { builder.queryParam("created_at_end", it) }
                paymentMethodType?.let { builder.queryParam("payment_method_type", it) }
                builder.build()
            }.apply { q?.let { header("q", it) } }
            .retrieve()
            .body<PaymentListResponse>()!!
}
