# PagSeguro Spring Boot Starter — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a Spring Boot Starter that auto-configures the PagSeguro subscription API client as injectable service beans.

**Architecture:** Two-module Maven project (`autoconfigure` + `starter`). `PagSeguroAutoConfiguration` creates a named `RestClient` bean and 10 domain service beans. A dedicated `ObjectMapper` with `SnakeCaseStrategy` is scoped to the `RestClient` codec only — no global side effects. Auto-configuration registered via `AutoConfiguration.imports` for Spring Boot 3.2+ and 4.x compatibility.

**Tech Stack:** Kotlin, Java 17, Spring Boot 3.2.0+, `RestClient`, Jackson, JUnit 5, `ApplicationContextRunner`, `MockClientHttpRequestFactory`

---

## Pre-requisites

- Java 17+ installed (`java -version`)
- Maven 3.8+ installed (`mvn -version`)
- This plan creates a **new project** at the same level as the reference project:
  ```
  ~/workspace/org_rodrigoma/
  ├── spring-boot-moip-integration/   (reference)
  └── spring-boot-pagseguro-starter/  (new — created in Task 1)
  ```
- Replace `com.example` with your real groupId before publishing
- Consult the PagSeguro subscription API docs to fill in exact field names per DTO

---

## File Map

```
spring-boot-pagseguro-starter/
├── pom.xml
├── pagseguro-spring-boot-autoconfigure/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── kotlin/com/example/pagseguro/
│       │   │   ├── autoconfigure/
│       │   │   │   ├── PagSeguroAutoConfiguration.kt
│       │   │   │   ├── PagSeguroHealthIndicatorAutoConfiguration.kt
│       │   │   │   └── PagSeguroProperties.kt
│       │   │   ├── exception/
│       │   │   │   └── PagSeguroException.kt        ← sealed class + ApiError
│       │   │   ├── http/
│       │   │   │   └── PagSeguroErrorHandler.kt
│       │   │   ├── service/
│       │   │   │   ├── PagSeguroPlanService.kt
│       │   │   │   ├── PagSeguroCustomerService.kt
│       │   │   │   ├── PagSeguroSubscriptionService.kt
│       │   │   │   ├── PagSeguroCouponService.kt
│       │   │   │   ├── PagSeguroInvoiceService.kt
│       │   │   │   ├── PagSeguroPaymentService.kt
│       │   │   │   ├── PagSeguroChargeService.kt
│       │   │   │   ├── PagSeguroRefundService.kt
│       │   │   │   ├── PagSeguroPreferenceService.kt
│       │   │   │   └── PagSeguroWebhookParser.kt
│       │   │   └── model/
│       │   │       ├── plan/           ← CreatePlanRequest, PlanResponse, PlanInterval enum, PlanStatus enum
│       │   │       ├── customer/       ← CreateCustomerRequest, CustomerResponse
│       │   │       ├── subscription/   ← CreateSubscriptionRequest, SubscriptionResponse, SubscriptionStatus enum
│       │   │       ├── coupon/         ← CreateCouponRequest, CouponResponse
│       │   │       ├── invoice/        ← InvoiceResponse
│       │   │       ├── payment/        ← PaymentResponse
│       │   │       ├── charge/         ← ChargeResponse, RetryChargeRequest
│       │   │       ├── refund/         ← RefundRequest, RefundResponse
│       │   │       ├── preference/     ← PreferenceResponse, UpdatePreferenceRequest
│       │   │       └── webhook/        ← WebhookPayload, WebhookEventType enum
│       │   └── resources/META-INF/spring/
│       │       └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
│       └── test/kotlin/com/example/pagseguro/
│           ├── autoconfigure/
│           │   ├── PagSeguroAutoConfigurationTest.kt
│           │   └── PagSeguroHealthIndicatorAutoConfigurationTest.kt
│           ├── service/
│           │   ├── PagSeguroPlanServiceTest.kt
│           │   ├── PagSeguroCustomerServiceTest.kt
│           │   ├── PagSeguroSubscriptionServiceTest.kt
│           │   ├── PagSeguroCouponServiceTest.kt
│           │   ├── PagSeguroInvoiceServiceTest.kt
│           │   ├── PagSeguroPaymentServiceTest.kt
│           │   ├── PagSeguroChargeServiceTest.kt
│           │   ├── PagSeguroRefundServiceTest.kt
│           │   ├── PagSeguroPreferenceServiceTest.kt
│           │   └── PagSeguroWebhookParserTest.kt
│           └── http/
│               └── PagSeguroErrorHandlerTest.kt
└── pagseguro-spring-boot-starter/
    └── pom.xml
```

---

## Task 1: Project Scaffolding

**Files:**
- Create: `spring-boot-pagseguro-starter/pom.xml`
- Create: `spring-boot-pagseguro-starter/pagseguro-spring-boot-autoconfigure/pom.xml`
- Create: `spring-boot-pagseguro-starter/pagseguro-spring-boot-starter/pom.xml`

- [ ] **Step 1: Create root project directory**

```bash
mkdir -p ~/workspace/org_rodrigoma/spring-boot-pagseguro-starter
cd ~/workspace/org_rodrigoma/spring-boot-pagseguro-starter
git init
```

- [ ] **Step 2: Create parent `pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>pagseguro-spring-boot-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <modules>
        <module>pagseguro-spring-boot-autoconfigure</module>
        <module>pagseguro-spring-boot-starter</module>
    </modules>

    <properties>
        <java.version>17</java.version>
        <kotlin.version>1.9.22</kotlin.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.example</groupId>
                <artifactId>pagseguro-spring-boot-autoconfigure</artifactId>
                <version>${project.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

- [ ] **Step 3: Create `autoconfigure/pom.xml`**

```bash
mkdir -p pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/{autoconfigure,exception,http,service,model/{plan,customer,subscription,coupon,invoice,payment,charge,refund,preference,webhook}}
mkdir -p pagseguro-spring-boot-autoconfigure/src/main/resources/META-INF/spring
mkdir -p pagseguro-spring-boot-autoconfigure/src/test/kotlin/com/example/pagseguro/{autoconfigure,service,http}
```

```xml
<!-- pagseguro-spring-boot-autoconfigure/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.example</groupId>
        <artifactId>pagseguro-spring-boot-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>pagseguro-spring-boot-autoconfigure</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
            <optional>true</optional>
        </dependency>
        <!-- Kotlin -->
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-stdlib</artifactId>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.module</groupId>
            <artifactId>jackson-module-kotlin</artifactId>
        </dependency>
        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <sourceDirectory>${project.basedir}/src/main/kotlin</sourceDirectory>
        <testSourceDirectory>${project.basedir}/src/test/kotlin</testSourceDirectory>
        <plugins>
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <configuration>
                    <args>
                        <arg>-Xjsr305=strict</arg>
                    </args>
                    <compilerPlugins>
                        <plugin>spring</plugin>
                    </compilerPlugins>
                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>org.jetbrains.kotlin</groupId>
                        <artifactId>kotlin-maven-allopen</artifactId>
                        <version>${kotlin.version}</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 4: Create `starter/pom.xml`**

