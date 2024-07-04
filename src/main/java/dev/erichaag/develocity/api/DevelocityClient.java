package dev.erichaag.develocity.api;

import java.util.List;
import java.util.Set;

public interface DevelocityClient {

    Build getBuild(String id, Set<BuildModel> buildModels);

    default Build getBuild(String id, BuildModel... buildModels) {
        return getBuild(id, Set.of(buildModels));
    }

    List<? extends Build> getBuilds(String query, Integer maxBuilds, String fromBuild, Set<BuildModel> buildModels);

    default List<? extends Build> getBuilds(String query, Integer maxBuilds, String fromBuild, BuildModel... buildModels) {
        return getBuilds(query, maxBuilds, fromBuild, Set.of(buildModels));
    }

}
