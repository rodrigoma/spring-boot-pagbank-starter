package io.github.rodrigoma.pagbank.sample

import io.github.rodrigoma.pagbank.model.customer.BillingInfoRequest
import io.github.rodrigoma.pagbank.model.customer.CreateCustomerRequest
import io.github.rodrigoma.pagbank.model.customer.CustomerListResponse
import io.github.rodrigoma.pagbank.model.customer.CustomerResponse
import io.github.rodrigoma.pagbank.model.customer.UpdateCustomerRequest
import io.github.rodrigoma.pagbank.service.PagBankCustomerService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sample/customers")
class PagBankSampleCustomerController(
    private val customerService: PagBankCustomerService,
) {
    @PostMapping("/demo")
    fun createDemoCustomer(): CustomerResponse =
        customerService.create(
            CreateCustomerRequest(
                name = "Maria Silva",
                email = "maria@example.com",
                taxId = "12345678909",
                referenceId = "demo-customer-001",
            ),
        )

    @GetMapping("/{id}")
    fun getCustomer(
        @PathVariable id: String,
    ): CustomerResponse = customerService.get(id)

    @PutMapping("/{id}")
    fun updateCustomer(
        @PathVariable id: String,
        @RequestBody request: UpdateCustomerRequest,
    ): CustomerResponse = customerService.update(id, request)

    @PutMapping("/{id}/billing_info")
    fun updateCustomerBillingInfo(
        @PathVariable id: String,
        @RequestBody billingInfo: List<BillingInfoRequest>,
    ): CustomerResponse = customerService.updateBillingInfo(id, billingInfo)

    @GetMapping
    fun listCustomers(
        @RequestParam(required = false) offset: Int?,
        @RequestParam(required = false) limit: Int?,
        @RequestParam(name = "reference_id", required = false) referenceId: String?,
    ): CustomerListResponse = customerService.list(offset, limit, referenceId)
}
