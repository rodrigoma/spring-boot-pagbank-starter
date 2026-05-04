package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.payment.PaymentListResponse
import io.github.rodrigoma.pagbank.model.payment.PaymentResponse
import io.github.rodrigoma.pagbank.model.payment.PaymentStatus
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

    @Suppress("LongParameterList")
    fun list(
        offset: Int? = null,
        limit: Int? = null,
        status: PaymentStatus? = null,
        paidAtStart: String? = null,
        paidAtEnd: String? = null,
        paymentMethodType: String? = null,
    ): PaymentListResponse {
        val uri =
            UriComponentsBuilder
                .fromPath("/payments")
                .apply {
                    offset?.let { queryParam("offset", it) }
                    limit?.let { queryParam("limit", it) }
                    status?.let { queryParam("status", it.name) }
                    paidAtStart?.let { queryParam("paid_at_start", it) }
                    paidAtEnd?.let { queryParam("paid_at_end", it) }
                    paymentMethodType?.let { queryParam("payment_method_type", it) }
                }.build()
                .toUriString()
        return restClient
            .get()
            .uri(uri)
            .retrieve()
            .body<PaymentListResponse>()!!
    }
}
