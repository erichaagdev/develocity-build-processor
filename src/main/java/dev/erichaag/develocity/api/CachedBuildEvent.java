package dev.erichaag.develocity.api;

import java.time.Instant;

public record CachedBuildEvent(Instant triggeredAt, Build build) implements BuildProcessorEvent {

}
