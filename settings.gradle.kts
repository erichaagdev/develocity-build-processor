plugins {
    id("com.gradle.develocity") version "3.19.2"
    id("com.gradle.common-custom-user-data-gradle-plugin") version "2.2.1"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

develocity {
    buildScan {
        termsOfUseAgree = "yes"
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
    }
}

rootProject.name = "develocity-build-processor"
