# Medium Priority Improvements Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Improve code quality, consistency, test coverage enforcement, contributor experience, and dependency currency across the `spring-boot-pagbank-starter` library project.
**Architecture:** All build tooling changes (Spotless, Detekt, JaCoCo) are applied via the shared convention plugin `buildSrc/src/main/kotlin/pagbank.kotlin-library.gradle.kts` so they automatically apply to both `pagbank-spring-boot-autoconfigure` and `pagbank-spring-boot-starter` modules. CI is extended to run each new check as a dedicated step. Documentation and version bumps are standalone changes applied at the root level.
**Tech Stack:** Kotlin 1.9.22, Java 17, Spring Boot 3.4.x, Gradle 8.12 with Kotlin DSL, Spotless 6.x + Ktlint 1.x, Detekt, JaCoCo.

---

## Task 1: Ktlint via Spotless

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/buildSrc/build.gradle.kts`
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/buildSrc/src/main/kotlin/pagbank.kotlin-library.gradle.kts`
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/.github/workflows/ci.yml`

### Steps

- [ ] **1.1** Add the Spotless plugin dependency to `buildSrc/build.gradle.kts` so it is available to all convention plugins.

  Open `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/buildSrc/build.gradle.kts` and replace its full content with:

  ```kotlin
  plugins {
      `kotlin-dsl`
  }

  repositories {
      mavenCentral()
      gradlePluginPortal()
  }

  dependencies {
      implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
      implementation("org.jetbrains.kotlin:kotlin-allopen:1.9.22")
      implementation("com.diffplug.spotless:spotless-plugin-gradle:6.25.0")
  }
  ```

- [ ] **1.2** Apply and configure Spotless with Ktlint inside the convention plugin.

  Open `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/buildSrc/src/main/kotlin/pagbank.kotlin-library.gradle.kts` and replace its full content with:

  ```kotlin
  plugins {
      kotlin("jvm")
      kotlin("plugin.spring")
      id("com.diffplug.spotless")
  }

  kotlin {
      jvmToolchain(17)
  }

  tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
      kotlinOptions {
          freeCompilerArgs = listOf("-Xjsr305=strict")
      }
  }

  repositories {
      mavenCentral()
  }

  dependencies {
      implementation(platform("org.springframework.boot:spring-boot-dependencies:3.2.0"))
      implementation("org.jetbrains.kotlin:kotlin-stdlib")
      implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
      implementation("org.springframework.boot:spring-boot-autoconfigure")
      testImplementation("org.springframework.boot:spring-boot-starter-test")
  }

  tasks.withType<Test>().configureEach {
      useJUnitPlatform()
  }

  java {
      withSourcesJar()
      withJavadocJar()
  }

  spotless {
      kotlin {
          ktlint("1.2.1")
          target("src/**/*.kt")
      }
      kotlinGradle {
          ktlint("1.2.1")
          target("*.gradle.kts")
      }
  }
  ```

  Note: The `implementation(platform(...))` line still references `3.2.0` here — Task 5 will bump it. Keep it as-is for now so each task remains independently applicable.

- [ ] **1.3** Add `spotlessCheck` and `spotlessApply` steps to CI.

  Open `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/.github/workflows/ci.yml` and replace its full content with:

  ```yaml
  name: CI

  on:
    push:
      branches: [ main ]
    pull_request:
      branches: [ main ]

  jobs:
    build:
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

        - name: Check formatting (Spotless + Ktlint)
          run: ./gradlew spotlessCheck

        - name: Build and test
          run: ./gradlew build
  ```

- [ ] **1.4** Verify locally that Spotless is wired correctly.

  ```bash
  cd /Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter
  ./gradlew spotlessCheck
  ```

  If any formatting violations are reported, fix them automatically:

  ```bash
  ./gradlew spotlessApply
  ```

  Then re-run `spotlessCheck` and confirm it exits with `BUILD SUCCESSFUL`.

- [ ] **1.5** Commit.

  ```bash
  cd /Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter
  git add buildSrc/build.gradle.kts \
           buildSrc/src/main/kotlin/pagbank.kotlin-library.gradle.kts \
           .github/workflows/ci.yml
  git commit -m "feat: add Spotless + Ktlint code formatting enforcement"
  ```

---

## Task 2: Detekt Static Analysis

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/buildSrc/build.gradle.kts`
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/buildSrc/src/main/kotlin/pagbank.kotlin-library.gradle.kts`
- Create: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/detekt.yml`
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/.github/workflows/ci.yml`

### Steps

- [ ] **2.1** Add the Detekt plugin dependency to `buildSrc/build.gradle.kts`.

  The final `dependencies` block (after Task 1) becomes:

  ```kotlin
  dependencies {
      implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
      implementation("org.jetbrains.kotlin:kotlin-allopen:1.9.22")
      implementation("com.diffplug.spotless:spotless-plugin-gradle:6.25.0")
      implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.6")
  }
  ```

- [ ] **2.2** Apply and configure Detekt in the convention plugin.

  In `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/buildSrc/src/main/kotlin/pagbank.kotlin-library.gradle.kts`, add `id("io.gitlab.arturbosch.detekt")` to the `plugins` block and append the following configuration block at the end of the file:

  The full file content after Tasks 1 and 2 combined:

  ```kotlin
  plugins {
      kotlin("jvm")
      kotlin("plugin.spring")
      id("com.diffplug.spotless")
      id("io.gitlab.arturbosch.detekt")
  }

  kotlin {
      jvmToolchain(17)
  }

  tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
      kotlinOptions {
          freeCompilerArgs = listOf("-Xjsr305=strict")
      }
  }

  repositories {
      mavenCentral()
  }

  dependencies {
      implementation(platform("org.springframework.boot:spring-boot-dependencies:3.2.0"))
      implementation("org.jetbrains.kotlin:kotlin-stdlib")
      implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
      implementation("org.springframework.boot:spring-boot-autoconfigure")
      testImplementation("org.springframework.boot:spring-boot-starter-test")
  }

  tasks.withType<Test>().configureEach {
      useJUnitPlatform()
  }

  java {
      withSourcesJar()
      withJavadocJar()
  }

  spotless {
      kotlin {
          ktlint("1.2.1")
          target("src/**/*.kt")
      }
      kotlinGradle {
          ktlint("1.2.1")
          target("*.gradle.kts")
      }
  }

  detekt {
      config.setFrom(rootProject.file("detekt.yml"))
      buildUponDefaultConfig = true
      allRules = false
  }
  ```

- [ ] **2.3** Create the Detekt configuration file at the repo root.

  Create `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/detekt.yml` with the following content (enables default rules, adjusts a few commonly noisy ones for library code):

  ```yaml
  build:
    maxIssues: 0

  config:
    validation: true
    warningsAsErrors: false

  complexity:
    LongParameterList:
      active: true
      functionThreshold: 6
      constructorThreshold: 7
    TooManyFunctions:
      active: true
      thresholdInFiles: 20
      thresholdInClasses: 15
      thresholdInInterfaces: 15
      thresholdInObjects: 15
      thresholdInEnums: 10

  naming:
    FunctionNaming:
      active: true
      excludes:
        - '**/test/**'
        - '**/*Test*'
        - '**/*Spec*'

  style:
    MagicNumber:
      active: true
      excludes:
        - '**/test/**'
        - '**/*Test*'
      ignoreNumbers:
        - '-1'
        - '0'
        - '1'
        - '2'
        - '100'
      ignoreEnums: true
      ignoreRanges: true
    WildcardImport:
      active: true
      excludeImports:
        - 'kotlinx.coroutines.*'
  ```

- [ ] **2.4** Add the `detekt` step to CI (append after `spotlessCheck` step).

  Full updated `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/.github/workflows/ci.yml`:

  ```yaml
  name: CI

  on:
    push:
      branches: [ main ]
    pull_request:
      branches: [ main ]

  jobs:
    build:
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

        - name: Check formatting (Spotless + Ktlint)
          run: ./gradlew spotlessCheck

        - name: Static analysis (Detekt)
          run: ./gradlew detekt

        - name: Build and test
          run: ./gradlew build
  ```

- [ ] **2.5** Verify Detekt runs cleanly locally.

  ```bash
  cd /Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter
  ./gradlew detekt
  ```

  Expected output ends with `BUILD SUCCESSFUL`. If any issues are reported, fix them in the offending source files before committing.

- [ ] **2.6** Commit.

  ```bash
  cd /Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter
  git add buildSrc/build.gradle.kts \
           buildSrc/src/main/kotlin/pagbank.kotlin-library.gradle.kts \
           detekt.yml \
           .github/workflows/ci.yml
  git commit -m "feat: add Detekt static analysis enforcement"
  ```

---

## Task 3: JaCoCo Test Coverage

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/buildSrc/src/main/kotlin/pagbank.kotlin-library.gradle.kts`
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/.github/workflows/ci.yml`

### Steps

- [ ] **3.1** Add the `jacoco` plugin and configure report and verification tasks in the convention plugin.

  The full file content of `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/buildSrc/src/main/kotlin/pagbank.kotlin-library.gradle.kts` after Tasks 1, 2, and 3 combined:

  ```kotlin
  plugins {
      kotlin("jvm")
      kotlin("plugin.spring")
      id("com.diffplug.spotless")
      id("io.gitlab.arturbosch.detekt")
      jacoco
  }

  kotlin {
      jvmToolchain(17)
  }

  tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
      kotlinOptions {
          freeCompilerArgs = listOf("-Xjsr305=strict")
      }
  }

  repositories {
      mavenCentral()
  }

  dependencies {
      implementation(platform("org.springframework.boot:spring-boot-dependencies:3.2.0"))
      implementation("org.jetbrains.kotlin:kotlin-stdlib")
      implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
      implementation("org.springframework.boot:spring-boot-autoconfigure")
      testImplementation("org.springframework.boot:spring-boot-starter-test")
  }

  tasks.withType<Test>().configureEach {
      useJUnitPlatform()
      finalizedBy(tasks.named("jacocoTestReport"))
  }

  java {
      withSourcesJar()
      withJavadocJar()
  }

  spotless {
      kotlin {
          ktlint("1.2.1")
          target("src/**/*.kt")
      }
      kotlinGradle {
          ktlint("1.2.1")
          target("*.gradle.kts")
      }
  }

  detekt {
      config.setFrom(rootProject.file("detekt.yml"))
      buildUponDefaultConfig = true
      allRules = false
  }

  tasks.named<JacocoReport>("jacocoTestReport") {
      dependsOn(tasks.named("test"))
      reports {
          html.required.set(true)
          xml.required.set(true)
          csv.required.set(false)
      }
  }

  tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
      dependsOn(tasks.named("jacocoTestReport"))
      violationRules {
          rule {
              limit {
                  counter = "INSTRUCTION"
                  value = "COVEREDRATIO"
                  minimum = "0.80".toBigDecimal()
              }
          }
      }
  }

  tasks.named("check") {
      dependsOn(tasks.named("jacocoTestCoverageVerification"))
  }
  ```

  Note: `jacoco` is a core Gradle plugin and requires no additional `buildSrc` dependency entry. The `jacocoTestReport` HTML output will appear at `<module>/build/reports/jacoco/test/html/index.html` and the XML at `<module>/build/reports/jacoco/test/jacocoTestReport.xml`.

- [ ] **3.2** Add the coverage verification step to CI.

  Full updated `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/.github/workflows/ci.yml`:

  ```yaml
  name: CI

  on:
    push:
      branches: [ main ]
    pull_request:
      branches: [ main ]

  jobs:
    build:
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

        - name: Check formatting (Spotless + Ktlint)
          run: ./gradlew spotlessCheck

        - name: Static analysis (Detekt)
          run: ./gradlew detekt

        - name: Build and test
          run: ./gradlew build

        - name: Test coverage verification (JaCoCo)
          run: ./gradlew jacocoTestCoverageVerification
  ```

  Note: `jacocoTestCoverageVerification` is listed after `build` (which runs `test`) to benefit from Gradle's build cache — the `test` task results are already cached and JaCoCo exec data is already present.

- [ ] **3.3** Verify JaCoCo tasks run cleanly locally.

  ```bash
  cd /Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter
  ./gradlew test jacocoTestReport jacocoTestCoverageVerification
  ```

  Expected: `BUILD SUCCESSFUL`. HTML reports are at:
  - `pagbank-spring-boot-autoconfigure/build/reports/jacoco/test/html/index.html`
  - `pagbank-spring-boot-starter/build/reports/jacoco/test/html/index.html`

  If `jacocoTestCoverageVerification` fails because instruction coverage is below 80%, investigate which classes are under-tested and add missing unit tests before committing.

- [ ] **3.4** Commit.

  ```bash
  cd /Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter
  git add buildSrc/src/main/kotlin/pagbank.kotlin-library.gradle.kts \
           .github/workflows/ci.yml
  git commit -m "feat: add JaCoCo test coverage reporting and 80% instruction coverage gate"
  ```

---

## Task 4: CONTRIBUTING.md

**Files:**
- Create: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/CONTRIBUTING.md`

