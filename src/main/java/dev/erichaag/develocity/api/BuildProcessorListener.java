package dev.erichaag.develocity.api;

import java.util.Set;

import static java.util.Collections.emptySet;

public interface BuildProcessorListener {

    static BuildProcessorListenerBuilder builder() {
        return new BuildProcessorListenerBuilder();
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

    default void onCachedBuild(CachedBuildEvent event) {
    }

    default void onFetchedBuild(FetchedBuildEvent event) {
    }

    default void onDiscoveryStarted(DiscoveryStartedEvent event) {
    }

    default void onDiscoveryFinished(DiscoveryFinishedEvent event) {
    }

    default void onProcessingStarted(ProcessingStartedEvent event) {
    }

    default void onProcessingFinished(ProcessingFinishedEvent event) {
    }

}
