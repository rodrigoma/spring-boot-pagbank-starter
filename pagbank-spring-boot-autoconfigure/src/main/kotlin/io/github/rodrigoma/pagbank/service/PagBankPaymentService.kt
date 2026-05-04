package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.payment.PaymentListResponse
import io.github.rodrigoma.pagbank.model.payment.PaymentResponse
import io.github.rodrigoma.pagbank.model.payment.PaymentStatus
import io.github.rodrigoma.pagbank.model.refund.RefundListResponse
import io.github.rodrigoma.pagbank.model.refund.RefundRequest
import io.github.rodrigoma.pagbank.model.refund.RefundResponse
import io.github.rodrigoma.pagbank.service.PagBankHeaders.IDEMPOTENCY_KEY
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.CREATED_AT_END
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.CREATED_AT_START
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.LIMIT
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.OFFSET
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.PAYMENT_METHOD_TYPE
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.STATUS
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
        idempotencyKey: String? = null,
    ): RefundResponse =
        restClient
            .post()
            .uri("/payments/{id}/refunds", id)
            .body(request)
            .apply { idempotencyKey?.let { header(IDEMPOTENCY_KEY, it) } }
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
                offset.let { builder.queryParam(OFFSET, it) }
                limit.let { builder.queryParam(LIMIT, it) }
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
                offset.let { builder.queryParam(OFFSET, it) }
                limit.let { builder.queryParam(LIMIT, it) }
                status?.let { builder.queryParam(STATUS, it.name) }
                createdAtStart?.let { builder.queryParam(CREATED_AT_START, it) }
                createdAtEnd?.let { builder.queryParam(CREATED_AT_END, it) }
                paymentMethodType?.let { builder.queryParam(PAYMENT_METHOD_TYPE, it) }
                builder.build()
            }.apply { q?.let { header("q", it) } }
            .retrieve()
            .body<PaymentListResponse>()!!
}
