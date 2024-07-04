package dev.erichaag.develocity.api;

import java.time.Instant;

public sealed interface BuildProcessorEvent permits CachedBuildEvent, FetchedBuildEvent, DiscoveryStartedEvent, DiscoveryFinishedEvent, ProcessingStartedEvent, ProcessingFinishedEvent {

    Instant triggeredAt();

}