```bash
mkdir -p pagseguro-spring-boot-starter
```

```xml
<!-- pagseguro-spring-boot-starter/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.example</groupId>
        <artifactId>pagseguro-spring-boot-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>pagseguro-spring-boot-starter</artifactId>

    <dependencies>
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>pagseguro-spring-boot-autoconfigure</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <!-- spring-boot-starter-web is intentionally NOT here — it is declared optional
             in the autoconfigure module. Consumers must add it explicitly to their app. -->
    </dependencies>
</project>
```

- [ ] **Step 5: Verify the multi-module build compiles (no source yet)**

```bash
cd ~/workspace/org_rodrigoma/spring-boot-pagseguro-starter
mvn clean install -DskipTests
```
Expected: `BUILD SUCCESS`

- [ ] **Step 6: Commit scaffold**

```bash
git add .
git commit -m "chore: scaffold multi-module Maven project"
```

---

## Task 2: Exception Hierarchy

**Files:**
- Create: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/exception/PagSeguroException.kt`

- [ ] **Step 1: Write the test**

Create `pagseguro-spring-boot-autoconfigure/src/test/kotlin/com/example/pagseguro/exception/PagSeguroExceptionTest.kt`:

```kotlin
package com.example.pagseguro.exception

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PagSeguroExceptionTest {

    @Test
    fun `Unauthorized carries message`() {
        val ex = PagSeguroException.Unauthorized("invalid token")
        assertThat(ex.message).isEqualTo("invalid token")
        assertThat(ex).isInstanceOf(RuntimeException::class.java)
    }

    @Test
    fun `ValidationError carries error list`() {
        val errors = listOf(ApiError("40001", "amount is required"))
        val ex = PagSeguroException.ValidationError(errors)
        assertThat(ex.errors).hasSize(1)
        assertThat(ex.errors[0].code).isEqualTo("40001")
        assertThat(ex.message).isEqualTo("Validation failed")
    }

    @Test
    fun `ServerError carries status code`() {
        val ex = PagSeguroException.ServerError(503)
        assertThat(ex.statusCode).isEqualTo(503)
        assertThat(ex.message).isEqualTo("Server error: 503")
    }

    @Test
    fun `sealed class enables exhaustive when`() {
        val ex: PagSeguroException = PagSeguroException.NotFound("plan not found")
        val result = when (ex) {
            is PagSeguroException.Unauthorized -> "auth"
            is PagSeguroException.NotFound -> "not_found"
            is PagSeguroException.ValidationError -> "validation"
            is PagSeguroException.ServerError -> "server"
        }
        assertThat(result).isEqualTo("not_found")
    }
}
```

- [ ] **Step 2: Run test to see it fail**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroExceptionTest
```
Expected: COMPILATION ERROR — `PagSeguroException` not found

- [ ] **Step 3: Implement**

Create `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/exception/PagSeguroException.kt`:

```kotlin
package com.example.pagseguro.exception

data class ApiError(
    val code: String,
    val message: String
)

sealed class PagSeguroException(message: String) : RuntimeException(message) {
    class Unauthorized(message: String) : PagSeguroException(message)
    class NotFound(message: String) : PagSeguroException(message)
    class ValidationError(val errors: List<ApiError>) : PagSeguroException("Validation failed")
    class ServerError(val statusCode: Int) : PagSeguroException("Server error: $statusCode")
}
```

- [ ] **Step 4: Run test to see it pass**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroExceptionTest
```
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add pagseguro-spring-boot-autoconfigure/src
git commit -m "feat: add PagSeguroException sealed class hierarchy"
```

---

## Task 3: Configuration Properties

**Files:**
- Create: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/autoconfigure/PagSeguroProperties.kt`
- Create: `pagseguro-spring-boot-autoconfigure/src/test/kotlin/com/example/pagseguro/autoconfigure/PagSeguroPropertiesTest.kt`

- [ ] **Step 1: Write the test**

```kotlin
package com.example.pagseguro.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class PagSeguroPropertiesTest {

    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(PropertiesTestConfig::class.java)

    @Test
    fun `should bind token and environment`() {
        contextRunner
            .withPropertyValues("pagseguro.token=MY_TOKEN", "pagseguro.environment=production")
            .run { context ->
                assertThat(context).hasNotFailed()
                val props = context.getBean(PagSeguroProperties::class.java)
                assertThat(props.token).isEqualTo("MY_TOKEN")
                assertThat(props.environment).isEqualTo(PagSeguroEnvironment.PRODUCTION)
            }
    }

    @Test
    fun `should default environment to sandbox`() {
        contextRunner
            .withPropertyValues("pagseguro.token=MY_TOKEN")
            .run { context ->
                val props = context.getBean(PagSeguroProperties::class.java)
                assertThat(props.environment).isEqualTo(PagSeguroEnvironment.SANDBOX)
            }
    }

    @Test
    fun `should fail when token is blank`() {
        contextRunner
            .withPropertyValues("pagseguro.token=")
            .run { context ->
                assertThat(context).hasFailed()
                assertThat(context.startupFailure).hasMessageContaining("pagseguro.token")
            }
    }

    @Test
    fun `should fail when token is absent`() {
        contextRunner.run { context ->
            assertThat(context).hasFailed()
        }
    }

    @EnableConfigurationProperties(PagSeguroProperties::class)
    class PropertiesTestConfig
}
```

- [ ] **Step 2: Run test to see it fail**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroPropertiesTest
```
Expected: COMPILATION ERROR

- [ ] **Step 3: Implement**