### Steps

- [ ] **4.1** Create the contributing guide at the repo root.

  Create `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/CONTRIBUTING.md` with the following exact content:

  ```markdown
  # Contributing to spring-boot-pagbank-starter

  Thank you for your interest in contributing! This document explains how to get started,
  the standards we follow, and the process for submitting changes.

  ---

  ## Prerequisites

  | Tool | Minimum version |
  |------|----------------|
  | JDK  | 17 (Temurin recommended) |
  | Git  | 2.40+ |

  Gradle is provided via the Gradle Wrapper (`./gradlew`); you do not need to install it separately.

  ---

  ## Cloning and building locally

  ```bash
  git clone https://github.com/rodrigoma/spring-boot-pagbank-starter.git
  cd spring-boot-pagbank-starter
  ./gradlew build
  ```

  A successful build compiles both modules, runs all tests, checks code formatting, runs static analysis, and verifies test coverage.

  ---

  ## Running tests

  Run the full test suite across all modules:

  ```bash
  ./gradlew test
  ```

  Generate JaCoCo HTML coverage reports (output in each module's `build/reports/jacoco/test/html/`):

  ```bash
  ./gradlew jacocoTestReport
  ```

  ---

  ## Code style

  This project uses [Ktlint](https://pinterest.github.io/ktlint/) via [Spotless](https://github.com/diffplug/spotless) to enforce consistent Kotlin formatting.

  Check for formatting issues:

  ```bash
  ./gradlew spotlessCheck
  ```

  Auto-fix all formatting issues:

  ```bash
  ./gradlew spotlessApply
  ```

  Run [Detekt](https://detekt.dev/) static analysis:

  ```bash
  ./gradlew detekt
  ```

  All three checks run automatically in CI. Pull requests with formatting violations or static analysis failures will not be merged.

  ---

  ## Commit message convention

  This project follows [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).

  Format: `<type>(<optional scope>): <short summary>`

  | Type | When to use |
  |------|-------------|
  | `feat` | A new feature |
  | `fix` | A bug fix |
  | `docs` | Documentation changes only |
  | `refactor` | Code change that is neither a fix nor a feature |
  | `test` | Adding or updating tests |
  | `chore` | Build tooling, dependency updates, CI changes |
  | `perf` | Performance improvement |

  Examples:
  ```
  feat: add support for PagBank subscription pause API
  fix: handle 401 response without rethrowing raw exception
  docs: add usage example for PagBankSubscriptionService
  chore: bump Spring Boot to 3.4.3
  ```

  Keep the summary line under 72 characters. Add a body when the change needs explanation beyond the summary.

  ---

  ## Pull request process

  1. Fork the repository and create a feature branch from `main`:
     ```bash
     git checkout -b feat/my-feature
     ```
  2. Make your changes and commit using Conventional Commits format.
  3. Ensure the full build passes locally:
     ```bash
     ./gradlew build jacocoTestCoverageVerification
     ```
  4. Push your branch and open a pull request against `main`.
  5. Fill in the PR description explaining **what** changed and **why**.
  6. Address any review feedback. The PR will be merged once it has at least one approving review and all CI checks pass.

  ---

  ## Reporting bugs

  Please open a [GitHub Issue](https://github.com/rodrigoma/spring-boot-pagbank-starter/issues) and include:

  - A clear, descriptive title.
  - The version of `pagbank-spring-boot-starter` you are using.
  - The Spring Boot version of your application.
  - A minimal reproducible example (code snippet or test case) that demonstrates the problem.
  - The full stack trace or error message if applicable.

  For security vulnerabilities, do **not** open a public issue. Contact the maintainer directly.
  ```

