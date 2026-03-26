package com.example.pagseguro.service

import com.example.pagseguro.model.invoice.*
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagSeguroInvoiceService(private val restClient: RestClient) {

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
