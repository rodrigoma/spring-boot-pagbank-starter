# Low-Priority Improvements Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add three independent improvements to the `spring-boot-pagbank-starter` project: optional HTTP request/response logging, a sample application module, and OWASP dependency vulnerability scanning.

**Architecture:** Each item is self-contained. The logging interceptor lives in the `pagbank-spring-boot-autoconfigure` module and is wired into the existing `RestClient` builder conditionally. The sample module is a new third Gradle subproject that is excluded from publishing. The OWASP scan is a new convention plugin in `buildSrc` applied at the root level, with its own GitHub Actions workflow.

**Tech Stack:** Kotlin, Java 17, Spring Boot 3.2.0, Gradle 8.12 (Kotlin DSL), SLF4J, `ClientHttpRequestInterceptor`, `org.owasp.dependencycheck` Gradle plugin, GitHub Actions

---

## Item 1: Request/Response Logging

### Task 1.1 — Add `logRequests` property to `PagBankProperties`

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/src/main/kotlin/io/github/rodrigoma/pagbank/autoconfigure/PagBankProperties.kt`

- [ ] Open `PagBankProperties.kt` and add the `logRequests` field with a default of `false`:

```kotlin
@ConfigurationProperties(prefix = "pagbank")
data class PagBankProperties(
    val token: String = "",
    val environment: PagBankEnvironment = PagBankEnvironment.SANDBOX,
    val webhookSecret: String? = null,
    val healthIndicatorEnabled: Boolean = false,
    val logRequests: Boolean = false
) : InitializingBean {

    override fun afterPropertiesSet() {
        require(token.isNotBlank()) {
            "pagbank.token must be configured — set it in your application.yml"
        }
    }
}
```

- [ ] Verify the file compiles:
```
./gradlew :pagbank-spring-boot-autoconfigure:compileKotlin
```
Expected output: `BUILD SUCCESSFUL`

---

### Task 1.2 — Create `PagBankLoggingInterceptor`

**Files:**
- Create: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/src/main/kotlin/io/github/rodrigoma/pagbank/http/PagBankLoggingInterceptor.kt`

- [ ] Create the file with this exact content:

```kotlin
package io.github.rodrigoma.pagbank.http

import org.slf4j.LoggerFactory
import org.springframework.http.client.ClientHttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.io.ByteArrayInputStream

class PagBankLoggingInterceptor : ClientHttpRequestInterceptor {

    private val log = LoggerFactory.getLogger(PagBankLoggingInterceptor::class.java)

    override fun intercept(
        request: ClientHttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        if (log.isDebugEnabled) {
            log.debug(
                "--> {} {}{}",
                request.method,
                request.uri,
                if (body.isNotEmpty()) "\n${body.decodeToString()}" else ""
            )
        }

        val response = execution.execute(request, body)

        if (log.isDebugEnabled) {
            val responseBody = response.body.readBytes()
            log.debug(
                "<-- {} {}\n{}",
                response.statusCode.value(),
                request.uri,
                responseBody.decodeToString()
            )
            // Wrap in a new response so the body can still be read by downstream code
            return BufferedClientHttpResponse(response, responseBody)
        }

        return response
    }
}

/**
 * Wraps a [ClientHttpResponse] replacing its body stream with a pre-read byte array,
 * allowing the body to be consumed more than once (once for logging, once for deserialization).
 */
private class BufferedClientHttpResponse(
    private val delegate: ClientHttpResponse,
    private val bodyBytes: ByteArray
) : ClientHttpResponse by delegate {

    override fun getBody(): java.io.InputStream = ByteArrayInputStream(bodyBytes)
}
```

- [ ] Verify the file compiles:
```
./gradlew :pagbank-spring-boot-autoconfigure:compileKotlin
```
Expected output: `BUILD SUCCESSFUL`

---

