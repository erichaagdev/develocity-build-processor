@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.develocity.api.models)
    alias(libs.plugins.application)
}

group = "dev.erichaag"
version = "0.0.2"

dependencies {
    implementation("org.apache.commons:commons-math3:3.6.1")
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

val test by testing.suites.getting(JvmTestSuite::class) {
    useJUnitJupiter()
}

develocityApi {
    version = "2024.1"
}

repositories {
    mavenCentral()
}
