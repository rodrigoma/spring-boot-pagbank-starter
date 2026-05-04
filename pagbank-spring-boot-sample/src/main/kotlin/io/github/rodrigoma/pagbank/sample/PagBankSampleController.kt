package io.github.rodrigoma.pagbank.sample

import io.github.rodrigoma.pagbank.model.coupon.CouponListResponse
import io.github.rodrigoma.pagbank.model.coupon.CouponResponse
import io.github.rodrigoma.pagbank.model.coupon.CouponStatus
import io.github.rodrigoma.pagbank.model.coupon.CreateCouponRequest
import io.github.rodrigoma.pagbank.model.coupon.Discount
import io.github.rodrigoma.pagbank.model.coupon.DiscountType
import io.github.rodrigoma.pagbank.model.coupon.Duration
import io.github.rodrigoma.pagbank.model.coupon.DurationType
import io.github.rodrigoma.pagbank.model.customer.BillingInfoRequest
import io.github.rodrigoma.pagbank.model.customer.CreateCustomerRequest
import io.github.rodrigoma.pagbank.model.customer.CustomerListResponse
import io.github.rodrigoma.pagbank.model.customer.CustomerResponse
import io.github.rodrigoma.pagbank.model.customer.UpdateCustomerRequest
import io.github.rodrigoma.pagbank.model.plan.CreatePlanRequest
import io.github.rodrigoma.pagbank.model.plan.IntervalUnit
import io.github.rodrigoma.pagbank.model.plan.Money
import io.github.rodrigoma.pagbank.model.plan.PlanInterval
import io.github.rodrigoma.pagbank.model.plan.PlanListResponse
import io.github.rodrigoma.pagbank.model.plan.PlanResponse
import io.github.rodrigoma.pagbank.model.plan.PlanStatus
import io.github.rodrigoma.pagbank.model.plan.UpdatePlanRequest
import io.github.rodrigoma.pagbank.model.preference.NotificationPreferences
import io.github.rodrigoma.pagbank.model.preference.PublicKeyResponse
import io.github.rodrigoma.pagbank.model.preference.RetryPreferences
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionResponse
import io.github.rodrigoma.pagbank.service.PagBankCouponService
import io.github.rodrigoma.pagbank.service.PagBankCustomerService
import io.github.rodrigoma.pagbank.service.PagBankPlanService
import io.github.rodrigoma.pagbank.service.PagBankPreferenceService
import io.github.rodrigoma.pagbank.service.PagBankSubscriptionService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Suppress("TooManyFunctions")
@RestController
@RequestMapping("/sample")
class PagBankSampleController(
    private val planService: PagBankPlanService,
    private val subscriptionService: PagBankSubscriptionService,
    private val couponService: PagBankCouponService,
    private val customerService: PagBankCustomerService,
    private val preferenceService: PagBankPreferenceService,
) {
    // --- Plans ---

    @GetMapping("/plans")
    fun listPlans(
        @RequestParam(required = false) offset: Int?,
        @RequestParam(required = false) limit: Int?,
        @RequestParam(name = "reference_id", required = false) referenceId: String?,
        @RequestParam(required = false) status: PlanStatus?,
    ): PlanListResponse = planService.list(offset, limit, referenceId, status)

    @GetMapping("/plans/{id}")
    fun getPlan(
        @PathVariable id: String,
    ): PlanResponse = planService.get(id)

    @PutMapping("/plans/{id}")
    fun updatePlan(
        @PathVariable id: String,
        @RequestBody request: UpdatePlanRequest,
    ): PlanResponse = planService.update(id, request)

    @PostMapping("/plans/demo")
    fun createDemoPlan(): PlanResponse =
        planService.create(
            CreatePlanRequest(
                name = "Demo Monthly Plan",
                amount = Money(value = 1990),
                interval = PlanInterval(length = 1, unit = IntervalUnit.MONTH),
                description = "A demo plan created via the sample app",
            ),
        )

    @PutMapping("/plans/{id}/activate")
    fun activatePlan(
        @PathVariable id: String,
    ) = planService.activate(id)

    @PutMapping("/plans/{id}/deactivate")
    fun deactivatePlan(
        @PathVariable id: String,
    ) = planService.deactivate(id)

    // --- Customers ---

    @PostMapping("/customers")
    fun createCustomer(
        @RequestBody request: CreateCustomerRequest,
    ): CustomerResponse = customerService.create(request)

    @GetMapping("/customers/{id}")
    fun getCustomer(
        @PathVariable id: String,
    ): CustomerResponse = customerService.get(id)

    @PutMapping("/customers/{id}")
    fun updateCustomer(
        @PathVariable id: String,
        @RequestBody request: UpdateCustomerRequest,
    ): CustomerResponse = customerService.update(id, request)

    @PutMapping("/customers/{id}/billing_info")
    fun updateCustomerBillingInfo(
        @PathVariable id: String,
        @RequestBody billingInfo: List<BillingInfoRequest>,
    ): CustomerResponse = customerService.updateBillingInfo(id, billingInfo)

    @GetMapping("/customers")
    fun listCustomers(
        @RequestParam(required = false) offset: Int?,
        @RequestParam(required = false) limit: Int?,
        @RequestParam(name = "reference_id", required = false) referenceId: String?,
    ): CustomerListResponse = customerService.list(offset, limit, referenceId)

    // --- Preferences (Merchant) ---

    @GetMapping("/preferences/notifications")
    fun getNotifications(): NotificationPreferences = preferenceService.getNotifications()

    @PutMapping("/preferences/notifications")
    fun updateNotifications(
        @RequestBody request: NotificationPreferences,
    ): NotificationPreferences = preferenceService.updateNotifications(request)

    @GetMapping("/preferences/retries")
    fun getRetries(): RetryPreferences = preferenceService.getRetries()

    @PutMapping("/preferences/retries")
    fun updateRetries(
        @RequestBody request: RetryPreferences,
    ): RetryPreferences = preferenceService.updateRetries(request)

    @GetMapping("/public-keys")
    fun getPublicKey(): PublicKeyResponse = preferenceService.getPublicKey()

    @PutMapping("/public-keys")
    fun rotatePublicKey(): PublicKeyResponse = preferenceService.rotatePublicKey()

    // --- Subscriptions ---

    @GetMapping("/subscriptions/{id}")
    fun getSubscription(
        @PathVariable id: String,
    ): SubscriptionResponse = subscriptionService.get(id)

    // --- Coupons ---

    @PostMapping("/coupons")
    fun createCoupon(
        @RequestBody request: CreateCouponRequest,
    ): CouponResponse = couponService.create(request)

    @PostMapping("/coupons/demo")
    fun createDemoCoupon(): CouponResponse =
        couponService.create(
            CreateCouponRequest(
                name = "DEMO10",
                discount = Discount(value = 10, type = DiscountType.PERCENT),
                duration = Duration(type = DurationType.FOREVER),
                description = "Demo 10% discount coupon created via sample app",
            ),
        )

    @GetMapping("/coupons")
    fun listCoupons(
        @RequestParam(required = false) offset: Int?,
        @RequestParam(required = false) limit: Int?,
        @RequestParam(name = "reference_id", required = false) referenceId: String?,
        @RequestParam(required = false) status: CouponStatus?,
    ): CouponListResponse = couponService.list(offset, limit, referenceId, status)

    @GetMapping("/coupons/{id}")
    fun getCoupon(
        @PathVariable id: String,
    ): CouponResponse = couponService.get(id)

    @PutMapping("/coupons/{id}/inactivate")
    fun inactivateCoupon(
        @PathVariable id: String,
    ) = couponService.inactivate(id)

    @PutMapping("/coupons/{id}/activate")
    fun activateCoupon(
        @PathVariable id: String,
    ) = couponService.activate(id)

    @PostMapping("/subscriptions/{subscriptionId}/coupons/{couponId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun applyCouponToSubscription(
        @PathVariable subscriptionId: String,
        @PathVariable couponId: String,
    ) = subscriptionService.applyCoupon(subscriptionId, couponId)
}
