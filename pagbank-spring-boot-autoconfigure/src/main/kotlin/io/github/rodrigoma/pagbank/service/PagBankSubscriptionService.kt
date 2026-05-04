package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.invoice.InvoiceStatus
import io.github.rodrigoma.pagbank.model.subscription.CreateSubscriptionRequest
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionInvoiceListResponse
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionListResponse
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionResponse
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionStatus
import io.github.rodrigoma.pagbank.model.subscription.UpdateSubscriptionRequest
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder

class PagBankSubscriptionService(
    private val restClient: RestClient,
) {
    fun create(request: CreateSubscriptionRequest): SubscriptionResponse =
        restClient
            .post()
            .uri("/subscriptions")
            .body(request)
            .retrieve()
            .body<SubscriptionResponse>()!!

    fun get(id: String): SubscriptionResponse =
        restClient
            .get()
            .uri("/subscriptions/{id}", id)
            .retrieve()
            .body<SubscriptionResponse>()!!

    fun update(
        id: String,
        request: UpdateSubscriptionRequest,
    ): SubscriptionResponse =
        restClient
            .put()
            .uri("/subscriptions/{id}", id)
            .body(request)
            .retrieve()
            .body<SubscriptionResponse>()!!

    fun cancel(id: String) {
        restClient
            .put()
            .uri("/subscriptions/{id}/cancel", id)
            .retrieve()
            .toBodilessEntity()
    }

    fun suspend(id: String) {
        restClient
            .put()
            .uri("/subscriptions/{id}/suspend", id)
            .retrieve()
            .toBodilessEntity()
    }

    fun activate(id: String) {
        restClient
            .put()
            .uri("/subscriptions/{id}/activate", id)
            .retrieve()
            .toBodilessEntity()
    }

    fun retry(id: String) {
        restClient
            .put()
            .uri("/subscriptions/{id}/retry", id)
            .retrieve()
            .toBodilessEntity()
    }

    fun removeCoupon(id: String) {
        restClient
            .delete()
            .uri("/subscriptions/{id}/coupons", id)
            .retrieve()
            .toBodilessEntity()
    }

    fun list(
        offset: Int? = null,
        limit: Int? = null,
        referenceId: String? = null,
        status: SubscriptionStatus? = null,
    ): SubscriptionListResponse {
        val uri =
            UriComponentsBuilder
                .fromPath("/subscriptions")
                .apply {
                    offset?.let { queryParam("offset", it) }
                    limit?.let { queryParam("limit", it) }
                    referenceId?.let { queryParam("reference_id", it) }
                    status?.let { queryParam("status", it.name) }
                }.build()
                .toUriString()
        return restClient
            .get()
            .uri(uri)
            .retrieve()
            .body<SubscriptionListResponse>()!!
    }

    fun listInvoices(
        id: String,
        offset: Int? = null,
        limit: Int? = null,
        status: InvoiceStatus? = null,
    ): SubscriptionInvoiceListResponse {
        val uri =
            UriComponentsBuilder
                .fromPath("/subscriptions/{id}/invoices")
                .apply {
                    offset?.let { queryParam("offset", it) }
                    limit?.let { queryParam("limit", it) }
                    status?.let { queryParam("status", it.name) }
                }.build()
                .expand(id)
                .toUriString()
        return restClient
            .get()
            .uri(uri)
            .retrieve()
            .body<SubscriptionInvoiceListResponse>()!!
    }
}
