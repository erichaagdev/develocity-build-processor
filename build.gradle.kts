@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.develocity.api.models)
    alias(libs.plugins.java.test.fixtures)
    alias(libs.plugins.java.library)
}

group = "dev.erichaag"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testFixturesImplementation(libs.junit.jupiter)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

develocityApi {
    version = "2024.1"
}

val test by testing.suites.getting(JvmTestSuite::class) {
    useJUnitJupiter(libs.versions.junit)
}
