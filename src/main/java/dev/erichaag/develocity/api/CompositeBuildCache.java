package dev.erichaag.develocity.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;

public final class CompositeBuildCache implements BuildCache {

    private final List<BuildCache> buildCaches = new ArrayList<>();

    public CompositeBuildCache(BuildCache buildCache) {
        buildCaches.add(buildCache);
    }

    public CompositeBuildCache followedBy(BuildCache buildCache) {
        buildCaches.add(buildCache);
        return this;
    }

    @Override
    public Optional<Build> load(String id, Set<BuildModel> buildModels) {
        return buildCaches.stream()
                .map(it -> it.load(id, buildModels))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(empty());
    }

    @Override
    public void save(Build build) {
        buildCaches.forEach(buildCache -> buildCache.save(build));
    }

}
