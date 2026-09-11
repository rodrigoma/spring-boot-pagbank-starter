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
    implementation("io.github.rodrigoma:pagbank-spring-boot-starter:1.0.0-RC1")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'io.github.rodrigoma:pagbank-spring-boot-starter:1.0.0-RC1'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.rodrigoma</groupId>
    <artifactId>pagbank-spring-boot-starter</artifactId>
    <version>1.0.0-RC1</version>
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

  # Optional — log outgoing requests/responses at DEBUG, sensitive fields masked (default: false)
  log-requests: false

  webhook:
    # Optional — require a valid x-authenticity-token header on webhooks (default: false)
    verify-signature: false
```

| Property                           | Type      | Default   | Required | Description                                           |
|------------------------------------|-----------|-----------|----------|-------------------------------------------------------|
| `pagbank.token`                    | `String`  | —         | Yes      | API token from your PagBank dashboard                 |
| `pagbank.environment`              | `Enum`    | `SANDBOX` | No       | Target environment: `SANDBOX` or `PRODUCTION`         |
| `pagbank.base-url`                 | `String`  | —         | No       | Absolute URL that overrides the environment's base URL (e.g. a local stub) |
| `pagbank.health-indicator-enabled` | `Boolean` | `false`   | No       | Enables Spring Boot Actuator health check for PagBank |
| `pagbank.log-requests`             | `Boolean` | `false`   | No       | Logs outgoing HTTP traffic at `DEBUG` (see [Request logging](#request-logging)) |
| `pagbank.webhook.verify-signature` | `Boolean` | `false`   | No       | Requires a valid `x-authenticity-token` on webhooks (see [Signature verification](#signature-verification)) |

### Environments

| Value        | Base URL                                           |
|--------------|----------------------------------------------------|
| `SANDBOX`    | `https://sandbox.api.assinaturas.pagseguro.com` |
| `PRODUCTION` | `https://api.assinaturas.pagseguro.com`         |

`pagbank.base-url`, when set, takes precedence over the environment's URL. The `environment` property still
defaults to `SANDBOX`; production is only reached with `PRODUCTION` spelled out (or an explicit `base-url`).

### Customizing the RestClient

To add timeouts, interceptors, a proxy-aware request factory or extra headers, register one or more
`PagBankRestClientCustomizer` beans. They receive the `RestClient.Builder` after the starter has configured
it and before `build()`:

```kotlin
@Configuration
class PagBankClientConfig {
    @Bean
    fun pagBankTimeouts() = PagBankRestClientCustomizer { builder ->
        builder.requestFactory(
            JdkClientHttpRequestFactory().apply { setReadTimeout(Duration.ofSeconds(10)) }
        )
    }
}
```

### Testing your integration

Point the starter at a local stub instead of re-assembling the client in a `@TestConfiguration`:

```yaml
# src/test/resources/application-test.yml
pagbank:
  token: test-token
  base-url: http://localhost:${wiremock.server.port}
```

The real `RestClient`, error handling and JSON mapping are exercised against your stub, so any change in
how the starter assembles the client is picked up by your tests automatically.

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
| `pagBankWebhookVerifier`     | `PagBankWebhookVerifier`     |

All services use a dedicated `RestClient` bean named `pagBankRestClient`. The client is pre-configured with:
- `Authorization: Bearer <token>` on every request
- Snake_case JSON serialization/deserialization
- Structured error handling that throws `PagBankException` on 4xx/5xx responses

### Jackson isolation

The starter **does not touch your application's Jackson configuration**. Its snake_case / non-null
`JsonMapper` is private to the PagBank `RestClient` and is never registered as a Spring bean, so Spring
Boot's own `JsonMapper` (and therefore your REST API's JSON contract) is left untouched.

> **Migrating from 1.0.0-RC1:** the bean `pagBankObjectMapper` no longer exists. It was registered as a
> `JsonMapper` bean, which caused Spring Boot's `JacksonAutoConfiguration` to back off and made the PagBank
> snake_case mapper the application-wide default. If you injected it, build your own mapper instead.
> `PagBankException` also gained new subclasses (`InvalidSignature`, `RateLimited`); exhaustive `when`
> expressions over it need the new branches.

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

Parse incoming webhook events with `PagBankWebhookParser`. Read the body as **`ByteArray`** — the
signature (see below) is computed over the raw bytes, and a `String` re-serialized by a proxy or
framework may differ in whitespace and no longer match:

```kotlin
@RestController
@RequestMapping("/webhooks")
class WebhookController(private val webhookParser: PagBankWebhookParser) {

    @PostMapping("/pagbank")
    fun handleWebhook(
        @RequestBody rawBody: ByteArray,
        @RequestHeader("x-authenticity-token", required = false) authenticityToken: String?,
    ): ResponseEntity<Void> {
        val payload = webhookParser.parseVerified(rawBody, authenticityToken)

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

#### Signature verification

PagBank signs notifications with an `x-authenticity-token` header whose value is
`hex(SHA-256(token + "-" + rawBody))` — see
[Confirmar autenticidade da notificação](https://developer.pagbank.com.br/reference/confirmar-autenticidade-da-notificacao).
That page documents the Orders/Charges API; the Subscriptions API this starter covers is **not**
documented to send the header, and community reports indicate it is sometimes absent in sandbox.
Verification is therefore **opt-in**:

```yaml
pagbank:
  webhook:
    verify-signature: true   # default: false
```

| `verify-signature` | `parseVerified(rawBody, header)` behaviour |
|---|---|
| `false` (default) | Parses without checking. A `WARN` is logged at startup so the choice is visible. |
| `true` | Rejects with `PagBankException.InvalidSignature` when the header is missing, malformed or does not match — before the JSON is read. |

`PagBankWebhookVerifier` is also available as a bean (`verify(rawBody: ByteArray, header: String?): Boolean`,
constant-time comparison) if you prefer to check the header yourself. Once you confirm PagBank sends the
header for your subscription webhooks, turn verification on — an unauthenticated webhook endpoint accepts
events from anyone.

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
