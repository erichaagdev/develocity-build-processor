package dev.erichaag.develocity.processing;

import dev.erichaag.develocity.api.BazelBuild;
import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.BuildModel;
import dev.erichaag.develocity.api.DevelocityClient;
import dev.erichaag.develocity.api.GradleBuild;
import dev.erichaag.develocity.api.MavenBuild;
import dev.erichaag.develocity.api.SbtBuild;
import dev.erichaag.develocity.processing.cache.ProcessorCache;
import dev.erichaag.develocity.processing.event.CachedBuildEvent;
import dev.erichaag.develocity.processing.event.DiscoveryFinishedEvent;
import dev.erichaag.develocity.processing.event.DiscoveryStartedEvent;
import dev.erichaag.develocity.processing.event.FetchedBuildEvent;
import dev.erichaag.develocity.processing.event.ProcessingFinishedEvent;
import dev.erichaag.develocity.processing.event.ProcessingStartedEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.time.Instant.now;

class BuildProcessorWorker {

    private static final int maxDiscoveryBuildsPerRequest = 1_000;

    private final DevelocityClient develocity;
    private final ProcessorCache processorCache;
    private final int maxBuildsPerRequest;
    private final Instant since;
    private final String query;
    private final List<BuildListener> buildListeners;
    private final List<ProcessListener> processListeners;
    private final Set<BuildModel> requiredBuildModels;

    private String lastCachedBuildId;
    private String lastUncachedBuildId;
    private int uncached = 0;

    BuildProcessorWorker(
            DevelocityClient develocity,
            ProcessorCache processorCache,
            int maxBuildsPerRequest,
            Instant since,
            String query,
            List<BuildListener> buildListeners,
            List<ProcessListener> processListeners,
            Set<BuildModel> requiredBuildModels) {
        this.develocity = develocity;
        this.processorCache = processorCache;
        this.maxBuildsPerRequest = maxBuildsPerRequest;
        this.since = since;
        this.query = query;
        this.buildListeners = buildListeners;
        this.processListeners = processListeners;
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
        final var cachedBuild = processorCache.load(build.getId(), requiredBuildModels);
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
            notifyListenersCachedBuild(cachedBuild);
            return;
        }
        final var build = develocity.getBuild(cachedBuild.getId(), requiredBuildModels);
        processorCache.save(build);
        notifyListenersFetchedBuild(build);
    }

    private void processUncachedBuilds() {
        final var builds = develocity.getBuilds(query, uncached, lastCachedBuildId, requiredBuildModels);
        builds.forEach(build -> {
            processorCache.save(build);
            notifyListenersFetchedBuild(build);
        });
    }

    private void notifyListenersDiscoveryStarted() {
        final var event = new DiscoveryStartedEvent(now(), since);
        processListeners.forEach(it -> it.onDiscoveryStarted(event));
    }

    private void notifyListenersDiscoveryFinished(List<Build> builds) {
        final var event = new DiscoveryFinishedEvent(now(), builds);
        processListeners.forEach(it -> it.onDiscoveryFinished(event));
    }

    private void notifyListenersProcessingStarted() {
        final var event = new ProcessingStartedEvent(now());
        processListeners.forEach(it -> it.onProcessingStarted(event));
    }

    private void notifyListenersProcessingFinished() {
        final var event = new ProcessingFinishedEvent(now());
        processListeners.forEach(it -> it.onProcessingFinished(event));
    }

    private void notifyListenersCachedBuild(Build build) {
        final var event = new CachedBuildEvent(now(), build);
        processListeners.forEach(it -> it.onCachedBuild(event));
        notifyListenersBuild(build);
    }

    private void notifyListenersFetchedBuild(Build build) {
        final var event = new FetchedBuildEvent(now(), build);
        processListeners.forEach(it -> it.onFetchedBuild(event));
        notifyListenersBuild(build);
    }

    private void notifyListenersBuild(Build build) {
        buildListeners.forEach(listener -> {
            listener.onBuild(build);
            switch (build) {
                case GradleBuild b -> listener.onGradleBuild(b);
                case MavenBuild b -> listener.onMavenBuild(b);
                case BazelBuild b -> listener.onBazelBuild(b);
                case SbtBuild b -> listener.onSbtBuild(b);
            }
        });
    }

    private static String getLastId(List<Build> builds) {
        return builds.isEmpty() ? null : builds.getLast().getId();
    }

}
