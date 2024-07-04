package dev.erichaag.develocity.api;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;

public interface BuildProcessorListener {

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

    record CachedBuildEvent(Instant triggeredAt, Build build) implements BuildProcessorEvent {
    }

    record FetchedBuildEvent(Instant triggeredAt, Build build) implements BuildProcessorEvent {
    }

    record DiscoveryStartedEvent(Instant triggeredAt, ZonedDateTime since) implements BuildProcessorEvent {
    }

    record DiscoveryFinishedEvent(Instant triggeredAt, List<Build> builds) implements BuildProcessorEvent {
    }

    record ProcessingStartedEvent(Instant triggeredAt) implements BuildProcessorEvent {
    }

    record ProcessingFinishedEvent(Instant triggeredAt) implements BuildProcessorEvent {
    }

    sealed interface BuildProcessorEvent permits
            CachedBuildEvent,
            FetchedBuildEvent,
            DiscoveryStartedEvent,
            DiscoveryFinishedEvent,
            ProcessingStartedEvent,
            ProcessingFinishedEvent {
        Instant triggeredAt();
    }

}
