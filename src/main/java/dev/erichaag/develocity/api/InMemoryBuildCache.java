package dev.erichaag.develocity.api;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;

public final class InMemoryBuildCache implements BuildCache {

    private static final int defaultMaxCacheSize = 10_000;

    private final Map<String, Build> buildsById = new ConcurrentHashMap<>();
    private final Queue<Build> buildQueue = new LinkedList<>();
    private final int maxCacheSize;

    public InMemoryBuildCache() {
        maxCacheSize = defaultMaxCacheSize;
    }

    public InMemoryBuildCache(int maxCacheSize) {
        if (maxCacheSize < 1) throw new IllegalArgumentException("Maximum cache size must be greater than 0");
        this.maxCacheSize = maxCacheSize;
    }

    @Override
    public Optional<Build> load(String id, Set<BuildModel> requiredBuildModels) {
        return ofNullable(buildsById.get(id)).filter(it -> it.getAvailableBuildModels().containsAll(requiredBuildModels));
    }

    @Override
    public void save(Build build) {
        buildsById.put(build.getId(), build);
        buildQueue.add(build);
        if (buildQueue.size() > maxCacheSize) buildsById.remove(buildQueue.remove().getId());
    }

}
