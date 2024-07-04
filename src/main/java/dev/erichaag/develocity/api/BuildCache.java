package dev.erichaag.develocity.api;

import java.util.Optional;
import java.util.Set;

public interface BuildCache {

    Optional<Build> load(String id, Set<BuildModel> buildModels);

    default Optional<Build> load(String id, BuildModel... buildModels) {
        return load(id, Set.of(buildModels));
    }

    void save(Build build);

}
