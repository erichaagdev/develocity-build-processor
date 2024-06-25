@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.application)
    alias(libs.plugins.develocity.api.models)
    alias(libs.plugins.java.test.fixtures)
}

group = "dev.erichaag"
version = "0.0.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.apache.commons.math)
    testFixturesImplementation(libs.junit.jupiter)
}

application {
    applicationName = "develocity-failure-insights"
    mainClass = "dev.erichaag.develocity.Main"
    executableDir = ""
}

distributions {
    main {
        contents {
            from(layout.projectDirectory.file("config.properties"))
            from(layout.projectDirectory.file("README.md"))
        }
    }
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
