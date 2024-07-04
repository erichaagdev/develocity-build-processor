package dev.erichaag.develocity.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static java.lang.Math.min;
import static java.util.Collections.emptyList;

public final class DevelocityClientStub implements DevelocityClient {

    private static final int defaultMaxBuilds = 100;

    private final List<Build> builds;
    private final Map<String, Build> buildsById = new HashMap<>();

    private Supplier<DevelocityClientException> newDevelocityClientException;
    private int throwCount;

    private DevelocityClientStub(List<Build> builds) {
        this.builds = builds;
        builds.forEach(it -> {
            if (it.getId() == null) throw new RuntimeException("All builds must have an ID");
            if (buildsById.containsKey(it.getId())) throw new RuntimeException("Duplicate build ID: " + it.getId());
            buildsById.put(it.getId(), it);
        });
    }

    public static DevelocityClientStub withBuilds(Build... builds) {
        return withBuilds(List.of(builds));
    }

    public static DevelocityClientStub withBuilds(List<Build> builds) {
        return new DevelocityClientStub(builds);
    }

    public void thenThrow(int throwCount, Supplier<DevelocityClientException> newDevelocityClientException) {
        this.throwCount = throwCount;
        this.newDevelocityClientException = newDevelocityClientException;
    }

    @Override
    public Optional<Build> getBuild(String id, Set<BuildModel> buildModels) {
        if (throwCount > 0) {
            throwCount--;
            throw newDevelocityClientException.get();
        }
        return builds.stream().filter(it -> it.getId().equals(id)).findFirst();
    }

    @Override
    public List<Build> getBuilds(String query, Integer maxBuilds, String fromBuild, Set<BuildModel> buildModels) {
        if (throwCount > 0) {
            throwCount--;
            throw newDevelocityClientException.get();
        }
        maxBuilds = maxBuilds == null ? defaultMaxBuilds : maxBuilds;
        if (fromBuild == null) {
            return builds.subList(0, min(builds.size(), maxBuilds));
        }
        final var buildIndex = builds.indexOf(buildsById.get(fromBuild));
        if (buildIndex == -1 || buildIndex == builds.size() - 1) {
            return emptyList();
        }
        return builds.subList(buildIndex + 1, min(builds.size(), buildIndex + maxBuilds + 1));
    }

}
