package io.github.rodrigoma.pagbank.service

import io.github.rodrigoma.pagbank.model.coupon.CouponListResponse
import io.github.rodrigoma.pagbank.model.coupon.CouponResponse
import io.github.rodrigoma.pagbank.model.coupon.CouponStatus
import io.github.rodrigoma.pagbank.model.coupon.CreateCouponRequest
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.LIMIT
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.OFFSET
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.REFERENCE_ID
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.STATUS
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

    fun list(
        offset: Int = 0,
        limit: Int = 100,
        referenceId: String? = null,
        status: CouponStatus? = null,
    ): CouponListResponse =
        restClient
            .get()
            .uri { builder ->
                builder.path("/coupons")
                offset.let { builder.queryParam(OFFSET, it) }
                limit.let { builder.queryParam(LIMIT, it) }
                referenceId?.let { builder.queryParam(REFERENCE_ID, it) }
                status?.let { builder.queryParam(STATUS, it.name) }
                builder.build()
            }.retrieve()
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
}
