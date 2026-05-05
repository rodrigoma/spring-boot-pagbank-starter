package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.refund.RefundListResponse
import io.github.rodrigoma.pagbank.model.refund.RefundResponse
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.LIMIT
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.OFFSET
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

    @JvmOverloads
    fun list(
        offset: Int = 0,
        limit: Int = 100,
    ): RefundListResponse =
        restClient
            .get()
            .uri { builder ->
                builder.path("/refunds")
                offset.let { builder.queryParam(OFFSET, it) }
                limit.let { builder.queryParam(LIMIT, it) }
                builder.build()
            }.retrieve()
            .body<RefundListResponse>()!!
}
