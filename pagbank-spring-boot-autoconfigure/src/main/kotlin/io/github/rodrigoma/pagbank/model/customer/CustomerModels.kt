package io.github.rodrigoma.pagbank.model.customer

import tools.jackson.core.JsonGenerator
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JsonNode
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.deser.std.StdDeserializer
import tools.jackson.databind.ser.std.StdSerializer

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

@JsonSerialize(using = CardRequestSerializer::class)
@JsonDeserialize(using = CardRequestDeserializer::class)
sealed class CardRequest {
    data class Plain(
        val number: String,
        val expYear: String,
        val expMonth: String,
        val holder: CardHolder,
        val securityCode: String,
    ) : CardRequest()

    data class Encrypted(
        val encrypted: String,
    ) : CardRequest()
}

class CardRequestSerializer : StdSerializer<CardRequest>(CardRequest::class.java) {
    override fun serialize(
        value: CardRequest,
        gen: JsonGenerator,
        ctxt: SerializationContext,
    ) {
        gen.writeStartObject()
        when (value) {
            is CardRequest.Plain -> {
                gen.writeStringProperty("number", value.number)
                gen.writeStringProperty("exp_year", value.expYear)
                gen.writeStringProperty("exp_month", value.expMonth)
                gen.writeStringProperty("security_code", value.securityCode)
                gen.writePOJOProperty("holder", value.holder)
            }
            is CardRequest.Encrypted -> gen.writeStringProperty("encrypted", value.encrypted)
        }
        gen.writeEndObject()
    }
}

class CardRequestDeserializer : StdDeserializer<CardRequest>(CardRequest::class.java) {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): CardRequest {
        val node = p.readValueAsTree<JsonNode>()
        return if (node.has("encrypted")) {
            CardRequest.Encrypted(
                encrypted = node.require("encrypted", ctxt).asString(),
            )
        } else {
            val holderNode = node.require("holder", ctxt)
            CardRequest.Plain(
                number = node.require("number", ctxt).asString(),
                expYear = node.require("exp_year", ctxt).asString(),
                expMonth = node.require("exp_month", ctxt).asString(),
                securityCode = node.require("security_code", ctxt).asString(),
                holder = CardHolder(name = holderNode.require("name", ctxt).asString()),
            )
        }
    }

    private fun JsonNode.require(
        field: String,
        ctxt: DeserializationContext,
    ): JsonNode =
        get(field)
            ?: ctxt.reportInputMismatch(CardRequest::class.java, "Missing required field: $field")
}

data class CardInfo(
    val token: String? = null,
    val brand: String? = null,
    val firstDigits: String? = null,
    val lastDigits: String? = null,
    val expYear: String? = null,
    val expMonth: String? = null,
    val holder: CardHolder? = null,
)

data class BillingInfoRequest(
    val type: BillingInfoType,
    val card: CardRequest? = null,
)

data class BillingInfo(
    val type: BillingInfoType,
    val card: CardInfo? = null,
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
    val billingInfo: List<BillingInfoRequest>? = null,
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
