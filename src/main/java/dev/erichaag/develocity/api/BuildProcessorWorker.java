package dev.erichaag.develocity.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.time.Instant.now;

class BuildProcessorWorker {

    private static final int maxDiscoveryBuildsPerRequest = 1_000;

    private final DevelocityClient develocity;
    private final BuildCache buildCache;
    private final int maxBuildsPerRequest;
    private final Instant since;
    private final String query;
    private final List<BuildProcessorListener> listeners;
    private final Set<BuildModel> requiredBuildModels;

    private String lastCachedBuildId;
    private String lastUncachedBuildId;
    private int uncached = 0;

    BuildProcessorWorker(
            DevelocityClient develocity,
            BuildCache buildCache,
            int maxBuildsPerRequest,
            Instant since,
            String query,
            List<BuildProcessorListener> listeners,
            Set<BuildModel> requiredBuildModels) {
        this.develocity = develocity;
        this.buildCache = buildCache;
        this.maxBuildsPerRequest = maxBuildsPerRequest;
        this.since = since;
        this.query = query;
        this.listeners = listeners;
        this.requiredBuildModels = requiredBuildModels;
    }

    public void process() {
        notifyListenersDiscoveryStarted();
        final var builds = discoverBuilds(query, since);
        notifyListenersDiscoveryFinished(builds);
        notifyListenersProcessingStarted();
        builds.forEach(this::process);
        if (uncached > 0) processUncachedBuilds();
        notifyListenersProcessingFinished();
    }

    private List<Build> discoverBuilds(String query, Instant since) {
        final var sinceMilli = since.toEpochMilli();
        final var builds = new ArrayList<Build>();
        while (true) {
            final var response = develocity.getBuilds(query, maxDiscoveryBuildsPerRequest, getLastId(builds));
            if (response.isEmpty()) return builds;
            if (response.getLast().getAvailableAt() < sinceMilli) {
                builds.addAll(response.stream().filter(it -> it.getAvailableAt() >= sinceMilli).toList());
                return builds;
            }
            builds.addAll(response);
        }
    }

    private void process(Build build) {
        final var cachedBuild = buildCache.load(build.getId(), requiredBuildModels);
        if (uncached == maxBuildsPerRequest || (cachedBuild.isPresent() && uncached > 0)) {
            processUncachedBuilds();
            lastCachedBuildId = lastUncachedBuildId;
            uncached = 0;
        }
        if (cachedBuild.isPresent()) {
            processCachedBuild(cachedBuild.get());
            lastCachedBuildId = build.getId();
        } else {
            lastUncachedBuildId = build.getId();
            uncached++;
        }
    }

    private void processCachedBuild(Build cachedBuild) {
        if (cachedBuild.getAvailableBuildModels().containsAll(requiredBuildModels)) {
            notifyListenersBuild(cachedBuild);
            notifyListenersCachedBuild(cachedBuild);
            return;
        }
        final var build = develocity.getBuild(cachedBuild.getId(), requiredBuildModels);
        buildCache.save(build);
        notifyListenersBuild(build);
        notifyListenersFetchedBuild(build);
    }

    private void processUncachedBuilds() {
        final var builds = develocity.getBuilds(query, uncached, lastCachedBuildId, requiredBuildModels);
        builds.forEach(build -> {
            buildCache.save(build);
            notifyListenersBuild(build);
            notifyListenersFetchedBuild(build);
        });
    }

    private void notifyListenersDiscoveryStarted() {
        final var event = new DiscoveryStartedEvent(now(), since);
        listeners.forEach(it -> it.onDiscoveryStarted(event));
    }

    private void notifyListenersDiscoveryFinished(List<Build> builds) {
        final var event = new DiscoveryFinishedEvent(now(), builds);
        listeners.forEach(it -> it.onDiscoveryFinished(event));
    }

    private void notifyListenersProcessingStarted() {
        final var event = new ProcessingStartedEvent(now());
        listeners.forEach(it -> it.onProcessingStarted(event));
    }

    private void notifyListenersProcessingFinished() {
        final var event = new ProcessingFinishedEvent(now());
        listeners.forEach(it -> it.onProcessingFinished(event));
    }

    private void notifyListenersBuild(Build build) {
        listeners.forEach(listener -> {
            listener.onBuild(build);
            switch (build) {
                case GradleBuild b -> listener.onGradleBuild(b);
                case MavenBuild b -> listener.onMavenBuild(b);
                case BazelBuild b -> listener.onBazelBuild(b);
                case SbtBuild b -> listener.onSbtBuild(b);
            }
        });
    }

    private void notifyListenersCachedBuild(Build build) {
        final var event = new CachedBuildEvent(now(), build);
        listeners.forEach(it -> it.onCachedBuild(event));
    }

    private void notifyListenersFetchedBuild(Build build) {
        final var event = new FetchedBuildEvent(now(), build);
        listeners.forEach(it -> it.onFetchedBuild(event));
    }

    private static String getLastId(List<Build> builds) {
        return builds.isEmpty() ? null : builds.getLast().getId();
    }

}
