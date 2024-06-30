package dev.erichaag.develocity.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static java.lang.Math.min;
import static java.util.Collections.emptyList;
import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;

public final class DevelocityClientStub implements DevelocityClient {

    private static final int defaultMaxBuilds = 100;

    private final List<Build> builds;
    private final Map<String, Build> buildsById = new HashMap<>();

    private DevelocityClientStub(List<Build> builds) {
        this.builds = builds.stream().sorted(reverseOrder(comparing(Build::getAvailableAt))).toList();
        builds.forEach(it -> {
            if (buildsById.containsKey(it.getId())) {
                throw new RuntimeException("Duplicate build ID: " + it.getId());
            }
            buildsById.put(it.getId(), it);
        });
    }

    public static DevelocityClientStub withBuilds(Build... builds) {
        return withBuilds(List.of(builds));
    }

    public static DevelocityClientStub withBuilds(List<Build> builds) {
        IntStream.range(0, builds.size())
                .filter(i -> builds.get(i).getId() == null)
                .forEach(i -> builds.get(i).getBuild().setId("foobarbazqux" + i));
        return new DevelocityClientStub(builds);
    }

    @Override
    public Optional<Build> getBuild(String id, Set<BuildModel> buildModels) {
        return builds.stream().filter(it -> it.getId().equals(id)).findFirst();
    }

    @Override
    public List<? extends Build> getBuilds(String query, Integer maxBuilds, String fromBuild, Set<BuildModel> buildModels) {
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