- [ ] **4.2** Commit.

  ```bash
  cd /Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter
  git add CONTRIBUTING.md
  git commit -m "docs: add CONTRIBUTING.md with build, style, commit, and PR guidelines"
  ```

---

## Task 5: Spring Boot Version Bump (3.2.0 → 3.4.3)

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/buildSrc/src/main/kotlin/pagbank.kotlin-library.gradle.kts`
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/build.gradle.kts`

Note: `buildSrc/build.gradle.kts` and `pagbank.publish.gradle.kts` do not reference the Spring Boot version and require no changes.

### Steps

- [ ] **5.1** Update the BOM version in the convention plugin.

  In `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/buildSrc/src/main/kotlin/pagbank.kotlin-library.gradle.kts`, change:

  ```kotlin
  implementation(platform("org.springframework.boot:spring-boot-dependencies:3.2.0"))
  ```

  to:

  ```kotlin
  implementation(platform("org.springframework.boot:spring-boot-dependencies:3.4.3"))
  ```

- [ ] **5.2** Update the BOM version for the `kapt` configuration in the autoconfigure module.

  In `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/build.gradle.kts`, change:

  ```kotlin
  kapt(platform("org.springframework.boot:spring-boot-dependencies:3.2.0"))
  ```

  to:

  ```kotlin
  kapt(platform("org.springframework.boot:spring-boot-dependencies:3.4.3"))
  ```

