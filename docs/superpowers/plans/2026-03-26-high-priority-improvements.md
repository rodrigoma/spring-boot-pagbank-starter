# High-Priority Improvements Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver four high-priority improvements to `spring-boot-pagbank-starter`: a production-grade README, an Apache 2.0 LICENSE file, pagination support in all list() methods, and full behavioral test coverage for all service classes.

**Architecture:** All changes live in the two-module Gradle project. Model changes (ListParams) go into a new `model/common/` package inside `pagbank-spring-boot-autoconfigure`. Service signatures are updated to accept `ListParams` with default values, keeping backward compatibility. Tests follow the established pattern from `PagBankPaymentServiceTest` using `MockClientHttpRequestFactory` + `MockClientHttpResponse`, requiring no external network access.

**Tech Stack:** Kotlin 1.9, Java 17, Spring Boot 3.2.0, Gradle 8.12, JUnit 5, AssertJ, `org.springframework.mock.http.client`

---

## Task 1 — LICENSE file (Apache 2.0)

**Files:**
- Create: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/LICENSE`

### Steps

- [ ] Create the file `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/LICENSE` with the following exact content:

```
                                 Apache License
                           Version 2.0, January 2004
                        http://www.apache.org/licenses/

   TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

   1. Definitions.

      "License" shall mean the terms and conditions for use, reproduction,
      and distribution as defined by Sections 1 through 9 of this document.

      "Licensor" shall mean the copyright owner or entity authorized by
      the copyright owner that is granting the License.

      "Legal Entity" shall mean the union of the acting entity and all
      other entities that control, are controlled by, or are under common
      control with that entity. For the purposes of this definition,
      "control" means (i) the power, direct or indirect, to cause the
      direction or management of such entity, whether by contract or
      otherwise, or (ii) ownership of fifty percent (50%) or more of the
      outstanding shares, or (iii) beneficial ownership of such entity.

      "You" (or "Your") shall mean an individual or Legal Entity
      exercising permissions granted by this License.

      "Source" form shall mean the preferred form for making modifications,
      including but not limited to software source code, documentation
      source, and configuration files.

      "Object" form shall mean any form resulting from mechanical
      transformation or translation of a Source form, including but
      not limited to compiled object code, generated documentation,
      and conversions to other media types.

      "Work" shall mean the work of authorship made available under
      the License, as indicated by a copyright notice that is included in
      or attached to the work (an example is provided in the Appendix below).

      "Derivative Works" shall mean any work, whether in Source or Object
      form, that is based on (or derived from) the Work and for which the
      editorial revisions, annotations, elaborations, or other modifications
      represent, as a whole, an original work of authorship. For the purposes
      of this License, Derivative Works shall not include works that remain
      separable from, or merely link (or bind by name) to the interfaces of,
      the Work and derivative works thereof.

      "Contribution" shall mean, as submitted to the Licensor for inclusion
      in the Work by the copyright owner or by an individual or Legal Entity
      authorized to submit on behalf of the copyright owner. For the purposes
      of this definition, "submitted" means any form of electronic, verbal,
      or written communication sent to the Licensor or its representatives,
      including but not limited to communication on electronic mailing lists,
      source code control systems, and issue tracking systems that are managed
      by, or on behalf of, the Licensor for the purpose of developing and
      discussing the Work, but excluding communication that is conspicuously
      marked or designated in writing by the copyright owner as "Not a
      Contribution."

      "Contributor" shall mean Licensor and any Legal Entity on behalf of
      whom a Contribution has been received by the Licensor and incorporated
      within the Work.

   2. Grant of Copyright License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      copyright license to reproduce, prepare Derivative Works of,
      publicly display, publicly perform, sublicense, and distribute the
      Work and such Derivative Works in Source or Object form.

   3. Grant of Patent License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      (except as stated in this section) patent license to make, have made,
      use, offer to sell, sell, import, and otherwise transfer the Work,
      where such license applies only to those patent claims licensable
      by such Contributor that are necessarily infringed by their
      Contribution(s) alone or by the combination of their Contribution(s)
      with the Work to which such Contribution(s) was submitted. If You
      institute patent litigation against any entity (including a cross-claim
      or counterclaim in a lawsuit) alleging that the Work or any Contribution
      embodied within the Work constitutes direct or contributory patent
      infringement, then any patent licenses granted to You under this License
      for that Work shall terminate as of the date such litigation is filed.

   4. Redistribution. You may reproduce and distribute copies of the
      Work or Derivative Works thereof in any medium, with or without
      modifications, and in Source or Object form, provided that You
      meet the following conditions:

      (a) You must give any other recipients of the Work or Derivative
          Works a copy of this License; and

      (b) You must cause any modified files to carry prominent notices
          stating that You changed the files; and

      (c) You must retain, in the Source form of any Derivative Works
          that You distribute, all copyright, patent, trademark, and
          attribution notices from the Source form of the Work,
          excluding those notices that do not pertain to any part of
          the Derivative Works; and

      (d) If the Work includes a "NOTICE" text file as part of its
          distribution, You must include a readable copy of the
          attribution notices contained within such NOTICE file, in
          at least one of the following places: within a NOTICE text
          file distributed as part of the Derivative Works; within
          the Source form or documentation, if provided along with the
          Derivative Works; or, within a display generated by the
          Derivative Works, if and wherever such third-party notices
          normally appear. The contents of the NOTICE file are for
          informational purposes only and do not modify the License.
          You may add Your own attribution notices within Derivative
          Works that You distribute, alongside or as an addendum to
          the NOTICE text from the Work, provided that such additional
          attribution notices cannot be construed as modifying the License.

      You may add Your own license statement for Your modifications and
      may provide additional grant of rights to use, reproduce, modify,
      prepare derivative works of, collect, publish, distribute, sublicense,
      and/or sell copies of the Contribution, either on their own behalf
      or on behalf of the Licensor.

   5. Submission of Contributions. Unless You explicitly state otherwise,
      any Contribution intentionally submitted for inclusion in the Work
      by You to the Licensor shall be under the terms and conditions of
      this License, without any additional terms or conditions.
      Notwithstanding the above, nothing herein shall supersede or modify
      the terms of any separate license agreement you may have executed
      with Licensor regarding such Contributions.

   6. Trademarks. This License does not grant permission to use the trade
      names, trademarks, service marks, or product names of the Licensor,
      except as required for reasonable and customary use in describing the
      origin of the Work and reproducing the content of the NOTICE file.

   7. Disclaimer of Warranty. Unless required by applicable law or
      agreed to in writing, Licensor provides the Work (and each
      Contributor provides its Contributions) on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
      implied, including, without limitation, any warranties or conditions
      of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A
      PARTICULAR PURPOSE. You are solely responsible for determining the
      appropriateness of using or reproducing the Work and assume any
      risks associated with Your exercise of permissions under this License.

   8. Limitation of Liability. In no event and under no legal theory,
      whether in tort (including negligence), contract, or otherwise,
      unless required by applicable law (such as deliberate and grossly
      negligent acts) or agreed to in writing, shall any Contributor be
      liable to You for damages, including any direct, indirect, special,
      incidental, or exemplary damages of any character arising as a
      result of this License or out of the use or inability to use the
      Work (including but not limited to damages for loss of goodwill,
      work stoppage, computer failure or malfunction, or all other
      commercial damages or losses), even if such Contributor has been
      advised of the possibility of such damages.

   9. Accepting Warranty or Additional Liability. While redistributing
      the Work or Derivative Works thereof, You may choose to offer,
      and charge a fee for, acceptance of support, warranty, indemnity,
      or other liability obligations and/or rights consistent with this
      License. However, in accepting such obligations, You may act only
      on Your own behalf and on Your sole responsibility, not on behalf
      of any other Contributor, and only if You agree to indemnify,
      defend, and hold each Contributor harmless for any liability
      incurred by, or claims asserted against, such Contributor by reason
      of your accepting any such warranty or additional liability.

   END OF TERMS AND CONDITIONS

   Copyright 2026 Rodrigo Montanha

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```

- [ ] Commit:
  ```
  git add LICENSE
  git commit -m "docs: add Apache 2.0 LICENSE"
  ```

---

## Task 2 — README.md

**Files:**
- Create: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/README.md`

