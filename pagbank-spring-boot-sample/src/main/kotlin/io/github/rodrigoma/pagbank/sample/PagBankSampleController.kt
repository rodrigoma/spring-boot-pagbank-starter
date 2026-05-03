package io.github.rodrigoma.pagbank.sample

import io.github.rodrigoma.pagbank.model.plan.CreatePlanRequest
import io.github.rodrigoma.pagbank.model.plan.IntervalUnit
import io.github.rodrigoma.pagbank.model.plan.Money
import io.github.rodrigoma.pagbank.model.plan.PlanInterval
import io.github.rodrigoma.pagbank.model.plan.PlanListResponse
import io.github.rodrigoma.pagbank.model.plan.PlanResponse
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionResponse
import io.github.rodrigoma.pagbank.service.PagBankPlanService
import io.github.rodrigoma.pagbank.service.PagBankSubscriptionService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sample")
class PagBankSampleController(
    private val planService: PagBankPlanService,
    private val subscriptionService: PagBankSubscriptionService,
) {
    @GetMapping("/plans")
    fun listPlans(): PlanListResponse = planService.list()

    @GetMapping("/plans/{id}")
    fun getPlan(
        @PathVariable id: String,
    ): PlanResponse = planService.get(id)

    @PostMapping("/plans/demo")
    fun createDemoPlan(): PlanResponse =
        planService.create(
            CreatePlanRequest(
                name = "Demo Monthly Plan",
                amount = Money(value = 1990),
                interval = PlanInterval(length = 1, unit = IntervalUnit.MONTH),
                description = "A demo plan created via the sample app",
            ),
        )

    @GetMapping("/subscriptions/{id}")
    fun getSubscription(
        @PathVariable id: String,
    ): SubscriptionResponse = subscriptionService.get(id)
}
