package io.github.rodrigoma.pagbank.model.coupon

enum class DiscountType { PERCENT, AMOUNT }

data class CreateCouponRequest(
    val code: String,
    val discountType: DiscountType,
    val discountValue: Int,
    val maxUses: Int? = null
)

data class CouponResponse(
    val id: String,
    val code: String,
    val discountType: DiscountType,
    val discountValue: Int
)