### Steps

- [ ] Create the file `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/README.md` with the following exact content:

```markdown
# PagBank Spring Boot Starter

[![CI](https://github.com/rodrigoma/spring-boot-pagbank-starter/actions/workflows/build.yml/badge.svg)](https://github.com/rodrigoma/spring-boot-pagbank-starter/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.rodrigoma/pagbank-spring-boot-starter.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.rodrigoma/pagbank-spring-boot-starter)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A Spring Boot auto-configuration library for the **PagBank Subscriptions API** (formerly PagSeguro). Drop it on the classpath and get 9 fully configured service beans — no boilerplate required.

## Requirements

| Dependency   | Minimum version |
|--------------|-----------------|
| Java         | 17              |
| Spring Boot  | 3.2.0           |
| Kotlin       | 1.9 (optional)  |

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

  # Optional — secret key for verifying incoming webhook signatures (HMAC-SHA256)
  webhook-secret: your-webhook-secret-here

  # Optional — expose a /actuator/health/pagBank endpoint (default: false)
  health-indicator-enabled: false
```

| Property                        | Type      | Default   | Required | Description                                              |
|---------------------------------|-----------|-----------|----------|----------------------------------------------------------|
| `pagbank.token`                 | `String`  | —         | Yes      | API token from your PagBank dashboard                    |
| `pagbank.environment`           | `Enum`    | `SANDBOX` | No       | Target environment: `SANDBOX` or `PRODUCTION`            |
| `pagbank.webhook-secret`        | `String`  | `null`    | No       | Secret for HMAC-SHA256 webhook signature verification    |
| `pagbank.health-indicator-enabled` | `Boolean` | `false` | No       | Enables Spring Boot Actuator health check for PagBank    |

### Environments

| Value        | Base URL                                             |
|--------------|------------------------------------------------------|
| `SANDBOX`    | `https://sandbox.assinaturas.pagseguro.uol.com.br`   |
| `PRODUCTION` | `https://assinaturas.pagseguro.uol.com.br`           |

## Auto-configured Beans

Once the starter is on the classpath and `pagbank.token` is set, the following beans are automatically registered:

| Bean name                     | Type                        |
|-------------------------------|-----------------------------|
| `pagBankPlanService`          | `PagBankPlanService`        |
| `pagBankCustomerService`      | `PagBankCustomerService`    |
| `pagBankSubscriptionService`  | `PagBankSubscriptionService`|
| `pagBankCouponService`        | `PagBankCouponService`      |
| `pagBankInvoiceService`       | `PagBankInvoiceService`     |
| `pagBankPaymentService`       | `PagBankPaymentService`     |
| `pagBankChargeService`        | `PagBankChargeService`      |
| `pagBankRefundService`        | `PagBankRefundService`      |
| `pagBankPreferenceService`    | `PagBankPreferenceService`  |
| `pagBankWebhookParser`        | `PagBankWebhookParser`      |

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
        // Returns up to 10 plans by default
        return planService.list()
    }

    fun deactivatePlan(planId: String) {
        planService.deactivate(planId)
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

### Webhook Verification and Parsing

Verify the HMAC-SHA256 signature that PagBank sends in the `x-pagbank-signature` header before processing the event:

```kotlin
@RestController
@RequestMapping("/webhooks")
class WebhookController(private val webhookParser: PagBankWebhookParser) {

    @PostMapping("/pagbank")
    fun handleWebhook(
        @RequestBody rawBody: String,
        @RequestHeader("x-pagbank-signature") signature: String
    ): ResponseEntity<Void> {
        if (!webhookParser.verify(rawBody, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val payload = webhookParser.parse(rawBody)

        when (payload.event) {
            WebhookEventType.SUBSCRIPTION_RECURRENCE -> {
                // handle recurring charge
            }
            WebhookEventType.SUBSCRIPTION_CANCELED -> {
                // handle cancellation
            }
            WebhookEventType.REFUND_CREATED -> {
                // handle refund
            }
            else -> { /* log and ignore */ }
        }

        return ResponseEntity.ok().build()
    }
}
```

> **Note:** `pagbank.webhook-secret` must be configured in `application.yml` for signature verification to work. If you call `verify()` without a secret configured, an `IllegalStateException` is thrown.

### Error Handling

Service calls throw `PagBankException` on API errors. You can catch it to inspect the HTTP status and error details:

```kotlin
try {
    val plan = planService.get("nonexistent-plan-id")
} catch (e: PagBankException) {
    println("HTTP ${e.status}: ${e.message}")
    // e.errors contains the structured API error list when available
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
```

- [ ] Commit:
  ```
  git add README.md
  git commit -m "docs: add production-grade README with installation, configuration, and usage examples"
  ```

---

## Task 3 — Shared ListParams model

**Files:**
- Create: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/src/main/kotlin/io/github/rodrigoma/pagbank/model/common/ListParams.kt`

### Steps

- [ ] Create the file with the following exact content:

```kotlin
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
```

- [ ] Commit:
  ```
  git add pagbank-spring-boot-autoconfigure/src/main/kotlin/io/github/rodrigoma/pagbank/model/common/ListParams.kt
  git commit -m "feat: add shared ListParams data class for pagination"
  ```

---

## Task 4 — Pagination in PagBankPlanService

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/src/main/kotlin/io/github/rodrigoma/pagbank/service/PagBankPlanService.kt`
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/service/PagBankPlanServiceTest.kt`

### Steps

- [ ] Edit `PagBankPlanService.kt`. Replace the `list()` method signature and body:

  Old:
  ```kotlin
  fun list(): PlanListResponse =
      restClient.get()
          .uri("/plans")
          .retrieve()
          .body<PlanListResponse>()!!
  ```

  New — also add the import at the top of the file after the existing imports:
  ```kotlin
  import io.github.rodrigoma.pagbank.model.common.ListParams
  ```

  New method body:
  ```kotlin
  fun list(params: ListParams = ListParams()): PlanListResponse =
      restClient.get()
          .uri("/plans?limit={limit}&offset={offset}", params.limit, params.offset)
          .retrieve()
          .body<PlanListResponse>()!!
  ```

- [ ] Edit `PagBankPlanServiceTest.kt`. Add the following two test methods after the existing `get should return plan by id` test:

  ```kotlin
  @Test
  fun `list should return PlanListResponse with default params`() {
      mockFactory.nextBody = mapper.writeValueAsBytes(
          mapOf("plans" to listOf(
              mapOf("id" to "PLAN_001", "name" to "Basic",
                    "amount" to mapOf("value" to 999, "currency" to "BRL"),
                    "interval" to mapOf("length" to 1, "unit" to "month"),
                    "status" to "ACTIVE", "created_at" to "2026-01-01T00:00:00Z",
                    "reference_id" to null, "trial" to null, "updated_at" to null)
          ))
      )
      val response = service.list()
      assertThat(response.plans).hasSize(1)
      assertThat(response.plans[0].id).isEqualTo("PLAN_001")
  }

  @Test
  fun `list should forward limit and offset params`() {
      mockFactory.nextBody = mapper.writeValueAsBytes(mapOf("plans" to emptyList<Any>()))
      // No exception = correct URI construction; URI content verified by the mock factory
      val response = service.list(ListParams(limit = 5, offset = 20))
      assertThat(response.plans).isEmpty()
  }
  ```

  Also add the import at the top of the test file:
  ```kotlin
  import io.github.rodrigoma.pagbank.model.common.ListParams
  ```

- [ ] Commit:
  ```
  git add pagbank-spring-boot-autoconfigure/src/main/kotlin/io/github/rodrigoma/pagbank/service/PagBankPlanService.kt \
          pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/service/PagBankPlanServiceTest.kt
  git commit -m "feat: add pagination (limit/offset) to PagBankPlanService.list()"
  ```

---

## Task 5 — Pagination in PagBankCustomerService

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/src/main/kotlin/io/github/rodrigoma/pagbank/service/PagBankCustomerService.kt`
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/service/PagBankCustomerServiceTest.kt`

### Steps

- [ ] Edit `PagBankCustomerService.kt`. Add import and update `list()`:

  Add import after existing imports:
  ```kotlin
  import io.github.rodrigoma.pagbank.model.common.ListParams
  ```

  Replace:
  ```kotlin
  fun list(): CustomerListResponse =
      restClient.get()
          .uri("/customers")
          .retrieve()
          .body<CustomerListResponse>()!!
  ```

  With:
  ```kotlin
  fun list(params: ListParams = ListParams()): CustomerListResponse =
      restClient.get()
          .uri("/customers?limit={limit}&offset={offset}", params.limit, params.offset)
          .retrieve()
          .body<CustomerListResponse>()!!
  ```

- [ ] Edit `PagBankCustomerServiceTest.kt`. The file currently exists but has no behavioral tests (check its current state). Replace the entire file content with:

  ```kotlin
  package io.github.rodrigoma.pagbank.service

  import io.github.rodrigoma.pagbank.model.common.ListParams
  import io.github.rodrigoma.pagbank.model.customer.*
  import com.fasterxml.jackson.databind.ObjectMapper
  import com.fasterxml.jackson.databind.PropertyNamingStrategies
  import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
  import org.assertj.core.api.Assertions.assertThat
  import org.junit.jupiter.api.BeforeEach
  import org.junit.jupiter.api.Test
  import org.springframework.http.HttpMethod
  import org.springframework.http.HttpStatus
  import org.springframework.http.MediaType
  import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
  import org.springframework.mock.http.client.MockClientHttpRequest
  import org.springframework.mock.http.client.MockClientHttpResponse
  import org.springframework.web.client.RestClient

  class PagBankCustomerServiceTest {

      private lateinit var service: PagBankCustomerService
      private val mapper: ObjectMapper = jacksonObjectMapper().apply {
          propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
      }

      private val mockFactory = object : org.springframework.http.client.ClientHttpRequestFactory {
          var nextBody: ByteArray = ByteArray(0)
          var nextStatus: HttpStatus = HttpStatus.OK
          var nextContentType: MediaType = MediaType.APPLICATION_JSON

          override fun createRequest(uri: java.net.URI, httpMethod: HttpMethod): org.springframework.http.client.ClientHttpRequest {
              val response = MockClientHttpResponse(nextBody, nextStatus)
              response.headers.contentType = nextContentType
              return MockClientHttpRequest(httpMethod, uri).also { it.setResponse(response) }
          }
      }

      @BeforeEach
      fun setUp() {
          val restClient = RestClient.builder()
              .requestFactory(mockFactory)
              .messageConverters { converters ->
                  converters.removeIf { it is MappingJackson2HttpMessageConverter }
                  converters.add(0, MappingJackson2HttpMessageConverter(mapper))
              }
              .build()
          service = PagBankCustomerService(restClient)
      }

      private fun customerMap(id: String = "CUST_123") = mapOf(
          "id" to id,
          "name" to "Maria Silva",
          "email" to "maria@example.com",
          "tax_id" to "123.456.789-00",
          "created_at" to "2026-01-01T00:00:00Z"
      )

      @Test
      fun `create should POST and return CustomerResponse`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(customerMap())
          val response = service.create(
              CreateCustomerRequest(
                  name = "Maria Silva",
                  email = "maria@example.com",
                  taxId = "123.456.789-00"
              )
          )
          assertThat(response.id).isEqualTo("CUST_123")
          assertThat(response.email).isEqualTo("maria@example.com")
      }

      @Test
      fun `get should return CustomerResponse by id`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(customerMap("CUST_456"))
          val response = service.get("CUST_456")
          assertThat(response.id).isEqualTo("CUST_456")
      }

      @Test
      fun `update should PUT and return updated CustomerResponse`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(customerMap())
          val response = service.update(
              "CUST_123",
              CreateCustomerRequest(
                  name = "Maria Silva",
                  email = "new@example.com",
                  taxId = "123.456.789-00"
              )
          )
          assertThat(response.id).isEqualTo("CUST_123")
      }

      @Test
      fun `list should return CustomerListResponse with default params`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(
              mapOf("customers" to listOf(customerMap()))
          )
          val response = service.list()
          assertThat(response.customers).hasSize(1)
          assertThat(response.customers[0].id).isEqualTo("CUST_123")
      }

      @Test
      fun `list should forward limit and offset params`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(mapOf("customers" to emptyList<Any>()))
          val response = service.list(ListParams(limit = 5, offset = 10))
          assertThat(response.customers).isEmpty()
      }
  }
  ```

- [ ] Commit:
  ```
  git add pagbank-spring-boot-autoconfigure/src/main/kotlin/io/github/rodrigoma/pagbank/service/PagBankCustomerService.kt \
          pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/service/PagBankCustomerServiceTest.kt
  git commit -m "feat: add pagination to PagBankCustomerService.list() + full customer service tests"
  ```

---

## Task 6 — Pagination in PagBankSubscriptionService

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/src/main/kotlin/io/github/rodrigoma/pagbank/service/PagBankSubscriptionService.kt`
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/service/PagBankSubscriptionServiceTest.kt`

### Steps

- [ ] Edit `PagBankSubscriptionService.kt`. Add import and update `list()`:

  Add import after existing imports:
  ```kotlin
  import io.github.rodrigoma.pagbank.model.common.ListParams
  ```

  Replace:
  ```kotlin
  fun list(): SubscriptionListResponse =
      restClient.get()
          .uri("/subscriptions")
          .retrieve()
          .body<SubscriptionListResponse>()!!
  ```

  With:
  ```kotlin
  fun list(params: ListParams = ListParams()): SubscriptionListResponse =
      restClient.get()
          .uri("/subscriptions?limit={limit}&offset={offset}", params.limit, params.offset)
          .retrieve()
          .body<SubscriptionListResponse>()!!
  ```

- [ ] Replace the entire content of `PagBankSubscriptionServiceTest.kt` with:

  ```kotlin
  package io.github.rodrigoma.pagbank.service

  import io.github.rodrigoma.pagbank.model.common.ListParams
  import io.github.rodrigoma.pagbank.model.subscription.*
  import com.fasterxml.jackson.databind.ObjectMapper
  import com.fasterxml.jackson.databind.PropertyNamingStrategies
  import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
  import org.assertj.core.api.Assertions.assertThat
  import org.junit.jupiter.api.BeforeEach
  import org.junit.jupiter.api.Test
  import org.springframework.http.HttpMethod
  import org.springframework.http.HttpStatus
  import org.springframework.http.MediaType
  import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
  import org.springframework.mock.http.client.MockClientHttpRequest
  import org.springframework.mock.http.client.MockClientHttpResponse
  import org.springframework.web.client.RestClient

  class PagBankSubscriptionServiceTest {

      private lateinit var service: PagBankSubscriptionService
      private val mapper: ObjectMapper = jacksonObjectMapper().apply {
          propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
      }

      private val mockFactory = object : org.springframework.http.client.ClientHttpRequestFactory {
          var nextBody: ByteArray = ByteArray(0)
          var nextStatus: HttpStatus = HttpStatus.OK
          var nextContentType: MediaType = MediaType.APPLICATION_JSON

          override fun createRequest(uri: java.net.URI, httpMethod: HttpMethod): org.springframework.http.client.ClientHttpRequest {
              val response = MockClientHttpResponse(nextBody, nextStatus)
              response.headers.contentType = nextContentType
              return MockClientHttpRequest(httpMethod, uri).also { it.setResponse(response) }
          }
      }

      @BeforeEach
      fun setUp() {
          val restClient = RestClient.builder()
              .requestFactory(mockFactory)
              .messageConverters { converters ->
                  converters.removeIf { it is MappingJackson2HttpMessageConverter }
                  converters.add(0, MappingJackson2HttpMessageConverter(mapper))
              }
              .build()
          service = PagBankSubscriptionService(restClient)
      }

      private fun subscriptionMap(id: String = "SUB_123") = mapOf(
          "id" to id,
          "plan_id" to "PLAN_001",
          "customer_id" to "CUST_001",
          "status" to "ACTIVE",
          "created_at" to "2026-01-01T00:00:00Z"
      )

      @Test
      fun `create should POST and return SubscriptionResponse`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(subscriptionMap())
          val response = service.create(
              CreateSubscriptionRequest(planId = "PLAN_001", customerId = "CUST_001")
          )
          assertThat(response.id).isEqualTo("SUB_123")
          assertThat(response.status).isEqualTo(SubscriptionStatus.ACTIVE)
      }

      @Test
      fun `get should return SubscriptionResponse by id`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(subscriptionMap("SUB_456"))
          val response = service.get("SUB_456")
          assertThat(response.id).isEqualTo("SUB_456")
      }

      @Test
      fun `cancel should PUT without a response body`() {
          mockFactory.nextBody = ByteArray(0)
          mockFactory.nextStatus = HttpStatus.NO_CONTENT
          // No exception = success
          service.cancel("SUB_123")
      }

      @Test
      fun `suspend should PUT without a response body`() {
          mockFactory.nextBody = ByteArray(0)
          mockFactory.nextStatus = HttpStatus.NO_CONTENT
          service.suspend("SUB_123")
      }

      @Test
      fun `reactivate should PUT without a response body`() {
          mockFactory.nextBody = ByteArray(0)
          mockFactory.nextStatus = HttpStatus.NO_CONTENT
          service.reactivate("SUB_123")
      }

      @Test
      fun `list should return SubscriptionListResponse with default params`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(
              mapOf("subscriptions" to listOf(subscriptionMap()))
          )
          val response = service.list()
          assertThat(response.subscriptions).hasSize(1)
          assertThat(response.subscriptions[0].id).isEqualTo("SUB_123")
      }

      @Test
      fun `list should forward limit and offset params`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(mapOf("subscriptions" to emptyList<Any>()))
          val response = service.list(ListParams(limit = 25, offset = 50))
          assertThat(response.subscriptions).isEmpty()
      }
  }
  ```

- [ ] Commit:
  ```
  git add pagbank-spring-boot-autoconfigure/src/main/kotlin/io/github/rodrigoma/pagbank/service/PagBankSubscriptionService.kt \
          pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/service/PagBankSubscriptionServiceTest.kt
  git commit -m "feat: add pagination to PagBankSubscriptionService.list() + full subscription service tests"
  ```

---

## Task 7 — Pagination in PagBankInvoiceService

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/src/main/kotlin/io/github/rodrigoma/pagbank/service/PagBankInvoiceService.kt`
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/service/PagBankInvoiceServiceTest.kt`

### Steps

- [ ] Edit `PagBankInvoiceService.kt`. Add import and update `listBySubscription()`:

  Add import after existing imports:
  ```kotlin
  import io.github.rodrigoma.pagbank.model.common.ListParams
  ```

  Replace:
  ```kotlin
  fun listBySubscription(subscriptionId: String): InvoiceListResponse =
      restClient.get()
          .uri("/subscriptions/{id}/invoices", subscriptionId)
          .retrieve()
          .body<InvoiceListResponse>()!!
  ```

  With:
  ```kotlin
  fun listBySubscription(subscriptionId: String, params: ListParams = ListParams()): InvoiceListResponse =
      restClient.get()
          .uri("/subscriptions/{id}/invoices?limit={limit}&offset={offset}",
               subscriptionId, params.limit, params.offset)
          .retrieve()
          .body<InvoiceListResponse>()!!
  ```

- [ ] Replace the entire content of `PagBankInvoiceServiceTest.kt` with:

  ```kotlin
  package io.github.rodrigoma.pagbank.service

  import io.github.rodrigoma.pagbank.model.common.ListParams
  import io.github.rodrigoma.pagbank.model.invoice.*
  import com.fasterxml.jackson.databind.ObjectMapper
  import com.fasterxml.jackson.databind.PropertyNamingStrategies
  import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
  import org.assertj.core.api.Assertions.assertThat
  import org.junit.jupiter.api.BeforeEach
  import org.junit.jupiter.api.Test
  import org.springframework.http.HttpMethod
  import org.springframework.http.HttpStatus
  import org.springframework.http.MediaType
  import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
  import org.springframework.mock.http.client.MockClientHttpRequest
  import org.springframework.mock.http.client.MockClientHttpResponse
  import org.springframework.web.client.RestClient

  class PagBankInvoiceServiceTest {

      private lateinit var service: PagBankInvoiceService
      private val mapper: ObjectMapper = jacksonObjectMapper().apply {
          propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
      }

      private val mockFactory = object : org.springframework.http.client.ClientHttpRequestFactory {
          var nextBody: ByteArray = ByteArray(0)
          var nextStatus: HttpStatus = HttpStatus.OK
          var nextContentType: MediaType = MediaType.APPLICATION_JSON

          override fun createRequest(uri: java.net.URI, httpMethod: HttpMethod): org.springframework.http.client.ClientHttpRequest {
              val response = MockClientHttpResponse(nextBody, nextStatus)
              response.headers.contentType = nextContentType
              return MockClientHttpRequest(httpMethod, uri).also { it.setResponse(response) }
          }
      }

      @BeforeEach
      fun setUp() {
          val restClient = RestClient.builder()
              .requestFactory(mockFactory)
              .messageConverters { converters ->
                  converters.removeIf { it is MappingJackson2HttpMessageConverter }
                  converters.add(0, MappingJackson2HttpMessageConverter(mapper))
              }
              .build()
          service = PagBankInvoiceService(restClient)
      }

      private fun invoiceMap(id: String = "INV_123") = mapOf(
          "id" to id,
          "subscription_id" to "SUB_001",
          "amount" to 2990,
          "status" to "PAID",
          "due_date" to "2026-02-01"
      )

      @Test
      fun `get should return InvoiceResponse by id`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(invoiceMap())
          val response = service.get("INV_123")
          assertThat(response.id).isEqualTo("INV_123")
          assertThat(response.amount).isEqualTo(2990)
          assertThat(response.status).isEqualTo("PAID")
      }

      @Test
      fun `listBySubscription should return InvoiceListResponse with default params`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(
              mapOf("invoices" to listOf(invoiceMap()))
          )
          val response = service.listBySubscription("SUB_001")
          assertThat(response.invoices).hasSize(1)
          assertThat(response.invoices[0].subscriptionId).isEqualTo("SUB_001")
      }

      @Test
      fun `listBySubscription should forward limit and offset params`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(mapOf("invoices" to emptyList<Any>()))
          val response = service.listBySubscription("SUB_001", ListParams(limit = 3, offset = 6))
          assertThat(response.invoices).isEmpty()
      }
  }
  ```

- [ ] Commit:
  ```
  git add pagbank-spring-boot-autoconfigure/src/main/kotlin/io/github/rodrigoma/pagbank/service/PagBankInvoiceService.kt \
          pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/service/PagBankInvoiceServiceTest.kt
  git commit -m "feat: add pagination to PagBankInvoiceService.listBySubscription() + full invoice service tests"
  ```

---

## Task 8 — Pagination in PagBankPaymentService

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/src/main/kotlin/io/github/rodrigoma/pagbank/service/PagBankPaymentService.kt`
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/service/PagBankPaymentServiceTest.kt`

### Steps

- [ ] Edit `PagBankPaymentService.kt`. Add import and update `list()`:

  Add import after existing imports:
  ```kotlin
  import io.github.rodrigoma.pagbank.model.common.ListParams
  ```

  Replace:
  ```kotlin
  fun list(): PaymentListResponse =
      restClient.get()
          .uri("/payments")
          .retrieve()
          .body<PaymentListResponse>()!!
  ```

  With:
  ```kotlin
  fun list(params: ListParams = ListParams()): PaymentListResponse =
      restClient.get()
          .uri("/payments?limit={limit}&offset={offset}", params.limit, params.offset)
          .retrieve()
          .body<PaymentListResponse>()!!
  ```

- [ ] Edit `PagBankPaymentServiceTest.kt`. Add the following test methods and the import for `ListParams` to the existing file (do NOT replace the existing `get` test):

  Add import at the top of the file:
  ```kotlin
  import io.github.rodrigoma.pagbank.model.common.ListParams
  ```

  Add these test methods inside the class after the existing `get should return PaymentResponse by id` test:
  ```kotlin
  @Test
  fun `list should return PaymentListResponse with default params`() {
      mockFactory.nextBody = mapper.writeValueAsBytes(
          mapOf("payments" to listOf(
              mapOf(
                  "id" to "PAY_001",
                  "invoice_id" to "INV_001",
                  "amount" to 2990,
                  "status" to "PAID",
                  "paid_at" to "2026-01-15T10:00:00Z"
              )
          ))
      )
      val response = service.list()
      assertThat(response.payments).hasSize(1)
      assertThat(response.payments[0].id).isEqualTo("PAY_001")
  }

  @Test
  fun `list should forward limit and offset params`() {
      mockFactory.nextBody = mapper.writeValueAsBytes(mapOf("payments" to emptyList<Any>()))
      val response = service.list(ListParams(limit = 20, offset = 40))
      assertThat(response.payments).isEmpty()
  }
  ```

- [ ] Commit:
  ```
  git add pagbank-spring-boot-autoconfigure/src/main/kotlin/io/github/rodrigoma/pagbank/service/PagBankPaymentService.kt \
          pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/service/PagBankPaymentServiceTest.kt
  git commit -m "feat: add pagination to PagBankPaymentService.list() + extend payment service tests"
  ```

---

## Task 9 — Pagination in PagBankChargeService

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/src/main/kotlin/io/github/rodrigoma/pagbank/service/PagBankChargeService.kt`
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/service/PagBankChargeServiceTest.kt`

### Steps

- [ ] Edit `PagBankChargeService.kt`. Add import and update `list()`:

  Add import after existing imports:
  ```kotlin
  import io.github.rodrigoma.pagbank.model.common.ListParams
  ```

  Replace:
  ```kotlin
  fun list(): ChargeListResponse =
      restClient.get()
          .uri("/charges")
          .retrieve()
          .body<ChargeListResponse>()!!
  ```

  With:
  ```kotlin
  fun list(params: ListParams = ListParams()): ChargeListResponse =
      restClient.get()
          .uri("/charges?limit={limit}&offset={offset}", params.limit, params.offset)
          .retrieve()
          .body<ChargeListResponse>()!!
  ```

- [ ] Replace the entire content of `PagBankChargeServiceTest.kt` with:

  ```kotlin
  package io.github.rodrigoma.pagbank.service

  import io.github.rodrigoma.pagbank.model.charge.*
  import io.github.rodrigoma.pagbank.model.common.ListParams
  import com.fasterxml.jackson.databind.ObjectMapper
  import com.fasterxml.jackson.databind.PropertyNamingStrategies
  import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
  import org.assertj.core.api.Assertions.assertThat
  import org.junit.jupiter.api.BeforeEach
  import org.junit.jupiter.api.Test
  import org.springframework.http.HttpMethod
  import org.springframework.http.HttpStatus
  import org.springframework.http.MediaType
  import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
  import org.springframework.mock.http.client.MockClientHttpRequest
  import org.springframework.mock.http.client.MockClientHttpResponse
  import org.springframework.web.client.RestClient

  class PagBankChargeServiceTest {

      private lateinit var service: PagBankChargeService
      private val mapper: ObjectMapper = jacksonObjectMapper().apply {
          propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
      }

      private val mockFactory = object : org.springframework.http.client.ClientHttpRequestFactory {
          var nextBody: ByteArray = ByteArray(0)
          var nextStatus: HttpStatus = HttpStatus.OK
          var nextContentType: MediaType = MediaType.APPLICATION_JSON

          override fun createRequest(uri: java.net.URI, httpMethod: HttpMethod): org.springframework.http.client.ClientHttpRequest {
              val response = MockClientHttpResponse(nextBody, nextStatus)
              response.headers.contentType = nextContentType
              return MockClientHttpRequest(httpMethod, uri).also { it.setResponse(response) }
          }
      }

      @BeforeEach
      fun setUp() {
          val restClient = RestClient.builder()
              .requestFactory(mockFactory)
              .messageConverters { converters ->
                  converters.removeIf { it is MappingJackson2HttpMessageConverter }
                  converters.add(0, MappingJackson2HttpMessageConverter(mapper))
              }
              .build()
          service = PagBankChargeService(restClient)
      }

      private fun chargeMap(id: String = "CHG_123") = mapOf(
          "id" to id,
          "subscription_id" to "SUB_001",
          "amount" to 2990,
          "status" to "PAID"
      )

      @Test
      fun `get should return ChargeResponse by id`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(chargeMap())
          val response = service.get("CHG_123")
          assertThat(response.id).isEqualTo("CHG_123")
          assertThat(response.amount).isEqualTo(2990)
      }

      @Test
      fun `list should return ChargeListResponse with default params`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(
              mapOf("charges" to listOf(chargeMap()))
          )
          val response = service.list()
          assertThat(response.charges).hasSize(1)
          assertThat(response.charges[0].id).isEqualTo("CHG_123")
      }

      @Test
      fun `list should forward limit and offset params`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(mapOf("charges" to emptyList<Any>()))
          val response = service.list(ListParams(limit = 50, offset = 100))
          assertThat(response.charges).isEmpty()
      }

      @Test
      fun `retry should POST without a response body`() {
          mockFactory.nextBody = ByteArray(0)
          mockFactory.nextStatus = HttpStatus.NO_CONTENT
          // No exception = success
          service.retry("CHG_123")
      }

      @Test
      fun `retry should POST with paymentMethod`() {
          mockFactory.nextBody = ByteArray(0)
          mockFactory.nextStatus = HttpStatus.NO_CONTENT
          service.retry("CHG_123", RetryChargeRequest(paymentMethod = "CARD"))
      }
  }
  ```

- [ ] Commit:
  ```
  git add pagbank-spring-boot-autoconfigure/src/main/kotlin/io/github/rodrigoma/pagbank/service/PagBankChargeService.kt \
          pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/service/PagBankChargeServiceTest.kt
  git commit -m "feat: add pagination to PagBankChargeService.list() + full charge service tests"
  ```

---

## Task 10 — Full test coverage for PagBankCouponService

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/service/PagBankCouponServiceTest.kt`

