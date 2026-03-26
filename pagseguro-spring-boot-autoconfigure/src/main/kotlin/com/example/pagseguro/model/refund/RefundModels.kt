package com.example.pagseguro.model.refund

data class RefundRequest(val amount: Int? = null)

data class RefundResponse(
    val id: String,
    val paymentId: String,
    val amount: Int,
    val status: String,
    val createdAt: String
)
