package dev.erichaag.develocity.processing;

import dev.erichaag.develocity.api.BazelBuild;
import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.GradleBuild;
import dev.erichaag.develocity.api.MavenBuild;
import dev.erichaag.develocity.api.SbtBuild;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class TrackingBuildListener implements BuildListener {

    private final List<Build> encounteredBuilds = new ArrayList<>();
    private final AtomicInteger gradleBuildsEncountered = new AtomicInteger();
    private final AtomicInteger mavenBuildsEncountered = new AtomicInteger();
    private final AtomicInteger bazelBuildsEncountered = new AtomicInteger();
    private final AtomicInteger sbtBuildsEncountered = new AtomicInteger();

    @Override
    public void onBuild(Build build) {
        encounteredBuilds.add(build);
    }

    @Override
    public void onGradleBuild(GradleBuild build) {
        gradleBuildsEncountered.incrementAndGet();
    }

    @Override
    public void onMavenBuild(MavenBuild build) {
        mavenBuildsEncountered.incrementAndGet();
    }

    @Override
    public void onBazelBuild(BazelBuild build) {
        bazelBuildsEncountered.incrementAndGet();
    }

    @Override
    public void onSbtBuild(SbtBuild build) {
        sbtBuildsEncountered.incrementAndGet();
    }

    public List<Build> encounteredBuilds() {
        return encounteredBuilds;
    }

    public int overallBuildsEncountered() {
        return encounteredBuilds.size();
    }

    public int gradleBuildsEncountered() {
        return gradleBuildsEncountered.get();
    }

    public int mavenBuildsEncountered() {
        return mavenBuildsEncountered.get();
    }

    public int bazelBuildsEncountered() {
        return bazelBuildsEncountered.get();
    }

    public int sbtBuildsEncountered() {
        return sbtBuildsEncountered.get();
    }

}
