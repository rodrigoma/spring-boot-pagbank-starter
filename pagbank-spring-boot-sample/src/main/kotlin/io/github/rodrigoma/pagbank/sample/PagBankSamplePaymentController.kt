package io.github.rodrigoma.pagbank.sample

import io.github.rodrigoma.pagbank.model.payment.PaymentListResponse
import io.github.rodrigoma.pagbank.model.payment.PaymentResponse
import io.github.rodrigoma.pagbank.model.payment.PaymentStatus
import io.github.rodrigoma.pagbank.model.refund.RefundListResponse
import io.github.rodrigoma.pagbank.model.refund.RefundRequest
import io.github.rodrigoma.pagbank.model.refund.RefundResponse
import io.github.rodrigoma.pagbank.service.PagBankPaymentService
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.CREATED_AT_END
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.CREATED_AT_START
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.LIMIT
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.OFFSET
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.PAYMENT_METHOD_TYPE
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.STATUS
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sample/payments")
class PagBankSamplePaymentController(
    private val paymentService: PagBankPaymentService,
) {
    @GetMapping("/{id}")
    fun getPayment(
        @PathVariable id: String,
    ): PaymentResponse = paymentService.get(id)

    @Suppress("LongParameterList")
    @GetMapping
    fun listPayments(
        @RequestParam(name = OFFSET, required = false) offset: Int = 0,
        @RequestParam(name = LIMIT, required = false) limit: Int = 100,
        @RequestParam(name = STATUS, required = false) status: PaymentStatus?,
        @RequestParam(name = CREATED_AT_START, required = false) createdAtStart: String?,
        @RequestParam(name = CREATED_AT_END, required = false) createdAtEnd: String?,
        @RequestParam(name = PAYMENT_METHOD_TYPE, required = false) paymentMethodType: String?,
        @RequestHeader(name = "q", required = false) q: String?,
    ): PaymentListResponse =
        paymentService.list(
            offset = offset,
            limit = limit,
            status = status,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            paymentMethodType = paymentMethodType,
            q = q,
        )

    @PostMapping("/{id}/refunds")
    fun createPaymentRefund(
        @PathVariable id: String,
        @RequestBody request: RefundRequest,
    ): RefundResponse = paymentService.createRefund(id, request)

    @GetMapping("/{id}/refunds")
    fun listPaymentRefunds(
        @PathVariable id: String,
        @RequestParam(name = OFFSET, required = false) offset: Int = 0,
        @RequestParam(name = LIMIT, required = false) limit: Int = 100,
    ): RefundListResponse = paymentService.listRefunds(id, offset, limit)
}
