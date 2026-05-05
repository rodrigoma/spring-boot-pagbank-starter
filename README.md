# PagBank Spring Boot Starter

[![CI](https://github.com/rodrigoma/spring-boot-pagbank-starter/actions/workflows/ci.yml/badge.svg)](https://github.com/rodrigoma/spring-boot-pagbank-starter/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.rodrigoma/pagbank-spring-boot-starter.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.rodrigoma/pagbank-spring-boot-starter)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A Spring Boot auto-configuration library for the **PagBank Subscriptions API** (formerly PagSeguro). Drop it on the classpath and get 9 fully configured service beans — no boilerplate required.

## Compatibility

| Library version | Spring Boot | Java | Kotlin |
|---|---|---|---|
| 1.x | 4.0+ | 21+ | 2.1+ |

## Requirements

| Dependency   | Minimum version |
|--------------|-----------------|
| Java         | 21              |
| Spring Boot  | 4.0.0           |
| Kotlin       | 2.1 (optional)  |

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.rodrigoma:pagbank-spring-boot-starter:1.0.0")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'io.github.rodrigoma:pagbank-spring-boot-starter:1.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.rodrigoma</groupId>
    <artifactId>pagbank-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Configuration

Add the following to your `application.yml`:

```yaml
pagbank:
  # Required — your PagBank API token
  token: your-pagbank-api-token-here

  # Optional — SANDBOX (default) or PRODUCTION
  environment: SANDBOX

  # Optional — expose a /actuator/health/pagBank endpoint (default: false)
  health-indicator-enabled: false
```

| Property                           | Type      | Default   | Required | Description                                           |
|------------------------------------|-----------|-----------|----------|-------------------------------------------------------|
| `pagbank.token`                    | `String`  | —         | Yes      | API token from your PagBank dashboard                 |
| `pagbank.environment`              | `Enum`    | `SANDBOX` | No       | Target environment: `SANDBOX` or `PRODUCTION`         |
| `pagbank.health-indicator-enabled` | `Boolean` | `false`   | No       | Enables Spring Boot Actuator health check for PagBank |

### Environments

| Value        | Base URL                                           |
|--------------|----------------------------------------------------|
| `SANDBOX`    | `https://sandbox.api.assinaturas.pagseguro.com` |
| `PRODUCTION` | `https://api.assinaturas.pagseguro.com`         |

## Auto-configured Beans

Once the starter is on the classpath and `pagbank.token` is set, the following beans are automatically registered:

| Bean name                    | Type                         |
|------------------------------|------------------------------|
| `pagBankPlanService`         | `PagBankPlanService`         |
| `pagBankCustomerService`     | `PagBankCustomerService`     |
| `pagBankSubscriptionService` | `PagBankSubscriptionService` |
| `pagBankCouponService`       | `PagBankCouponService`       |
| `pagBankInvoiceService`      | `PagBankInvoiceService`      |
| `pagBankPaymentService`      | `PagBankPaymentService`      |
| `pagBankRefundService`       | `PagBankRefundService`       |
| `pagBankPreferenceService`   | `PagBankPreferenceService`   |
| `pagBankWebhookParser`       | `PagBankWebhookParser`       |

All services use a dedicated `RestClient` bean named `pagBankRestClient`. The client is pre-configured with:
- `Authorization: Bearer <token>` on every request
- Snake_case JSON serialization/deserialization (no global `ObjectMapper` side effects)
- Structured error handling that throws `PagBankException` on 4xx/5xx responses

## Usage Examples

### Managing Plans

```kotlin
@Service
class MyPlanSetupService(private val planService: PagBankPlanService) {

    fun createMonthlyPlan(): PlanResponse {
        val request = CreatePlanRequest(
            name = "Monthly Basic",
            amount = Money(value = 2990),          // R$ 29,90 in cents
            interval = PlanInterval(length = 1, unit = "month"),
            paymentMethod = listOf("CARD")
        )
        return planService.create(request)
    }

    fun listActivePlans(): PlanListResponse {
        return planService.list()
    }

    fun inactivatePlan(planId: String) {
        planService.inactivate(planId)
    }
}
```

### Managing Subscriptions

```kotlin
@Service
class MySubscriptionService(private val subscriptionService: PagBankSubscriptionService) {

    fun subscribe(planId: String, customerId: String): SubscriptionResponse {
        val request = CreateSubscriptionRequest(
            planId = planId,
            customerId = customerId
        )
        return subscriptionService.create(request)
    }

    fun cancelSubscription(subscriptionId: String) {
        subscriptionService.cancel(subscriptionId)
    }

    fun listSubscriptions(): SubscriptionListResponse {
        return subscriptionService.list()
    }
}
```

### Webhook Parsing

Parse incoming webhook events with `PagBankWebhookParser`:

```kotlin
@RestController
@RequestMapping("/webhooks")
class WebhookController(private val webhookParser: PagBankWebhookParser) {

    @PostMapping("/pagbank")
    fun handleWebhook(@RequestBody rawBody: String): ResponseEntity<Void> {
        val payload = webhookParser.parse(rawBody)

        when (payload.event) {
            WebhookEventType.SUBSCRIPTION_RECURRENCE -> {
                // handle recurring charge
            }
            WebhookEventType.SUBSCRIPTION_CANCELED -> {
                // handle cancellation
            }
            WebhookEventType.PAYMENT_REFUND_ACTIVATED -> {
                // handle refund
            }
            else -> { /* log and ignore */ }
        }

        return ResponseEntity.ok().build()
    }
}
```

### Error Handling

Service calls throw `PagBankException` on API errors. You can catch it to inspect the HTTP status and error details:

```kotlin
try {
    val plan = planService.get("nonexistent-plan-id")
} catch (e: PagBankException.NotFound) {
    println("Plan not found")
} catch (e: PagBankException.ValidationError) {
    println("HTTP ${e.httpStatus}: ${e.errors.joinToString { it.description }}")
} catch (e: PagBankException.ServerError) {
    println("Server error: ${e.statusCode}")
}
```

## Health Indicator

When `pagbank.health-indicator-enabled=true` and Spring Boot Actuator is on the classpath, a `/actuator/health/pagBank` endpoint is exposed. It performs a lightweight connectivity check against the configured environment.

```yaml
pagbank:
  health-indicator-enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health
```

## License

Copyright 2026 Rodrigo Montanha

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for the full license text.
