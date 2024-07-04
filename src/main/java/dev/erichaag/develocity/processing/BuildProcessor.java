package dev.erichaag.develocity.processing;

import dev.erichaag.develocity.api.BuildModel;
import dev.erichaag.develocity.api.DevelocityClient;
import dev.erichaag.develocity.processing.cache.BuildCache;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BuildProcessor {

    private final DevelocityClient develocity;
    private final BuildCache buildCache;
    private final int maxBuildsPerRequest;
    private final List<BuildListener> buildListeners = new ArrayList<>();
    private final List<ProcessListener> processListeners = new ArrayList<>();
    private final Set<BuildModel> requiredBuildModels = new HashSet<>();

    public BuildProcessor(DevelocityClient develocity, BuildCache buildCache, int maxBuildsPerRequest) {
        this.develocity = develocity;
        this.buildCache = buildCache;
        this.maxBuildsPerRequest = maxBuildsPerRequest;
    }

    public BuildProcessor register(BuildListener listener) {
        buildListeners.add(listener);
        requiredBuildModels.addAll(listener.getRequiredBuildModels());
        return this;
    }

    public BuildProcessor register(ProcessListener listener) {
        processListeners.add(listener);
        return this;
    }

    public void process(Instant since) {
        process(since, null);
    }

    public void process(Instant since, String query) {
        new BuildProcessorWorker(
                develocity,
                buildCache,
                maxBuildsPerRequest,
                since,
                query,
                buildListeners,
                processListeners,
                requiredBuildModels).process();
    }

}
