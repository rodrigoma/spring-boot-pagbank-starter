package io.github.rodrigoma.pagbank.sample

import io.github.rodrigoma.pagbank.model.coupon.CouponListResponse
import io.github.rodrigoma.pagbank.model.coupon.CouponResponse
import io.github.rodrigoma.pagbank.model.coupon.CreateCouponRequest
import io.github.rodrigoma.pagbank.model.coupon.Discount
import io.github.rodrigoma.pagbank.model.coupon.DiscountType
import io.github.rodrigoma.pagbank.model.coupon.Duration
import io.github.rodrigoma.pagbank.model.coupon.DurationType
import io.github.rodrigoma.pagbank.model.plan.CreatePlanRequest
import io.github.rodrigoma.pagbank.model.plan.IntervalUnit
import io.github.rodrigoma.pagbank.model.plan.Money
import io.github.rodrigoma.pagbank.model.plan.PlanInterval
import io.github.rodrigoma.pagbank.model.plan.PlanListResponse
import io.github.rodrigoma.pagbank.model.plan.PlanResponse
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionResponse
import io.github.rodrigoma.pagbank.service.PagBankCouponService
import io.github.rodrigoma.pagbank.service.PagBankPlanService
import io.github.rodrigoma.pagbank.service.PagBankSubscriptionService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sample")
class PagBankSampleController(
    private val planService: PagBankPlanService,
    private val subscriptionService: PagBankSubscriptionService,
    private val couponService: PagBankCouponService,
) {
    // --- Plans ---

    @GetMapping("/plans")
    fun listPlans(): PlanListResponse = planService.list()

    @GetMapping("/plans/{id}")
    fun getPlan(
        @PathVariable id: String,
    ): PlanResponse = planService.get(id)

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
    fun listCoupons(): CouponListResponse = couponService.list()

    @GetMapping("/coupons/{id}")
    fun getCoupon(
        @PathVariable id: String,
    ): CouponResponse = couponService.get(id)

    @PutMapping("/coupons/{id}/inactivate")
    fun inactivateCoupon(
        @PathVariable id: String,
    ): CouponResponse = couponService.inactivate(id)

    @PutMapping("/coupons/{id}/activate")
    fun activateCoupon(
        @PathVariable id: String,
    ): CouponResponse = couponService.activate(id)

    @PostMapping("/subscriptions/{subscriptionId}/coupons/{couponId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun applyCouponToSubscription(
        @PathVariable subscriptionId: String,
        @PathVariable couponId: String,
    ) = couponService.applyToSubscription(subscriptionId, couponId)
}
