package io.github.rodrigoma.pagbank.autoconfigure

import org.springframework.web.client.RestClient

/**
 * Callback applied to the `RestClient.Builder` used for `pagBankRestClient` right before it is built.
 *
 * Register one or more beans of this type to add timeouts, interceptors, a proxy-aware request factory,
 * extra headers, etc. without re-assembling the client yourself. Customizers run in bean order
 * (`@Order` / `Ordered` honoured) after the starter's own configuration, so they may override it.
 */
fun interface PagBankRestClientCustomizer {
    fun customize(builder: RestClient.Builder)
}
