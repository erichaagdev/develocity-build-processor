package dev.erichaag.develocity.api;

import java.time.Instant;

public record ProcessingStartedEvent(Instant triggeredAt) implements BuildProcessorEvent {

}
