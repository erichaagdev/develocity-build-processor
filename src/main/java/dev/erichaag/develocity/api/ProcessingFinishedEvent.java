package dev.erichaag.develocity.api;

import java.time.Instant;

public record ProcessingFinishedEvent(Instant triggeredAt) implements BuildProcessorEvent {

}
