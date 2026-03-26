package com.example.pagseguro.service

import com.example.pagseguro.model.coupon.*
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagSeguroCouponService(private val restClient: RestClient) {

    fun create(request: CreateCouponRequest): CouponResponse =
        restClient.post()
            .uri("/coupons")
            .body(request)
            .retrieve()
            .body<CouponResponse>()!!

    fun get(id: String): CouponResponse =
        restClient.get()
            .uri("/coupons/{id}", id)
            .retrieve()
            .body<CouponResponse>()!!

    fun delete(id: String) {
        restClient.delete()
            .uri("/coupons/{id}", id)
            .retrieve()
            .toBodilessEntity()
    }

    fun applyToSubscription(subscriptionId: String, couponId: String) {
        restClient.post()
            .uri("/subscriptions/{id}/coupons", subscriptionId)
            .body(mapOf("couponId" to couponId))
            .retrieve()
            .toBodilessEntity()
    }
}
