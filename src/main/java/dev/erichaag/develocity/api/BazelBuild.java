package dev.erichaag.develocity.api;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static dev.erichaag.develocity.api.AttributesNotPresentException.attributesNotPresent;
import static dev.erichaag.develocity.api.BuildModel.BAZEL_ATTRIBUTES;
import static dev.erichaag.develocity.api.BuildModel.BAZEL_CRITICAL_PATH;
import static dev.erichaag.develocity.api.MethodNotSupportedException.methodNotSupportedForBazel;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toUnmodifiableSet;

public final class BazelBuild implements Build {

    private final ApiBuild build;

    BazelBuild(ApiBuild build) {
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
                .map(BazelAttributes::getBuildStartTime)
                .map(Instant::ofEpochMilli)
                .orElseThrow(attributesNotPresent("getStartTime()"));
    }

    @Override
    public Duration getDuration() {
        return getAttributes()
                .map(BazelAttributes::getBuildDuration)
                .map(Duration::ofMillis)
                .orElseThrow(attributesNotPresent("getDuration()"));
    }

    @Override
    public String getProjectName() {
        throw methodNotSupportedForBazel("getProjectName()");
    }

    @Override
    public List<String> getRequestedWorkUnits() {
        return getAttributes()
                .map(BazelAttributes::getTargetPatterns)
                .orElseThrow(attributesNotPresent("getRequestedWorkUnits()"));
    }

    @Override
    public boolean hasFailed() {
        throw methodNotSupportedForBazel("hasFailed()");
    }

    @Override
    public String getUser() {
        return getAttributes()
                .map(BazelAttributes::getUser)
                .orElseThrow(attributesNotPresent("getUser()"));
    }

    @Override
    public List<String> getTags() {
        return getAttributes()
                .map(BazelAttributes::getTags)
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
        if (getAttributes().isPresent()) buildModels.add(BAZEL_ATTRIBUTES);
        if (getCriticalPath().isPresent()) buildModels.add(BAZEL_CRITICAL_PATH);
        return buildModels.build().collect(toUnmodifiableSet());
    }

    @Override
    public ApiBuild getBuild() {
        return build;
    }

    public Optional<BazelAttributes> getAttributes() {
        return ofNullable(build.getModels())
                .map(BuildModels::getBazelAttributes)
                .map(BuildModelsBazelAttributes::getModel);
    }

    public Optional<BazelCriticalPath> getCriticalPath() {
        return ofNullable(build.getModels())
                .map(BuildModels::getBazelCriticalPath)
                .map(BuildModelsBazelCriticalPath::getModel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BazelBuild that = (BazelBuild) o;
        return Objects.equals(build, that.build);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(build);
    }

    @Override
    public String toString() {
        return "BazelBuild{build=" + build + "}";
    }

}
