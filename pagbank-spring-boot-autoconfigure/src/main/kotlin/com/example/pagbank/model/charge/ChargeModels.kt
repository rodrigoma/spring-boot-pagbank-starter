package com.example.pagbank.model.charge

data class ChargeResponse(
    val id: String,
    val subscriptionId: String,
    val amount: Int,
    val status: String
)
data class ChargeListResponse(val charges: List<ChargeResponse>)
data class RetryChargeRequest(val paymentMethod: String? = null)