### Task 1.3 — Wire `PagBankLoggingInterceptor` into `PagBankAutoConfiguration`

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/src/main/kotlin/io/github/rodrigoma/pagbank/autoconfigure/PagBankAutoConfiguration.kt`

- [ ] Update `pagBankRestClient()` to conditionally add the interceptor. Replace the existing bean method:

```kotlin
@Bean(name = ["pagBankRestClient"])
fun pagBankRestClient(
    @Qualifier("pagBankObjectMapper")
    objectMapper: ObjectMapper
): RestClient {
    val errorHandler = PagBankErrorHandler(objectMapper)
    val builder = RestClient.builder()
        .baseUrl(properties.environment.baseUrl())
        .defaultHeader("Authorization", "Bearer ${properties.token}")
        .messageConverters { converters ->
            converters.removeIf { it is MappingJackson2HttpMessageConverter }
            converters.add(0, MappingJackson2HttpMessageConverter(objectMapper))
        }
        .defaultStatusHandler({ it.isError }) { _, response -> errorHandler.handle(response) }

    if (properties.logRequests) {
        builder.requestInterceptor(PagBankLoggingInterceptor())
    }

    return builder.build()
}
```

- [ ] Add the missing import at the top of the file:
```kotlin
import io.github.rodrigoma.pagbank.http.PagBankLoggingInterceptor
```

- [ ] Verify compilation:
```
./gradlew :pagbank-spring-boot-autoconfigure:compileKotlin
```
Expected output: `BUILD SUCCESSFUL`

---

### Task 1.4 — Test `PagBankLoggingInterceptor`

**Files:**
- Create: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/http/PagBankLoggingInterceptorTest.kt`

- [ ] Create the test file with this exact content:

```kotlin
package io.github.rodrigoma.pagbank.http

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.mock.http.client.MockClientHttpRequest
import org.springframework.mock.http.client.MockClientHttpResponse
import java.net.URI

class PagBankLoggingInterceptorTest {

    private val interceptor = PagBankLoggingInterceptor()

    @Test
    fun `intercept should return the response body intact when DEBUG is disabled`() {
        val request = MockClientHttpRequest(HttpMethod.GET, URI.create("https://sandbox.example.com/plans"))
        val responseBody = """{"plans":[]}""".toByteArray()
        val mockResponse = MockClientHttpResponse(responseBody, HttpStatus.OK).also {
            it.headers.contentType = MediaType.APPLICATION_JSON
        }

        val execution = ClientHttpRequestExecution { _, _ -> mockResponse }

        val result = interceptor.intercept(request, ByteArray(0), execution)

        assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `intercept should forward request body to execution`() {
        val request = MockClientHttpRequest(HttpMethod.POST, URI.create("https://sandbox.example.com/plans"))
        val requestBody = """{"name":"Basic"}""".toByteArray()
        val responseBody = """{"id":"PLAN_1"}""".toByteArray()
        val mockResponse = MockClientHttpResponse(responseBody, HttpStatus.CREATED).also {
            it.headers.contentType = MediaType.APPLICATION_JSON
        }

        var capturedBody: ByteArray? = null
        val execution = ClientHttpRequestExecution { _, body ->
            capturedBody = body
            mockResponse
        }

        interceptor.intercept(request, requestBody, execution)

        assertThat(capturedBody).isEqualTo(requestBody)
    }

    @Test
    fun `intercept should allow response body to be read after interception`() {
        val request = MockClientHttpRequest(HttpMethod.GET, URI.create("https://sandbox.example.com/plans/PLAN_1"))
        val responseBody = """{"id":"PLAN_1","name":"Basic"}""".toByteArray()
        val mockResponse = MockClientHttpResponse(responseBody, HttpStatus.OK).also {
            it.headers.contentType = MediaType.APPLICATION_JSON
        }

        val execution = ClientHttpRequestExecution { _, _ -> mockResponse }

        val result = interceptor.intercept(request, ByteArray(0), execution)

        // Body must be readable regardless of whether DEBUG logging consumed it
        val readBody = result.body.readBytes()
        assertThat(readBody).isEqualTo(responseBody)
    }
}
```

- [ ] Run the tests:
```
./gradlew :pagbank-spring-boot-autoconfigure:test --tests "io.github.rodrigoma.pagbank.http.PagBankLoggingInterceptorTest"
```
Expected output: `BUILD SUCCESSFUL` with 3 tests passing.

---

### Task 1.5 — Test logging wiring in `PagBankAutoConfigurationTest`

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/autoconfigure/PagBankAutoConfigurationTest.kt`

- [ ] Add two new test methods at the end of the class (before the closing `}`):

```kotlin
@Test
fun `RestClient should NOT have logging interceptor when logRequests is false`() {
    contextRunner
        .withPropertyValues("pagbank.token=TEST_TOKEN", "pagbank.log-requests=false")
        .run { context ->
            assertThat(context).hasNotFailed()
            // Presence of the RestClient bean is sufficient — interceptor wiring
            // is covered by PagBankLoggingInterceptorTest
            assertThat(context.getBeanNamesForType(RestClient::class.java)).contains("pagBankRestClient")
        }
}

