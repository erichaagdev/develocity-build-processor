package dev.erichaag.develocity.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BuildProcessor {

    private final DevelocityClient develocity;
    private final BuildCache buildCache;
    private final int maxBuildsPerRequest;
    private final List<BuildProcessorListener> listeners = new ArrayList<>();
    private final Set<BuildModel> requiredBuildModels = new HashSet<>();

    public BuildProcessor(DevelocityClient develocity, BuildCache buildCache, int maxBuildsPerRequest) {
        this.develocity = develocity;
        this.buildCache = buildCache;
        this.maxBuildsPerRequest = maxBuildsPerRequest;
    }

    public BuildProcessor register(BuildProcessorListener listener) {
        listeners.add(listener);
        requiredBuildModels.addAll(listener.getRequiredBuildModels());
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
                listeners,
                requiredBuildModels).process();
    }

}
