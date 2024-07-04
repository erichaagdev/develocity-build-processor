package dev.erichaag.develocity.api;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static dev.erichaag.develocity.api.MethodNotSupportedException.methodNotSupportedForBazel;
import static java.util.Collections.emptySet;

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
        throw methodNotSupportedForBazel("getStartTime()");
    }

    @Override
    public Duration getDuration() {
        throw methodNotSupportedForBazel("getDuration()");
    }

    @Override
    public String getProjectName() {
        throw methodNotSupportedForBazel("getProjectName()");
    }

    @Override
    public List<String> getRequestedWorkUnits() {
        throw methodNotSupportedForBazel("getRequestedWorkUnits()");
    }

    @Override
    public boolean hasFailed() {
        throw methodNotSupportedForBazel("hasFailed()");
    }

    @Override
    public String getUser() {
        throw methodNotSupportedForBazel("getUser()");
    }

    @Override
    public List<String> getTags() {
        throw methodNotSupportedForBazel("getTags()");
    }

    @Override
    public List<Value> getValues() {
        throw methodNotSupportedForBazel("getValues()");
    }

    @Override
    public Set<BuildModel> getAvailableBuildModels() {
        return emptySet();
    }

    @Override
    public ApiBuild getBuild() {
        return build;
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
