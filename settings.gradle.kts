plugins {
    id("com.gradle.develocity") version "4.1.1"
    id("com.gradle.common-custom-user-data-gradle-plugin") version "2.3"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

develocity {
    buildScan {
        termsOfUseAgree = "yes"
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
    }
}

rootProject.name = "develocity-build-processor"
