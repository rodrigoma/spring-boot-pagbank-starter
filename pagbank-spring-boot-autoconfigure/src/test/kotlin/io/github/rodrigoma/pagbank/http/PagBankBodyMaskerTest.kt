package io.github.rodrigoma.pagbank.http

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PagBankBodyMaskerTest {
    private val body =
        """
        {
          "name": "Ana Souza",
          "email": "ana@example.com",
          "tax_id": "12345678909",
          "phones": [{"country": "55", "area": "11", "number": "999998888"}],
          "billing_info": [
            {"type": "CREDIT_CARD", "card": {"number": "4111111111111111", "exp_month": "12", "exp_year": "2030",
              "security_code": "123", "holder": {"name": "ANA SOUZA"}, "token": "tok_abc", "encrypted": "blob"}}
          ],
          "plan": {"name": "Monthly Basic"}
        }
        """.trimIndent().toByteArray()

    @Test
    fun `mask should hide sensitive fields and keep the rest`() {
        val masked = PagBankBodyMasker.mask(body)

        assertThat(masked)
            .doesNotContain("ana@example.com")
            .doesNotContain("12345678909")
            .doesNotContain("999998888")
            .doesNotContain("4111111111111111")
            .doesNotContain("\"123\"")
            .doesNotContain("ANA SOUZA")
            .doesNotContain("tok_abc")
            .doesNotContain("blob")
            .contains("\"exp_month\":\"12\"")
            .contains("\"Monthly Basic\"")
            .contains("\"Ana Souza\"")
    }

    @Test
    fun `mask should keep the last four digits of tax_id`() {
        assertThat(PagBankBodyMasker.mask(body)).contains("\"tax_id\":\"***8909\"")
    }

    @Test
    fun `mask should replace other sensitive values with asterisks`() {
        assertThat(PagBankBodyMasker.mask(body)).contains("\"email\":\"***\"").contains("\"number\":\"***\"")
    }

    @Test
    fun `mask should report only the size when the body is not JSON`() {
        assertThat(PagBankBodyMasker.mask("<html>oops</html>".toByteArray())).isEqualTo("<17 bytes, not JSON>")
    }

    @Test
    fun `mask should handle empty body`() {
        assertThat(PagBankBodyMasker.mask(ByteArray(0))).isEqualTo("")
    }
}
