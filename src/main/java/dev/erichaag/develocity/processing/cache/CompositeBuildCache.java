package dev.erichaag.develocity.processing.cache;

import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.BuildModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;

public final class CompositeBuildCache implements BuildCache {

    private final List<BuildCache> buildCaches = new ArrayList<>();

    private CompositeBuildCache(BuildCache buildCache) {
        buildCaches.add(buildCache);
    }

    public static CompositeBuildCache firstChecking(BuildCache buildCache) {
        return new CompositeBuildCache(buildCache);
    }

    public CompositeBuildCache followedBy(BuildCache buildCache) {
        buildCaches.add(buildCache);
        return this;
    }

    @Override
    public Optional<Build> load(String id, Set<BuildModel> requiredBuildModels) {
        for (int i = 0; i < buildCaches.size(); i++) {
            final var build = buildCaches.get(i).load(id, requiredBuildModels);
            if (build.isPresent()) {
                for (int j = i - 1; j >= 0; j--) {
                    buildCaches.get(j).save(build.get());
                }
                return build;
             }
        }
        return empty();
    }

    @Override
    public void save(Build build) {
        buildCaches.forEach(buildCache -> buildCache.save(build));
    }

}
