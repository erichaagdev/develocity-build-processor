package dev.erichaag.develocity.api;

import java.time.Instant;

public record FetchedBuildEvent(Instant triggeredAt, Build build) implements BuildProcessorEvent {

}
