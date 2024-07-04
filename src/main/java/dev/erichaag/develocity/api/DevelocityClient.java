package dev.erichaag.develocity.api;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DevelocityClient {

    Optional<Build> getBuild(String id, Set<BuildModel> buildModels);

    default Optional<Build> getBuild(String id, BuildModel... buildModels) {
        return getBuild(id, Set.of(buildModels));
    }

    List<Build> getBuilds(String query, Integer maxBuilds, String fromBuild, Set<BuildModel> buildModels);

    default List<Build> getBuilds(String query, Integer maxBuilds, String fromBuild, BuildModel... buildModels) {
        return getBuilds(query, maxBuilds, fromBuild, Set.of(buildModels));
    }

    static HttpClientDevelocityClientBuilder forServer(URI serverUrl) {
        return new HttpClientDevelocityClientBuilder(serverUrl);
    }

    static HttpClientDevelocityClientBuilder forServer(String serverUrl) {
        return forServer(URI.create(serverUrl));
    }

}
