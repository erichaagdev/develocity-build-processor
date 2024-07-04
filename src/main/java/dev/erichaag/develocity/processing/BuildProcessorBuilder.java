package dev.erichaag.develocity.processing;

import dev.erichaag.develocity.api.BazelBuild;
import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.BuildModel;
import dev.erichaag.develocity.api.DevelocityClient;
import dev.erichaag.develocity.api.GradleBuild;
import dev.erichaag.develocity.api.MavenBuild;
import dev.erichaag.develocity.api.SbtBuild;
import dev.erichaag.develocity.processing.cache.ProcessorCache;
import dev.erichaag.develocity.processing.event.CachedBuildEvent;
import dev.erichaag.develocity.processing.event.DiscoveryFinishedEvent;
import dev.erichaag.develocity.processing.event.DiscoveryStartedEvent;
import dev.erichaag.develocity.processing.event.FetchedBuildEvent;
import dev.erichaag.develocity.processing.event.ProcessingFinishedEvent;
import dev.erichaag.develocity.processing.event.ProcessingStartedEvent;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static java.time.Instant.now;

public final class BuildProcessorBuilder {

    private final DevelocityClient develocity;
    private final List<BuildListener> buildListeners = new ArrayList<>();
    private final List<ProcessListener> processListeners = new ArrayList<>();
    private final BuildListenerBuilder buildListenerBuilder = BuildListener.builder();
    private final ProcessListenerBuilder processListenerBuilder = ProcessListener.builder();
    private final Set<BuildModel> requiredBuildModels = new HashSet<>();

    private ProcessorCache processorCache;
    private Integer maxBuildsPerRequest;
    private Integer backOffLimit;
    private Double backOffFactor;
    private Integer retryLimit;
    private Double retryFactor;

    BuildProcessorBuilder(DevelocityClient develocity) {
        this.develocity = develocity;
    }

    public BuildProcessorBuilder register(BuildListener listener) {
        buildListeners.add(listener);
        return this;
    }

    public BuildProcessorBuilder register(ProcessListener listener) {
        processListeners.add(listener);
        return this;
    }

    public BuildProcessorBuilder onBuild(Consumer<Build> onBuild) {
        buildListenerBuilder.onBuild(onBuild);
        return this;
    }

    public BuildProcessorBuilder onGradleBuild(Consumer<GradleBuild> onGradleBuild) {
        buildListenerBuilder.onGradleBuild(onGradleBuild);
        return this;
    }

    public BuildProcessorBuilder onMavenBuild(Consumer<MavenBuild> onMavenBuild) {
        buildListenerBuilder.onMavenBuild(onMavenBuild);
        return this;
    }

    public BuildProcessorBuilder onBazelBuild(Consumer<BazelBuild> onBazelBuild) {
        buildListenerBuilder.onBazelBuild(onBazelBuild);
        return this;
    }

    public BuildProcessorBuilder onSbtBuild(Consumer<SbtBuild> onSbtBuild) {
        buildListenerBuilder.onSbtBuild(onSbtBuild);
        return this;
    }

    public BuildProcessorBuilder onDiscoveryStarted(Consumer<DiscoveryStartedEvent> onDiscoveryStarted) {
        processListenerBuilder.onDiscoveryStarted(onDiscoveryStarted);
        return this;
    }

    public BuildProcessorBuilder onDiscoveryFinished(Consumer<DiscoveryFinishedEvent> onDiscoveryFinished) {
        processListenerBuilder.onDiscoveryFinished(onDiscoveryFinished);
        return this;
    }

    public BuildProcessorBuilder onProcessingStarted(Consumer<ProcessingStartedEvent> onProcessingStarted) {
        processListenerBuilder.onProcessingStarted(onProcessingStarted);
        return this;
    }

    public BuildProcessorBuilder onCachedBuild(Consumer<CachedBuildEvent> onCachedBuild) {
        processListenerBuilder.onCachedBuild(onCachedBuild);
        return this;
    }

    public BuildProcessorBuilder onFetchedBuild(Consumer<FetchedBuildEvent> onFetchedBuild) {
        processListenerBuilder.onFetchedBuild(onFetchedBuild);
        return this;
    }

    public BuildProcessorBuilder onProcessingFinished(Consumer<ProcessingFinishedEvent> onProcessingFinished) {
        processListenerBuilder.onProcessingFinished(onProcessingFinished);
        return this;
    }

    public BuildProcessorBuilder withRequiredBuildModels(BuildModel... buildModels) {
        requiredBuildModels.addAll(Set.of(buildModels));
        return this;
    }

    public BuildProcessorBuilder withProcessorCache(ProcessorCache processorCache) {
        this.processorCache = processorCache;
        return this;
    }

    public BuildProcessorBuilder withMaxBuildsPerRequest(int maxBuildsPerRequest) {
        this.maxBuildsPerRequest = maxBuildsPerRequest;
        return this;
    }

    public BuildProcessorBuilder withBackOffLimit(int backOffLimit) {
        this.backOffLimit = backOffLimit;
        return this;
    }

    public BuildProcessorBuilder withBackOffFactor(double backOffFactor) {
        this.backOffFactor = backOffFactor;
        return this;
    }

    public BuildProcessorBuilder withRetryLimit(int retryLimit) {
        this.retryLimit = retryLimit;
        return this;
    }

    public BuildProcessorBuilder withRetryFactor(double retryFactor) {
        this.retryFactor = retryFactor;
        return this;
    }

    public BuildProcessor process(ZonedDateTime since) {
        return process(since.toInstant(), null);
    }

    public BuildProcessor process(ZonedDateTime since, String query) {
        return process(since.toInstant(), query);
    }

    public BuildProcessor process(OffsetDateTime since) {
        return process(since, null);
    }

    public BuildProcessor process(OffsetDateTime since, String query) {
        return process(since.toInstant(), query);
    }

    public BuildProcessor process(Duration since) {
        return process(now().minus(since), null);
    }

    public BuildProcessor process(Duration since, String query) {
        return process(now().minus(since), query);
    }

    public BuildProcessor process(Instant since) {
        return process(since, null);
    }

    public BuildProcessor process(Instant since, String query) {
        final var processor = build();
        processor.process(since, query);
        return processor;
    }

    public BuildProcessor build() {
        buildListeners.add(buildListenerBuilder.build());
        processListeners.add(processListenerBuilder.build());
        return new BuildProcessor(
                develocity,
                processorCache,
                maxBuildsPerRequest,
                backOffLimit,
                backOffFactor,
                retryLimit,
                retryFactor,
                buildListeners,
                processListeners,
                requiredBuildModels
        );
    }

}
