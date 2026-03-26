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
    kapt(platform("org.springframework.boot:spring-boot-dependencies:3.2.0"))
    kapt("org.springframework.boot:spring-boot-configuration-processor")
}
