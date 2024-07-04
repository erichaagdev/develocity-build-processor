package dev.erichaag.develocity.api;

import java.util.List;

final class Builds {

    public static GradleBuild gradleBuild(String id) {
        return (GradleBuild) Build.from(gradleApiBuild(id));
    }

    public static GradleAttributes gradleAttributes() {
        return new GradleAttributes()
                .buildDuration(100L)
                .buildStartTime(0L)
                .gradleVersion("8.6")
                .hasFailed(false)
                .pluginVersion("3.17.5")
                .requestedTasks(List.of("build"))
                .rootProjectName("develocity-build-processor")
                .tags(List.of("CI", "Linux", "main"));
    }

    @SuppressWarnings("unchecked")
    public static GradleBuild gradleBuildWith(String id, Object... buildModels) {
        final var models = new BuildModels();
        for (final var buildModel : buildModels) {
            switch (buildModel) {
                case GradleArtifactTransformExecutions m -> models.setGradleArtifactTransformExecutions(new BuildModelsGradleArtifactTransformExecutions().model(m));
                case GradleAttributes m -> models.setGradleAttributes(new BuildModelsGradleAttributes().model(m.id(id)));
                case GradleBuildCachePerformance m -> models.setGradleBuildCachePerformance(new BuildModelsGradleBuildCachePerformance().model(m));
                case GradleDeprecations m -> models.setGradleDeprecations(new BuildModelsGradleDeprecations().model(m));
                case GradleNetworkActivity m -> models.setGradleNetworkActivity(new BuildModelsGradleNetworkActivity().model(m));
                case List<?> m -> models.setGradleProjects(new BuildModelsGradleProjects().model((List<GradleProject>) m));
                default -> throw new IllegalArgumentException("Unexpected object: " + buildModel);
            }
        }
        return (GradleBuild) Build.from(gradleApiBuild(id).models(models));
    }

    private static ApiBuild gradleApiBuild(String id) {
        return new ApiBuild()
                .id(id)
                .availableAt(100L)
                .buildAgentVersion("3.17.5")
                .buildToolType("gradle")
                .buildToolVersion("8.6");
    }

}
