plugins {
    id("java")
    id("maven-publish")
    id("signing")
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing.repositories.maven {
    name = "distribution"
    url = uri(layout.buildDirectory.dir("distributionRepository"))
}

tasks.javadoc {
    (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
}

val mavenJava by publishing.publications.creating(MavenPublication::class) {
    from(components["java"])
}

signing {
    val signingSecretKey = providers.gradleProperty("signingSecretKey")
    val signingPassword = providers.gradleProperty("signingPassword")
    if (signingSecretKey.isPresent && signingPassword.isPresent) {
        sign(mavenJava)
        useInMemoryPgpKeys(signingSecretKey.get(), signingPassword.get())
        isRequired = true
    }
}

val verifySigningCredentials by tasks.registering {
    val signingSecretKey = providers.gradleProperty("signingSecretKey")
    val signingPassword = providers.gradleProperty("signingPassword")
    doFirst {
        if (!signingSecretKey.isPresent && !signingPassword.isPresent) throw GradleException("'signingSecretKey' and 'signingPassword' properties must be set")
        if (!signingSecretKey.isPresent) throw GradleException("'signingSecretKey' property must be set")
        if (!signingPassword.isPresent) throw GradleException("'signingPassword' property must be set")
    }
}

val publishMavenJavaPublicationToDistributionRepository = tasks.named<PublishToMavenRepository>("publishMavenJavaPublicationToDistributionRepository") {
    dependsOn(verifySigningCredentials)
    val repositoryUrl = repository.url
    doFirst {
        File(repositoryUrl.toURL().file).deleteRecursively()
    }
}

val packageDistribution by tasks.registering(Zip::class) {
    destinationDirectory = layout.buildDirectory.dir("packageDistribution")
    from(publishMavenJavaPublicationToDistributionRepository.map { it.repository.url })
    exclude("**/*.asc.*")
}
