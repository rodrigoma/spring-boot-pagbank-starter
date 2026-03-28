package io.github.rodrigoma.pagbank.model.coupon

enum class DiscountType { PERCENT, AMOUNT }

enum class CouponStatus { ACTIVE, INACTIVE }

enum class DurationType { ONCE, REPEATING, FOREVER }

data class Discount(
    val value: Int,
    val type: DiscountType,
)

data class Duration(
    val type: DurationType,
    val occurrences: Int? = null,
)

data class CouponLink(
    val rel: String,
    val href: String,
    val media: String? = null,
    val type: String? = null,
)

data class CreateCouponRequest(
    val name: String,
    val discount: Discount,
    val duration: Duration,
    val referenceId: String? = null,
    val description: String? = null,
    val redemptionLimit: Int? = null,
    val expAt: String? = null,
)

data class CouponResponse(
    val id: String,
    val name: String?,
    val discount: Discount,
    val duration: Duration,
    val status: CouponStatus,
    val referenceId: String? = null,
    val description: String? = null,
    val redemptionLimit: Int? = null,
    val expAt: String? = null,
    val inUse: Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val links: List<CouponLink>? = null,
)

data class CouponListResponse(
    val coupons: List<CouponResponse>,
)
