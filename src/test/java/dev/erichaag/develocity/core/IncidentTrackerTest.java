package dev.erichaag.develocity.core;

import dev.erichaag.develocity.api.BuildAttributesValue;
import dev.erichaag.develocity.core.IncidentTracker.BuildView;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class IncidentTrackerTest {

    private final IncidentTracker incidentTracker = new IncidentTracker();
    private final Set<BuildView> buildViews = new TreeSet<>();
    private final List<Incident> incidents = new ArrayList<>();

    @Test
    void givenSingleFailure_thenSingleIncident() {
        successfulBuild(3000);
        failedBuild(2000);
        successfulBuild(1000);

        runScenario();

        assertEquals(1, incidentCount());
        assertEquals(2100, firstIncident().startedOn().getEpochSecond());
        assertEquals(3000, firstIncident().resolvedOn().getEpochSecond());
    }

    @Test
    void givenConsecutiveFailures_thenSingleIncident() {
        successfulBuild(4000);
        failedBuild(3000);
        failedBuild(2000);
        successfulBuild(1000);

        runScenario();

        assertEquals(1, incidentCount());
        assertEquals(2100, firstIncident().startedOn().getEpochSecond());
        assertEquals(4000, firstIncident().resolvedOn().getEpochSecond());
    }

    @Test
    void givenMultipleFailures_thenMultipleIncidents() {
        successfulBuild(6000);
        failedBuild(5000);
        failedBuild(4000);
        successfulBuild(3000);
        failedBuild(2000);
        successfulBuild(1000);

        runScenario();

        assertEquals(2, incidentCount());
        assertEquals(2100, firstIncident().startedOn().getEpochSecond());
        assertEquals(3000, firstIncident().resolvedOn().getEpochSecond());
        assertEquals(4100, secondIncident().startedOn().getEpochSecond());
        assertEquals(6000, secondIncident().resolvedOn().getEpochSecond());
    }

    private void failedBuild(int buildStartTime) {
        buildViews.add(new BuildView(
                "user",
                "myProject",
                List.of("build"),
                List.of("CI"),
                List.of(new BuildAttributesValue().name("Git branch").value("feature")),
                true,
                Instant.ofEpochSecond(buildStartTime),
                Duration.ofSeconds(100)));
    }

    private void successfulBuild(int buildStartTime) {
        buildViews.add(new BuildView(
                "user",
                "myProject",
                List.of("build"),
                List.of("CI"),
                List.of(new BuildAttributesValue().name("Git branch").value("feature")),
                false,
                Instant.ofEpochSecond(buildStartTime),
                Duration.ofSeconds(100)));
    }

    private void runScenario() {
        buildViews.forEach(incidentTracker::processBuild);
        incidents.addAll(incidentTracker.getResolvedIncidents());
    }

    private int incidentCount() {
        return incidents.size();
    }

    private Incident firstIncident() {
        //noinspection SequencedCollectionMethodCanBeUsed
        return incidents.get(0);
    }

    private Incident secondIncident() {
        return incidents.get(1);
    }

}
