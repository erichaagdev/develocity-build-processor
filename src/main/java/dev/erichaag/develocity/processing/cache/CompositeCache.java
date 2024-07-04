package dev.erichaag.develocity.processing.cache;

import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.BuildModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;

public final class CompositeCache implements ProcessorCache {

    private final List<ProcessorCache> caches = new ArrayList<>();

    private CompositeCache(ProcessorCache cache) {
        caches.add(cache);
    }

    public static CompositeCache firstChecking(ProcessorCache cache) {
        return new CompositeCache(cache);
    }

    public CompositeCache followedBy(ProcessorCache cache) {
        caches.add(cache);
        return this;
    }

    @Override
    public Optional<Build> load(String id, Set<BuildModel> requiredBuildModels) {
        for (int i = 0; i < caches.size(); i++) {
            final var build = caches.get(i).load(id, requiredBuildModels);
            if (build.isPresent()) {
                for (int j = i - 1; j >= 0; j--) {
                    caches.get(j).save(build.get());
                }
                return build;
             }
        }
        return empty();
    }

    @Override
    public void save(Build build) {
        caches.forEach(buildCache -> buildCache.save(build));
    }

}
