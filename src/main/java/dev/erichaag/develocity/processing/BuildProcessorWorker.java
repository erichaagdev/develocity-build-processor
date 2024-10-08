package dev.erichaag.develocity.processing;

import dev.erichaag.develocity.api.BazelBuild;
import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.BuildModel;
import dev.erichaag.develocity.api.DevelocityClient;
import dev.erichaag.develocity.api.DevelocityClientException;
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
import java.util.function.Supplier;

import static java.lang.Integer.min;
import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

class BuildProcessorWorker {

    private static final int maxDiscoveryBuildsPerRequest = 1_000;

    private final DevelocityClient develocity;
    private final ProcessorCache processorCache;
    private final int maxBuildsPerRequest;
    private final int backOffLimit;
    private final double backOffFactor;
    private final int retryLimit;
    private final double retryFactor;
    private final Instant since;
    private final String query;
    private final List<BuildListener> buildListeners;
    private final List<ProcessListener> processListeners;
    private final Set<BuildModel> requiredBuildModels;

    private String lastCachedBuildId;
    private int uncached = 0;
    private int backOff = 0;

    BuildProcessorWorker(
            DevelocityClient develocity,
            ProcessorCache processorCache,
            int maxBuildsPerRequest,
            int backOffLimit,
            double backOffFactor,
            int retryLimit,
            double retryFactor,
            Instant since,
            String query,
            List<BuildListener> buildListeners,
            List<ProcessListener> processListeners,
            Set<BuildModel> requiredBuildModels) {
        this.develocity = develocity;
        this.processorCache = processorCache;
        this.maxBuildsPerRequest = maxBuildsPerRequest;
        this.backOffFactor = backOffFactor;
        this.backOffLimit = backOffLimit;
        this.retryLimit = retryLimit;
        this.retryFactor = retryFactor;
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
        final var builds = new ArrayList<Build>();
        while (true) {
            final var response = develocity.getBuilds(query, maxDiscoveryBuildsPerRequest, getLastId(builds));
            if (response.isEmpty()) return builds;
            if (response.getLast().getAvailableAt().compareTo(since) < 0) {
                builds.addAll(response.stream().filter(it -> it.getAvailableAt().compareTo(since) >= 0).toList());
                return builds;
            }
            builds.addAll(response);
        }
    }

    private void process(Build build) {
        if (requiredBuildModels.isEmpty()) {
            notifyListenersFetchedBuild(build);
            return;
        }
        final var cachedBuild = processorCache.load(build.getId(), requiredBuildModels);
        if (uncached == currentMaxBuildsPerRequest() || (cachedBuild.isPresent() && uncached > 0)) {
            processUncachedBuilds();
        }
        if (cachedBuild.isPresent()) {
            processCachedBuild(cachedBuild.get());
            lastCachedBuildId = build.getId();
        } else {
            uncached++;
        }
    }

    private void processCachedBuild(Build cachedBuild) {
        if (cachedBuild.containsAllRelevantBuildModelsFrom(requiredBuildModels)) {
            notifyListenersCachedBuild(cachedBuild);
            return;
        }
        // The build was discovered so it must exist
        //noinspection OptionalGetWithoutIsPresent
        final var build = develocity.getBuild(cachedBuild.getId(), requiredBuildModels).get();
        saveToProcessorCache(build);
        notifyListenersFetchedBuild(build);
    }

    private void processUncachedBuilds() {
        while (uncached > 0) {
            final var builds = withRetryAndBackOff(() -> develocity.getBuilds(query, min(currentMaxBuildsPerRequest(), uncached), lastCachedBuildId, requiredBuildModels));
            uncached -= builds.size();
            builds.forEach(it -> {
                saveToProcessorCache(it);
                notifyListenersFetchedBuild(it);
            });
        }
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

    private void saveToProcessorCache(Build build) {
        processorCache.save(build);
        lastCachedBuildId = build.getId();
    }

    private int currentMaxBuildsPerRequest() {
        return max(1, (int) (maxBuildsPerRequest * pow(backOffFactor, backOff)));
    }

    private List<Build> withRetryAndBackOff(Supplier<List<Build>> getBuilds) {
        final var exceptions = new ArrayList<RuntimeException>();
        do {
            try {
                return getBuilds.get();
            } catch (RuntimeException e) {
                if (e instanceof DevelocityClientException dce) {
                    if (dce.getStatusCode() == 429 || dce.getStatusCode() == 503) {
                        if (exceptions.size() < retryLimit) sleep((int) (1_000 * pow(retryFactor, exceptions.size())));
                        exceptions.add(e);
                    } else if (dce.getStatusCode() == 504) {
                        if (++backOff > backOffLimit) throw new BackOffLimitExceededException(backOffLimit);
                        return emptyList();
                    } else {
                        throw e;
                    }
                } else {
                    throw e;
                }
            }
        } while (exceptions.size() <= retryLimit);
        throw new RetryLimitExceededException(retryLimit, exceptions.getLast());
    }

    private static String getLastId(List<Build> builds) {
        return builds.isEmpty() ? null : builds.getLast().getId();
    }

    private static void sleep(int milliseconds) {
        try {
            MILLISECONDS.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
