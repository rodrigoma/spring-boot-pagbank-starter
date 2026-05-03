package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.customer.BillingInfo
import io.github.rodrigoma.pagbank.model.customer.CreateCustomerRequest
import io.github.rodrigoma.pagbank.model.customer.CustomerListResponse
import io.github.rodrigoma.pagbank.model.customer.CustomerResponse
import io.github.rodrigoma.pagbank.model.customer.UpdateCustomerRequest
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
        request: UpdateCustomerRequest,
    ): CustomerResponse =
        restClient
            .put()
            .uri("/customers/{id}", id)
            .body(request)
            .retrieve()
            .body<CustomerResponse>()!!

    fun updateBillingInfo(
        id: String,
        billingInfo: List<BillingInfo>,
    ): CustomerResponse =
        restClient
            .put()
            .uri("/customers/{id}/billing_info", id)
            .body(billingInfo)
            .retrieve()
            .body<CustomerResponse>()!!

    fun list(
        offset: Int? = null,
        limit: Int? = null,
        referenceId: String? = null,
    ): CustomerListResponse =
        restClient
            .get()
            .uri { builder ->
                builder.path("/customers")
                offset?.let { builder.queryParam("offset", it) }
                limit?.let { builder.queryParam("limit", it) }
                referenceId?.let { builder.queryParam("reference_id", it) }
                builder.build()
            }.retrieve()
            .body<CustomerListResponse>()!!
}
