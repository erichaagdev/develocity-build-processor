package dev.erichaag.develocity.core;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

record Incident(
        String key,
        String username,
        String projectName,
        Collection<String> requested,
        Instant startedOn,
        Instant resolvedOn,
        boolean isCI) {

    public Duration duration() {
        return Duration.between(startedOn, resolvedOn);
    }

}
