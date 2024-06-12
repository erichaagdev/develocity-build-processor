package dev.erichaag.develocity.api;

import java.util.List;

public interface DevelocityClient {

    Build getBuild(String id, BuildModel... buildModels);

    List<Build> getBuilds(String query, Integer maxBuilds, String fromBuild, BuildModel... buildModels);

}
