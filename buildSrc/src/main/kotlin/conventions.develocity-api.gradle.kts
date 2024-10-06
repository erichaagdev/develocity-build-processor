@file:Suppress("UnstableApiUsage", "unused", "HasPlatformType")

plugins {
    id("java")
    id("org.openapi.generator")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

val develocityApiExtension = extensions.create<DevelocityApiExtension>("develocityApi")

repositories {
    exclusiveContent {
        forRepository {
            ivy {
                name = "Develocity API Specification"
                url = uri("https://docs.gradle.com/enterprise/api-manual/ref")
                patternLayout { artifact("/develocity-[revision]-api.yaml") }
                metadataSources { artifact() }
            }
        }
        filter { includeModule("com.gradle", "develocity-api-specification") }
    }
}

val develocityApiSpecification: DependencyScopeConfiguration = configurations.dependencyScope("develocityApiSpecification") {
    attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, "develocity-api-specification"))
}.get()

val resolvableDevelocityApiSpecification = configurations.resolvable("resolvableDevelocityApiSpecification") {
    extendsFrom(develocityApiSpecification)
    attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, "develocity-api-specification"))
}

dependencies {
    develocityApiSpecification(develocityApiExtension.version.map { "com.gradle:develocity-api-specification:$it" })
    implementation(libs.findLibrary("jackson-annotations").get())
    implementation(libs.findLibrary("jackson-databind").get())
    implementation(libs.findLibrary("jakarta-annotations").get())
}

pluginManager.withPlugin("java-library") {
    dependencies {
        "api"(libs.findLibrary("jackson-annotations").get())
        "api"(libs.findLibrary("jakarta-annotations").get())
    }
}

val postProcessDevelocityApiSpecification by tasks.registering(PostProcessDevelocityApiSpecification::class) {
    inputSpecification = resolvableDevelocityApiSpecification
    outputSpecification = layout.buildDirectory.file("$name/openapi.yaml")
}

openApiGenerate {
    generatorName = "java"
    inputSpec = postProcessDevelocityApiSpecification.flatMap { it.outputSpecification.asFile }.map { it.toString() }
    outputDir = layout.buildDirectory.dir("generated/openapi").map { it.asFile.absolutePath }
    modelPackage = provider { "$group.develocity.api" }
    apiPackage = provider { "$group.unused.api" }
    invokerPackage = provider { "$group.unused.invoker" }
    cleanupOutput = true
    openapiNormalizer = mapOf("REF_AS_PARENT_IN_ALLOF" to "true")
    // see https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/java.md for a description of each configuration option
    configOptions = mapOf(
        "additionalModelTypeAnnotations" to "@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)",
        "containerDefaultToNull" to "true",
        "disallowAdditionalPropertiesIfNotPresent" to "false",
        "hideGenerationTimestamp" to "true",
        "library" to "native",
        "openApiNullable" to "false",
        "supportUrlQuery" to "false",
        "useBeanValidation" to "false",
        "useJakartaEe" to "true",
    )
}

val generateDevelocityApiModels by tasks.registering(Sync::class) {
    from(tasks.openApiGenerate) {
        includeEmptyDirs = false
        include("src/main/java/dev/erichaag/develocity/api/*")
        eachFile {
            path = path.removePrefix("src/main/java")
        }
    }
    into(layout.buildDirectory.dir(name))
}

val checkSupportedBuildModels by tasks.registering(CheckSupportedBuildModels::class) {
    inputSpecification = resolvableDevelocityApiSpecification
    sources = sourceSets.main.map { it.java.sourceDirectories }
    outputReportDirectory = layout.buildDirectory.dir("reports/$name")
}

val generate by tasks.registering {
    dependsOn(generateDevelocityApiModels)
}

val check by tasks.getting {
    dependsOn(checkSupportedBuildModels)
}

sourceSets {
    main {
        java {
            srcDir(generateDevelocityApiModels)
        }
    }
}

abstract class DevelocityApiExtension {
    abstract val version: Property<String>
}

abstract class PostProcessDevelocityApiSpecification : DefaultTask() {

    @get:InputFiles
    abstract val inputSpecification: Property<FileCollection>

    @get:OutputFile
    abstract val outputSpecification: RegularFileProperty

    @TaskAction
    fun action() {
        inputSpecification.get().singleFile.readText()
            .replace(" Build:", " ApiBuild:")
            .replace("Build'", "ApiBuild'")
            .run { outputSpecification.get().asFile.writeText(this) }
    }

}

abstract class CheckSupportedBuildModels : DefaultTask() {

    @get:InputFiles
    abstract val inputSpecification: Property<FileCollection>

    @get:InputFiles
    abstract val sources: Property<FileCollection>

    @get:OutputDirectory
    abstract val outputReportDirectory: DirectoryProperty

    @TaskAction
    fun action() {
        val buildModelJava = sources.get().asFileTree.matching { include("**/BuildModel.java") }.singleFile.readText()
        val requiredBuildModels = inputSpecification.get()
            .singleFile
            .readLines()
            .filter { it.startsWith("  /api/builds/{id}") && it.endsWith(":") && it != "  /api/builds/{id}:" }
            .map { it.removeSurrounding("  /api/builds/{id}/", ":") }
        val unsupportedBuildModels = requiredBuildModels.filter { !buildModelJava.contains(it) }
        if (unsupportedBuildModels.isNotEmpty()) {
            throw GradleException("Unsupported build models: ${unsupportedBuildModels.sorted().joinToString(", ")}")
        }
        outputReportDirectory.get()
            .asFile
            .resolve("supported-models.txt")
            .writeText(requiredBuildModels.subtract(unsupportedBuildModels).sorted().joinToString("\n"))
    }

}
