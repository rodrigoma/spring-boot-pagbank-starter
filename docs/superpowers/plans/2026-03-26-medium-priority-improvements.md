# Medium Priority Improvements Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Improve code quality, consistency, test coverage enforcement, contributor experience, and dependency currency across the `spring-boot-pagbank-starter` library project.

**Architecture:** All build tooling changes (Spotless, Detekt, JaCoCo) are applied via the shared convention plugin `buildSrc/src/main/kotlin/pagbank.kotlin-library.gradle.kts` so they automatically apply to both `pagbank-spring-boot-autoconfigure` and `pagbank-spring-boot-starter` modules. CI is extended to run each new check as a dedicated step. Documentation and version bumps are standalone changes applied at the root level.

**Tech Stack:** Kotlin 2.1.x, Java 21, Spring Boot 4.x, Gradle 8.12 with Kotlin DSL, Spotless 7.x + Ktlint 1.x, Detekt 1.23.x, JaCoCo.

> **Minimum runtime requirement after this plan:** Spring Boot 4.x, Java 21. Users on Spring Boot 3.x or Java 17 must upgrade before using versions of this library built with these settings.

---

## Task 1: Ktlint via Spotless

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/buildSrc/build.gradle.kts`
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/buildSrc/src/main/kotlin/pagbank.kotlin-library.gradle.kts`
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/.github/workflows/ci.yml`

### Steps

- [ ] **1.1** Add the Spotless plugin dependency to `buildSrc/build.gradle.kts`.

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
      implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.20")
      implementation("org.jetbrains.kotlin:kotlin-allopen:2.1.20")
      implementation("com.diffplug.spotless:spotless-plugin-gradle:7.0.2")
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
      jvmToolchain(21)
      compilerOptions {
          freeCompilerArgs.add("-Xjsr305=strict")
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
          ktlint("1.5.0")
          target("src/**/*.kt")
      }
      kotlinGradle {
          ktlint("1.5.0")
          target("*.gradle.kts")
      }
  }
  ```

  > Note: The BOM still references `3.2.0` here — Task 5 will bump it to Spring Boot 4. Keep it as-is so each task remains independently applicable.

