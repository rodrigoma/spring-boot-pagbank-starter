plugins {
    id("pagbank.security")
    id("com.gradleup.nmcp") version "0.1.4"
}

nmcp {
    publishAggregation {
        project(":pagbank-spring-boot-autoconfigure")
        project(":pagbank-spring-boot-starter")
        username = System.getenv("OSSRH_USERNAME") ?: ""
        password = System.getenv("OSSRH_PASSWORD") ?: ""
        publicationType = "AUTOMATIC"
    }
}
