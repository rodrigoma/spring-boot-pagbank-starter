# Maven → Gradle Migration — Design Spec

**Date:** 2026-03-26
**Status:** Approved

## Overview

Migrate the two-module Maven project (`pagbank-spring-boot-autoconfigure` + `pagbank-spring-boot-starter`) to Gradle with Kotlin DSL, using `buildSrc` convention plugins to share build logic. The migration includes Maven Central publishing configuration via OSSRH + GPG signing.

## Approach

Multi-project Gradle build with `buildSrc` convention plugins (Opção A). No Version Catalog — the project has few dependencies and two modules, so `buildSrc` is sufficient. Gradle Wrapper is committed to the repository.

## File Structure

```
spring-boot-pagbank-starter/
├── settings.gradle.kts                        # rootProject name + module includes
├── build.gradle.kts                           # empty root (plugins block only if needed)
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties          # Gradle 8.x
├── gradlew
├── gradlew.bat
├── buildSrc/
│   ├── build.gradle.kts                       # applies kotlin-dsl plugin
│   └── src/main/kotlin/
│       ├── pagbank.kotlin-library.gradle.kts  # Kotlin + Spring compilation convention
│       └── pagbank.publish.gradle.kts         # maven-publish + signing convention
├── pagbank-spring-boot-autoconfigure/
│   └── build.gradle.kts                       # applies both conventions + module deps
└── pagbank-spring-boot-starter/
    └── build.gradle.kts                       # applies both conventions + module deps
```

All `pom.xml` files are removed.

## Convention Plugins

### `pagbank.kotlin-library.gradle.kts`

Applied to both submodules. Responsibilities:

- Apply `kotlin("jvm")` with `jvmToolchain(17)`
- Compiler arg `-Xjsr305=strict`
- Apply `kotlin("plugin.spring")` (AllOpen for Spring classes)
- Declare shared dependencies:
  - `implementation`: `kotlin-stdlib`, `jackson-module-kotlin`, `spring-boot-autoconfigure`
  - `testImplementation`: `spring-boot-starter-test`

### `pagbank.publish.gradle.kts`

Applied to both submodules. Responsibilities:

- Apply `maven-publish` plugin
- Configure `MavenPublication` with required POM fields for Maven Central:
  - `groupId`, `artifactId`, `version`
  - `name`, `description`, `url`
  - `licenses` (Apache 2.0)
  - `developers`
  - `scm` (GitHub URL)
- Apply `signing` plugin using in-memory GPG key:
  - `signingKey` ← `ORG_GRADLE_PROJECT_signingKey` env var
  - `signingPassword` ← `ORG_GRADLE_PROJECT_signingPassword` env var
- Configure OSSRH (Sonatype) repository for Maven Central publishing

## Module Build Scripts

### `pagbank-spring-boot-autoconfigure/build.gradle.kts`

```
plugins {
    id("pagbank.kotlin-library")
    id("pagbank.publish")
}

dependencies {
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
```

### `pagbank-spring-boot-starter/build.gradle.kts`

```
plugins {
    id("pagbank.kotlin-library")
    id("pagbank.publish")
}

dependencies {
    api(project(":pagbank-spring-boot-autoconfigure"))
    implementation("org.springframework.boot:spring-boot-starter")
}
```

## Dependency versions

Spring Boot BOM is imported via `platform("org.springframework.boot:spring-boot-dependencies:3.2.0")` in the `pagbank.kotlin-library` convention plugin, replacing the `<parent>` POM inheritance from Maven.

Kotlin version is managed via the `kotlin("jvm")` plugin version declared in `buildSrc/build.gradle.kts`.

## Removals

| File | Action |
|------|--------|
| `pom.xml` (root) | Delete |
| `pagbank-spring-boot-autoconfigure/pom.xml` | Delete |
| `pagbank-spring-boot-starter/pom.xml` | Delete |
| `target/` directories | Delete (build artifacts) |

## .gitignore additions

```
.gradle/
build/
```

## Gradle Wrapper

Gradle 8.x (latest stable at time of implementation). Wrapper files committed to the repository:
- `gradlew`
- `gradlew.bat`
- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties`

## Verification

Migration is complete when:
1. `./gradlew build` compiles all modules and runs all existing tests successfully
2. `./gradlew publishToMavenLocal` produces the expected JARs and POMs in `~/.m2`
3. No `pom.xml` files remain in the repository
