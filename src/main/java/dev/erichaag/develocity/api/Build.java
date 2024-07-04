package dev.erichaag.develocity.api;

import java.util.Set;

public sealed interface Build permits GradleBuild, MavenBuild, BazelBuild, SbtBuild {

    String getId();

    long getAvailableAt();

    String getBuildToolVersion();

    String getBuildAgentVersion();

    Set<BuildModel> getAvailableBuildModels();

    ApiBuild getBuild();

    static Build from(ApiBuild build) {
        return switch (build.getBuildToolType()) {
            case "gradle" -> new GradleBuild(build);
            case "maven" -> new MavenBuild(build);
            case "bazel" -> new BazelBuild(build);
            case "sbt" -> new SbtBuild(build);
            default -> throw new IllegalArgumentException("Unknown build tool: " + build.getBuildToolType());
        };
    }

}