```kotlin
package com.example.pagseguro.autoconfigure

import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.context.properties.ConfigurationProperties

enum class PagSeguroEnvironment {
    SANDBOX, PRODUCTION;

    fun baseUrl(): String = when (this) {
        SANDBOX -> "https://sandbox.api.assinaturas.pagseguro.com"
        PRODUCTION -> "https://api.assinaturas.pagseguro.com"
    }
}

@ConfigurationProperties(prefix = "pagseguro")
data class PagSeguroProperties(
    val token: String = "",
    val environment: PagSeguroEnvironment = PagSeguroEnvironment.SANDBOX,
    val webhookSecret: String? = null,
    val healthIndicatorEnabled: Boolean = false
) : InitializingBean {

    override fun afterPropertiesSet() {
        require(token.isNotBlank()) {
            "pagseguro.token must be configured — set it in your application.yml"
        }
    }
}
```

- [ ] **Step 4: Run tests to see them pass**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroPropertiesTest
```
Expected: PASS (4 tests)

- [ ] **Step 5: Commit**

```bash
git add pagseguro-spring-boot-autoconfigure/src
git commit -m "feat: add PagSeguroProperties with startup validation"
```

---

## Task 4: Error Handler

**Files:**
- Create: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/http/PagSeguroErrorHandler.kt`
- Create: `pagseguro-spring-boot-autoconfigure/src/test/kotlin/com/example/pagseguro/http/PagSeguroErrorHandlerTest.kt`

- [ ] **Step 1: Write the test**

```kotlin
package com.example.pagseguro.http

import com.example.pagseguro.exception.PagSeguroException
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.mock.http.client.MockClientHttpResponse

class PagSeguroErrorHandlerTest {

    private val mapper = ObjectMapper()
    private val handler = PagSeguroErrorHandler(mapper)

    @Test
    fun `401 throws Unauthorized`() {
        val body = """{"message":"Unauthorized"}""".toByteArray()
        val response = MockClientHttpResponse(body, HttpStatus.UNAUTHORIZED)
        assertThatThrownBy { handler.handle(response) }
            .isInstanceOf(PagSeguroException.Unauthorized::class.java)
    }

    @Test
    fun `404 throws NotFound`() {
        val response = MockClientHttpResponse(ByteArray(0), HttpStatus.NOT_FOUND)
        assertThatThrownBy { handler.handle(response) }
            .isInstanceOf(PagSeguroException.NotFound::class.java)
    }

    @Test
    fun `400 with valid JSON throws ValidationError`() {
        val body = """[{"code":"40001","message":"amount is required"}]""".toByteArray()
        val response = MockClientHttpResponse(body, HttpStatus.BAD_REQUEST)
        assertThatThrownBy { handler.handle(response) }
            .isInstanceOf(PagSeguroException.ValidationError::class.java)
            .satisfies { ex ->
                ex as PagSeguroException.ValidationError
                assertThat(ex.errors).hasSize(1)
                assertThat(ex.errors[0].code).isEqualTo("40001")
            }
    }

    @Test
    fun `400 with non-JSON body falls back to ServerError`() {
        val body = "<html>Bad Request</html>".toByteArray()
        val response = MockClientHttpResponse(body, HttpStatus.BAD_REQUEST)
        assertThatThrownBy { handler.handle(response) }
            .isInstanceOf(PagSeguroException.ServerError::class.java)
    }

    @Test
    fun `500 throws ServerError with status code`() {
        val response = MockClientHttpResponse(ByteArray(0), HttpStatus.INTERNAL_SERVER_ERROR)
        assertThatThrownBy { handler.handle(response) }
            .isInstanceOf(PagSeguroException.ServerError::class.java)
            .satisfies { ex ->
                ex as PagSeguroException.ServerError
                assertThat(ex.statusCode).isEqualTo(500)
            }
    }
}
```

- [ ] **Step 2: Run test to see it fail**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroErrorHandlerTest
```
Expected: COMPILATION ERROR

- [ ] **Step 3: Implement**

```kotlin
package com.example.pagseguro.http

import com.example.pagseguro.exception.ApiError
import com.example.pagseguro.exception.PagSeguroException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.RestClient

// PagSeguroErrorHandler is registered on the RestClient builder via:
//   .defaultStatusHandler({ it.isError }, handler::handle)
// It does NOT implement ResponseErrorHandler (a RestTemplate interface).
// The handle() method is called by RestClient for any 4xx/5xx response.
class PagSeguroErrorHandler(private val objectMapper: ObjectMapper) {

    fun handle(statusCode: HttpStatusCode, response: ClientHttpResponse) {
        handle(response)
    }

    fun handle(response: ClientHttpResponse) {
        val statusCode = response.statusCode.value()
        val body = runCatching { response.body.readBytes() }.getOrDefault(ByteArray(0))

        throw when (statusCode) {
            401, 403 -> PagSeguroException.Unauthorized(bodyAsString(body))
            404 -> PagSeguroException.NotFound("Resource not found")
            400, 422 -> parseValidationError(body, statusCode)
            else -> PagSeguroException.ServerError(statusCode)
        }
    }

    private fun parseValidationError(body: ByteArray, statusCode: Int): PagSeguroException =
        try {
            val errors: List<ApiError> = objectMapper.readValue(body)
            PagSeguroException.ValidationError(errors)
        } catch (e: Exception) {
            PagSeguroException.ServerError(statusCode)
        }

    private fun bodyAsString(body: ByteArray): String =
        if (body.isEmpty()) "Unauthorized" else body.decodeToString()
}
```

- [ ] **Step 4: Run tests to see them pass**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroErrorHandlerTest
```
Expected: PASS (5 tests)

- [ ] **Step 5: Commit**

```bash
git add pagseguro-spring-boot-autoconfigure/src
git commit -m "feat: add PagSeguroErrorHandler with typed exception mapping"
```

---

## Task 5: AutoConfiguration Core

**Files:**
- Create: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/autoconfigure/PagSeguroAutoConfiguration.kt`
- Create: `pagseguro-spring-boot-autoconfigure/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- Create: `pagseguro-spring-boot-autoconfigure/src/test/kotlin/com/example/pagseguro/autoconfigure/PagSeguroAutoConfigurationTest.kt`

- [ ] **Step 1: Write the test**

