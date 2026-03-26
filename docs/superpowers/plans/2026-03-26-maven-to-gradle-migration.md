# Maven → Gradle Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate the two-module Maven project to Gradle 8.x with Kotlin DSL, `buildSrc` convention plugins, and Maven Central publishing via OSSRH.

**Architecture:** Multi-project Gradle build with `settings.gradle.kts` at root. Shared build logic (Kotlin/Spring compilation, Maven Central publishing) lives in two `buildSrc` convention plugins applied to both submodules. No Maven files remain after migration.

**Tech Stack:** Gradle 8.12, Kotlin DSL (`*.gradle.kts`), `kotlin-dsl` plugin in buildSrc, Spring Boot BOM 3.2.0, `maven-publish` + `signing` plugins, OSSRH (Sonatype) for Maven Central.

---

## Files Created

| File | Purpose |
|------|---------|
| `settings.gradle.kts` | Root project name + submodule includes |
| `build.gradle.kts` | Empty root build script |
| `buildSrc/build.gradle.kts` | Enables `kotlin-dsl` + adds Kotlin plugin as dependency |
| `buildSrc/src/main/kotlin/pagbank.kotlin-library.gradle.kts` | Convention: Kotlin/Spring compilation, BOM, shared deps |
| `buildSrc/src/main/kotlin/pagbank.publish.gradle.kts` | Convention: maven-publish, POM metadata, signing, OSSRH |
| `pagbank-spring-boot-autoconfigure/build.gradle.kts` | Module build script (autoconfigure) |
| `pagbank-spring-boot-starter/build.gradle.kts` | Module build script (starter) |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle version lock |

## Files Modified

| File | Change |
|------|--------|
| `.gitignore` | Add `.gradle/`, `build/`, wrapper JAR exception |

## Files Deleted

| File |
|------|
| `pom.xml` |
| `pagbank-spring-boot-autoconfigure/pom.xml` |
| `pagbank-spring-boot-starter/pom.xml` |

---

### Task 1: Update .gitignore

**Files:**
- Modify: `.gitignore`

- [ ] **Step 1: Replace .gitignore content**

Open `.gitignore` and replace its entire content with:

```
# Build output
target/
build/

# Gradle
.gradle/

# IDE
.DS_Store
.idea/
*.iml
.settings/
.project
.classpath

# Compiled files
*.class

# JARs — keep gradle wrapper and built artifacts in src/main
*.jar
!gradle/wrapper/gradle-wrapper.jar
!**/src/main/**/*.jar
```

- [ ] **Step 2: Commit**

```bash
git add .gitignore
git commit -m "chore: update .gitignore for Gradle"
```

---

### Task 2: Bootstrap Gradle Wrapper

**Files:**
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `gradle/wrapper/gradle-wrapper.jar`
- Create: `gradlew`
- Create: `gradlew.bat`

> **Prerequisite:** Gradle must be installed locally (`brew install gradle` on macOS). Run `gradle --version` to confirm. If not installed, install via `brew install gradle`.

- [ ] **Step 1: Generate wrapper**

Run from the project root:

```bash
gradle wrapper --gradle-version 8.12 --distribution-type bin
```

Expected output:
```
BUILD SUCCESSFUL in Xs
1 actionable task: 1 executed
```

- [ ] **Step 2: Make gradlew executable**

```bash
chmod +x gradlew
```

- [ ] **Step 3: Verify wrapper works**

```bash
./gradlew --version
```

Expected output includes:
```
Gradle 8.12
```

- [ ] **Step 4: Commit**

```bash
git add gradlew gradlew.bat gradle/wrapper/
git commit -m "chore: add Gradle 8.12 wrapper"
```

---

### Task 3: Create root build files

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`

- [ ] **Step 1: Create settings.gradle.kts**

```kotlin
rootProject.name = "pagbank-spring-boot-parent"

