plugins {
    id("pagbank.kotlin-library")
    id("pagbank.publish")
}

group = "io.github.rodrigoma"

dependencies {
    // Needed at runtime: spring-boot-starter-web does not bring it, and it is what lets the starter
    // build a request factory with timeouts using whichever HTTP client the consumer has on the classpath.
    api("org.springframework.boot:spring-boot-http-client")
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator")
}