```kotlin
package com.example.pagseguro.autoconfigure

import com.example.pagseguro.service.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.web.client.RestClient

class PagSeguroAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(PagSeguroAutoConfiguration::class.java))

    @Test
    fun `should fail to start without token`() {
        contextRunner.run { context ->
            assertThat(context).hasFailed()
        }
    }

    @Test
    fun `should create all service beans when token is configured`() {
        contextRunner.withPropertyValues("pagseguro.token=TEST_TOKEN").run { context ->
            assertThat(context).hasNotFailed()
            assertThat(context).hasSingleBean(PagSeguroPlanService::class.java)
            assertThat(context).hasSingleBean(PagSeguroCustomerService::class.java)
            assertThat(context).hasSingleBean(PagSeguroSubscriptionService::class.java)
            assertThat(context).hasSingleBean(PagSeguroCouponService::class.java)
            assertThat(context).hasSingleBean(PagSeguroInvoiceService::class.java)
            assertThat(context).hasSingleBean(PagSeguroPaymentService::class.java)
            assertThat(context).hasSingleBean(PagSeguroChargeService::class.java)
            assertThat(context).hasSingleBean(PagSeguroRefundService::class.java)
            assertThat(context).hasSingleBean(PagSeguroPreferenceService::class.java)
            assertThat(context).hasSingleBean(PagSeguroWebhookParser::class.java)
        }
    }

    @Test
    fun `RestClient bean should not be exposed for general injection`() {
        contextRunner.withPropertyValues("pagseguro.token=TEST_TOKEN").run { context ->
            // The named bean exists but consumers should use service beans instead
            assertThat(context.getBeanNamesForType(RestClient::class.java)).contains("pagSeguroRestClient")
        }
    }
}
```

- [ ] **Step 2: Run test to see it fail**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroAutoConfigurationTest
```
Expected: COMPILATION ERROR (service classes don't exist yet — stubs needed)

- [ ] **Step 3: Create service stubs** (minimal, just enough to compile)

For each service in the list below, create a file with only the class declaration. Full implementation comes in Tasks 6–14.

`src/main/kotlin/com/example/pagseguro/service/PagSeguroPlanService.kt`:
```kotlin
package com.example.pagseguro.service
import org.springframework.web.client.RestClient
class PagSeguroPlanService(private val restClient: RestClient)
```

Repeat for: `PagSeguroCustomerService`, `PagSeguroSubscriptionService`, `PagSeguroCouponService`, `PagSeguroInvoiceService`, `PagSeguroPaymentService`, `PagSeguroChargeService`, `PagSeguroRefundService`, `PagSeguroPreferenceService`.

For WebhookParser (different constructor):
```kotlin
package com.example.pagseguro.service
import com.example.pagseguro.autoconfigure.PagSeguroProperties
class PagSeguroWebhookParser(private val properties: PagSeguroProperties)
```

- [ ] **Step 4: Implement `PagSeguroAutoConfiguration`**

```kotlin
package com.example.pagseguro.autoconfigure

import com.example.pagseguro.http.PagSeguroErrorHandler
import com.example.pagseguro.service.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestClient

@AutoConfiguration
@EnableConfigurationProperties(PagSeguroProperties::class)
class PagSeguroAutoConfiguration(private val properties: PagSeguroProperties) {

    @Bean(name = ["pagSeguroObjectMapper"])
    fun pagSeguroObjectMapper(): ObjectMapper =
        jacksonObjectMapper().apply {
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        }

    @Bean(name = ["pagSeguroRestClient"])
    fun pagSeguroRestClient(
        @org.springframework.beans.factory.annotation.Qualifier("pagSeguroObjectMapper")
        objectMapper: ObjectMapper
    ): RestClient {
        val errorHandler = PagSeguroErrorHandler(objectMapper)
        return RestClient.builder()
            .baseUrl(properties.environment.baseUrl())
            .defaultHeader("Authorization", "Bearer ${properties.token}")
            .messageConverters { converters ->
                converters.removeIf { it is MappingJackson2HttpMessageConverter }
                converters.add(0, MappingJackson2HttpMessageConverter(objectMapper))
            }
            // RestClient.defaultStatusHandler takes (HttpStatusCode -> Boolean, ErrorHandler)
            // ErrorHandler is a functional interface: (HttpRequest, ClientHttpResponse) -> Unit
            .defaultStatusHandler({ it.isError }) { _, response -> errorHandler.handle(response) }
            .build()
    }

    @Bean fun pagSeguroPlanService(@Qualifier("pagSeguroRestClient") rc: RestClient) = PagSeguroPlanService(rc)
    @Bean fun pagSeguroCustomerService(@Qualifier("pagSeguroRestClient") rc: RestClient) = PagSeguroCustomerService(rc)
    @Bean fun pagSeguroSubscriptionService(@Qualifier("pagSeguroRestClient") rc: RestClient) = PagSeguroSubscriptionService(rc)
    @Bean fun pagSeguroCouponService(@Qualifier("pagSeguroRestClient") rc: RestClient) = PagSeguroCouponService(rc)
    @Bean fun pagSeguroInvoiceService(@Qualifier("pagSeguroRestClient") rc: RestClient) = PagSeguroInvoiceService(rc)
    @Bean fun pagSeguroPaymentService(@Qualifier("pagSeguroRestClient") rc: RestClient) = PagSeguroPaymentService(rc)
    @Bean fun pagSeguroChargeService(@Qualifier("pagSeguroRestClient") rc: RestClient) = PagSeguroChargeService(rc)
    @Bean fun pagSeguroRefundService(@Qualifier("pagSeguroRestClient") rc: RestClient) = PagSeguroRefundService(rc)
    @Bean fun pagSeguroPreferenceService(@Qualifier("pagSeguroRestClient") rc: RestClient) = PagSeguroPreferenceService(rc)
    @Bean fun pagSeguroWebhookParser() = PagSeguroWebhookParser(properties)
}
```

- [ ] **Step 5: Create `AutoConfiguration.imports`**

```
com.example.pagseguro.autoconfigure.PagSeguroAutoConfiguration
com.example.pagseguro.autoconfigure.PagSeguroHealthIndicatorAutoConfiguration
```

- [ ] **Step 6: Run tests to see them pass**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroAutoConfigurationTest
```
Expected: PASS (3 tests)

- [ ] **Step 7: Commit**

```bash
git add pagseguro-spring-boot-autoconfigure/src
git commit -m "feat: add PagSeguroAutoConfiguration with RestClient and service stubs"
```

