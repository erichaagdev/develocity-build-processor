package dev.erichaag.develocity.processing.event;

import java.time.Instant;

public record ProcessingFinishedEvent(Instant triggeredAt) implements BuildProcessorEvent {

}
