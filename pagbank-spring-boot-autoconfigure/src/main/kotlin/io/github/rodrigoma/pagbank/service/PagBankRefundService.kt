package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.refund.RefundListResponse
import io.github.rodrigoma.pagbank.model.refund.RefundResponse
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagBankRefundService(
    private val restClient: RestClient,
) {
    fun get(id: String): RefundResponse =
        restClient
            .get()
            .uri("/refunds/{id}", id)
            .retrieve()
            .body<RefundResponse>()!!

    fun list(
        offset: Int = 0,
        limit: Int = 100,
    ): RefundListResponse =
        restClient
            .get()
            .uri { builder ->
                builder.path("/refunds")
                offset.let { builder.queryParam("offset", it) }
                limit.let { builder.queryParam("limit", it) }
                builder.build()
            }.retrieve()
            .body<RefundListResponse>()!!
}