include(
    "pagbank-spring-boot-autoconfigure",
    "pagbank-spring-boot-starter"
)
```

- [ ] **Step 2: Create empty root build.gradle.kts**

```kotlin
// Root build script — module configuration lives in subprojects and buildSrc
```

- [ ] **Step 3: Verify Gradle sees the project structure**

```bash
./gradlew projects
```

Expected output:
```
Root project 'pagbank-spring-boot-parent'
+--- Project ':pagbank-spring-boot-autoconfigure'
\--- Project ':pagbank-spring-boot-starter'
```

- [ ] **Step 4: Commit**

```bash
git add settings.gradle.kts build.gradle.kts
git commit -m "chore: add root Gradle build files"
```

---

### Task 4: Create buildSrc

**Files:**
- Create: `buildSrc/build.gradle.kts`

- [ ] **Step 1: Create buildSrc/build.gradle.kts**

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
}
```

- [ ] **Step 2: Verify buildSrc compiles**

```bash
./gradlew help
```

Expected: `BUILD SUCCESSFUL` (buildSrc compiles even with no convention plugins yet)

- [ ] **Step 3: Commit**

```bash
git add buildSrc/
git commit -m "chore: add buildSrc with kotlin-dsl"
```

---

### Task 5: Create kotlin-library convention plugin

**Files:**
- Create: `buildSrc/src/main/kotlin/pagbank.kotlin-library.gradle.kts`

- [ ] **Step 1: Create the convention plugin**

Create `buildSrc/src/main/kotlin/pagbank.kotlin-library.gradle.kts`:

```kotlin
plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
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

java {
    withSourcesJar()
    withJavadocJar()
}
```

- [ ] **Step 2: Verify buildSrc compiles**

```bash
./gradlew help
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add buildSrc/src/
git commit -m "chore: add pagbank.kotlin-library convention plugin"
```

---

### Task 6: Create publish convention plugin

**Files:**
- Create: `buildSrc/src/main/kotlin/pagbank.publish.gradle.kts`

- [ ] **Step 1: Create the convention plugin**

Create `buildSrc/src/main/kotlin/pagbank.publish.gradle.kts`:

```kotlin
plugins {
    `maven-publish`
    signing
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set(project.name)
                description.set("Spring Boot starter for PagBank (PagSeguro) subscription and recurring payment API")
                url.set("https://github.com/rodrigoma/spring-boot-pagbank-starter")

                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("rodrigoma")
                        name.set("Rodrigo Montanha")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/rodrigoma/spring-boot-pagbank-starter.git")
                    developerConnection.set("scm:git:ssh://github.com:rodrigoma/spring-boot-pagbank-starter.git")
                    url.set("https://github.com/rodrigoma/spring-boot-pagbank-starter")
                }
            }
        }
    }

    repositories {
        maven {
            name = "ossrh"
            val releasesUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsUrl else releasesUrl
            credentials {
                username = findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
                password = findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    val signingKey = findProperty("signingKey") as String? ?: System.getenv("ORG_GRADLE_PROJECT_signingKey")
    val signingPassword = findProperty("signingPassword") as String? ?: System.getenv("ORG_GRADLE_PROJECT_signingPassword")
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["mavenJava"])
    }
}
```

- [ ] **Step 2: Verify buildSrc compiles**

```bash
./gradlew help
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add buildSrc/src/main/kotlin/pagbank.publish.gradle.kts
git commit -m "chore: add pagbank.publish convention plugin"
```

---

### Task 7: Create autoconfigure module build script

**Files:**
- Create: `pagbank-spring-boot-autoconfigure/build.gradle.kts`

- [ ] **Step 1: Create build.gradle.kts**

Create `pagbank-spring-boot-autoconfigure/build.gradle.kts`:

```kotlin
plugins {
    id("pagbank.kotlin-library")
    id("pagbank.publish")
    kotlin("kapt")
}

group = "io.github.rodrigoma"
version = "1.0.0-SNAPSHOT"

dependencies {
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
}
```

- [ ] **Step 2: Add kapt to buildSrc dependencies**

