package io.github.rodrigoma.pagbank.sample

import io.github.rodrigoma.pagbank.model.plan.CreatePlanRequest
import io.github.rodrigoma.pagbank.model.plan.IntervalUnit
import io.github.rodrigoma.pagbank.model.plan.Money
import io.github.rodrigoma.pagbank.model.plan.PlanInterval
import io.github.rodrigoma.pagbank.model.plan.PlanListResponse
import io.github.rodrigoma.pagbank.model.plan.PlanResponse
import io.github.rodrigoma.pagbank.model.plan.PlanStatus
import io.github.rodrigoma.pagbank.model.plan.UpdatePlanRequest
import io.github.rodrigoma.pagbank.service.PagBankPlanService
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.LIMIT
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.OFFSET
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.REFERENCE_ID
import io.github.rodrigoma.pagbank.service.PagBankQueryParams.STATUS
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sample/plans")
class PagBankSamplePlanController(
    private val planService: PagBankPlanService,
) {
    @GetMapping
    fun listPlans(
        @RequestParam(name = OFFSET, required = false) offset: Int = 0,
        @RequestParam(name = LIMIT, required = false) limit: Int = 100,
        @RequestParam(name = REFERENCE_ID, required = false) referenceId: String?,
        @RequestParam(name = STATUS, required = false) status: PlanStatus?,
    ): PlanListResponse = planService.list(offset, limit, referenceId, status)

    @GetMapping("/{id}")
    fun getPlan(
        @PathVariable id: String,
    ): PlanResponse = planService.get(id)

    @PutMapping("/{id}")
    fun updatePlan(
        @PathVariable id: String,
        @RequestBody request: UpdatePlanRequest,
    ): PlanResponse = planService.update(id, request)

    @PostMapping("/demo")
    fun createDemoPlan(): PlanResponse =
        planService.create(
            CreatePlanRequest(
                name = "Demo Monthly Plan",
                amount = Money(value = 1990),
                interval = PlanInterval(length = 1, unit = IntervalUnit.MONTH),
                description = "A demo plan created via the sample app",
            ),
        )

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}/activate")
    fun activatePlan(
        @PathVariable id: String,
    ) = planService.activate(id)

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}/inactivate")
    fun inactivatePlan(
        @PathVariable id: String,
    ) = planService.inactivate(id)
}
