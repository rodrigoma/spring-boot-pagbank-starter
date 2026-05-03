package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.plan.CreatePlanRequest
import io.github.rodrigoma.pagbank.model.plan.PlanListResponse
import io.github.rodrigoma.pagbank.model.plan.PlanResponse
import io.github.rodrigoma.pagbank.model.plan.PlanStatus
import io.github.rodrigoma.pagbank.model.plan.UpdatePlanRequest
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagBankPlanService(
    private val restClient: RestClient,
) {
    fun create(request: CreatePlanRequest): PlanResponse =
        restClient
            .post()
            .uri("/plans")
            .body(request)
            .retrieve()
            .body<PlanResponse>()!!

    fun get(id: String): PlanResponse =
        restClient
            .get()
            .uri("/plans/{id}", id)
            .retrieve()
            .body<PlanResponse>()!!

    fun update(
        id: String,
        request: UpdatePlanRequest,
    ): PlanResponse =
        restClient
            .put()
            .uri("/plans/{id}", id)
            .body(request)
            .retrieve()
            .body<PlanResponse>()!!

    fun list(
        offset: Int? = null,
        limit: Int? = null,
        referenceId: String? = null,
        status: PlanStatus? = null,
    ): PlanListResponse =
        restClient
            .get()
            .uri { builder ->
                builder.path("/plans")
                offset?.let { builder.queryParam("offset", it) }
                limit?.let { builder.queryParam("limit", it) }
                referenceId?.let { builder.queryParam("reference_id", it) }
                status?.let { builder.queryParam("status", it.name) }
                builder.build()
            }.retrieve()
            .body<PlanListResponse>()!!

    fun activate(id: String) {
        restClient
            .put()
            .uri("/plans/{id}/activate", id)
            .retrieve()
            .toBodilessEntity()
    }

    fun deactivate(id: String) {
        restClient
            .put()
            .uri("/plans/{id}/inactivate", id)
            .retrieve()
            .toBodilessEntity()
    }
}
