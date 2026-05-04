package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.invoice.InvoiceResponse
import io.github.rodrigoma.pagbank.model.payment.PaymentListResponse
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

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
        offset: Int = 0,
        limit: Int = 100,
    ): PaymentListResponse =
        restClient
            .get()
            .uri { builder ->
                builder.path("/invoices/{id}/payments")
                offset.let { builder.queryParam("offset", it) }
                limit.let { builder.queryParam("limit", it) }
                builder.build(id)
            }.retrieve()
            .body<PaymentListResponse>()!!
}