### Steps

- [ ] Replace the entire content of `PagBankCouponServiceTest.kt` with:

  ```kotlin
  package io.github.rodrigoma.pagbank.service

  import io.github.rodrigoma.pagbank.model.coupon.*
  import com.fasterxml.jackson.databind.ObjectMapper
  import com.fasterxml.jackson.databind.PropertyNamingStrategies
  import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
  import org.assertj.core.api.Assertions.assertThat
  import org.junit.jupiter.api.BeforeEach
  import org.junit.jupiter.api.Test
  import org.springframework.http.HttpMethod
  import org.springframework.http.HttpStatus
  import org.springframework.http.MediaType
  import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
  import org.springframework.mock.http.client.MockClientHttpRequest
  import org.springframework.mock.http.client.MockClientHttpResponse
  import org.springframework.web.client.RestClient

  class PagBankCouponServiceTest {

      private lateinit var service: PagBankCouponService
      private val mapper: ObjectMapper = jacksonObjectMapper().apply {
          propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
      }

      private val mockFactory = object : org.springframework.http.client.ClientHttpRequestFactory {
          var nextBody: ByteArray = ByteArray(0)
          var nextStatus: HttpStatus = HttpStatus.OK
          var nextContentType: MediaType = MediaType.APPLICATION_JSON

          override fun createRequest(uri: java.net.URI, httpMethod: HttpMethod): org.springframework.http.client.ClientHttpRequest {
              val response = MockClientHttpResponse(nextBody, nextStatus)
              response.headers.contentType = nextContentType
              return MockClientHttpRequest(httpMethod, uri).also { it.setResponse(response) }
          }
      }

      @BeforeEach
      fun setUp() {
          val restClient = RestClient.builder()
              .requestFactory(mockFactory)
              .messageConverters { converters ->
                  converters.removeIf { it is MappingJackson2HttpMessageConverter }
                  converters.add(0, MappingJackson2HttpMessageConverter(mapper))
              }
              .build()
          service = PagBankCouponService(restClient)
      }

      private fun couponMap(id: String = "CPN_123") = mapOf(
          "id" to id,
          "code" to "SAVE10",
          "discount_type" to "PERCENT",
          "discount_value" to 10
      )

      @Test
      fun `create should POST and return CouponResponse`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(couponMap())
          val response = service.create(
              CreateCouponRequest(
                  code = "SAVE10",
                  discountType = DiscountType.PERCENT,
                  discountValue = 10
              )
          )
          assertThat(response.id).isEqualTo("CPN_123")
          assertThat(response.code).isEqualTo("SAVE10")
          assertThat(response.discountType).isEqualTo(DiscountType.PERCENT)
          assertThat(response.discountValue).isEqualTo(10)
      }

      @Test
      fun `get should return CouponResponse by id`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(couponMap("CPN_456"))
          val response = service.get("CPN_456")
          assertThat(response.id).isEqualTo("CPN_456")
      }

      @Test
      fun `delete should DELETE without a response body`() {
          mockFactory.nextBody = ByteArray(0)
          mockFactory.nextStatus = HttpStatus.NO_CONTENT
          // No exception = success
          service.delete("CPN_123")
      }

      @Test
      fun `applyToSubscription should POST without a response body`() {
          mockFactory.nextBody = ByteArray(0)
          mockFactory.nextStatus = HttpStatus.NO_CONTENT
          service.applyToSubscription("SUB_001", "CPN_123")
      }
  }
  ```