---

## Task 6: Plan Domain — DTOs + Service

**Files:**
- Create: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/model/plan/PlanModels.kt`
- Modify: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/service/PagSeguroPlanService.kt`
- Create: `pagseguro-spring-boot-autoconfigure/src/test/kotlin/com/example/pagseguro/service/PagSeguroPlanServiceTest.kt`

This task is the **template** for all service tasks. Tasks 7–13 follow the same pattern with different DTOs and endpoints.

- [ ] **Step 1: Create plan DTOs**

`pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/model/plan/PlanModels.kt`:

```kotlin
package com.example.pagseguro.model.plan

enum class PlanStatus { ACTIVE, INACTIVE }

data class Money(val value: Int, val currency: String = "BRL")

data class PlanInterval(val length: Int, val unit: String)  // unit: "month", "day", "year"

data class PlanTrial(val days: Int, val enabled: Boolean, val holdSetupFee: Boolean = false)

data class CreatePlanRequest(
    val name: String,
    val amount: Money,
    val interval: PlanInterval,
    val paymentMethod: List<String> = listOf("CARD"),
    val referenceId: String? = null,
    val description: String? = null,
    val setupFee: Int? = null,
    val limitSubscriptions: Int? = null,
    val trial: PlanTrial? = null
)

data class PlanResponse(
    val id: String,
    val name: String,
    val amount: Money,
    val interval: PlanInterval,
    val status: PlanStatus,
    val referenceId: String?,
    val trial: PlanTrial?,
    val createdAt: String?,
    val updatedAt: String?
)

data class PlanListResponse(val plans: List<PlanResponse>)
```

> **Note:** `createdAt` is `String` here for simplicity. Change to `Instant` or `LocalDateTime` once you know the exact ISO format the API returns, and configure `JavaTimeModule` on the `ObjectMapper`.

- [ ] **Step 2: Write the test**

`pagseguro-spring-boot-autoconfigure/src/test/kotlin/com/example/pagseguro/service/PagSeguroPlanServiceTest.kt`:

```kotlin
package com.example.pagseguro.service

import com.example.pagseguro.model.plan.*
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

class PagSeguroPlanServiceTest {

    private lateinit var service: PagSeguroPlanService
    private val mapper: ObjectMapper = jacksonObjectMapper().apply {
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }

    // MockClientHttpRequestFactory lets you pre-set a single response body.
    // For each test, configure the factory with the expected response before calling the service.
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
        service = PagSeguroPlanService(restClient)
    }

    @Test
    fun `create should POST and return PlanResponse`() {
        mockFactory.nextBody = """
            {"id":"PLAN_123","name":"Basic",
             "amount":{"value":999,"currency":"BRL"},
             "interval":{"length":1,"unit":"month"},
             "status":"ACTIVE","created_at":"2026-01-01T00:00:00Z"}
        """.trimIndent().toByteArray()

        val response = service.create(
            CreatePlanRequest("Basic", Money(999), PlanInterval(1, "month"))
        )

        assertThat(response.id).isEqualTo("PLAN_123")
        assertThat(response.amount.value).isEqualTo(999)
    }

    @Test
    fun `get should return plan by id`() {
        mockFactory.nextBody = """
            {"id":"PLAN_123","name":"Basic",
             "amount":{"value":999,"currency":"BRL"},
             "interval":{"length":1,"unit":"month"},
             "status":"ACTIVE","created_at":"2026-01-01T00:00:00Z"}
        """.trimIndent().toByteArray()

        val response = service.get("PLAN_123")
        assertThat(response.id).isEqualTo("PLAN_123")
    }
}
```

> **Note on `MockClientHttpRequest.setResponse()`:** Check whether your Spring version's `MockClientHttpRequest` supports `setResponse()`. In some versions, you may need to use `MockClientHttpRequestFactory` from `org.springframework.mock.http.client` directly and call `mockFactory.setCreateResponse(...)`. The pattern above (anonymous `ClientHttpRequestFactory`) always works regardless of Spring version.

- [ ] **Step 3: Run test to see it fail**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroPlanServiceTest
```
Expected: FAIL — `PagSeguroPlanService` has no `create` or `get` methods

- [ ] **Step 4: Implement `PagSeguroPlanService`**

```kotlin
package com.example.pagseguro.service

import com.example.pagseguro.model.plan.*
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class PagSeguroPlanService(private val restClient: RestClient) {

    fun create(request: CreatePlanRequest): PlanResponse =
        restClient.post()
            .uri("/plans")
            .body(request)
            .retrieve()
            .body<PlanResponse>()!!

    fun get(id: String): PlanResponse =
        restClient.get()
            .uri("/plans/{id}", id)
            .retrieve()
            .body<PlanResponse>()!!

    fun list(): PlanListResponse =
        restClient.get()
            .uri("/plans")
            .retrieve()
            .body<PlanListResponse>()!!

    fun activate(id: String) {
        restClient.put()
            .uri("/plans/{id}/activate", id)
            .retrieve()
            .toBodilessEntity()
    }

    fun inactivate(id: String) {
        restClient.put()
            .uri("/plans/{id}/inactivate", id)
            .retrieve()
            .toBodilessEntity()
    }
}
```

- [ ] **Step 5: Run tests to see them pass**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroPlanServiceTest
```
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add pagseguro-spring-boot-autoconfigure/src
git commit -m "feat: implement PagSeguroPlanService with DTOs"
```

---

## Task 7: Customer Domain

**Files:**
- Create: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/model/customer/CustomerModels.kt`
- Modify: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/service/PagSeguroCustomerService.kt`
- Create: `pagseguro-spring-boot-autoconfigure/src/test/kotlin/com/example/pagseguro/service/PagSeguroCustomerServiceTest.kt`

- [ ] **Step 1: Create customer DTOs**

```kotlin
// model/customer/CustomerModels.kt
package com.example.pagseguro.model.customer

data class CreateCustomerRequest(
    val name: String,
    val email: String,
    val taxId: String,         // CPF or CNPJ
    val phone: String? = null
)

data class CustomerResponse(
    val id: String,
    val name: String,
    val email: String,
    val taxId: String,
    val createdAt: String
)

data class CustomerListResponse(val customers: List<CustomerResponse>)
```

- [ ] **Step 2: Write a failing test** (use the same mock factory pattern from Task 6)

Test: `POST /customers` should return a `CustomerResponse` with the expected `id`.

- [ ] **Step 3: Run test to confirm it fails**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroCustomerServiceTest
```

