package dev.erichaag.develocity.processing;

import dev.erichaag.develocity.api.BazelBuild;
import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.BuildModel;
import dev.erichaag.develocity.api.GradleBuild;
import dev.erichaag.develocity.api.MavenBuild;
import dev.erichaag.develocity.api.SbtBuild;

import java.util.Set;

import static java.util.Collections.emptySet;

public interface BuildListener {

    static BuildListenerBuilder builder() {
        return new BuildListenerBuilder();
    }

    default Set<BuildModel> getRequiredBuildModels() {
        return emptySet();
    }

    default void onBuild(Build build) {
    }

    default void onGradleBuild(GradleBuild build) {
    }

    default void onMavenBuild(MavenBuild build) {
    }

    default void onBazelBuild(BazelBuild build) {
    }

    default void onSbtBuild(SbtBuild build) {
    }

}
