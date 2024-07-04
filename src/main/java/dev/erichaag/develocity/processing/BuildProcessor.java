package dev.erichaag.develocity.processing;

import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.BuildModel;
import dev.erichaag.develocity.api.DevelocityClient;
import dev.erichaag.develocity.processing.cache.ProcessorCache;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.join;
import static java.time.Instant.now;
import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toUnmodifiableSet;

public final class BuildProcessor {

    private static final int defaultMaxBuildsPerRequest = 100;

    private final DevelocityClient develocity;
    private final ProcessorCache processorCache;
    private final int maxBuildsPerRequest;
    private final int backOffLimit;
    private final double backOffFactor;
    private final int retryLimit;
    private final double retryFactor;
    private final List<BuildListener> buildListeners;
    private final List<ProcessListener> processListeners;
    private final Set<BuildModel> requiredBuildModels;

    BuildProcessor(
            DevelocityClient develocity,
            ProcessorCache processorCache,
            Integer maxBuildsPerRequest,
            Integer backOffLimit,
            Double backOffFactor,
            Integer retryLimit,
            Double retryFactor,
            List<BuildListener> buildListeners,
            List<ProcessListener> processListeners
    ) {
        this.develocity = develocity;
        this.processorCache = requireNonNullElseGet(processorCache, NoopCache::new);
        this.maxBuildsPerRequest = requireNonNullElse(maxBuildsPerRequest, defaultMaxBuildsPerRequest);
        this.backOffLimit = requireNonNullElse(backOffLimit, 8);
        this.backOffFactor = requireNonNullElse(backOffFactor, .75);
        this.retryLimit = requireNonNullElse(retryLimit, 5);
        this.retryFactor = requireNonNullElse(retryFactor, 1.5);
        this.buildListeners = buildListeners;
        this.processListeners = processListeners;
        this.requiredBuildModels = buildListeners.stream()
                .flatMap(it -> it.getRequiredBuildModels().stream())
                .collect(toUnmodifiableSet());
        validate();
    }

    private void validate() {
        final var validationErrors = new ArrayList<String>();
        if (maxBuildsPerRequest < 1 || maxBuildsPerRequest > 1000) validationErrors.add("maxBuildsPerRequest must be between 1 (inclusive) and 1000 (inclusive)");
        if (backOffLimit <= 0) validationErrors.add("backOffLimit must be greater than 0");
        if (backOffFactor <= 0 || backOffFactor >= 1) validationErrors.add("backOffFactor must be between 0 (exclusive) and 1 (exclusive)");
        if (retryLimit <= 0) validationErrors.add("retryLimit must be greater than 0");
        if (retryFactor <= 1) validationErrors.add("retryFactor must be greater than 1");
        if (!validationErrors.isEmpty()) throw new IllegalArgumentException(join(", ", validationErrors));
    }

    public static BuildProcessorBuilder forClient(DevelocityClient develocity) {
        return new BuildProcessorBuilder(develocity);
    }

    public static BuildProcessorBuilder forServer(String serverUrl) {
        final var develocity = DevelocityClient.forServer(serverUrl).followingRedirects().build();
        return new BuildProcessorBuilder(develocity);
    }

    public static BuildProcessorBuilder forServerWithAnonymousAccess(String serverUrl) {
        final var develocity = DevelocityClient.forServer(serverUrl).followingRedirects().withAnonymousAccess().build();
        return new BuildProcessorBuilder(develocity);
    }

    public void process(ZonedDateTime since) {
        process(since, null);
    }

    public void process(ZonedDateTime since, String query) {
        process(since.toInstant(), query);
    }

    public void process(OffsetDateTime since) {
        process(since, null);
    }

    public void process(OffsetDateTime since, String query) {
        process(since.toInstant(), query);
    }

    public void process(Duration since) {
        process(now().minus(since), null);
    }

    public void process(Duration since, String query) {
        process(now().minus(since), query);
    }

    public void process(Instant since) {
        process(since, null);
    }

    public void process(Instant since, String query) {
        new BuildProcessorWorker(
                develocity,
                processorCache,
                maxBuildsPerRequest,
                backOffLimit,
                backOffFactor,
                retryLimit,
                retryFactor,
                since,
                query,
                buildListeners,
                processListeners,
                requiredBuildModels).process();
    }

    private static final class NoopCache implements ProcessorCache {

        @Override
        public Optional<Build> load(String id, Set<BuildModel> requiredBuildModels) {
            return empty();
        }

        @Override
        public void save(Build build) {

        }

    }

}
