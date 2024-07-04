package dev.erichaag.develocity.api;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;

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
    public ApiBuild getBuild() {
        return build;
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

    public Optional<List<GradleDeprecationEntry>> getDeprecations() {
        return ofNullable(build.getModels())
                .map(BuildModels::getGradleDeprecations)
                .map(BuildModelsGradleDeprecations::getModel)
                .map(GradleDeprecations::getDeprecations);
    }

    public Optional<List<GradleArtifactTransformExecutionEntry>> getArtifactTransformExecutions() {
        return ofNullable(build.getModels())
                .map(BuildModels::getGradleArtifactTransformExecutions)
                .map(BuildModelsGradleArtifactTransformExecutions::getModel)
                .map(GradleArtifactTransformExecutions::getArtifactTransformExecutions);
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

