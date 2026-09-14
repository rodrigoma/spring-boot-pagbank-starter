package io.github.rodrigoma.pagbank.model.customer

import io.github.rodrigoma.pagbank.http.PagBankBodyMasker
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.databind.PropertyNamingStrategies.SNAKE_CASE
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.readValue

class CardRequestTest {
    private val mapper = jacksonMapperBuilder().propertyNamingStrategy(SNAKE_CASE).build()

    @Test
    fun `Encrypted with security code serializes both fields and nothing else`() {
        val json = mapper.writeValueAsString(CardRequest.Encrypted(encrypted = "blob", securityCode = "123"))
        assertThat(json).isEqualTo("""{"encrypted":"blob","security_code":"123"}""")
    }

    @Test
    fun `Encrypted without security code omits the key`() {
        val json = mapper.writeValueAsString(CardRequest.Encrypted(encrypted = "blob"))
        assertThat(json).isEqualTo("""{"encrypted":"blob"}""")
    }

    @Test
    fun `Plain serializes all card fields`() {
        val card =
            CardRequest.Plain(
                number = "4111111111111111",
                expYear = "2043",
                expMonth = "12",
                holder = CardHolder(name = "Maria Silva"),
                securityCode = "123",
            )
        val json = mapper.writeValueAsString(card)
        assertThat(json).isEqualTo(
            """{"number":"4111111111111111","exp_year":"2043","exp_month":"12",""" +
                """"security_code":"123","holder":{"name":"Maria Silva"}}""",
        )
    }

    @Test
    fun `Encrypted deserializes with and without security code`() {
        assertThat(mapper.readValue<CardRequest>("""{"encrypted":"blob","security_code":"123"}"""))
            .isEqualTo(CardRequest.Encrypted("blob", "123"))
        assertThat(mapper.readValue<CardRequest>("""{"encrypted":"blob"}"""))
            .isEqualTo(CardRequest.Encrypted("blob"))
    }

    @Test
    fun `toString never prints card secrets`() {
        val encrypted = CardRequest.Encrypted(encrypted = "blob", securityCode = "123").toString()
        assertThat(encrypted).doesNotContain("123").contains("securityCode=***")

        val plain =
            CardRequest
                .Plain(
                    number = "4111111111111111",
                    expYear = "2043",
                    expMonth = "12",
                    holder = CardHolder(name = "Maria Silva"),
                    securityCode = "123",
                ).toString()
        assertThat(plain).doesNotContain("4111111111111111").doesNotContain("123").contains("Maria Silva")
    }

    @Test
    fun `request log masking hides the encrypted card security code`() {
        val body = mapper.writeValueAsBytes(mapOf("card" to CardRequest.Encrypted("blob", "123")))
        assertThat(PagBankBodyMasker.mask(body)).isEqualTo("""{"card":{"encrypted":"***","security_code":"***"}}""")
    }
}