- [ ] **1.3** Update CI to use Java 21 and add `spotlessCheck`.

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

        - name: Set up Java 21
          uses: actions/setup-java@v4
          with:
            java-version: '21'
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
  git commit -m "feat: add Spotless + Ktlint formatting, bump Kotlin to 2.1.20 and Java to 21"
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
      implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.20")
      implementation("org.jetbrains.kotlin:kotlin-allopen:2.1.20")
      implementation("com.diffplug.spotless:spotless-plugin-gradle:7.0.2")
      implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.8")
  }
  ```

- [ ] **2.2** Apply and configure Detekt in the convention plugin.

  Full file content of `pagbank.kotlin-library.gradle.kts` after Tasks 1 and 2 combined:

  ```kotlin
  plugins {
      kotlin("jvm")
      kotlin("plugin.spring")
      id("com.diffplug.spotless")
      id("io.gitlab.arturbosch.detekt")
  }

  kotlin {
      jvmToolchain(21)
      compilerOptions {
          freeCompilerArgs.add("-Xjsr305=strict")
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
          ktlint("1.5.0")
          target("src/**/*.kt")
      }
      kotlinGradle {
          ktlint("1.5.0")
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

  Create `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/detekt.yml`:

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

- [ ] **2.4** Add the `detekt` step to CI.

  Full updated `.github/workflows/ci.yml`:

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

        - name: Set up Java 21
          uses: actions/setup-java@v4
          with:
            java-version: '21'
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

  Expected: `BUILD SUCCESSFUL`. If issues are reported, fix them in the offending source files before committing.

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

- [ ] **3.1** Add the `jacoco` plugin and configure report and verification tasks.

  Full file content of `pagbank.kotlin-library.gradle.kts` after Tasks 1, 2, and 3 combined:

  ```kotlin
  plugins {
      kotlin("jvm")
      kotlin("plugin.spring")
      id("com.diffplug.spotless")
      id("io.gitlab.arturbosch.detekt")
      jacoco
  }

  kotlin {
      jvmToolchain(21)
      compilerOptions {
          freeCompilerArgs.add("-Xjsr305=strict")
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
          ktlint("1.5.0")
          target("src/**/*.kt")
      }
      kotlinGradle {
          ktlint("1.5.0")
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

  > `jacoco` is a core Gradle plugin — no additional `buildSrc` dependency needed.

- [ ] **3.2** Add the coverage verification step to CI.

  Full updated `.github/workflows/ci.yml`:

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

        - name: Set up Java 21
          uses: actions/setup-java@v4
          with:
            java-version: '21'
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

- [ ] **3.3** Verify JaCoCo tasks run cleanly locally.

  ```bash
  cd /Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter
  ./gradlew test jacocoTestReport jacocoTestCoverageVerification
  ```

  Expected: `BUILD SUCCESSFUL`. HTML reports at:
  - `pagbank-spring-boot-autoconfigure/build/reports/jacoco/test/html/index.html`
  - `pagbank-spring-boot-starter/build/reports/jacoco/test/html/index.html`

  If `jacocoTestCoverageVerification` fails (instruction coverage below 80%), add missing unit tests before committing.

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

  ````markdown
  # Contributing to spring-boot-pagbank-starter

  Thank you for your interest in contributing! This document explains how to get started,
  the standards we follow, and the process for submitting changes.

  ---

  ## Prerequisites

  | Tool | Minimum version |
  |------|----------------|
  | JDK  | 21 (Temurin recommended) |
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
  chore: bump Spring Boot to 4.0.0
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
  ````

- [ ] **4.2** Commit.

  ```bash
  cd /Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter
  git add CONTRIBUTING.md
  git commit -m "docs: add CONTRIBUTING.md with build, style, commit, and PR guidelines"
  ```

---

## Task 5: Dependency bump — Spring Boot 4, Kotlin 2.1, Java 21

**Files:**
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/buildSrc/src/main/kotlin/pagbank.kotlin-library.gradle.kts`
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/pagbank-spring-boot-autoconfigure/build.gradle.kts`
- Modify: `/Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter/README.md`

> **Before starting this task:** check the latest Spring Boot 4 GA version at https://spring.io/projects/spring-boot and substitute it for `4.0.x` below. At time of writing the plan, `4.0.0` is used as the placeholder.

### Steps

- [ ] **5.1** Update the Spring Boot BOM in the convention plugin.

  In `pagbank.kotlin-library.gradle.kts`, change:

  ```kotlin
  implementation(platform("org.springframework.boot:spring-boot-dependencies:3.2.0"))
  ```

  to:

  ```kotlin
  implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.x"))
  ```

- [ ] **5.2** Update the BOM for the `kapt` configuration in the autoconfigure module.

  In `pagbank-spring-boot-autoconfigure/build.gradle.kts`, change:

  ```kotlin
  kapt(platform("org.springframework.boot:spring-boot-dependencies:3.2.0"))
  ```

  to:

  ```kotlin
  kapt(platform("org.springframework.boot:spring-boot-dependencies:4.0.x"))
  ```

- [ ] **5.3** Update the compatibility table in `README.md`.

  Find the compatibility section (or add one after the badges) and set:

  ```markdown
  ## Compatibility

  | Library version | Spring Boot | Java | Kotlin |
  |---|---|---|---|
  | 1.x | 4.0+ | 21+ | 2.1+ |
  ```

- [ ] **5.4** Verify the build compiles and all tests pass.

  ```bash
  cd /Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter
  ./gradlew build
  ```

  Expected: `BUILD SUCCESSFUL` with no test failures.

  **Common Spring Boot 4 / Spring Framework 7 migration points to watch for:**
  - `RestClient` API: verify no constructor or builder method signatures changed.
  - `HealthIndicator`: interface is in `org.springframework.boot.actuate.health` — unchanged, but verify import paths.
  - Auto-configuration: `AutoConfiguration.imports` format is the same; `spring.factories` support was removed in Boot 3.x already.
  - Jakarta EE 11: already on `jakarta.*` since Boot 3 — no change needed.
  - `@SpringBootTest` and test slice annotations: check for any deprecation removals.

  If any API is missing or renamed, fix the source file before committing.

- [ ] **5.5** Run the full quality gate.

  ```bash
  cd /Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter
  ./gradlew spotlessCheck detekt jacocoTestCoverageVerification
  ```

  Expected: `BUILD SUCCESSFUL`.

- [ ] **5.6** Commit.

  ```bash
  cd /Users/montanha/workspace/org_rodrigoma/spring-boot-pagbank-starter
  git add buildSrc/src/main/kotlin/pagbank.kotlin-library.gradle.kts \
           pagbank-spring-boot-autoconfigure/build.gradle.kts \
           README.md
  git commit -m "chore: bump to Spring Boot 4, Kotlin 2.1, Java 21 — requires Boot 4+ at runtime"
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
