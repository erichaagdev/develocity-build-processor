package dev.erichaag.develocity.processing.event;

import dev.erichaag.develocity.api.Build;

import java.time.Instant;
import java.util.List;

public record DiscoveryFinishedEvent(Instant triggeredAt, List<Build> builds) implements BuildProcessorEvent {

}
