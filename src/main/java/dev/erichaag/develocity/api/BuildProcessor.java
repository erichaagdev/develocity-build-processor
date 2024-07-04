package dev.erichaag.develocity.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.time.Instant.now;
import static java.util.Optional.empty;

public final class BuildProcessor {

    private static final ObjectMapper objectMapper = new JsonMapper();
    private static final String cacheDirectoryName = ".develocity-failure-insights";
    private static final int maxDiscoveryBuildsPerRequest = 1_000;

    private final DevelocityClient develocity;
    private final int maxBuildsPerRequest;
    private final List<BuildProcessorListener> listeners = new ArrayList<>();
    private final Set<BuildModel> requiredBuildModels = new HashSet<>();

    public BuildProcessor(DevelocityClient develocity, int maxBuildsPerRequest) {
        this.develocity = develocity;
        this.maxBuildsPerRequest = maxBuildsPerRequest;
    }

    public void registerListener(BuildProcessorListener listener) {
        listeners.add(listener);
        requiredBuildModels.addAll(listener.getRequiredBuildModels());
    }

    public void process(ZonedDateTime since) {
        process(since, null);
    }

    public void process(ZonedDateTime since, String query) {
        new BuildProcessorWorker(develocity, maxBuildsPerRequest, since, query, listeners, requiredBuildModels).process();
    }

    private static class BuildProcessorWorker {

        private final DevelocityClient develocity;
        private final int maxBuildsPerRequest;
        private final ZonedDateTime since;
        private final String query;
        private final List<BuildProcessorListener> listeners;
        private final Set<BuildModel> requiredBuildModels;

        private String lastCachedBuildId;
        private String lastUncachedBuildId;
        private int uncached = 0;

        BuildProcessorWorker(DevelocityClient develocity, int maxBuildsPerRequest, ZonedDateTime since, String query, List<BuildProcessorListener> listeners, Set<BuildModel> requiredBuildModels) {
            this.develocity = develocity;
            this.maxBuildsPerRequest = maxBuildsPerRequest;
            this.since = since;
            this.query = query;
            this.listeners = listeners;
            this.requiredBuildModels = requiredBuildModels;
        }

        public void process() {
            notifyListenersDiscoveryStarted();
            final var builds = discoverBuilds(query, since);
            notifyListenersDiscoveryFinished(builds);
            notifyListenersProcessingStarted();
            builds.forEach(this::process);
            if (uncached > 0) processUncachedBuilds();
            notifyListenersProcessingFinished();
        }

        private void notifyListenersDiscoveryStarted() {
            final var event = new DiscoveryStartedEvent(now(), since);
            listeners.forEach(it -> it.onDiscoveryStarted(event));
        }

        private void notifyListenersDiscoveryFinished(List<Build> builds) {
            final var event = new DiscoveryFinishedEvent(now(), builds);
            listeners.forEach(it -> it.onDiscoveryFinished(event));
        }

        private void notifyListenersProcessingStarted() {
            final var event = new ProcessingStartedEvent(now());
            listeners.forEach(it -> it.onProcessingStarted(event));
        }

        private void notifyListenersProcessingFinished() {
            final var event = new ProcessingFinishedEvent(now());
            listeners.forEach(it -> it.onProcessingFinished(event));
        }

        private void notifyListenersBuild(Build build) {
            listeners.forEach(it -> process(build, it));
        }

        private void notifyListenersCachedBuild(Build build) {
            final var event = new CachedBuildEvent(now(), build);
            listeners.forEach(it -> it.onCachedBuild(event));
        }

        private void notifyListenersFetchedBuild(Build build) {
            final var event = new FetchedBuildEvent(now(), build);
            listeners.forEach(it -> it.onFetchedBuild(event));
        }

