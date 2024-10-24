plugins {
    id("com.gradle.develocity") version "3.18.1"
    id("com.gradle.common-custom-user-data-gradle-plugin") version "2.0.2"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

develocity {
    buildScan {
        termsOfUseAgree = "yes"
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
    }
}

rootProject.name = "develocity-build-processor"
