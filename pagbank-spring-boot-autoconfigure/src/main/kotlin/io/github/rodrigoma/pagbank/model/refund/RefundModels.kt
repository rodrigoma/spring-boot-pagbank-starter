package io.github.rodrigoma.pagbank.model.refund

import io.github.rodrigoma.pagbank.model.common.Currency
import io.github.rodrigoma.pagbank.model.common.Currency.BRL
import io.github.rodrigoma.pagbank.model.common.PagBankLink

enum class RefundType { FULL, PARTIAL }

enum class RefundStatus { SUCCESS, FAILED }

data class RefundAmount(
    val value: Int,
    val currency: Currency = BRL,
)

data class RefundPayment(
    val id: String,
    val amount: RefundAmount? = null,
)

data class RefundRequest(
    val amount: RefundAmount,
)

data class RefundResultSet(
    val total: Int,
)

data class RefundResponse(
    val id: String,
    val amount: RefundAmount,
    val status: RefundStatus,
    val type: RefundType? = null,
    val payment: RefundPayment? = null,
    val createdAt: String? = null,
    val links: List<PagBankLink>? = null,
)

data class RefundListResponse(
    val resultSet: RefundResultSet? = null,
    val refunds: List<RefundResponse>,
)
