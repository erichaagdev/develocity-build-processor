package dev.erichaag.develocity.processing.event;

import java.time.Instant;

public record ProcessingStartedEvent(Instant triggeredAt) implements BuildProcessorEvent {

}
