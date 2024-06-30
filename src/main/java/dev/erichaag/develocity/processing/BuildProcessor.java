package dev.erichaag.develocity.processing;

import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.BuildModel;
import dev.erichaag.develocity.api.DevelocityClient;
import dev.erichaag.develocity.processing.cache.ProcessorCache;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toUnmodifiableSet;

public final class BuildProcessor {

    private static final int defaultMaxBuildsPerRequest = 100;

    private final DevelocityClient develocity;
    private final ProcessorCache processorCache;
    private final int maxBuildsPerRequest;
    private final List<BuildListener> buildListeners;
    private final List<ProcessListener> processListeners;
    private final Set<BuildModel> requiredBuildModels;

    BuildProcessor(
            DevelocityClient develocity,
            ProcessorCache processorCache,
            Integer maxBuildsPerRequest,
            List<BuildListener> buildListeners,
            List<ProcessListener> processListeners) {
        this.develocity = develocity;
        this.processorCache = requireNonNullElseGet(processorCache, NoCache::new);
        this.maxBuildsPerRequest = requireNonNullElse(maxBuildsPerRequest, defaultMaxBuildsPerRequest);
        this.buildListeners = buildListeners;
        this.processListeners = processListeners;
        this.requiredBuildModels = buildListeners.stream()
                .flatMap(it -> it.getRequiredBuildModels().stream())
                .collect(toUnmodifiableSet());
    }

    public static BuildProcessorBuilder forClient(DevelocityClient develocity) {
        return new BuildProcessorBuilder(develocity);
    }

    public void process(Instant since) {
        process(since, null);
    }

    public void process(Instant since, String query) {
        new BuildProcessorWorker(
                develocity,
                processorCache,
                maxBuildsPerRequest,
                since,
                query,
                buildListeners,
                processListeners,
                requiredBuildModels).process();
    }

    private static final class NoCache implements ProcessorCache {

        @Override
        public Optional<Build> load(String id, Set<BuildModel> requiredBuildModels) {
            return empty();
        }

        @Override
        public void save(Build build) {

        }

    }

}