- [ ] Commit:
  ```
  git add pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/service/PagBankCouponServiceTest.kt
  git commit -m "test: add full behavioral tests for PagBankCouponService"
  ```

---

## Task 11 — Full test coverage for PagBankRefundService

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/service/PagBankRefundServiceTest.kt`

### Steps

- [ ] Replace the entire content of `PagBankRefundServiceTest.kt` with:

  ```kotlin
  package io.github.rodrigoma.pagbank.service

  import io.github.rodrigoma.pagbank.model.refund.*
  import com.fasterxml.jackson.databind.ObjectMapper
  import com.fasterxml.jackson.databind.PropertyNamingStrategies
  import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
  import org.assertj.core.api.Assertions.assertThat
  import org.junit.jupiter.api.BeforeEach
  import org.junit.jupiter.api.Test
  import org.springframework.http.HttpMethod
  import org.springframework.http.HttpStatus
  import org.springframework.http.MediaType
  import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
  import org.springframework.mock.http.client.MockClientHttpRequest
  import org.springframework.mock.http.client.MockClientHttpResponse
  import org.springframework.web.client.RestClient

  class PagBankRefundServiceTest {

      private lateinit var service: PagBankRefundService
      private val mapper: ObjectMapper = jacksonObjectMapper().apply {
          propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
      }

      private val mockFactory = object : org.springframework.http.client.ClientHttpRequestFactory {
          var nextBody: ByteArray = ByteArray(0)
          var nextStatus: HttpStatus = HttpStatus.OK
          var nextContentType: MediaType = MediaType.APPLICATION_JSON

          override fun createRequest(uri: java.net.URI, httpMethod: HttpMethod): org.springframework.http.client.ClientHttpRequest {
              val response = MockClientHttpResponse(nextBody, nextStatus)
              response.headers.contentType = nextContentType
              return MockClientHttpRequest(httpMethod, uri).also { it.setResponse(response) }
          }
      }

      @BeforeEach
      fun setUp() {
          val restClient = RestClient.builder()
              .requestFactory(mockFactory)
              .messageConverters { converters ->
                  converters.removeIf { it is MappingJackson2HttpMessageConverter }
                  converters.add(0, MappingJackson2HttpMessageConverter(mapper))
              }
              .build()
          service = PagBankRefundService(restClient)
      }

      private fun refundMap(id: String = "REF_123") = mapOf(
          "id" to id,
          "payment_id" to "PAY_001",
          "amount" to 2990,
          "status" to "COMPLETED",
          "created_at" to "2026-01-20T12:00:00Z"
      )

      @Test
      fun `create should POST full refund and return RefundResponse`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(refundMap())
          val response = service.create("PAY_001")
          assertThat(response.id).isEqualTo("REF_123")
          assertThat(response.paymentId).isEqualTo("PAY_001")
          assertThat(response.amount).isEqualTo(2990)
          assertThat(response.status).isEqualTo("COMPLETED")
      }

      @Test
      fun `create should POST partial refund with amount`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(refundMap())
          val response = service.create("PAY_001", RefundRequest(amount = 1000))
          assertThat(response.id).isEqualTo("REF_123")
      }

      @Test
      fun `get should return RefundResponse by id`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(refundMap("REF_456"))
          val response = service.get("REF_456")
          assertThat(response.id).isEqualTo("REF_456")
      }
  }
  ```

- [ ] Commit:
  ```
  git add pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/service/PagBankRefundServiceTest.kt
  git commit -m "test: add full behavioral tests for PagBankRefundService"
  ```

---

## Task 12 — Full test coverage for PagBankPreferenceService

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/service/PagBankPreferenceServiceTest.kt`

