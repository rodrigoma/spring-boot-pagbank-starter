package io.github.rodrigoma.pagbank.sample

import io.github.rodrigoma.pagbank.model.refund.RefundListResponse
import io.github.rodrigoma.pagbank.model.refund.RefundResponse
import io.github.rodrigoma.pagbank.service.PagBankRefundService
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.LIMIT
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.OFFSET
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sample/refunds")
class PagBankSampleRefundController(
    private val refundService: PagBankRefundService,
) {
    @GetMapping("/{id}")
    fun getRefund(
        @PathVariable id: String,
    ): RefundResponse = refundService.get(id)

    @GetMapping
    fun listRefunds(
        @RequestParam(name = OFFSET, required = false) offset: Int = 0,
        @RequestParam(name = LIMIT, required = false) limit: Int = 100,
    ): RefundListResponse = refundService.list(offset, limit)
}
