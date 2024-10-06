@file:Suppress("UnstableApiUsage", "unused")

plugins {
    alias(libs.plugins.develocityApiConventions)
    alias(libs.plugins.javaLibrary)
    alias(libs.plugins.javaTestFixtures)
    alias(libs.plugins.releaseConventions)
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
    version = "2024.2"
}

val test by testing.suites.getting(JvmTestSuite::class) {
    useJUnitJupiter(libs.versions.junit)
}

val mavenJava by publishing.publications.getting(MavenPublication::class) {
    pom {
        name = "Develocity Build Processor"
        description = "A library to process Develocity™ build data with built-in caching and retry mechanisms."
        url = "https://github.com/erichaagdev/develocity-build-processor"
        licenses {
            license {
                name = "MIT License"
                url = "https://raw.githubusercontent.com/erichaagdev/develocity-build-processor/main/LICENSE"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "erichaagdev"
                name = "Eric Haag"
                email = "eah0592@gmail.com"
            }
        }
        scm {
            connection = "scm:git:git://github.com/erichaagdev/develocity-build-processor.git"
            developerConnection = "scm:git:ssh://github.com/erichaagdev/develocity-build-processor.git"
            url = "https://github.com/erichaagdev/develocity-build-processor"
        }
    }
}
