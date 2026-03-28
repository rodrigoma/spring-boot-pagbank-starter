package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.common.ListParams
import io.github.rodrigoma.pagbank.model.customer.CreateCustomerRequest
import io.github.rodrigoma.pagbank.model.customer.CustomerListResponse
import io.github.rodrigoma.pagbank.model.customer.CustomerResponse
import io.github.rodrigoma.pagbank.model.customer.UpdateBillingInfoRequest
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagBankCustomerService(
    private val restClient: RestClient,
) {
    fun create(request: CreateCustomerRequest): CustomerResponse =
        restClient
            .post()
            .uri("/customers")
            .body(request)
            .retrieve()
            .body<CustomerResponse>()!!

    fun get(id: String): CustomerResponse =
        restClient
            .get()
            .uri("/customers/{id}", id)
            .retrieve()
            .body<CustomerResponse>()!!

    fun update(
        id: String,
        request: CreateCustomerRequest,
    ): CustomerResponse =
        restClient
            .put()
            .uri("/customers/{id}", id)
            .body(request)
            .retrieve()
            .body<CustomerResponse>()!!

    fun updateBillingInfo(
        id: String,
        request: UpdateBillingInfoRequest,
    ): CustomerResponse =
        restClient
            .put()
            .uri("/customers/{id}/billing_info", id)
            .body(request)
            .retrieve()
            .body<CustomerResponse>()!!

    fun list(params: ListParams = ListParams()): CustomerListResponse =
        restClient
            .get()
            .uri("/customers?limit={limit}&offset={offset}", params.limit, params.offset)
            .retrieve()
            .body<CustomerListResponse>()!!
}
