package dev.erichaag.develocity.api;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static dev.erichaag.develocity.api.AttributesNotPresentException.attributesNotPresent;
import static dev.erichaag.develocity.api.BuildModel.GRADLE_ARTIFACT_TRANSFORM_EXECUTIONS;
import static dev.erichaag.develocity.api.BuildModel.GRADLE_ATTRIBUTES;
import static dev.erichaag.develocity.api.BuildModel.GRADLE_BUILD_CACHE_PERFORMANCE;
import static dev.erichaag.develocity.api.BuildModel.GRADLE_BUILD_PROFILE_OVERVIEW;
import static dev.erichaag.develocity.api.BuildModel.GRADLE_CONFIGURATION_CACHE;
import static dev.erichaag.develocity.api.BuildModel.GRADLE_DEPRECATIONS;
import static dev.erichaag.develocity.api.BuildModel.GRADLE_NETWORK_ACTIVITY;
import static dev.erichaag.develocity.api.BuildModel.GRADLE_PLUGINS;
import static dev.erichaag.develocity.api.BuildModel.GRADLE_PROJECTS;
import static dev.erichaag.develocity.api.BuildModel.GRADLE_RESOURCE_USAGE;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toUnmodifiableSet;

public final class GradleBuild implements Build {

    private final ApiBuild build;

    GradleBuild(ApiBuild build) {
        this.build = build;
    }

    @Override
    public String getId() {
        return build.getId();
    }

    @Override
    public Instant getAvailableAt() {
        return Instant.ofEpochMilli(build.getAvailableAt());
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
                .map(GradleAttributes::getBuildStartTime)
                .map(Instant::ofEpochMilli)
                .orElseThrow(attributesNotPresent("getStartTime()"));
    }

    @Override
    public Duration getDuration() {
        return getAttributes()
                .map(GradleAttributes::getBuildDuration)
                .map(Duration::ofMillis)
                .orElseThrow(attributesNotPresent("getDuration()"));
    }

    @Override
    public String getProjectName() {
        return getAttributes()
                .map(GradleAttributes::getRootProjectName)
                .orElseThrow(attributesNotPresent("getProjectName()"));
    }

    @Override
    public List<String> getRequestedWorkUnits() {
        return getAttributes()
                .map(GradleAttributes::getRequestedTasks)
                .orElseThrow(attributesNotPresent("getRequestedWorkUnits()"));
    }

    @Override
    public boolean hasFailed() {
        return getAttributes()
                .map(GradleAttributes::getHasFailed)
                .orElseThrow(attributesNotPresent("hasFailed()"));
    }

    @Override
    public String getUser() {
        return getAttributes()
                .map(GradleAttributes::getEnvironment)
                .map(BuildAttributesEnvironment::getUsername)
                .orElseThrow(attributesNotPresent("getUser()"));
    }

    @Override
    public List<String> getTags() {
        return getAttributes()
                .map(GradleAttributes::getTags)
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
        if (getArtifactTransformExecutions().isPresent()) buildModels.add(GRADLE_ARTIFACT_TRANSFORM_EXECUTIONS);
        if (getAttributes().isPresent()) buildModels.add(GRADLE_ATTRIBUTES);
        if (getBuildCachePerformance().isPresent()) buildModels.add(GRADLE_BUILD_CACHE_PERFORMANCE);
        if (getBuildProfileOverview().isPresent()) buildModels.add(GRADLE_BUILD_PROFILE_OVERVIEW);
        if (getConfigurationCache().isPresent()) buildModels.add(GRADLE_CONFIGURATION_CACHE);
        if (getDeprecations().isPresent()) buildModels.add(GRADLE_DEPRECATIONS);
        if (getNetworkActivity().isPresent()) buildModels.add(GRADLE_NETWORK_ACTIVITY);
        if (getPlugins().isPresent()) buildModels.add(GRADLE_PLUGINS);
        if (getProjects().isPresent()) buildModels.add(GRADLE_PROJECTS);
        if (getResourceUsage().isPresent()) buildModels.add(GRADLE_RESOURCE_USAGE);
        return buildModels.build().collect(toUnmodifiableSet());
    }

    @Override
    public ApiBuild getBuild() {
        return build;
    }

    public Optional<List<GradleArtifactTransformExecutionEntry>> getArtifactTransformExecutions() {
        return ofNullable(build.getModels())
                .map(BuildModels::getGradleArtifactTransformExecutions)
                .map(BuildModelsGradleArtifactTransformExecutions::getModel)
                .map(GradleArtifactTransformExecutions::getArtifactTransformExecutions);
    }

    public Optional<GradleAttributes> getAttributes() {
        return ofNullable(build.getModels())
                .map(BuildModels::getGradleAttributes)
                .map(BuildModelsGradleAttributes::getModel);
    }

    public Optional<GradleBuildCachePerformance> getBuildCachePerformance() {
        return ofNullable(build.getModels())
                .map(BuildModels::getGradleBuildCachePerformance)
                .map(BuildModelsGradleBuildCachePerformance::getModel);
    }

    public Optional<GradleBuildProfileOverview> getBuildProfileOverview() {
        return ofNullable(build.getModels())
                .map(BuildModels::getGradleBuildProfileOverview)
                .map(BuildModelsGradleBuildProfileOverview::getModel);
    }

    public Optional<GradleConfigurationCache> getConfigurationCache() {
        return ofNullable(build.getModels())
                .map(BuildModels::getGradleConfigurationCache)
                .map(BuildModelsGradleConfigurationCache::getModel);
    }

    public Optional<List<GradleDeprecationEntry>> getDeprecations() {
        return ofNullable(build.getModels())
                .map(BuildModels::getGradleDeprecations)
                .map(BuildModelsGradleDeprecations::getModel)
                .map(GradleDeprecations::getDeprecations);
    }

    public Optional<GradleNetworkActivity> getNetworkActivity() {
        return ofNullable(build.getModels())
                .map(BuildModels::getGradleNetworkActivity)
                .map(BuildModelsGradleNetworkActivity::getModel);
    }

    public Optional<GradlePlugins> getPlugins() {
        return ofNullable(build.getModels())
                .map(BuildModels::getGradlePlugins)
                .map(BuildModelsGradlePlugins::getModel);
    }

    public Optional<List<GradleProject>> getProjects() {
        return ofNullable(build.getModels())
                .map(BuildModels::getGradleProjects)
                .map(BuildModelsGradleProjects::getModel);
    }

    public Optional<GradleResourceUsage> getResourceUsage() {
        return ofNullable(build.getModels())
                .map(BuildModels::getGradleResourceUsage)
                .map(BuildModelsGradleResourceUsage::getModel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GradleBuild that = (GradleBuild) o;
        return Objects.equals(build, that.build);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(build);
    }

    @Override
    public String toString() {
        return "GradleBuild{build=" + build + "}";
    }

}
