package io.github.rodrigoma.pagbank.model.coupon

import io.github.rodrigoma.pagbank.model.common.PagBankLink

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

data class CreateCouponRequest(
    val name: String,
    val discount: Discount,
    val duration: Duration,
    val status: CouponStatus? = null,
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
    val links: List<PagBankLink>? = null,
)

data class CouponResultSet(
    val total: Int,
    val status: List<String> = emptyList(),
)

data class CouponListResponse(
    val resultSet: CouponResultSet,
    val coupons: List<CouponResponse>,
)
