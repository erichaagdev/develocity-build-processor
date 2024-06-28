package dev.erichaag.develocity.processing.cache;

import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.BuildModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;

public final class CompositeProcessorCache implements ProcessorCache {

    private final List<ProcessorCache> processorCaches = new ArrayList<>();

    private CompositeProcessorCache(ProcessorCache processorCache) {
        processorCaches.add(processorCache);
    }

    public static CompositeProcessorCache firstChecking(ProcessorCache processorCache) {
        return new CompositeProcessorCache(processorCache);
    }

    public CompositeProcessorCache followedBy(ProcessorCache processorCache) {
        processorCaches.add(processorCache);
        return this;
    }

    @Override
    public Optional<Build> load(String id, Set<BuildModel> requiredBuildModels) {
        for (int i = 0; i < processorCaches.size(); i++) {
            final var build = processorCaches.get(i).load(id, requiredBuildModels);
            if (build.isPresent()) {
                for (int j = i - 1; j >= 0; j--) {
                    processorCaches.get(j).save(build.get());
                }
                return build;
             }
        }
        return empty();
    }

    @Override
    public void save(Build build) {
        processorCaches.forEach(buildCache -> buildCache.save(build));
    }

}
