package dev.erichaag.develocity.processing.event;

import dev.erichaag.develocity.api.Build;

import java.time.Instant;

public record CachedBuildEvent(Instant triggeredAt, Build build) implements BuildProcessorEvent {

}