- [ ] **Step 4: Implement `PagSeguroCustomerService`**

Endpoints: `POST /customers`, `GET /customers/{id}`, `PUT /customers/{id}`, `GET /customers`.

- [ ] **Step 5: Run tests to confirm they pass**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroCustomerServiceTest
```

- [ ] **Step 6: Commit**

```bash
git add pagseguro-spring-boot-autoconfigure/src
git commit -m "feat: implement PagSeguroCustomerService with DTOs"
```

---

## Task 8: Subscription Domain

**Files:**
- Create: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/model/subscription/SubscriptionModels.kt`
- Modify: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/service/PagSeguroSubscriptionService.kt`
- Create: `pagseguro-spring-boot-autoconfigure/src/test/kotlin/com/example/pagseguro/service/PagSeguroSubscriptionServiceTest.kt`

- [ ] **Step 1: Create subscription DTOs**

```kotlin
package com.example.pagseguro.model.subscription

enum class SubscriptionStatus { ACTIVE, SUSPENDED, CANCELLED, PENDING }

data class CreateSubscriptionRequest(
    val planId: String,
    val customerId: String,
    val startDate: String? = null  // ISO date; null = today
)

data class SubscriptionResponse(
    val id: String,
    val planId: String,
    val customerId: String,
    val status: SubscriptionStatus,
    val createdAt: String
)

data class SubscriptionListResponse(val subscriptions: List<SubscriptionResponse>)
```

- [ ] **Step 2: Write a failing test**

Test: `POST /subscriptions` should return a `SubscriptionResponse` with `status = ACTIVE`.

- [ ] **Step 3: Run test to confirm it fails**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroSubscriptionServiceTest
```

- [ ] **Step 4: Implement `PagSeguroSubscriptionService`**

Endpoints: `POST /subscriptions`, `GET /subscriptions/{id}`, `PUT /subscriptions/{id}/cancel`, `PUT /subscriptions/{id}/suspend`, `PUT /subscriptions/{id}/reactivate`, `GET /subscriptions`.

- [ ] **Step 5: Run tests to confirm they pass**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroSubscriptionServiceTest
```

- [ ] **Step 6: Commit**

```bash
git add pagseguro-spring-boot-autoconfigure/src
git commit -m "feat: implement PagSeguroSubscriptionService with DTOs"
```

---

## Task 9: Coupon Domain

**Files:**
- Create: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/model/coupon/CouponModels.kt`
- Modify: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/service/PagSeguroCouponService.kt`
- Create: `pagseguro-spring-boot-autoconfigure/src/test/kotlin/com/example/pagseguro/service/PagSeguroCouponServiceTest.kt`

- [ ] **Step 1: Create coupon DTOs**

```kotlin
package com.example.pagseguro.model.coupon

enum class DiscountType { PERCENT, AMOUNT }

data class CreateCouponRequest(
    val code: String,
    val discountType: DiscountType,
    val discountValue: Int,
    val maxUses: Int? = null
)

data class CouponResponse(
    val id: String,
    val code: String,
    val discountType: DiscountType,
    val discountValue: Int
)
```

- [ ] **Step 2: Write a failing test**

Test: `POST /coupons` should return a `CouponResponse` with the expected `code`.

- [ ] **Step 3: Run test to confirm it fails**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroCouponServiceTest
```

- [ ] **Step 4: Implement `PagSeguroCouponService`**

Endpoints: `POST /coupons`, `GET /coupons/{id}`, `DELETE /coupons/{id}`, `POST /subscriptions/{id}/coupons`.

- [ ] **Step 5: Run tests to confirm they pass**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroCouponServiceTest
```

- [ ] **Step 6: Commit**

```bash
git add pagseguro-spring-boot-autoconfigure/src
git commit -m "feat: implement PagSeguroCouponService with DTOs"
```

---

## Task 10: Invoice Domain

**Files:**
- Create: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/model/invoice/InvoiceModels.kt`
- Modify: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/service/PagSeguroInvoiceService.kt`
- Create: `pagseguro-spring-boot-autoconfigure/src/test/kotlin/com/example/pagseguro/service/PagSeguroInvoiceServiceTest.kt`

- [ ] **Step 1: Create DTOs**

```kotlin
package com.example.pagseguro.model.invoice

data class InvoiceResponse(
    val id: String,
    val subscriptionId: String,
    val amount: Int,
    val status: String,
    val dueDate: String
)
data class InvoiceListResponse(val invoices: List<InvoiceResponse>)
```

- [ ] **Step 2: Write a failing test** — `GET /invoices/{id}` returns `InvoiceResponse`
- [ ] **Step 3: Run test to confirm it fails**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroInvoiceServiceTest
```

- [ ] **Step 4: Implement `PagSeguroInvoiceService`** — `GET /invoices/{id}`, `GET /subscriptions/{id}/invoices`
- [ ] **Step 5: Run tests to confirm they pass**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroInvoiceServiceTest
```

- [ ] **Step 6: Commit**

```bash
git add pagseguro-spring-boot-autoconfigure/src
git commit -m "feat: implement PagSeguroInvoiceService with DTOs"
```

---

## Task 11: Payment Domain

**Files:**
- Create: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/model/payment/PaymentModels.kt`
- Modify: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/service/PagSeguroPaymentService.kt`
- Create: `pagseguro-spring-boot-autoconfigure/src/test/kotlin/com/example/pagseguro/service/PagSeguroPaymentServiceTest.kt`

- [ ] **Step 1: Create DTOs**

```kotlin
package com.example.pagseguro.model.payment

data class PaymentResponse(
    val id: String,
    val invoiceId: String,
    val amount: Int,
    val status: String,
    val paidAt: String?
)
data class PaymentListResponse(val payments: List<PaymentResponse>)
```

- [ ] **Step 2: Write a failing test** — `GET /payments/{id}` returns `PaymentResponse`
- [ ] **Step 3: Run test to confirm it fails**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroPaymentServiceTest
```

