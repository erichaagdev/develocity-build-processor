package dev.erichaag.develocity.api;

import java.util.Optional;
import java.util.Set;

public interface BuildCache {

    Optional<Build> load(String id, Set<BuildModel> requiredBuildModels);

    default Optional<Build> load(String id, BuildModel... requiredBuildModels) {
        return load(id, Set.of(requiredBuildModels));
    }

    void save(Build build);

}
