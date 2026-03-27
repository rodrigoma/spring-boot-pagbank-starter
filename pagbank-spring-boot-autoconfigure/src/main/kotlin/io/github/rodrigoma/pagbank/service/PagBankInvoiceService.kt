package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.common.ListParams
import io.github.rodrigoma.pagbank.model.invoice.InvoiceListResponse
import io.github.rodrigoma.pagbank.model.invoice.InvoiceResponse
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

    fun listBySubscription(
        subscriptionId: String,
        params: ListParams = ListParams(),
    ): InvoiceListResponse =
        restClient
            .get()
            .uri("/subscriptions/{id}/invoices?limit={limit}&offset={offset}", subscriptionId, params.limit, params.offset)
            .retrieve()
            .body<InvoiceListResponse>()!!
}
