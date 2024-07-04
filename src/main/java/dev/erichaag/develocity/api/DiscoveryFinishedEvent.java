package dev.erichaag.develocity.api;

import java.time.Instant;
import java.util.List;

public record DiscoveryFinishedEvent(Instant triggeredAt, List<Build> builds) implements BuildProcessorEvent {

}
