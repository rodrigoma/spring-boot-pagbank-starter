package io.github.rodrigoma.pagbank.model.customer

enum class BillingInfoType { CREDIT_CARD }

data class CustomerPhone(
    val area: String,
    val country: String,
    val number: String,
    val id: Int? = null,
)

data class CustomerAddress(
    val street: String,
    val number: String,
    val locality: String,
    val city: String,
    val regionCode: String,
    val country: String,
    val postalCode: String,
    val complement: String? = null,
)

data class CardHolder(
    val name: String,
)

data class Card(
    val encrypted: String? = null,
    val token: String? = null,
    val number: String? = null,
    val securityCode: String? = null,
    val expYear: String? = null,
    val expMonth: String? = null,
    val holder: CardHolder? = null,
    val brand: String? = null,
    val firstDigits: String? = null,
    val lastDigits: String? = null,
)

data class BillingInfo(
    val type: BillingInfoType,
    val card: Card? = null,
)

data class CustomerLink(
    val rel: String? = null,
    val href: String? = null,
    val media: String? = null,
    val type: String? = null,
)

data class CreateCustomerRequest(
    val name: String,
    val email: String,
    val taxId: String,
    val referenceId: String? = null,
    val birthDate: String? = null,
    val phones: List<CustomerPhone>? = null,
    val address: CustomerAddress? = null,
    val billingInfo: List<BillingInfo>? = null,
)

data class UpdateCustomerRequest(
    val name: String? = null,
    val email: String? = null,
    val referenceId: String? = null,
    val birthDate: String? = null,
    val phones: List<CustomerPhone>? = null,
    val address: CustomerAddress? = null,
)

data class CustomerResponse(
    val id: String,
    val name: String,
    val email: String,
    val taxId: String,
    val referenceId: String? = null,
    val birthDate: String? = null,
    val phones: List<CustomerPhone>? = null,
    val address: CustomerAddress? = null,
    val billingInfo: List<BillingInfo>? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val links: List<CustomerLink>? = null,
)

data class CustomerResultSet(
    val total: Int,
    val offset: Int,
    val limit: Int,
)

data class CustomerListResponse(
    val resultSet: CustomerResultSet,
    val customers: List<CustomerResponse>,
)
