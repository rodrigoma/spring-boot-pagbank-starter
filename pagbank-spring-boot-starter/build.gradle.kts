plugins {
    id("pagbank.kotlin-library")
    id("pagbank.publish")
}

group = "io.github.rodrigoma"

dependencies {
    api(project(":pagbank-spring-boot-autoconfigure"))
    implementation("org.springframework.boot:spring-boot-starter")
}
