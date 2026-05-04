package io.github.rodrigoma.pagbank.sample

import io.github.rodrigoma.pagbank.model.invoice.InvoiceResponse
import io.github.rodrigoma.pagbank.model.payment.PaymentListResponse
import io.github.rodrigoma.pagbank.service.PagBankInvoiceService
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.LIMIT
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.OFFSET
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sample/invoices")
class PagBankSampleInvoiceController(
    private val invoiceService: PagBankInvoiceService,
) {
    @GetMapping("/{id}")
    fun getInvoice(
        @PathVariable id: String,
    ): InvoiceResponse = invoiceService.get(id)

    @GetMapping("/{id}/payments")
    fun listInvoicePayments(
        @PathVariable id: String,
        @RequestParam(name = OFFSET, required = false) offset: Int = 0,
        @RequestParam(name = LIMIT, required = false) limit: Int = 100,
    ): PaymentListResponse = invoiceService.listPayments(id, offset, limit)
}
