package dev.erichaag.develocity.api;

import java.time.Instant;
import java.time.ZonedDateTime;

public record DiscoveryStartedEvent(Instant triggeredAt, ZonedDateTime since) implements BuildProcessorEvent {

}
