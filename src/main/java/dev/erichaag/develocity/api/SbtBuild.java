package dev.erichaag.develocity.api;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static dev.erichaag.develocity.api.MethodNotSupportedException.methodNotSupportedForSbt;
import static java.util.Collections.emptySet;

public final class SbtBuild implements Build {

    private final ApiBuild build;

    SbtBuild(ApiBuild build) {
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
        throw methodNotSupportedForSbt("getStartTime()");
    }

    @Override
    public Duration getDuration() {
        throw methodNotSupportedForSbt("getDuration()");
    }

    @Override
    public String getProjectName() {
        throw methodNotSupportedForSbt("getProjectName()");
    }

    @Override
    public List<String> getRequestedWorkUnits() {
        throw methodNotSupportedForSbt("getRequestedWorkUnits()");
    }

    @Override
    public boolean hasFailed() {
        throw methodNotSupportedForSbt("hasFailed()");
    }

    @Override
    public String getUser() {
        throw methodNotSupportedForSbt("getUser()");
    }

    @Override
    public List<String> getTags() {
        throw methodNotSupportedForSbt("getTags()");
    }

    @Override
    public List<Value> getValues() {
        throw methodNotSupportedForSbt("getValues()");
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
        SbtBuild that = (SbtBuild) o;
        return Objects.equals(build, that.build);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(build);
    }

    @Override
    public String toString() {
        return "SbtBuild{build=" + build + "}";
    }

}