        private List<Build> discoverBuilds(String query, ZonedDateTime since) {
            final var sinceMilli = since.toInstant().toEpochMilli();
            final var builds = new ArrayList<Build>();
            while (true) {
                final var response = develocity.getBuilds(query, maxDiscoveryBuildsPerRequest, getLastId(builds));
                if (response.isEmpty()) return builds;
                if (response.getLast().getAvailableAt() < sinceMilli) {
                    builds.addAll(response.stream().filter(it -> it.getAvailableAt() >= sinceMilli).toList());
                    return builds;
                }
                builds.addAll(response);
            }
        }

        private void process(Build build) {
            final var cachedBuild = getCachedBuild(build.getId());
            if (uncached == maxBuildsPerRequest || (cachedBuild.isPresent() && uncached > 0)) {
                processUncachedBuilds();
                lastCachedBuildId = lastUncachedBuildId;
                uncached = 0;
            }
            if (cachedBuild.isPresent()) {
                processCachedBuild(cachedBuild.get());
                lastCachedBuildId = build.getId();
            } else {
                lastUncachedBuildId = build.getId();
                uncached++;
            }
        }

        private void processCachedBuild(CachedBuild cachedBuild) {
            if (cachedBuild.buildModels().equals(requiredBuildModels)) {
                notifyListenersBuild(cachedBuild.asBuild());
                notifyListenersCachedBuild(cachedBuild.asBuild());
                return;
            }
            final var build = develocity.getBuild(cachedBuild.asBuild().getId(), requiredBuildModels.toArray(new BuildModel[0]));
            writeCachedBuild(new CachedBuild(requiredBuildModels, build));
            notifyListenersBuild(build);
            notifyListenersFetchedBuild(build);
        }

        private void processUncachedBuilds() {
            final var builds = develocity.getBuilds(query, uncached, lastCachedBuildId, requiredBuildModels.toArray(new BuildModel[0]));
            builds.forEach(build -> {
                writeCachedBuild(new CachedBuild(requiredBuildModels, build));
                notifyListenersBuild(build);
                notifyListenersFetchedBuild(build);
            });
        }

        private static void process(Build build, BuildProcessorListener listener) {
            listener.onBuild(build);
            switch (build) {
                case GradleBuild b -> listener.onGradleBuild(b);
                case MavenBuild b -> listener.onMavenBuild(b);
                case BazelBuild b -> listener.onBazelBuild(b);
                case SbtBuild b -> listener.onSbtBuild(b);
            }
        }

        private static File getCachedBuildFile(String id) {
            return Path.of(System.getProperty("user.home"))
                    .resolve(cacheDirectoryName)
                    .resolve(id.substring(0, 2))
                    .resolve(id + ".json")
                    .toFile();
        }

        private static Optional<CachedBuild> getCachedBuild(String id) {
            final var cachedBuildFile = getCachedBuildFile(id);
            try {
                if (cachedBuildFile.exists()) {
                    return Optional.of(objectMapper.readValue(cachedBuildFile, CachedBuild.class));
                }
            } catch (IOException ignored) {
                //noinspection ResultOfMethodCallIgnored
                cachedBuildFile.delete();
            }
            return empty();
        }

        private static void writeCachedBuild(CachedBuild cachedBuild) {
            final var cachedBuildFile = getCachedBuildFile(cachedBuild.build().getId());
            //noinspection ResultOfMethodCallIgnored
            cachedBuildFile.getParentFile().mkdirs();
            try {
                Files.write(cachedBuildFile.toPath(), objectMapper.writeValueAsBytes(cachedBuild));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private static String getLastId(List<Build> builds) {
            return builds.isEmpty() ? null : builds.getLast().getId();
        }

        private record CachedBuild(Set<BuildModel> buildModels, ApiBuild build) { // todo rename to apiBuild. done for backwards compatibility.

            private CachedBuild(Set<BuildModel> buildModels, Build build) {
                this(buildModels, build.getBuild());
            }

            private Build asBuild() {
                return Build.from(build);
            }

        }

    }

}
