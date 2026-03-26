package com.example.pagseguro.model.invoice

data class InvoiceResponse(
    val id: String,
    val subscriptionId: String,
    val amount: Int,
    val status: String,
    val dueDate: String
)
data class InvoiceListResponse(val invoices: List<InvoiceResponse>)
