package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.invoice.InvoiceResponse
import io.github.rodrigoma.pagbank.model.payment.PaymentListResponse
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder

class PagBankInvoiceService(
    private val restClient: RestClient,
) {
    fun get(id: String): InvoiceResponse =
        restClient
            .get()
            .uri("/invoices/{id}", id)
            .retrieve()
            .body<InvoiceResponse>()!!

    fun listPayments(
        id: String,
        offset: Int? = null,
        limit: Int? = null,
    ): PaymentListResponse {
        val uri =
            UriComponentsBuilder
                .fromPath("/invoices/{id}/payments")
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
            .body<PaymentListResponse>()!!
    }
}
