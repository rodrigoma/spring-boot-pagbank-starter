package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.plan.CreatePlanRequest
import io.github.rodrigoma.pagbank.model.plan.PlanListResponse
import io.github.rodrigoma.pagbank.model.plan.PlanResponse
import io.github.rodrigoma.pagbank.model.plan.PlanStatus
import io.github.rodrigoma.pagbank.model.plan.UpdatePlanRequest
import io.github.rodrigoma.pagbank.service.PagBankHeaders.IDEMPOTENCY_KEY
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.LIMIT
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.OFFSET
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.REFERENCE_ID
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.STATUS
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagBankPlanService(
    private val restClient: RestClient,
) {
    @JvmOverloads
    fun create(
        request: CreatePlanRequest,
        idempotencyKey: String? = null,
    ): PlanResponse =
        restClient
            .post()
            .uri("/plans")
            .body(request)
            .apply { idempotencyKey?.let { header(IDEMPOTENCY_KEY, it) } }
            .retrieve()
            .body<PlanResponse>()!!

    fun get(id: String): PlanResponse =
        restClient
            .get()
            .uri("/plans/{id}", id)
            .retrieve()
            .body<PlanResponse>()!!

    @JvmOverloads
    fun update(
        id: String,
        request: UpdatePlanRequest,
        idempotencyKey: String? = null,
    ): PlanResponse =
        restClient
            .put()
            .uri("/plans/{id}", id)
            .body(request)
            .apply { idempotencyKey?.let { header(IDEMPOTENCY_KEY, it) } }
            .retrieve()
            .body<PlanResponse>()!!

    @JvmOverloads
    fun list(
        offset: Int = 0,
        limit: Int = 100,
        referenceId: String? = null,
        status: PlanStatus? = null,
    ): PlanListResponse =
        restClient
            .get()
            .uri { builder ->
                builder.path("/plans")
                offset.let { builder.queryParam(OFFSET, it) }
                limit.let { builder.queryParam(LIMIT, it) }
                referenceId?.let { builder.queryParam(REFERENCE_ID, it) }
                status?.let { builder.queryParam(STATUS, it.name) }
                builder.build()
            }.retrieve()
            .body<PlanListResponse>()!!

    @JvmOverloads
    fun activate(
        id: String,
        idempotencyKey: String? = null,
    ) {
        restClient
            .put()
            .uri("/plans/{id}/activate", id)
            .apply { idempotencyKey?.let { header(IDEMPOTENCY_KEY, it) } }
            .retrieve()
            .toBodilessEntity()
    }

    @JvmOverloads
    fun inactivate(
        id: String,
        idempotencyKey: String? = null,
    ) {
        restClient
            .put()
            .uri("/plans/{id}/inactivate", id)
            .apply { idempotencyKey?.let { header(IDEMPOTENCY_KEY, it) } }
            .retrieve()
            .toBodilessEntity()
    }
}
