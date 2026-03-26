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
