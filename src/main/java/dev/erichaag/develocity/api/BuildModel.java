package dev.erichaag.develocity.api;

public enum BuildModel {

    ALL_MODELS("*"),
    GRADLE_ATTRIBUTES("gradle-attributes"),
    GRADLE_BUILD_CACHE_PERFORMANCE("gradle-build-cache-performance"),
    GRADLE_NETWORK_ACTIVITY("gradle-network-activity"),
    GRADLE_PROJECTS("gradle-projects"),
    MAVEN_ATTRIBUTES("maven-attributes"),
    MAVEN_BUILD_CACHE_PERFORMANCE("maven-build-cache-performance"),
    MAVEN_DEPENDENCY_RESOLUTION("maven-dependency-resolution"),
    MAVEN_MODULES("maven-modules");

    final String modelName;

    BuildModel(String modelName) {
        this.modelName = modelName;
    }

}
