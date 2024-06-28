package dev.erichaag.develocity.processing.event;

import java.time.Instant;

public record DiscoveryStartedEvent(Instant triggeredAt, Instant since) implements BuildProcessorEvent {

}
