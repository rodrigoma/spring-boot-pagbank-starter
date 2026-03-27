plugins {
    id("pagbank.kotlin-library")
    id("org.springframework.boot")
}

group = "io.github.rodrigoma"
version = "1.0.0-SNAPSHOT"

dependencies {
    implementation(project(":pagbank-spring-boot-starter"))
    implementation("org.springframework.boot:spring-boot-starter-web")
}

// Spring Boot produces the fat jar; disable the plain jar task
tasks.named<Jar>("jar") {
    enabled = false
}

// Disable the JaCoCo coverage gate for the sample module
tasks.named("jacocoTestCoverageVerification") {
    enabled = false
}
