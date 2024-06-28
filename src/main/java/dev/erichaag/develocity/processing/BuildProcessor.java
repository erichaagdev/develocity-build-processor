package dev.erichaag.develocity.processing;

import dev.erichaag.develocity.api.BuildModel;
import dev.erichaag.develocity.api.DevelocityClient;
import dev.erichaag.develocity.processing.cache.ProcessorCache;

import java.time.Instant;
import java.util.List;
import java.util.Set;

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
        this.processorCache = processorCache;
        this.maxBuildsPerRequest = maxBuildsPerRequest == null ? defaultMaxBuildsPerRequest : maxBuildsPerRequest;
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

}
