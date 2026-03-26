package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.charge.*
import io.github.rodrigoma.pagbank.model.common.ListParams
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagBankChargeService(private val restClient: RestClient) {

    fun get(id: String): ChargeResponse =
        restClient.get()
            .uri("/charges/{id}", id)
            .retrieve()
            .body<ChargeResponse>()!!

    fun list(params: ListParams = ListParams()): ChargeListResponse =
        restClient.get()
            .uri("/charges?limit={limit}&offset={offset}", params.limit, params.offset)
            .retrieve()
            .body<ChargeListResponse>()!!

    fun retry(id: String, request: RetryChargeRequest = RetryChargeRequest()) {
        restClient.post()
            .uri("/charges/{id}/retry", id)
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }
}
