package dev.erichaag.develocity.api;

import java.time.Instant;

public record DiscoveryStartedEvent(Instant triggeredAt, Instant since) implements BuildProcessorEvent {

}
