package io.github.rodrigoma.pagbank.sample

import io.github.rodrigoma.pagbank.model.invoice.InvoiceStatus
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionInvoiceListResponse
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionListResponse
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionResponse
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionStatus
import io.github.rodrigoma.pagbank.model.subscription.UpdateSubscriptionRequest
import io.github.rodrigoma.pagbank.service.PagBankSubscriptionService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sample/subscriptions")
class PagBankSampleSubscriptionController(
    private val subscriptionService: PagBankSubscriptionService,
) {
    @GetMapping
    fun listSubscriptions(
        @RequestParam(required = false) offset: Int?,
        @RequestParam(required = false) limit: Int?,
        @RequestParam(name = "reference_id", required = false) referenceId: String?,
        @RequestParam(required = false) status: SubscriptionStatus?,
    ): SubscriptionListResponse = subscriptionService.list(offset, limit, referenceId, status)

    @GetMapping("/{id}")
    fun getSubscription(
        @PathVariable id: String,
    ): SubscriptionResponse = subscriptionService.get(id)

    @PutMapping("/{id}")
    fun updateSubscription(
        @PathVariable id: String,
        @RequestBody request: UpdateSubscriptionRequest,
    ): SubscriptionResponse = subscriptionService.update(id, request)

    @PutMapping("/{id}/cancel")
    fun cancelSubscription(
        @PathVariable id: String,
    ) = subscriptionService.cancel(id)

    @PutMapping("/{id}/suspend")
    fun suspendSubscription(
        @PathVariable id: String,
    ) = subscriptionService.suspend(id)

    @PutMapping("/{id}/activate")
    fun activateSubscription(
        @PathVariable id: String,
    ) = subscriptionService.activate(id)

    @PutMapping("/{id}/retry")
    fun retrySubscription(
        @PathVariable id: String,
    ) = subscriptionService.retry(id)

    @DeleteMapping("/{id}/coupons")
    fun removeSubscriptionCoupon(
        @PathVariable id: String,
    ) = subscriptionService.removeCoupon(id)

    @GetMapping("/{id}/invoices")
    fun listSubscriptionInvoices(
        @PathVariable id: String,
        @RequestParam(required = false) offset: Int?,
        @RequestParam(required = false) limit: Int?,
        @RequestParam(required = false) status: InvoiceStatus?,
    ): SubscriptionInvoiceListResponse = subscriptionService.listInvoices(id, offset, limit, status)
}
