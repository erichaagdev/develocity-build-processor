package dev.erichaag.develocity.api;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static dev.erichaag.develocity.api.AttributesNotPresentException.attributesNotPresent;
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
    public Instant getStartTime() {
        return getAttributes()
                .map(MavenAttributes::getBuildStartTime)
                .map(Instant::ofEpochMilli)
                .orElseThrow(attributesNotPresent("getStartTime()"));
    }

    @Override
    public Duration getDuration() {
        return getAttributes()
                .map(MavenAttributes::getBuildDuration)
                .map(Duration::ofMillis)
                .orElseThrow(attributesNotPresent("getDuration()"));
    }

    @Override
    public String getProjectName() {
        return getAttributes()
                .map(MavenAttributes::getTopLevelProjectName)
                .orElseThrow(attributesNotPresent("getProjectName()"));
    }

    @Override
    public List<String> getRequestedWorkUnits() {
        return getAttributes()
                .map(MavenAttributes::getRequestedGoals)
                .orElseThrow(attributesNotPresent("getRequestedWorkUnits()"));
    }

    @Override
    public boolean hasFailed() {
        return getAttributes()
                .map(MavenAttributes::getHasFailed)
                .orElseThrow(attributesNotPresent("hasFailed()"));
    }

    @Override
    public String getUser() {
        return getAttributes()
                .map(MavenAttributes::getEnvironment)
                .map(BuildAttributesEnvironment::getUsername)
                .orElseThrow(attributesNotPresent("getUser()"));
    }

    @Override
    public List<String> getTags() {
        return getAttributes()
                .map(MavenAttributes::getTags)
                .orElseThrow(attributesNotPresent("getTags()"));
    }

    @Override
    public List<Value> getValues() {
        return getAttributes()
                .orElseThrow(attributesNotPresent("getValues()"))
                .getValues()
                .stream()
                .map(Value::new)
                .toList();
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
