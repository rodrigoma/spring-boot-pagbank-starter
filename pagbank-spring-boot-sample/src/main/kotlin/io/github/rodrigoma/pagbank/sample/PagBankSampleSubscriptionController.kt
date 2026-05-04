package io.github.rodrigoma.pagbank.sample

import io.github.rodrigoma.pagbank.model.invoice.InvoiceStatus
import io.github.rodrigoma.pagbank.model.subscription.CreateSubscriptionRequest
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionCustomerRef
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionInvoiceListResponse
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionListResponse
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionPaymentMethod
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionPlanRef
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionResponse
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionStatus
import io.github.rodrigoma.pagbank.model.subscription.UpdateSubscriptionRequest
import io.github.rodrigoma.pagbank.model.plan.PaymentMethod
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionCard
import io.github.rodrigoma.pagbank.service.PagBankSubscriptionService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sample/subscriptions")
class PagBankSampleSubscriptionController(
    private val subscriptionService: PagBankSubscriptionService,
) {
    @PostMapping("/demo")
    fun createDemoSubscription(): SubscriptionResponse =
        subscriptionService.create(
            CreateSubscriptionRequest(
                plan = SubscriptionPlanRef(id = "PLAN_D0468208-3528-4FC9-B640-3C38C12F58AA"),
                customer = SubscriptionCustomerRef(id = "CUST_DB860896-D21D-43F0-B299-60E912BE353C"),
                paymentMethod = listOf(
                    SubscriptionPaymentMethod(
                        type = PaymentMethod.CREDIT_CARD,
                        card = SubscriptionCard(token = "CARD_F07EF61F-4425-4FCA-87DE-EDF1F33B912C", securityCode = "123"),
                    ),
                ),
                referenceId = "demo-subscription-001",
            ),
        )

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

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}/cancel")
    fun cancelSubscription(
        @PathVariable id: String,
    ) = subscriptionService.cancel(id)

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}/suspend")
    fun suspendSubscription(
        @PathVariable id: String,
    ) = subscriptionService.suspend(id)

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}/activate")
    fun activateSubscription(
        @PathVariable id: String,
    ) = subscriptionService.activate(id)

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}/retry")
    fun retrySubscription(
        @PathVariable id: String,
    ) = subscriptionService.retry(id)

    @ResponseStatus(HttpStatus.NO_CONTENT)
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
