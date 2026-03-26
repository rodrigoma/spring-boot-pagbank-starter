package com.example.pagbank.model.customer

data class CreateCustomerRequest(
    val name: String,
    val email: String,
    val taxId: String,
    val phone: String? = null
)

data class CustomerResponse(
    val id: String,
    val name: String,
    val email: String,
    val taxId: String,
    val createdAt: String
)

data class CustomerListResponse(val customers: List<CustomerResponse>)
