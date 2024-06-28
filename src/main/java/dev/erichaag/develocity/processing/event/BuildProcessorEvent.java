package dev.erichaag.develocity.processing.event;

import java.time.Instant;

public sealed interface BuildProcessorEvent permits CachedBuildEvent, FetchedBuildEvent, DiscoveryStartedEvent, DiscoveryFinishedEvent, ProcessingStartedEvent, ProcessingFinishedEvent {

    Instant triggeredAt();

}