### Steps

- [ ] Replace the entire content of `PagBankPreferenceServiceTest.kt` with:

  ```kotlin
  package io.github.rodrigoma.pagbank.service

  import io.github.rodrigoma.pagbank.model.preference.*
  import com.fasterxml.jackson.databind.ObjectMapper
  import com.fasterxml.jackson.databind.PropertyNamingStrategies
  import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
  import org.assertj.core.api.Assertions.assertThat
  import org.junit.jupiter.api.BeforeEach
  import org.junit.jupiter.api.Test
  import org.springframework.http.HttpMethod
  import org.springframework.http.HttpStatus
  import org.springframework.http.MediaType
  import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
  import org.springframework.mock.http.client.MockClientHttpRequest
  import org.springframework.mock.http.client.MockClientHttpResponse
  import org.springframework.web.client.RestClient

  class PagBankPreferenceServiceTest {

      private lateinit var service: PagBankPreferenceService
      private val mapper: ObjectMapper = jacksonObjectMapper().apply {
          propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
      }

      private val mockFactory = object : org.springframework.http.client.ClientHttpRequestFactory {
          var nextBody: ByteArray = ByteArray(0)
          var nextStatus: HttpStatus = HttpStatus.OK
          var nextContentType: MediaType = MediaType.APPLICATION_JSON

          override fun createRequest(uri: java.net.URI, httpMethod: HttpMethod): org.springframework.http.client.ClientHttpRequest {
              val response = MockClientHttpResponse(nextBody, nextStatus)
              response.headers.contentType = nextContentType
              return MockClientHttpRequest(httpMethod, uri).also { it.setResponse(response) }
          }
      }

      @BeforeEach
      fun setUp() {
          val restClient = RestClient.builder()
              .requestFactory(mockFactory)
              .messageConverters { converters ->
                  converters.removeIf { it is MappingJackson2HttpMessageConverter }
                  converters.add(0, MappingJackson2HttpMessageConverter(mapper))
              }
              .build()
          service = PagBankPreferenceService(restClient)
      }

      private fun preferenceMap(retryDays: Int = 3, email: String? = "ops@example.com") = mapOf(
          "retry_days" to retryDays,
          "notification_email" to email
      )

      @Test
      fun `get should return PreferenceResponse`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(preferenceMap())
          val response = service.get()
          assertThat(response.retryDays).isEqualTo(3)
          assertThat(response.notificationEmail).isEqualTo("ops@example.com")
      }

      @Test
      fun `get should return PreferenceResponse with null notificationEmail`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(preferenceMap(retryDays = 5, email = null))
          val response = service.get()
          assertThat(response.retryDays).isEqualTo(5)
          assertThat(response.notificationEmail).isNull()
      }

      @Test
      fun `update should PUT and return updated PreferenceResponse`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(preferenceMap(retryDays = 7))
          val response = service.update(UpdatePreferenceRequest(retryDays = 7))
          assertThat(response.retryDays).isEqualTo(7)
      }

      @Test
      fun `update should PUT with notificationEmail and return updated PreferenceResponse`() {
          mockFactory.nextBody = mapper.writeValueAsBytes(
              preferenceMap(retryDays = 3, email = "new@example.com")
          )
          val response = service.update(
              UpdatePreferenceRequest(notificationEmail = "new@example.com")
          )
          assertThat(response.notificationEmail).isEqualTo("new@example.com")
      }
  }
  ```

