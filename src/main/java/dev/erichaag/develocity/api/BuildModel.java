package dev.erichaag.develocity.api;

public enum BuildModel {

    ALL_MODELS("*", null),
    GRADLE_ARTIFACT_TRANSFORM_EXECUTIONS("gradle-artifact-transform-executions", GradleBuild.class),
    GRADLE_ATTRIBUTES("gradle-attributes", GradleBuild.class),
    GRADLE_BUILD_CACHE_PERFORMANCE("gradle-build-cache-performance", GradleBuild.class),
    GRADLE_DEPRECATIONS("gradle-deprecations", GradleBuild.class),
    GRADLE_NETWORK_ACTIVITY("gradle-network-activity", GradleBuild.class),
    GRADLE_PROJECTS("gradle-projects", GradleBuild.class),
    MAVEN_ATTRIBUTES("maven-attributes", MavenBuild.class),
    MAVEN_BUILD_CACHE_PERFORMANCE("maven-build-cache-performance", MavenBuild.class),
    MAVEN_DEPENDENCY_RESOLUTION("maven-dependency-resolution", MavenBuild.class),
    MAVEN_MODULES("maven-modules", MavenBuild.class);

    private final String modelName;
    private final Class<? extends Build> modelFor;

    BuildModel(String modelName, Class<? extends Build> modelFor) {
        this.modelName = modelName;
        this.modelFor = modelFor;
    }

    boolean isModelFor(Build build) {
        return modelFor == null || modelFor.isInstance(build);
    }

    public String modelName() {
        return modelName;
    }

}