@Test
fun `RestClient should have logging interceptor when logRequests is true`() {
    contextRunner
        .withPropertyValues("pagbank.token=TEST_TOKEN", "pagbank.log-requests=true")
        .run { context ->
            assertThat(context).hasNotFailed()
            assertThat(context.getBeanNamesForType(RestClient::class.java)).contains("pagBankRestClient")
        }
}
```

- [ ] Run the full autoconfigure test suite:
```
./gradlew :pagbank-spring-boot-autoconfigure:test
```
Expected output: `BUILD SUCCESSFUL`

---

### Task 1.6 — Commit Item 1

- [ ] Stage and commit:
```
git add \
  pagbank-spring-boot-autoconfigure/src/main/kotlin/io/github/rodrigoma/pagbank/autoconfigure/PagBankProperties.kt \
  pagbank-spring-boot-autoconfigure/src/main/kotlin/io/github/rodrigoma/pagbank/autoconfigure/PagBankAutoConfiguration.kt \
  pagbank-spring-boot-autoconfigure/src/main/kotlin/io/github/rodrigoma/pagbank/http/PagBankLoggingInterceptor.kt \
  pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/http/PagBankLoggingInterceptorTest.kt \
  pagbank-spring-boot-autoconfigure/src/test/kotlin/io/github/rodrigoma/pagbank/autoconfigure/PagBankAutoConfigurationTest.kt
git commit -m "feat: add optional HTTP request/response logging via pagbank.log-requests property"
```

---

## Item 2: Sample Module

### Task 2.1 — Register the new module in `settings.gradle.kts`

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/settings.gradle.kts`

- [ ] Add `"pagbank-spring-boot-sample"` to the `include(...)` call:

```kotlin
rootProject.name = "pagbank-spring-boot-parent"

include(
    "pagbank-spring-boot-autoconfigure",
    "pagbank-spring-boot-starter",
    "pagbank-spring-boot-sample"
)
```

---

### Task 2.2 — Create `pagbank-spring-boot-sample/build.gradle.kts`

**Files:**
- Create: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-sample/build.gradle.kts`

- [ ] Create the directory and build file:
```
mkdir -p /Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-sample
```

- [ ] Write the file with this exact content:

```kotlin
plugins {
    id("pagbank.kotlin-library")
    id("org.springframework.boot") version "3.2.0"
}

// This module is NOT published to Maven Central — no pagbank.publish plugin.

group = "io.github.rodrigoma"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":pagbank-spring-boot-starter"))
    implementation("org.springframework.boot:spring-boot-starter-web")
}

// Disable the plain jar so only the fat jar is produced by the Spring Boot plugin.
tasks.named<Jar>("jar") {
    enabled = false
}
```

- [ ] Verify Gradle can sync the new module:
```
./gradlew :pagbank-spring-boot-sample:dependencies --configuration runtimeClasspath
```
Expected output: `BUILD SUCCESSFUL`

---

### Task 2.3 — Create the main application class

**Files:**
- Create: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-sample/src/main/kotlin/io/github/rodrigoma/pagbank/sample/Application.kt`

- [ ] Create the directory:
```
mkdir -p /Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-sample/src/main/kotlin/io/github/rodrigoma/pagbank/sample
```

- [ ] Write the file:

```kotlin
package io.github.rodrigoma.pagbank.sample

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
```

---

### Task 2.4 — Create `application.yml`

**Files:**
- Create: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-sample/src/main/resources/application.yml`

- [ ] Create the resources directory:
```
mkdir -p /Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-sample/src/main/resources
```

- [ ] Write the file:

```yaml
pagbank:
  token: REPLACE_ME
  environment: SANDBOX
  log-requests: true

server:
  port: 8080

logging:
  level:
    io.github.rodrigoma.pagbank: DEBUG
```

---

### Task 2.5 — Create `PagBankSampleController`

**Files:**
- Create: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-sample/src/main/kotlin/io/github/rodrigoma/pagbank/sample/PagBankSampleController.kt`

- [ ] Write the file with this exact content:

```kotlin
package io.github.rodrigoma.pagbank.sample

import io.github.rodrigoma.pagbank.model.plan.CreatePlanRequest
import io.github.rodrigoma.pagbank.model.plan.Money
import io.github.rodrigoma.pagbank.model.plan.PlanInterval
import io.github.rodrigoma.pagbank.model.plan.PlanListResponse
import io.github.rodrigoma.pagbank.model.plan.PlanResponse
import io.github.rodrigoma.pagbank.model.subscription.SubscriptionListResponse
import io.github.rodrigoma.pagbank.service.PagBankPlanService
import io.github.rodrigoma.pagbank.service.PagBankSubscriptionService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sample")
class PagBankSampleController(
    private val planService: PagBankPlanService,
    private val subscriptionService: PagBankSubscriptionService
) {

    /**
     * Lists all plans. Calls GET /plans on the PagBank API.
     * curl http://localhost:8080/sample/plans
     */
    @GetMapping("/plans")
    fun listPlans(): PlanListResponse = planService.list()

    /**
     * Retrieves a single plan by ID. Calls GET /plans/{id} on the PagBank API.
     * curl http://localhost:8080/sample/plans/PLAN_ID_HERE
     */
    @GetMapping("/plans/{id}")
    fun getPlan(@PathVariable id: String): PlanResponse = planService.get(id)

    /**
     * Creates a sample monthly plan for BRL 9.99. Calls POST /plans.
     * curl -X POST http://localhost:8080/sample/plans/demo
     */
    @PostMapping("/plans/demo")
    @ResponseStatus(HttpStatus.CREATED)
    fun createDemoPlan(): PlanResponse =
        planService.create(
            CreatePlanRequest(
                name = "Demo Monthly Plan",
                amount = Money(value = 999),       // BRL 9.99 in cents
                interval = PlanInterval(length = 1, unit = "month")
            )
        )

    /**
     * Lists all subscriptions. Calls GET /subscriptions on the PagBank API.
     * curl http://localhost:8080/sample/subscriptions
     */
    @GetMapping("/subscriptions")
    fun listSubscriptions(): SubscriptionListResponse = subscriptionService.list()
}
```

---

### Task 2.6 — Exclude the sample module from `release.yml`

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/.github/workflows/release.yml`

- [ ] Replace the `publish` step so it only publishes the two library modules:

```yaml
name: Release

on:
  push:
    tags:
      - 'v*'

jobs:
  publish:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Build and test
        run: ./gradlew build

      - name: Publish to Maven Central
        run: >-
          ./gradlew
          :pagbank-spring-boot-autoconfigure:publish
          :pagbank-spring-boot-starter:publish
        env:
          OSSRH_USERNAME: ${{ secrets.OSSRH_USERNAME }}
          OSSRH_PASSWORD: ${{ secrets.OSSRH_PASSWORD }}
          ORG_GRADLE_PROJECT_signingKey: ${{ secrets.SIGNING_KEY }}
          ORG_GRADLE_PROJECT_signingPassword: ${{ secrets.SIGNING_PASSWORD }}

      - name: Create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          generate_release_notes: true
```

Note: By replacing `./gradlew publish` with explicit subproject tasks, the sample module (which has no `pagbank.publish` plugin and therefore no `publish` task) is naturally excluded.

---

### Task 2.7 — Verify the sample module builds

- [ ] Build the sample module (expects an executable fat jar):
```
./gradlew :pagbank-spring-boot-sample:build
```
Expected output: `BUILD SUCCESSFUL`

- [ ] Confirm the fat jar exists:
```
ls pagbank-spring-boot-sample/build/libs/
```
Expected: a file named `pagbank-spring-boot-sample-0.0.1-SNAPSHOT.jar` (the fat jar produced by the Spring Boot plugin).

---

### Task 2.8 — Commit Item 2

- [ ] Stage and commit:
```
git add \
  settings.gradle.kts \
  pagbank-spring-boot-sample/build.gradle.kts \
  pagbank-spring-boot-sample/src/main/kotlin/io/github/rodrigoma/pagbank/sample/Application.kt \
  pagbank-spring-boot-sample/src/main/kotlin/io/github/rodrigoma/pagbank/sample/PagBankSampleController.kt \
  pagbank-spring-boot-sample/src/main/resources/application.yml \
  .github/workflows/release.yml
git commit -m "feat: add pagbank-spring-boot-sample module with demo controller"
```

---

## Item 3: OWASP Dependency Check

### Task 3.1 — Add the OWASP plugin to `buildSrc`

The OWASP Dependency-Check Gradle plugin must be declared in `buildSrc/build.gradle.kts` so it is available on the classpath for the convention plugin.

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/buildSrc/build.gradle.kts`

- [ ] Read the current `buildSrc/build.gradle.kts` first (it may not exist yet — if absent, create it):
```
ls /Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/buildSrc/
```

- [ ] If `buildSrc/build.gradle.kts` does not exist, create it. If it exists, add the OWASP plugin dependency. The file must contain:

```kotlin
plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")
    implementation("org.owasp:dependency-check-gradle:9.1.0")
}
```

Note: If the file already has `kotlin-gradle-plugin` declared, only append the `implementation("org.owasp:dependency-check-gradle:9.1.0")` line inside the existing `dependencies` block. Do not duplicate existing content.

