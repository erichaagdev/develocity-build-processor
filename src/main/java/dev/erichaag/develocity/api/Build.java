package dev.erichaag.develocity.api;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;

public sealed interface Build permits GradleBuild, MavenBuild, BazelBuild, SbtBuild {

    String getId();

    Instant getAvailableAt();

    String getBuildToolVersion();

    String getBuildAgentVersion();

    Instant getStartTime();

    Duration getDuration();

    String getProjectName();

    List<String> getRequestedWorkUnits();

    boolean hasFailed();

    String getUser();

    List<String> getTags();

    default boolean hasTag(String tag) {
        return getTags().contains(tag);
    }

    List<Value> getValues();

    default boolean hasValue(String key) {
        return getValues().stream().anyMatch(it -> it.name().equals(key));
    }

    default List<String> getValues(String key) {
        return getValues().stream()
                .filter(it -> it.name().equals(key))
                .map(Value::value)
                .toList();
    }

    default Optional<String> getValue(String key) {
        final var values = getValues(key);
        if (values.size() > 1) throw new IllegalStateException("Multiple values for key '" + key + "'");
        return values.stream().findFirst();
    }

    default Optional<String> getFirstValue(String key) {
        return getValues(key).stream().findFirst();
    }

    ApiBuild getBuild();

    Set<BuildModel> getAvailableBuildModels();

    default boolean containsAllRelevantBuildModelsFrom(Collection<BuildModel> requiredBuildModels) {
        return getAvailableBuildModels()
                .containsAll(requiredBuildModels.stream()
                        .filter(it -> it.isModelFor(this))
                        .collect(toUnmodifiableSet()));
    }

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
