package io.github.rodrigoma.pagbank.model.common

/**
 * Shared pagination parameters accepted by all PagBank list() endpoints.
 *
 * @param limit  Maximum number of results to return. PagBank default: 10.
 * @param offset Zero-based index of the first result to return. PagBank default: 0.
 */
data class ListParams(
    val limit: Int = 10,
    val offset: Int = 0
)
