package io.github.rodrigoma.pagbank.service

import java.security.MessageDigest
import java.util.HexFormat

/**
 * Verifies the `x-authenticity-token` header PagBank sends with webhook notifications.
 *
 * The expected value is `hex(SHA-256(token + "-" + rawBody))`, where `rawBody` is the request body
 * **exactly as received** — any re-formatting (extra whitespace, re-serialization) changes the hash.
 * That is why the API takes a [ByteArray]: read the body as bytes, not as a parsed object.
 *
 * Comparison is done in constant time via [MessageDigest.isEqual].
 *
 * Reference: https://developer.pagbank.com.br/reference/confirmar-autenticidade-da-notificacao
 */
class PagBankWebhookVerifier(
    private val token: String,
) {
    /**
     * Returns `true` when [authenticityToken] matches the signature of [rawBody].
     * A `null`, blank or non-hex header yields `false` — never an exception.
     */
    fun verify(
        rawBody: ByteArray,
        authenticityToken: String?,
    ): Boolean {
        val provided = authenticityToken?.takeIf { it.isNotBlank() }?.let(::decodeHex) ?: return false
        return MessageDigest.isEqual(expectedDigest(rawBody), provided)
    }

    private fun decodeHex(value: String): ByteArray? = runCatching { HexFormat.of().parseHex(value.trim()) }.getOrNull()

    private fun expectedDigest(rawBody: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").run {
            update(token.toByteArray())
            update('-'.code.toByte())
            update(rawBody)
            digest()
        }

    /** Convenience overload; prefer the [ByteArray] variant so the payload is hashed exactly as received. */
    fun verify(
        rawBody: String,
        authenticityToken: String?,
    ): Boolean = verify(rawBody.toByteArray(), authenticityToken)
}
