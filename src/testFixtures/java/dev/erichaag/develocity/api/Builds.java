package dev.erichaag.develocity.api;

import java.util.List;
import java.util.function.UnaryOperator;

import static java.util.Objects.requireNonNull;
import static java.util.function.UnaryOperator.identity;

public final class Builds {

    public static final String buildToolTypeGradle = "gradle";
    public static final String buildToolTypeMaven = "maven";
    public static final String buildToolTypeBazel = "bazel";
    public static final String buildToolTypeSbt = "sbt";

    public static GradleBuild gradle(String id) {
        return gradle(id, identity());
    }

    public static GradleBuild gradle(String id, UnaryOperator<ApiBuild> modify) {
        requireNonNull(id, "Build must have an ID");
        return (GradleBuild) Build.from(modify.apply(gradleApiBuild(id)));
    }

    public static GradleBuild gradle(String id, Object... buildModels) {
        return gradle(id, identity(), buildModels);
    }

    @SuppressWarnings("unchecked")
    public static GradleBuild gradle(String id, UnaryOperator<ApiBuild> modify, Object... buildModels) {
        requireNonNull(id, "Build must have an ID");
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
        return (GradleBuild) Build.from(modify.apply(gradleApiBuild(id)).models(models));
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

    public static List<GradleProject> gradleProjects() {
        return List.of(new GradleProject().name("develocity-build-processor"));
    }

    public static MavenBuild maven(String id) {
        return maven(id, identity());
    }

    public static MavenBuild maven(String id, UnaryOperator<ApiBuild> modify) {
        requireNonNull(id, "Build must have an ID");
        return (MavenBuild) Build.from(modify.apply(mavenApiBuild(id)));
    }

    public static BazelBuild bazel(String id) {
        return bazel(id, identity());
    }

    public static BazelBuild bazel(String id, UnaryOperator<ApiBuild> modify) {
        requireNonNull(id, "Build must have an ID");
        return (BazelBuild) Build.from(modify.apply(bazelApiBuild(id)));
    }

    public static SbtBuild sbt(String id) {
        return sbt(id, identity());
    }

    public static SbtBuild sbt(String id, UnaryOperator<ApiBuild> modify) {
        requireNonNull(id, "Build must have an ID");
        return (SbtBuild) Build.from(modify.apply(sbtApiBuild(id)));
    }

    private static ApiBuild gradleApiBuild(String id) {
        return new ApiBuild()
                .id(id)
                .availableAt(100L)
                .buildAgentVersion("3.17.5")
                .buildToolType(buildToolTypeGradle)
                .buildToolVersion("8.6");
    }

    private static ApiBuild mavenApiBuild(String id) {
        return new ApiBuild()
                .id(id)
                .availableAt(100L)
                .buildAgentVersion("1.21.4")
                .buildToolType(buildToolTypeMaven)
                .buildToolVersion("3.9.8");
    }

    private static ApiBuild bazelApiBuild(String id) {
        return new ApiBuild()
                .id(id)
                .availableAt(100L)
                .buildAgentVersion("1.2")
                .buildToolType(buildToolTypeBazel)
                .buildToolVersion("7.2.1");
    }

    private static ApiBuild sbtApiBuild(String id) {
        return new ApiBuild()
                .id(id)
                .availableAt(100L)
                .buildAgentVersion("1.0.1")
                .buildToolType(buildToolTypeSbt)
                .buildToolVersion("1.10.0");
    }

}
