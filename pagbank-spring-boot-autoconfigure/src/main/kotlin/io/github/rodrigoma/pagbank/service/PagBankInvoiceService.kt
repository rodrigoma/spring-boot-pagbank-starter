package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.invoice.InvoiceResponse
import io.github.rodrigoma.pagbank.model.payment.PaymentListResponse
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.LIMIT
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.OFFSET
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

    @JvmOverloads
    fun listPayments(
        id: String,
        offset: Int = 0,
        limit: Int = 100,
    ): PaymentListResponse =
        restClient
            .get()
            .uri { builder ->
                builder.path("/invoices/{id}/payments")
                offset.let { builder.queryParam(OFFSET, it) }
                limit.let { builder.queryParam(LIMIT, it) }
                builder.build(id)
            }.retrieve()
            .body<PaymentListResponse>()!!
}