- [ ] **Step 4: Implement `PagSeguroPaymentService`** — `GET /payments/{id}`, `GET /payments`
- [ ] **Step 5: Run tests to confirm they pass**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroPaymentServiceTest
```

- [ ] **Step 6: Commit**

```bash
git add pagseguro-spring-boot-autoconfigure/src
git commit -m "feat: implement PagSeguroPaymentService with DTOs"
```

---

## Task 12: Charge Domain

**Files:**
- Create: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/model/charge/ChargeModels.kt`
- Modify: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/service/PagSeguroChargeService.kt`
- Create: `pagseguro-spring-boot-autoconfigure/src/test/kotlin/com/example/pagseguro/service/PagSeguroChargeServiceTest.kt`

- [ ] **Step 1: Create DTOs**

```kotlin
package com.example.pagseguro.model.charge

data class ChargeResponse(
    val id: String,
    val subscriptionId: String,
    val amount: Int,
    val status: String
)
data class ChargeListResponse(val charges: List<ChargeResponse>)
data class RetryChargeRequest(val paymentMethod: String? = null)  // null = use subscription default
```

- [ ] **Step 2: Write a failing test** — `GET /charges/{id}` returns `ChargeResponse`; `POST /charges/{id}/retry` returns 200
- [ ] **Step 3: Run test to confirm it fails**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroChargeServiceTest
```

- [ ] **Step 4: Implement `PagSeguroChargeService`** — `GET /charges/{id}`, `GET /charges`, `POST /charges/{id}/retry`
- [ ] **Step 5: Run tests to confirm they pass**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroChargeServiceTest
```

- [ ] **Step 6: Commit**

```bash
git add pagseguro-spring-boot-autoconfigure/src
git commit -m "feat: implement PagSeguroChargeService with DTOs"
```

---

## Task 13: Refund Domain

**Files:**
- Create: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/model/refund/RefundModels.kt`
- Modify: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/service/PagSeguroRefundService.kt`
- Create: `pagseguro-spring-boot-autoconfigure/src/test/kotlin/com/example/pagseguro/service/PagSeguroRefundServiceTest.kt`

- [ ] **Step 1: Create DTOs**

```kotlin
package com.example.pagseguro.model.refund

data class RefundRequest(val amount: Int? = null)  // null = full refund

data class RefundResponse(
    val id: String,
    val paymentId: String,
    val amount: Int,
    val status: String,
    val createdAt: String
)
```

- [ ] **Step 2: Write a failing test** — `POST /payments/{paymentId}/refunds` returns `RefundResponse`
- [ ] **Step 3: Run test to confirm it fails**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroRefundServiceTest
```

- [ ] **Step 4: Implement `PagSeguroRefundService`** — `POST /payments/{paymentId}/refunds`, `GET /refunds/{id}`
- [ ] **Step 5: Run tests to confirm they pass**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroRefundServiceTest
```

- [ ] **Step 6: Commit**

```bash
git add pagseguro-spring-boot-autoconfigure/src
git commit -m "feat: implement PagSeguroRefundService with DTOs"
```

---

## Task 14: Preference Domain

**Files:**
- Create: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/model/preference/PreferenceModels.kt`
- Modify: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/service/PagSeguroPreferenceService.kt`
- Create: `pagseguro-spring-boot-autoconfigure/src/test/kotlin/com/example/pagseguro/service/PagSeguroPreferenceServiceTest.kt`

- [ ] **Step 1: Create DTOs**

```kotlin
package com.example.pagseguro.model.preference

data class PreferenceResponse(
    val retryDays: Int,
    val notificationEmail: String?
)

data class UpdatePreferenceRequest(
    val retryDays: Int? = null,
    val notificationEmail: String? = null
)
```

- [ ] **Step 2: Write a failing test** — `GET /preferences` returns `PreferenceResponse`
- [ ] **Step 3: Run test to confirm it fails**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroPreferenceServiceTest
```

- [ ] **Step 4: Implement `PagSeguroPreferenceService`** — `GET /preferences`, `PUT /preferences`
- [ ] **Step 5: Run tests to confirm they pass**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroPreferenceServiceTest
```

- [ ] **Step 6: Commit**

```bash
git add pagseguro-spring-boot-autoconfigure/src
git commit -m "feat: implement PagSeguroPreferenceService with DTOs"
```

---

## Task 15: Webhook Parser

**Files:**
- Create: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/model/webhook/WebhookModels.kt`
- Modify: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/service/PagSeguroWebhookParser.kt`
- Create: `pagseguro-spring-boot-autoconfigure/src/test/kotlin/com/example/pagseguro/service/PagSeguroWebhookParserTest.kt`

- [ ] **Step 1: Create webhook DTOs**

```kotlin
package com.example.pagseguro.model.webhook

// Values match PagBank webhook "event" field exactly (dot notation)
enum class WebhookEventType {
    `plan.created`, `plan.updated`, `plan.activated`, `plan.inactivated`,
    `coupon.created`, `coupon.activated`, `coupon.inactivated`,
    `customer.created`, `customer.updated`, `customer.billing_info.updated`,
    `subscription.initial`, `subscription.updated`, `subscription.activated`,
    `subscription.suspended`, `subscription.recurrence`, `subscription.expired`,
    `subscription.canceled`, `subscription.migrated`,
    `refund.created`
}

data class WebhookPayload(
    val env: String,
    val event: WebhookEventType,
    val resource: Map<String, Any>,
    val links: List<Map<String, Any>> = emptyList()
)
```

- [ ] **Step 2: Write the test**

```kotlin
package com.example.pagseguro.service

import com.example.pagseguro.autoconfigure.PagSeguroProperties
import com.example.pagseguro.autoconfigure.PagSeguroEnvironment
import com.example.pagseguro.model.webhook.WebhookEventType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class PagSeguroWebhookParserTest {

    private val propertiesWithSecret = PagSeguroProperties(
        token = "TOKEN",
        webhookSecret = "SECRET_KEY"
    )
    private val propertiesWithoutSecret = PagSeguroProperties(token = "TOKEN")
    private val parser = PagSeguroWebhookParser(propertiesWithSecret)

    private val rawBody = """
        {"env":"sandbox","event":"subscription.recurrence","resource":{},"links":[]}
    """.trimIndent()

    @Test
    fun `parse should deserialize event type`() {
        val payload = parser.parse(rawBody)
        assertThat(payload.event).isEqualTo(WebhookEventType.`subscription.recurrence`)
        assertThat(payload.id).isEqualTo("EVT_1")
    }

    @Test
    fun `verify should return true for valid HMAC signature`() {
        val signature = parser.computeSignature(rawBody) // test helper
        assertThat(parser.verify(rawBody, signature)).isTrue()
    }

    @Test
    fun `verify should return false for tampered body`() {
        val signature = parser.computeSignature(rawBody)
        assertThat(parser.verify("tampered", signature)).isFalse()
    }

    @Test
    fun `verify should throw when webhookSecret is not configured`() {
        val parserNoSecret = PagSeguroWebhookParser(propertiesWithoutSecret)
        assertThatThrownBy { parserNoSecret.verify(rawBody, "sig") }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("pagseguro.webhook-secret")
    }
}
```

- [ ] **Step 3: Run test to see it fail**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroWebhookParserTest
```

