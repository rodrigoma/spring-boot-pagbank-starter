# Spring Boot PagSeguro Starter — Design Spec

**Date:** 2026-03-25
**Status:** Approved

## Overview

A Spring Boot auto-configuration library that integrates the PagSeguro subscription/recurring payment API into Spring Boot applications. Consumers add the starter as a dependency, configure credentials via `application.yml`, and get fully configured service beans ready for injection.

**Goals:**
- Cover all PagSeguro subscription API domains
- Support Spring Boot 3.2.x and 4.x (minimum 3.2.0 — `RestClient` GA)
- Open-source library for the community
- Written in Kotlin

## Stack

| Concern | Choice |
|---------|--------|
| Language | Kotlin |
| Java minimum | 17 |
| Spring Boot minimum | 3.2.0 (Spring Framework 6.1+ required for `RestClient` GA) |
| HTTP client | `RestClient` (Spring 6.1+) |
| JSON | Jackson with `SnakeCaseStrategy` via dedicated `ObjectMapper` scoped to the `RestClient` codec |
| Auto-config registry | `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` |

## Project Structure

Two-module Maven project following the official Spring Boot starter convention:

```
spring-boot-pagseguro-starter/
├── pom.xml                                  (parent)
├── pagseguro-spring-boot-autoconfigure/     (core logic)
│   ├── pom.xml
│   └── src/main/kotlin/.../
│       ├── autoconfigure/
│       │   ├── PagSeguroAutoConfiguration.kt
│       │   ├── PagSeguroHealthIndicatorAutoConfiguration.kt
│       │   └── PagSeguroProperties.kt
│       ├── service/
│       │   ├── PagSeguroPlanService.kt
│       │   ├── PagSeguroCustomerService.kt
│       │   ├── PagSeguroSubscriptionService.kt
│       │   ├── PagSeguroCouponService.kt
│       │   ├── PagSeguroInvoiceService.kt
│       │   ├── PagSeguroPaymentService.kt
│       │   ├── PagSeguroChargeService.kt
│       │   ├── PagSeguroRefundService.kt
│       │   ├── PagSeguroPreferenceService.kt
│       │   └── PagSeguroWebhookParser.kt
│       ├── model/
│       │   ├── plan/
│       │   ├── customer/
│       │   ├── subscription/
│       │   ├── coupon/
│       │   ├── invoice/
│       │   ├── payment/
│       │   ├── charge/
│       │   ├── refund/
│       │   ├── preference/
│       │   └── webhook/
│       └── exception/
│           └── PagSeguroException.kt        (sealed class + ApiError defined here)
└── pagseguro-spring-boot-starter/           (umbrella dependency)
    └── pom.xml                              (depends on autoconfigure + spring-boot-starter)
```

### Umbrella starter `pom.xml` dependencies

```xml
<dependencies>
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>pagseguro-spring-boot-autoconfigure</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

`spring-boot-starter-actuator` is declared as **optional** in the `autoconfigure` module, not in the umbrella.

## Configuration

```yaml
pagseguro:
  token: YOUR_TOKEN                  # required — startup fails if blank
  environment: sandbox               # sandbox (default) | production
  webhook-secret: YOUR_WEBHOOK_SECRET  # optional — required only if using PagSeguroWebhookParser.verify()
  health-indicator-enabled: false    # optional, requires Spring Actuator on classpath
```

**Startup validation:** `PagSeguroProperties` implements `InitializingBean`. `afterPropertiesSet()` throws `IllegalStateException` if `token` is blank. No `@ConditionalOnProperty` is used — the intent is to fail fast with a clear message when the token is misconfigured, not to silently skip auto-configuration.

**Registration:** `@EnableConfigurationProperties(PagSeguroProperties::class)` on `PagSeguroAutoConfiguration` ensures Spring binds the properties.

Base URLs:
- Sandbox: `https://sandbox.api.assinaturas.pagseguro.com`
- Production: `https://api.assinaturas.pagseguro.com`

## Auto-Configuration

### `PagSeguroAutoConfiguration`

- Annotated with `@EnableConfigurationProperties(PagSeguroProperties::class)`
- Creates a `RestClient` bean with `@Bean(name = ["pagSeguroRestClient"])` — public visibility (not Kotlin `internal`) but named to avoid conflicts; documented as internal API not intended for consumer use
- Configures the `RestClient` with:
  - Base URL derived from `environment` property
  - `Authorization: Bearer <token>` default header
  - `PagSeguroErrorHandler` as the status handler (maps HTTP errors to `PagSeguroException` subclasses)
  - A dedicated `MappingJackson2HttpMessageConverter` with a `SnakeCaseStrategy`-configured `ObjectMapper` — scoped to this `RestClient` only, with no effect on the application's default `ObjectMapper`
- Creates all 10 service beans injecting the named `RestClient`

### `PagSeguroHealthIndicatorAutoConfiguration`

- `@ConditionalOnProperty(name = ["pagseguro.health-indicator-enabled"], havingValue = "true")`
- `@ConditionalOnClass(HealthIndicator::class)` (Spring Actuator present)
- `@ConditionalOnBean(name = ["pagSeguroRestClient"])` — conditions on the named bean produced by `PagSeguroAutoConfiguration`, not the configuration class itself (conditioning on a `@Configuration` class is unreliable due to bean registration ordering)
- Creates `PagSeguroHealthIndicator` that calls `GET /plans?limit=1`; a 200 response (including an empty list) signals UP. A 401/403 signals OUT_OF_SERVICE. Any other error signals DOWN.

Registration in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:
```
com.example.pagseguro.autoconfigure.PagSeguroAutoConfiguration
com.example.pagseguro.autoconfigure.PagSeguroHealthIndicatorAutoConfiguration
```