- [ ] **5.3** Verify the build compiles and all tests pass.

  ```bash
  cd /Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter
  ./gradlew build
  ```

  Expected: `BUILD SUCCESSFUL` with no test failures. If any tests fail due to API changes in Spring Boot 3.4.x (e.g., changed bean wiring, renamed test utilities), fix them before committing. Consult the [Spring Boot 3.4 release notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes) for breaking changes. Common migration points from 3.2 to 3.4 include:
  - `MockMvcBuilderCustomizer` and `MockMvcAutoConfiguration` may have changed.
  - Some deprecated `@AutoConfigureMockMvc` behaviour may have been removed.
  - Properties migrated under new namespaces (use `spring-boot-properties-migrator` if needed).

- [ ] **5.4** Run the full quality gate to confirm nothing was broken.

  ```bash
  cd /Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter
  ./gradlew spotlessCheck detekt jacocoTestCoverageVerification
  ```

  Expected: `BUILD SUCCESSFUL`.

- [ ] **5.5** Commit.

  ```bash
  cd /Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter
  git add buildSrc/src/main/kotlin/pagbank.kotlin-library.gradle.kts \
           pagbank-spring-boot-autoconfigure/build.gradle.kts
  git commit -m "chore: bump Spring Boot from 3.2.0 to 3.4.3"
  ```

---

## Final verification

After all five tasks are complete, run the full build one more time to confirm the entire pipeline is green:

```bash
cd /Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter
./gradlew spotlessCheck detekt build jacocoTestCoverageVerification
```

Expected output (all tasks):

```
> Task :pagbank-spring-boot-autoconfigure:spotlessCheck UP-TO-DATE
> Task :pagbank-spring-boot-starter:spotlessCheck UP-TO-DATE
> Task :pagbank-spring-boot-autoconfigure:detekt
> Task :pagbank-spring-boot-starter:detekt
> Task :pagbank-spring-boot-autoconfigure:compileKotlin
> Task :pagbank-spring-boot-autoconfigure:compileTestKotlin
> Task :pagbank-spring-boot-autoconfigure:test
> Task :pagbank-spring-boot-autoconfigure:jacocoTestReport
> Task :pagbank-spring-boot-autoconfigure:jacocoTestCoverageVerification
> Task :pagbank-spring-boot-starter:compileKotlin
> Task :pagbank-spring-boot-starter:test
> Task :pagbank-spring-boot-starter:jacocoTestReport
> Task :pagbank-spring-boot-starter:jacocoTestCoverageVerification

BUILD SUCCESSFUL
```