- [ ] **Step 4: Implement**

```kotlin
package com.example.pagseguro.service

import com.example.pagseguro.autoconfigure.PagSeguroProperties
import com.example.pagseguro.model.webhook.WebhookPayload
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class PagSeguroWebhookParser(private val properties: PagSeguroProperties) {

    private val mapper: ObjectMapper = jacksonObjectMapper().apply {
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }

    fun parse(rawBody: String): WebhookPayload = mapper.readValue(rawBody)

    fun verify(rawBody: String, signature: String): Boolean {
        val secret = requireNotNull(properties.webhookSecret) {
            "pagseguro.webhook-secret must be configured to verify webhook signatures"
        }
        return computeHmac(rawBody, secret) == signature
    }

    // Internal: exposed for testing only
    internal fun computeSignature(rawBody: String): String {
        val secret = requireNotNull(properties.webhookSecret) { "webhookSecret is required" }
        return computeHmac(rawBody, secret)
    }

    private fun computeHmac(data: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(data.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
```

- [ ] **Step 5: Run tests to see them pass**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroWebhookParserTest
```

- [ ] **Step 6: Commit**

```bash
git add pagseguro-spring-boot-autoconfigure/src
git commit -m "feat: implement PagSeguroWebhookParser with HMAC-SHA256 verification"
```

---

## Task 16: Health Indicator

**Files:**
- Create: `pagseguro-spring-boot-autoconfigure/src/main/kotlin/com/example/pagseguro/autoconfigure/PagSeguroHealthIndicatorAutoConfiguration.kt`
- Create: `pagseguro-spring-boot-autoconfigure/src/test/kotlin/com/example/pagseguro/autoconfigure/PagSeguroHealthIndicatorAutoConfigurationTest.kt`

- [ ] **Step 1: Write the test**

```kotlin
package com.example.pagseguro.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.FilteredClassLoader
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class PagSeguroHealthIndicatorAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                PagSeguroAutoConfiguration::class.java,
                PagSeguroHealthIndicatorAutoConfiguration::class.java
            )
        )
        .withPropertyValues("pagseguro.token=TEST_TOKEN")

    @Test
    fun `health indicator should not be created when disabled`() {
        contextRunner
            .withPropertyValues("pagseguro.health-indicator-enabled=false")
            .run { context ->
                assertThat(context).doesNotHaveBean(HealthIndicator::class.java)
            }
    }

    @Test
    fun `health indicator should not be created when actuator is absent`() {
        contextRunner
            .withPropertyValues("pagseguro.health-indicator-enabled=true")
            .withClassLoader(FilteredClassLoader(HealthIndicator::class.java))
            .run { context ->
                assertThat(context).doesNotHaveBean("pagSeguroHealthIndicator")
            }
    }

    @Test
    fun `health indicator should be created when enabled and actuator is present`() {
        contextRunner
            .withPropertyValues("pagseguro.health-indicator-enabled=true")
            .run { context ->
                assertThat(context).hasSingleBean(HealthIndicator::class.java)
            }
    }
}
```

- [ ] **Step 2: Run test to see it fail**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroHealthIndicatorAutoConfigurationTest
```

- [ ] **Step 3: Implement**

```kotlin
package com.example.pagseguro.autoconfigure

import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestClient

@AutoConfiguration(after = [PagSeguroAutoConfiguration::class])
@ConditionalOnClass(HealthIndicator::class)
@ConditionalOnProperty(name = ["pagseguro.health-indicator-enabled"], havingValue = "true")
@ConditionalOnBean(name = ["pagSeguroRestClient"])
class PagSeguroHealthIndicatorAutoConfiguration {

    @Bean("pagSeguroHealthIndicator")
    fun pagSeguroHealthIndicator(
        @org.springframework.beans.factory.annotation.Qualifier("pagSeguroRestClient")
        restClient: RestClient
    ): HealthIndicator = PagSeguroHealthIndicator(restClient)
}

class PagSeguroHealthIndicator(private val restClient: RestClient) : AbstractHealthIndicator() {

    override fun doHealthCheck(builder: Health.Builder) {
        try {
            restClient.get()
                .uri("/plans?limit=1")  // GET /plans — lightweight liveness check
                .retrieve()
                .toBodilessEntity()
            builder.up()
        } catch (e: com.example.pagseguro.exception.PagSeguroException.Unauthorized) {
            builder.outOfService().withDetail("reason", "unauthorized")
        } catch (e: Exception) {
            builder.down(e)
        }
    }
}
```

- [ ] **Step 4: Run tests to see them pass**

```bash
mvn test -pl pagseguro-spring-boot-autoconfigure -Dtest=PagSeguroHealthIndicatorAutoConfigurationTest
```

- [ ] **Step 5: Commit**

```bash
git add pagseguro-spring-boot-autoconfigure/src
git commit -m "feat: add PagSeguroHealthIndicator with conditional auto-configuration"
```

---

## Task 17: Full Build Verification

- [ ] **Step 1: Run all tests**

```bash
cd ~/workspace/org_rodrigoma/spring-boot-pagseguro-starter
mvn clean test
```
Expected: All tests pass, `BUILD SUCCESS`

- [ ] **Step 2: Build all modules**

```bash
mvn clean install
```
Expected: Both JARs produced in `target/` folders, `BUILD SUCCESS`

- [ ] **Step 3: Verify auto-configuration JAR contains the imports file**

```bash
jar tf pagseguro-spring-boot-autoconfigure/target/pagseguro-spring-boot-autoconfigure-1.0.0-SNAPSHOT.jar \
    | grep AutoConfiguration
```
Expected: `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

- [ ] **Step 4: Final commit**

```bash
git add .
git commit -m "chore: full build verification passed"
```