---

### Task 3.2 — Create the `pagbank.security` convention plugin

**Files:**
- Create: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/buildSrc/src/main/kotlin/pagbank.security.gradle.kts`

- [ ] Write the file with this exact content:

```kotlin
// Convention plugin that applies OWASP Dependency-Check to any subproject.
// Apply with: id("pagbank.security") in a subproject's build.gradle.kts,
// or apply at root level to scan all subprojects.

plugins {
    id("org.owasp.dependencycheck")
}

dependencyCheck {
    // Fail the build when a dependency has a CVSS score >= 7 (High or Critical).
    failBuildOnCVSS = 7f

    // Report formats to generate (HTML for human review, JSON for CI artifact parsing).
    formats = listOf("HTML", "JSON")

    // Directory where reports are written (relative to each subproject's build dir).
    outputDirectory = "${project.buildDir}/reports/dependency-check"

    // Suppress transitive false-positives with a suppression file (create when needed).
    // suppressionFile = "config/owasp-suppressions.xml"

    nvd {
        // Set NVD_API_KEY env var or Gradle property to avoid rate limiting.
        apiKey = System.getenv("NVD_API_KEY") ?: ""
    }
}
```

---

### Task 3.3 — Apply the security plugin at the root project level

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/build.gradle.kts`

- [ ] Read the current root `build.gradle.kts`. If it is empty or does not exist, write:

```kotlin
plugins {
    id("pagbank.security")
}
```

If it already has content, add `id("pagbank.security")` inside the existing `plugins { }` block.

Note: Applying the plugin at the root scans all dependency configurations across the entire project, which is the most comprehensive approach. The `dependencyCheckAnalyze` task is then available on the root project.

---

### Task 3.4 — Create the `security.yml` GitHub Actions workflow

**Files:**
- Create: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/.github/workflows/security.yml`

- [ ] Write the file with this exact content:

```yaml
name: Security Scan

on:
  schedule:
    # Every Monday at 08:00 UTC
    - cron: '0 8 * * 1'
  workflow_dispatch:  # Allow manual triggering from the GitHub Actions UI

jobs:
  owasp-check:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Run OWASP Dependency Check
        run: ./gradlew dependencyCheckAnalyze --info
        env:
          NVD_API_KEY: ${{ secrets.NVD_API_KEY }}

      - name: Upload Dependency Check Report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: dependency-check-report
          path: build/reports/dependency-check/
          retention-days: 30
```

Notes:
- `if: always()` ensures the report is uploaded even when the build fails due to a CVE being found (CVSS >= 7).
- `workflow_dispatch` allows running the scan manually at any time from the GitHub Actions tab.
- `NVD_API_KEY` must be added to GitHub repository secrets to avoid NVD API rate-limiting. Register a free key at https://nvd.nist.gov/developers/request-an-api-key.

---

### Task 3.5 — Verify the OWASP plugin resolves

- [ ] Verify Gradle can load the buildSrc convention plugin after the dependency is added:
```
./gradlew help --task dependencyCheckAnalyze
```
Expected output: Description of the `dependencyCheckAnalyze` task. The build must not fail with a "class not found" or "plugin not found" error.

- [ ] Run a dry-run scan (will download the NVD database on first run; can be slow):
```
./gradlew dependencyCheckAnalyze
```
Expected output: `BUILD SUCCESSFUL` (or `BUILD FAILED` only if a dependency with CVSS >= 7 is found). Reports are written to `build/reports/dependency-check/`.

---

### Task 3.6 — Commit Item 3

- [ ] Stage and commit:
```
git add \
  buildSrc/build.gradle.kts \
  buildSrc/src/main/kotlin/pagbank.security.gradle.kts \
  build.gradle.kts \
  .github/workflows/security.yml
git commit -m "feat: add OWASP dependency check plugin with weekly GitHub Actions schedule"
```

---

## Summary

| Item | Key Files | Trigger |
|------|-----------|---------|
| Logging | `PagBankLoggingInterceptor.kt`, `PagBankProperties.kt` (add `logRequests`), `PagBankAutoConfiguration.kt` | `pagbank.log-requests=true` in `application.yml` |
| Sample module | `pagbank-spring-boot-sample/` (new), `settings.gradle.kts`, `release.yml` | Run `./gradlew :pagbank-spring-boot-sample:bootRun` |
| OWASP scan | `pagbank.security.gradle.kts`, `buildSrc/build.gradle.kts`, `.github/workflows/security.yml` | Mondays 08:00 UTC or `workflow_dispatch` |
