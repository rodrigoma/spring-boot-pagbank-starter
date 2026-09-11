package io.github.rodrigoma.pagbank.http

import tools.jackson.core.JacksonException
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.ObjectNode

/**
 * Renders an HTTP body for logging with personal and card data replaced by `***`.
 *
 * PagBank payloads carry CPF/CNPJ, e-mail, phone numbers and card data (including plain card numbers when
 * `CardRequest.Plain` is used). Logging them verbatim ships PII and cardholder data to whatever log sink the
 * application uses, so every value under a sensitive key is masked. `tax_id` keeps its last four digits to
 * allow correlating an incident with a customer; everything else is fully hidden. Non-JSON bodies are
 * reported by size only.
 */
object PagBankBodyMasker {
    private const val MASK = "***"
    private const val TAX_ID_VISIBLE_DIGITS = 4
    private val fullyMasked = setOf("number", "security_code", "encrypted", "token", "email")
    private const val TAX_ID = "tax_id"
    private const val HOLDER = "holder"
    private const val NAME = "name"

    private val mapper: JsonMapper = JsonMapper.builder().build()

    fun mask(body: ByteArray): String =
        when {
            body.isEmpty() -> ""
            else ->
                parse(body)?.let { root ->
                    maskNode(root, parentKey = null)
                    mapper.writeValueAsString(root)
                } ?: "<${body.size} bytes, not JSON>"
        }

    private fun parse(body: ByteArray): JsonNode? =
        try {
            mapper.readTree(body)
        } catch (_: JacksonException) {
            null
        }

    private fun maskNode(
        node: JsonNode,
        parentKey: String?,
    ) {
        when {
            node.isObject -> maskObject(node as ObjectNode, parentKey)
            node.isArray -> node.values().forEach { maskNode(it, parentKey) }
        }
    }

    private fun maskObject(
        node: ObjectNode,
        parentKey: String?,
    ) {
        node.propertyNames().toList().forEach { key ->
            val child = node.get(key)
            when {
                key == TAX_ID && child.isValueNode -> node.put(key, maskTaxId(child.asString()))
                key in fullyMasked && child.isValueNode -> node.put(key, MASK)
                key == NAME && parentKey == HOLDER && child.isValueNode -> node.put(key, MASK)
                else -> maskNode(child, key)
            }
        }
    }

    private fun maskTaxId(value: String): String =
        if (value.length > TAX_ID_VISIBLE_DIGITS) MASK + value.takeLast(TAX_ID_VISIBLE_DIGITS) else MASK
}
