package dev.erichaag.develocity.processing.cache;

import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.BuildModel;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;

public final class InMemoryCache implements ProcessorCache {

    private static final int defaultMaxCacheSize = 10_000;

    private final Map<String, Build> buildsById = new ConcurrentHashMap<>();
    private final Queue<Build> buildQueue = new LinkedList<>();
    private final int maxCacheSize;

    private InMemoryCache(int maxCacheSize) {
        if (maxCacheSize < 1) throw new IllegalArgumentException("Maximum cache size must be greater than 0");
        this.maxCacheSize = maxCacheSize;
    }

    public static InMemoryCache withDefaultSize() {
        return new InMemoryCache(defaultMaxCacheSize);
    }

    public static InMemoryCache withSize(int size) {
        return new InMemoryCache(size);
    }

    @Override
    public Optional<Build> load(String id, Set<BuildModel> requiredBuildModels) {
        final var build = ofNullable(buildsById.get(id)).filter(it -> it.containsAllRelevantBuildModelsFrom(requiredBuildModels));
        if (build.isPresent()) {
            buildQueue.remove(build.get());
            buildQueue.add(build.get());
        }
        return build;
    }

    @Override
    public void save(Build build) {
        if (buildsById.containsKey(build.getId())) buildQueue.remove(build);
        buildsById.put(build.getId(), build);
        buildQueue.add(build);
        if (buildQueue.size() > maxCacheSize) buildsById.remove(buildQueue.remove().getId());
    }

}
