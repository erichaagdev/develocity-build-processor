package dev.erichaag.develocity.api;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static dev.erichaag.develocity.api.BuildModel.MAVEN_ATTRIBUTES;
import static dev.erichaag.develocity.api.BuildModel.MAVEN_BUILD_CACHE_PERFORMANCE;
import static dev.erichaag.develocity.api.BuildModel.MAVEN_DEPENDENCY_RESOLUTION;
import static dev.erichaag.develocity.api.BuildModel.MAVEN_MODULES;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toUnmodifiableSet;

public final class MavenBuild implements Build {

    private final ApiBuild build;

    MavenBuild(ApiBuild build) {
        this.build = build;
    }

    @Override
    public String getId() {
        return build.getId();
    }

    @Override
    public long getAvailableAt() {
        return build.getAvailableAt();
    }

    @Override
    public String getBuildToolVersion() {
        return build.getBuildToolVersion();
    }

    @Override
    public String getBuildAgentVersion() {
        return build.getBuildAgentVersion();
    }

    @Override
    public Set<BuildModel> getAvailableBuildModels() {
        final var buildModels = Stream.<BuildModel>builder();
        if (getAttributes().isPresent()) buildModels.add(MAVEN_ATTRIBUTES);
        if (getBuildCachePerformance().isPresent()) buildModels.add(MAVEN_BUILD_CACHE_PERFORMANCE);
        if (getDependencyResolution().isPresent()) buildModels.add(MAVEN_DEPENDENCY_RESOLUTION);
        if (getModules().isPresent()) buildModels.add(MAVEN_MODULES);
        return buildModels.build().collect(toUnmodifiableSet());
    }

    @Override
    public ApiBuild getBuild() {
        return build;
    }

    public Optional<MavenAttributes> getAttributes() {
        return ofNullable(build.getModels())
                .map(BuildModels::getMavenAttributes)
                .map(BuildModelsMavenAttributes::getModel);
    }

    public Optional<MavenBuildCachePerformance> getBuildCachePerformance() {
        return ofNullable(build.getModels())
                .map(BuildModels::getMavenBuildCachePerformance)
                .map(BuildModelsMavenBuildCachePerformance::getModel);
    }

    public Optional<MavenDependencyResolution> getDependencyResolution() {
        return ofNullable(build.getModels())
                .map(BuildModels::getMavenDependencyResolution)
                .map(BuildModelsMavenDependencyResolution::getModel);
    }

    public Optional<List<MavenModule>> getModules() {
        return ofNullable(build.getModels())
                .map(BuildModels::getMavenModules)
                .map(BuildModelsMavenModules::getModel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MavenBuild that = (MavenBuild) o;
        return Objects.equals(build, that.build);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(build);
    }

    @Override
    public String toString() {
        return "MavenBuild{build=" + build + "}";
    }

}