Open `buildSrc/build.gradle.kts` and add `kotlin-allopen` is already there. Verify the file looks like:

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
}
```

> Note: `kotlin("kapt")` (plugin id `org.jetbrains.kotlin.kapt`) is bundled with `kotlin-gradle-plugin`, so no extra buildSrc dependency is needed.

- [ ] **Step 3: Verify module compiles**

```bash
./gradlew :pagbank-spring-boot-autoconfigure:compileKotlin
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add pagbank-spring-boot-autoconfigure/build.gradle.kts
git commit -m "chore: add Gradle build script for autoconfigure module"
```

---

### Task 8: Create starter module build script

**Files:**
- Create: `pagbank-spring-boot-starter/build.gradle.kts`

- [ ] **Step 1: Create build.gradle.kts**

Create `pagbank-spring-boot-starter/build.gradle.kts`:

```kotlin
plugins {
    id("pagbank.kotlin-library")
    id("pagbank.publish")
}

group = "io.github.rodrigoma"
version = "1.0.0-SNAPSHOT"

dependencies {
    api(project(":pagbank-spring-boot-autoconfigure"))
    implementation("org.springframework.boot:spring-boot-starter")
}
```

- [ ] **Step 2: Verify full build with tests**

```bash
./gradlew build
```

Expected: `BUILD SUCCESSFUL` with all existing tests passing. Look for output like:
```
> Task :pagbank-spring-boot-autoconfigure:test
...
> Task :pagbank-spring-boot-starter:test
...
BUILD SUCCESSFUL
```

If any test fails, do NOT proceed — investigate the failure before continuing.

- [ ] **Step 3: Commit**

```bash
git add pagbank-spring-boot-starter/build.gradle.kts
git commit -m "chore: add Gradle build script for starter module"
```

---

### Task 9: Verify publishToMavenLocal

- [ ] **Step 1: Run publishToMavenLocal**

```bash
./gradlew publishToMavenLocal
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: Verify artifacts in local Maven repo**

```bash
find ~/.m2/repository/io/github/rodrigoma -name "*.jar" | sort
```

Expected output (4 JARs per module — main, sources, javadoc + pom):
```
~/.m2/repository/io/github/rodrigoma/pagbank-spring-boot-autoconfigure/1.0.0-SNAPSHOT/pagbank-spring-boot-autoconfigure-1.0.0-SNAPSHOT.jar
~/.m2/repository/io/github/rodrigoma/pagbank-spring-boot-autoconfigure/1.0.0-SNAPSHOT/pagbank-spring-boot-autoconfigure-1.0.0-SNAPSHOT-sources.jar
~/.m2/repository/io/github/rodrigoma/pagbank-spring-boot-autoconfigure/1.0.0-SNAPSHOT/pagbank-spring-boot-autoconfigure-1.0.0-SNAPSHOT-javadoc.jar
~/.m2/repository/io/github/rodrigoma/pagbank-spring-boot-starter/1.0.0-SNAPSHOT/pagbank-spring-boot-starter-1.0.0-SNAPSHOT.jar
~/.m2/repository/io/github/rodrigoma/pagbank-spring-boot-starter/1.0.0-SNAPSHOT/pagbank-spring-boot-starter-1.0.0-SNAPSHOT-sources.jar
~/.m2/repository/io/github/rodrigoma/pagbank-spring-boot-starter/1.0.0-SNAPSHOT/pagbank-spring-boot-starter-1.0.0-SNAPSHOT-javadoc.jar
```

---

### Task 10: Delete Maven files

**Files:**
- Delete: `pom.xml`
- Delete: `pagbank-spring-boot-autoconfigure/pom.xml`
- Delete: `pagbank-spring-boot-starter/pom.xml`

- [ ] **Step 1: Delete all pom.xml files**

```bash
git rm pom.xml pagbank-spring-boot-autoconfigure/pom.xml pagbank-spring-boot-starter/pom.xml
```

- [ ] **Step 2: Run full build one more time to confirm nothing broke**

```bash
./gradlew build
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Verify no pom.xml files remain**

```bash
find . -name "pom.xml" -not -path "./.gradle/*"
```

Expected: no output.

- [ ] **Step 4: Commit**

```bash
git commit -m "chore: remove Maven pom.xml files — migrated to Gradle"
```
