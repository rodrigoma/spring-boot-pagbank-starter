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
