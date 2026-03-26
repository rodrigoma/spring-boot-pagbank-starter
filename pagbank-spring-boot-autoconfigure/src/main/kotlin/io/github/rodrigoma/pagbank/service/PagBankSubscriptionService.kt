package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.subscription.*
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagBankSubscriptionService(private val restClient: RestClient) {

    fun create(request: CreateSubscriptionRequest): SubscriptionResponse =
        restClient.post()
            .uri("/subscriptions")
            .body(request)
            .retrieve()
            .body<SubscriptionResponse>()!!

    fun get(id: String): SubscriptionResponse =
        restClient.get()
            .uri("/subscriptions/{id}", id)
            .retrieve()
            .body<SubscriptionResponse>()!!

    fun cancel(id: String) {
        restClient.put()
            .uri("/subscriptions/{id}/cancel", id)
            .retrieve()
            .toBodilessEntity()
    }

    fun suspend(id: String) {
        restClient.put()
            .uri("/subscriptions/{id}/suspend", id)
            .retrieve()
            .toBodilessEntity()
    }

    fun reactivate(id: String) {
        restClient.put()
            .uri("/subscriptions/{id}/reactivate", id)
            .retrieve()
            .toBodilessEntity()
    }

    fun list(): SubscriptionListResponse =
        restClient.get()
            .uri("/subscriptions")
            .retrieve()
            .body<SubscriptionListResponse>()!!
}
