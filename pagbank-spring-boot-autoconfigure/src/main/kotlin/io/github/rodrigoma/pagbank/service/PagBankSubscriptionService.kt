package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.invoice.InvoiceStatus
import io.github.rodrigoma.pagbank.model.subscription.CreateSubscriptionRequest
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionInvoiceListResponse
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionListResponse
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionResponse
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionStatus
import io.github.rodrigoma.pagbank.model.subscription.UpdateSubscriptionRequest
import io.github.rodrigoma.pagbank.service.PagBankHeaders.IDEMPOTENCY_KEY
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.LIMIT
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.OFFSET
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.REFERENCE_ID
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.STATUS
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagBankSubscriptionService(
    private val restClient: RestClient,
) {
    fun create(
        request: CreateSubscriptionRequest,
        idempotencyKey: String? = null,
    ): SubscriptionResponse =
        restClient
            .post()
            .uri("/subscriptions")
            .body(request)
            .apply { idempotencyKey?.let { header(IDEMPOTENCY_KEY, it) } }
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
        idempotencyKey: String? = null,
    ): SubscriptionResponse =
        restClient
            .put()
            .uri("/subscriptions/{id}", id)
            .body(request)
            .apply { idempotencyKey?.let { header(IDEMPOTENCY_KEY, it) } }
            .retrieve()
            .body<SubscriptionResponse>()!!

    fun cancel(
        id: String,
        idempotencyKey: String? = null,
    ) {
        restClient
            .put()
            .uri("/subscriptions/{id}/cancel", id)
            .apply { idempotencyKey?.let { header(IDEMPOTENCY_KEY, it) } }
            .retrieve()
            .toBodilessEntity()
    }

    fun suspend(
        id: String,
        idempotencyKey: String? = null,
    ) {
        restClient
            .put()
            .uri("/subscriptions/{id}/suspend", id)
            .apply { idempotencyKey?.let { header(IDEMPOTENCY_KEY, it) } }
            .retrieve()
            .toBodilessEntity()
    }

    fun activate(
        id: String,
        idempotencyKey: String? = null,
    ) {
        restClient
            .put()
            .uri("/subscriptions/{id}/activate", id)
            .apply { idempotencyKey?.let { header(IDEMPOTENCY_KEY, it) } }
            .retrieve()
            .toBodilessEntity()
    }

    fun retry(
        id: String,
        idempotencyKey: String? = null,
    ) {
        restClient
            .put()
            .uri("/subscriptions/{id}/retry", id)
            .apply { idempotencyKey?.let { header(IDEMPOTENCY_KEY, it) } }
            .retrieve()
            .toBodilessEntity()
    }

    fun removeCoupon(
        id: String,
        idempotencyKey: String? = null,
    ) {
        restClient
            .delete()
            .uri("/subscriptions/{id}/coupons", id)
            .apply { idempotencyKey?.let { header(IDEMPOTENCY_KEY, it) } }
            .retrieve()
            .toBodilessEntity()
    }

    fun list(
        offset: Int = 0,
        limit: Int = 100,
        referenceId: String? = null,
        status: SubscriptionStatus? = null,
    ): SubscriptionListResponse =
        restClient
            .get()
            .uri { builder ->
                builder.path("/subscriptions")
                offset.let { builder.queryParam(OFFSET, it) }
                limit.let { builder.queryParam(LIMIT, it) }
                referenceId?.let { builder.queryParam(REFERENCE_ID, it) }
                status?.let { builder.queryParam(STATUS, it.name) }
                builder.build()
            }.retrieve()
            .body<SubscriptionListResponse>()!!

    fun listInvoices(
        id: String,
        offset: Int = 0,
        limit: Int = 100,
        status: InvoiceStatus? = null,
    ): SubscriptionInvoiceListResponse =
        restClient
            .get()
            .uri { builder ->
                builder.path("/subscriptions/{id}/invoices")
                offset.let { builder.queryParam(OFFSET, it) }
                limit.let { builder.queryParam(LIMIT, it) }
                status?.let { builder.queryParam(STATUS, it.name) }
                builder.build(id)
            }.retrieve()
            .body<SubscriptionInvoiceListResponse>()!!
}
