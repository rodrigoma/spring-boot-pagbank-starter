package io.github.rodrigoma.pagbank.sample

import io.github.rodrigoma.pagbank.model.coupon.CouponListResponse
import io.github.rodrigoma.pagbank.model.coupon.CouponResponse
import io.github.rodrigoma.pagbank.model.coupon.CouponStatus
import io.github.rodrigoma.pagbank.model.coupon.CreateCouponRequest
import io.github.rodrigoma.pagbank.model.coupon.Discount
import io.github.rodrigoma.pagbank.model.coupon.DiscountType
import io.github.rodrigoma.pagbank.model.coupon.Duration
import io.github.rodrigoma.pagbank.model.coupon.DurationType
import io.github.rodrigoma.pagbank.service.PagBankCouponService
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.LIMIT
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.OFFSET
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.REFERENCE_ID
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.STATUS
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

@RestController
@RequestMapping("/sample/coupons")
class PagBankSampleCouponController(
    private val couponService: PagBankCouponService,
) {
    @PostMapping
    fun createCoupon(
        @RequestBody request: CreateCouponRequest,
    ): CouponResponse = couponService.create(request)

    @PostMapping("/demo")
    fun createDemoCoupon(): CouponResponse =
        couponService.create(
            CreateCouponRequest(
                name = "DEMO10",
                discount = Discount(value = 10, type = DiscountType.PERCENT),
                duration = Duration(type = DurationType.FOREVER),
                description = "Demo 10% discount coupon created via sample app",
            ),
        )

    @GetMapping
    fun listCoupons(
        @RequestParam(name = OFFSET, required = false) offset: Int = 0,
        @RequestParam(name = LIMIT, required = false) limit: Int = 100,
        @RequestParam(name = REFERENCE_ID, required = false) referenceId: String?,
        @RequestParam(name = STATUS, required = false) status: CouponStatus?,
    ): CouponListResponse = couponService.list(offset, limit, referenceId, status)

    @GetMapping("/{id}")
    fun getCoupon(
        @PathVariable id: String,
    ): CouponResponse = couponService.get(id)

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}/inactivate")
    fun inactivateCoupon(
        @PathVariable id: String,
    ) = couponService.inactivate(id)

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}/activate")
    fun activateCoupon(
        @PathVariable id: String,
    ) = couponService.activate(id)
}
