package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.refund.RefundListResponse
import io.github.rodrigoma.pagbank.model.refund.RefundResponse
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder

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
        offset: Int? = null,
        limit: Int? = null,
    ): RefundListResponse {
        val uri =
            UriComponentsBuilder
                .fromPath("/refunds")
                .apply {
                    offset?.let { queryParam("offset", it) }
                    limit?.let { queryParam("limit", it) }
                }.build()
                .toUriString()
        return restClient
            .get()
            .uri(uri)
            .retrieve()
            .body<RefundListResponse>()!!
    }
}
