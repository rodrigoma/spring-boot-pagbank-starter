package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.common.ListParams
import io.github.rodrigoma.pagbank.model.plan.*
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagBankPlanService(private val restClient: RestClient) {

    fun create(request: CreatePlanRequest): PlanResponse =
        restClient.post()
            .uri("/plans")
            .body(request)
            .retrieve()
            .body<PlanResponse>()!!

    fun get(id: String): PlanResponse =
        restClient.get()
            .uri("/plans/{id}", id)
            .retrieve()
            .body<PlanResponse>()!!

    fun list(params: ListParams = ListParams()): PlanListResponse =
        restClient.get()
            .uri("/plans?limit={limit}&offset={offset}", params.limit, params.offset)
            .retrieve()
            .body<PlanListResponse>()!!

    fun activate(id: String) {
        restClient.put()
            .uri("/plans/{id}/activate", id)
            .retrieve()
            .toBodilessEntity()
    }

    fun deactivate(id: String) {
        restClient.put()
            .uri("/plans/{id}/inactivate", id)
            .retrieve()
            .toBodilessEntity()
    }
}
