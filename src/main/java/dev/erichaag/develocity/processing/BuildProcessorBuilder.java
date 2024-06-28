package dev.erichaag.develocity.processing;

import dev.erichaag.develocity.api.DevelocityClient;
import dev.erichaag.develocity.processing.cache.ProcessorCache;

import java.util.ArrayList;
import java.util.List;

public final class BuildProcessorBuilder {

    private final DevelocityClient develocity;
    private final List<BuildListener> buildListeners = new ArrayList<>();
    private final List<ProcessListener> processListeners = new ArrayList<>();

    private ProcessorCache processorCache;
    private int maxBuildsPerRequest;

    public BuildProcessorBuilder(DevelocityClient develocity) {
        this.develocity = develocity;
    }

    public BuildProcessorBuilder register(BuildListener listener) {
        buildListeners.add(listener);
        return this;
    }

    public BuildProcessorBuilder register(ProcessListener listener) {
        processListeners.add(listener);
        return this;
    }

    public BuildProcessorBuilder withProcessorCache(ProcessorCache processorCache) {
        this.processorCache = processorCache;
        return this;
    }

    public BuildProcessorBuilder withMaxBuildsPerRequest(int maxBuildsPerRequest) {
        this.maxBuildsPerRequest = maxBuildsPerRequest;
        return this;
    }

    public BuildProcessor build() {
        return new BuildProcessor(
                develocity,
                processorCache,
                maxBuildsPerRequest,
                buildListeners,
                processListeners
        );
    }

}
