package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.coupon.CouponListResponse
import io.github.rodrigoma.pagbank.model.coupon.CouponResponse
import io.github.rodrigoma.pagbank.model.coupon.CreateCouponRequest
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagBankCouponService(
    private val restClient: RestClient,
) {
    fun create(request: CreateCouponRequest): CouponResponse =
        restClient
            .post()
            .uri("/coupons")
            .body(request)
            .retrieve()
            .body<CouponResponse>()!!

    fun get(id: String): CouponResponse =
        restClient
            .get()
            .uri("/coupons/{id}", id)
            .retrieve()
            .body<CouponResponse>()!!

    fun list(): CouponListResponse =
        restClient
            .get()
            .uri("/coupons")
            .retrieve()
            .body<CouponListResponse>()!!

    fun inactivate(id: String) {
        restClient
            .put()
            .uri("/coupons/{id}/inactivate", id)
            .retrieve()
            .toBodilessEntity()
    }

    fun activate(id: String) {
        restClient
            .put()
            .uri("/coupons/{id}/activate", id)
            .retrieve()
            .toBodilessEntity()
    }

    fun applyToSubscription(
        subscriptionId: String,
        couponId: String,
    ) {
        restClient
            .post()
            .uri("/subscriptions/{id}/coupons", subscriptionId)
            .body(mapOf("couponId" to couponId))
            .retrieve()
            .toBodilessEntity()
    }
}