- [ ] Commit:
  ```
  git add pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/service/PagBankPreferenceServiceTest.kt
  git commit -m "test: add full behavioral tests for PagBankPreferenceService"
  ```

---

## Task 13 — Verify all tests pass

**Files:** None (verification only)

### Steps

- [ ] Run the full test suite from the repository root:
  ```
  ./gradlew :pagbank-spring-boot-autoconfigure:test --info
  ```
  Expected: `BUILD SUCCESSFUL` with all tests passing. No `FAILED` lines in the output.

- [ ] If any test fails, check the HTML report at:
  ```
  pagbank-spring-boot-autoconfigure/build/reports/tests/test/index.html
  ```
  Fix the failing test by re-reading the service source and adjusting the mock data to match the exact field names. Commit the fix before proceeding.

- [ ] Verify the build compiles cleanly:
  ```
  ./gradlew build
  ```
  Expected output ends with: `BUILD SUCCESSFUL`

---

## Summary

| Task | Deliverable | Files changed |
|------|-------------|---------------|
| 1    | Apache 2.0 LICENSE | 1 created |
| 2    | README.md | 1 created |
| 3    | `ListParams` data class | 1 created |
| 4    | Plan pagination + tests | 2 modified |
| 5    | Customer pagination + full tests | 2 modified |
| 6    | Subscription pagination + full tests | 2 modified |
| 7    | Invoice pagination + full tests | 2 modified |
| 8    | Payment pagination + extended tests | 2 modified |
| 9    | Charge pagination + full tests | 2 modified |
| 10   | Coupon full tests | 1 modified |
| 11   | Refund full tests | 1 modified |
| 12   | Preference full tests | 1 modified |
| 13   | Verification | — |

**Total commits:** 12 (one per task except Task 13)
**New files:** 3 (`LICENSE`, `README.md`, `ListParams.kt`)
**Modified files:** 16 (6 service sources + 9 test files + 1 extended test)
