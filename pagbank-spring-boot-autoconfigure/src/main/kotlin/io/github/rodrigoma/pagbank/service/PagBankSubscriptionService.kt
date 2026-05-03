package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.common.ListParams
import io.github.rodrigoma.pagbank.model.subscription.CreateSubscriptionRequest
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionListResponse
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionResponse
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

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

    fun reactivate(id: String) {
        restClient
            .put()
            .uri("/subscriptions/{id}/reactivate", id)
            .retrieve()
            .toBodilessEntity()
    }

    fun applyCoupon(
        subscriptionId: String,
        couponId: String,
    ) {
        restClient
            .post()
            .uri("/subscriptions/{id}/coupons", subscriptionId)
            .body(mapOf("couponId" to couponId))
            .retrieve()
            .toBodilessEntity()
    }

    fun list(params: ListParams = ListParams()): SubscriptionListResponse =
        restClient
            .get()
            .uri("/subscriptions?limit={limit}&offset={offset}", params.limit, params.offset)
            .retrieve()
            .body<SubscriptionListResponse>()!!
}
