package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.invoice.*
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagBankInvoiceService(private val restClient: RestClient) {

    fun get(id: String): InvoiceResponse =
        restClient.get()
            .uri("/invoices/{id}", id)
            .retrieve()
            .body<InvoiceResponse>()!!

    fun listBySubscription(subscriptionId: String): InvoiceListResponse =
        restClient.get()
            .uri("/subscriptions/{id}/invoices", subscriptionId)
            .retrieve()
            .body<InvoiceListResponse>()!!
}