## Service Beans

All service beans follow the same pattern: constructor-injected `RestClient`, methods map 1:1 to PagSeguro API endpoints.

| Bean | Key Operations |
|------|---------------|
| `PagSeguroPlanService` | `create`, `list`, `get`, `activate`, `inactivate` |
| `PagSeguroCustomerService` | `create`, `get`, `update`, `list` |
| `PagSeguroSubscriptionService` | `create`, `get`, `cancel`, `suspend`, `reactivate`, `list` |
| `PagSeguroCouponService` | `create`, `apply`, `get`, `delete` |
| `PagSeguroInvoiceService` | `get`, `list` |
| `PagSeguroPaymentService` | `get`, `list` |
| `PagSeguroChargeService` | `get`, `list`, `retry` |
| `PagSeguroRefundService` | `create`, `get` |
| `PagSeguroPreferenceService` | `get`, `update` |
| `PagSeguroWebhookParser` | `parse(rawBody: String): WebhookPayload` |

## DTOs

Kotlin data classes per domain under `model/<domain>/`. The `RestClient`-scoped `ObjectMapper` (see JSON Configuration) handles the mapping between Kotlin camelCase properties and PagSeguro's snake_case JSON fields — no per-field `@JsonProperty` annotations needed. Kotlin null safety (`?`) documents optional API fields.

Example:
```kotlin
data class CreatePlanRequest(
    val name: String,
    val amount: Int,
    val interval: PlanInterval,
    val trialDays: Int? = null   // maps to "trial_days" via global snake_case strategy
)
```

## Error Handling

### `ApiError`

Represents a single validation error returned by the PagSeguro API:

```kotlin
data class ApiError(
    val code: String,       // e.g. "40001"
    val message: String     // e.g. "Invalid plan amount"
)
```

### `PagSeguroException`

Sealed class hierarchy in `exception/PagSeguroException.kt`. All subclasses are declared in the same file:

```kotlin
sealed class PagSeguroException(message: String) : RuntimeException(message) {
    class Unauthorized(message: String) : PagSeguroException(message)
    class NotFound(message: String) : PagSeguroException(message)
    class ValidationError(val errors: List<ApiError>) : PagSeguroException("Validation failed")
    class ServerError(val statusCode: Int) : PagSeguroException("Server error: $statusCode")
}
```

### `PagSeguroErrorHandler`

A named class (not a lambda) registered on the `RestClient` builder via `.defaultStatusHandler(...)`. It reads the response body and throws the appropriate subclass based on the HTTP status code:

- 401 → `Unauthorized`
- 404 → `NotFound`
- 400/422 → attempt to deserialize body to `List<ApiError>`; if deserialization fails (non-JSON body), fall back to `ServerError` with the raw status code
- 5xx → `ServerError` with status code

## Webhooks

`PagSeguroWebhookParser.parse(rawBody: String): WebhookPayload` deserializes the payload into a typed object with a `WebhookEventType` enum (e.g., `SUBSCRIPTION_CREATED`, `CHARGE_PAID`, `CHARGE_FAILED`).

**Signature verification:** `PagSeguroWebhookParser` also exposes `verify(rawBody: String, signature: String): Boolean` which validates the `x-pagseguro-signature` header value using HMAC-SHA256 with `pagseguro.webhook-secret` as the key. This secret is separate from the API bearer token — PagSeguro issues a dedicated webhook secret per notification URL. If `webhook-secret` is not configured and `verify()` is called, the method throws `IllegalStateException`. Consumers are expected to call `verify` before `parse`. The method is separate (not bundled into `parse`) so consumers can choose their own error response when verification fails.

**Constructor:** `PagSeguroWebhookParser(properties: PagSeguroProperties)` — does not require `RestClient` (no HTTP calls); receives properties for access to `webhookSecret`.

## JSON Configuration

The `RestClient` is configured with a dedicated `ObjectMapper` instance (not the application's default one) that has `PropertyNamingStrategies.SNAKE_CASE` applied. This is done by setting a custom `MappingJackson2HttpMessageConverter` directly on the `RestClient` builder — scoped only to PagSeguro API calls and with no side effects on the consumer's application-wide JSON serialization (e.g., REST controller responses).

## Testing Strategy

**Unit tests — service layer:**

`RestClient` is constructed via `RestClient.builder()`. For tests, the builder is given a `MockClientHttpRequestFactory` (from `spring-test`), which intercepts requests without a network call. `MockRestServiceServer` is NOT used — it is designed for `RestTemplate` and is not compatible with a standalone `RestClient.builder()`.

```kotlin
val mockFactory = MockClientHttpRequestFactory()
val restClient = RestClient.builder()
    .requestFactory(mockFactory)
    .build()
val planService = PagSeguroPlanService(restClient)
```

One test class per service.

**Auto-configuration tests:**

`ApplicationContextRunner` to verify bean creation, property validation, and conditional activation:

```kotlin
@Test
fun `should fail to start without token`() {
    contextRunner
        .withPropertyValues("pagseguro.environment=sandbox")
        .run { context -> assertThat(context).hasFailed() }
}

@Test
fun `should create all service beans when token is provided`() {
    contextRunner
        .withPropertyValues("pagseguro.token=TOKEN_123")
        .run { context ->
            assertThat(context).hasSingleBean(PagSeguroPlanService::class.java)
            assertThat(context).hasSingleBean(PagSeguroSubscriptionService::class.java)
        }
}
```

## Out of Scope

- Multi-tenant / multiple account support
- Reactive (WebFlux/coroutines) support
- PagSeguro checkout or one-time payment APIs (subscription APIs only)
- Spring Boot versions below 3.2.0
