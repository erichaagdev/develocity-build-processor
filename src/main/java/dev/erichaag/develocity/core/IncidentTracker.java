package dev.erichaag.develocity.core;

import dev.erichaag.develocity.api.BuildAttributesValue;
import dev.erichaag.develocity.api.BuildModel;
import dev.erichaag.develocity.api.GradleBuild;
import dev.erichaag.develocity.api.MavenBuild;
import dev.erichaag.develocity.processing.BuildListener;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static dev.erichaag.develocity.api.BuildModel.GRADLE_ATTRIBUTES;
import static dev.erichaag.develocity.api.BuildModel.MAVEN_ATTRIBUTES;
import static java.lang.String.join;
import static java.time.Duration.ofMillis;
import static java.time.Instant.ofEpochMilli;
import static java.util.List.copyOf;

public final class IncidentTracker implements BuildListener {

    private final Map<String, Incident> unresolvedIncidents = new HashMap<>();
    private final Set<BuildView> buildViews = new TreeSet<>();

    private List<Incident> resolvedIncidents;

    List<Incident> getResolvedIncidents() {
        if (resolvedIncidents == null) {
            resolvedIncidents = new ArrayList<>();
            buildViews.forEach(this::processBuild);
        }
        return copyOf(resolvedIncidents);
    }

    @Override
    public Set<BuildModel> getRequiredBuildModels() {
        return Set.of(GRADLE_ATTRIBUTES, MAVEN_ATTRIBUTES);
    }

    @Override
    public void onGradleBuild(GradleBuild build) {
        build.getAttributes().ifPresent(attributes ->
                buildViews.add(new BuildView(
                    attributes.getEnvironment().getUsername(),
                    attributes.getRootProjectName(),
                    attributes.getRequestedTasks(),
                    attributes.getTags(),
                    attributes.getValues(),
                    attributes.getHasFailed(),
                    ofEpochMilli(attributes.getBuildStartTime()),
                    ofMillis(attributes.getBuildDuration()))));
    }

    @Override
    public void onMavenBuild(MavenBuild build) {
        build.getAttributes().ifPresent(attributes ->
                buildViews.add(new BuildView(
                        attributes.getEnvironment().getUsername(),
                        attributes.getTopLevelProjectName(),
                        attributes.getRequestedGoals(),
                        attributes.getTags(),
                        attributes.getValues(),
                        attributes.getHasFailed(),
                        ofEpochMilli(attributes.getBuildStartTime()),
                        ofMillis(attributes.getBuildDuration()))));
    }

    void processBuild(BuildView buildView) {
        processBuild(
                buildView.username(),
                buildView.projectName(),
                buildView.requested(),
                buildView.tags(),
                buildView.values(),
                buildView.hasFailed(),
                buildView.buildStartTime(),
                buildView.buildDuration()
        );
    }

    private void processBuild(
            String username,
            String projectName,
            Collection<String> requested,
            Collection<String> tags,
            Collection<BuildAttributesValue> values,
            boolean isFailure,
            Instant buildStartTime,
            Duration buildDuration) {
        final var isCI = hasTag("CI", tags);
        final var isLocal = hasTag("LOCAL", tags);
        final var isIdeSync = hasTag("IDE sync", tags);
        final var gitBranch = findValue("Git branch", values);
        final var buildValidationScripts = findValue("Build validation scripts", values);
        if (buildValidationScripts.isEmpty() && (isCI || isLocal) && gitBranch.isPresent()) {
            handleIncident(username, projectName, requested, isFailure, buildStartTime, buildDuration, isCI, isIdeSync, gitBranch.get());
        }
    }

    private void handleIncident(
            String username,
            String projectName,
            Collection<String> requested,
            boolean isFailure,
            Instant buildStartTime,
            Duration buildDuration,
            boolean isCI,
            boolean isIdeSync,
            String gitBranch) {
        final var incidentName = isCI ? buildCiIncidentName(projectName, requested, gitBranch) : buildLocalIncidentName(username, projectName, requested, isIdeSync);
        if (isFailure) {
            if (!hasUnresolvedIncident(incidentName)) {
                trackIncident(incidentName, username, projectName, requested, buildStartTime.plus(buildDuration), isCI);
            }
        } else if (hasUnresolvedIncident(incidentName)) {
            markIncidentResolved(incidentName, buildStartTime);
        }
    }

    private boolean hasUnresolvedIncident(String incidentName) {
        return unresolvedIncidents.containsKey(incidentName);
    }

    private void trackIncident(String incidentName, String username, String projectName, Collection<String> requested, Instant startedOn, boolean isCI) {
        unresolvedIncidents.put(incidentName, new Incident(incidentName, username, projectName, requested, startedOn, null, isCI));
    }

    private void markIncidentResolved(String incidentName, Instant resolvedOn) {
        final var incident = unresolvedIncidents.remove(incidentName);
        resolvedIncidents.add(new Incident(incidentName, incident.username(), incident.projectName(), incident.requested(), incident.startedOn(), resolvedOn, incident.isCI()));
    }

    private static String buildCiIncidentName(String projectName, Collection<String> requested, String gitBranch) {
        return "ci," + projectName + "," + join(" ", requested) + "," + gitBranch;
    }

    private static String buildLocalIncidentName(String username, String projectName, Collection<String> requested, boolean isIdeSync) {
        return "local," + username + "," + projectName + "," + join(" ", requested) + ",ide_sync=" + isIdeSync;
    }

    private static boolean hasTag(String name, Collection<String> tags) {
        return tags.stream().anyMatch(it -> it.equalsIgnoreCase(name));
    }

    private static Optional<String> findValue(String name, Collection<BuildAttributesValue> values) {
        return values.stream().filter(it -> it.getName().equalsIgnoreCase(name)).findFirst().map(BuildAttributesValue::getValue);
    }

    record BuildView(
            String username,
            String projectName,
            List<String> requested,
            List<String> tags,
            List<BuildAttributesValue> values,
            boolean hasFailed,
            Instant buildStartTime,
            Duration buildDuration) implements Comparable<BuildView> {

        @Override
        public int compareTo(BuildView o) {
            return buildStartTime.compareTo(o.buildStartTime);
        }
    }

}
