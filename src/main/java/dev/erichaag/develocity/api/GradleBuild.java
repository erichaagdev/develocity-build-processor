package dev.erichaag.develocity.api;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static dev.erichaag.develocity.api.BuildModel.GRADLE_ATTRIBUTES;
import static dev.erichaag.develocity.api.BuildModel.GRADLE_BUILD_CACHE_PERFORMANCE;
import static dev.erichaag.develocity.api.BuildModel.GRADLE_DEPRECATIONS;
import static dev.erichaag.develocity.api.BuildModel.GRADLE_NETWORK_ACTIVITY;
import static dev.erichaag.develocity.api.BuildModel.GRADLE_PROJECTS;
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
        if (getAttributes().isPresent()) buildModels.add(GRADLE_ATTRIBUTES);
        if (getBuildCachePerformance().isPresent()) buildModels.add(GRADLE_BUILD_CACHE_PERFORMANCE);
        if (getDeprecations().isPresent()) buildModels.add(GRADLE_DEPRECATIONS);
        if (getNetworkActivity().isPresent()) buildModels.add(GRADLE_NETWORK_ACTIVITY);
        if (getProjects().isPresent()) buildModels.add(GRADLE_PROJECTS);
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

    public Optional<List<GradleProject>> getProjects() {
        return ofNullable(build.getModels())
                .map(BuildModels::getGradleProjects)
                .map(BuildModelsGradleProjects::getModel);
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
