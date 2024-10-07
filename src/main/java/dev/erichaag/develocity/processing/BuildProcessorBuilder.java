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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.time.Instant.now;

/**
 * A builder class for constructing a {@link BuildProcessor}.
 * 
 * <p>An instance of this builder can be obtained by calling one of the static
 *  * factory methods on {@link BuildProcessor}.
 *
 * <p>This builder allows you to configure all aspects of a
 * {@link BuildProcessor}, including build and progress listeners, caching, and
 * various configuration options.
 *
 * <p>This builder provides a fluent interface, allowing method calls to be
 * chained together for convenient configuration.
 *
 * @see BuildProcessor
 */
public final class BuildProcessorBuilder {

    private final DevelocityClient develocity;
    private final List<BuildListener> buildListeners = new ArrayList<>();
    private final List<ProcessListener> processListeners = new ArrayList<>();
    private final BuildListenerBuilder buildListenerBuilder = BuildListener.builder();
    private final ProcessListenerBuilder processListenerBuilder = ProcessListener.builder();

    private ProcessorCache processorCache;
    private Integer maxBuildsPerRequest;
    private Integer backOffLimit;
    private Double backOffFactor;
    private Integer retryLimit;
    private Double retryFactor;

    BuildProcessorBuilder(DevelocityClient develocity) {
        this.develocity = develocity;
    }

    /**
     * Registers a {@link BuildListener} to receive notifications about builds
     * encountered during processing.
     *
     * <p>Multiple listeners can be registered using this method.
     *
     * @param listener the listener to register
     * @return this builder instance for fluent configuration
     */
    public BuildProcessorBuilder register(BuildListener listener) {
        buildListeners.add(listener);
        return this;
    }

    /**
     * Registers a {@link ProcessListener} to receive callbacks about the
     * build processing lifecycle.
     *
     * <p>Multiple listeners can be registered using this method.
     *
     * @param listener the listener to register
     * @return this builder instance for fluent configuration
     */
    public BuildProcessorBuilder register(ProcessListener listener) {
        processListeners.add(listener);
        return this;
    }

    /**
     * Registers a callback function to be invoked when any build is encountered.
     *
     * @param onBuild the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public BuildProcessorBuilder onBuild(Consumer<Build> onBuild) {
        buildListenerBuilder.onBuild(onBuild);
        return this;
    }

    /**
     * Registers a callback function to be invoked when a Gradle build is
     * encountered.
     *
     * @param onGradleBuild the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public BuildProcessorBuilder onGradleBuild(Consumer<GradleBuild> onGradleBuild) {
        buildListenerBuilder.onGradleBuild(onGradleBuild);
        return this;
    }

    /**
     * Registers a callback function to be invoked when a Maven build is
     * encountered.
     *
     * @param onMavenBuild the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public BuildProcessorBuilder onMavenBuild(Consumer<MavenBuild> onMavenBuild) {
        buildListenerBuilder.onMavenBuild(onMavenBuild);
        return this;
    }

    /**
     * Registers a callback function to be invoked when a Bazel build is
     * encountered.
     *
     * @param onBazelBuild the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public BuildProcessorBuilder onBazelBuild(Consumer<BazelBuild> onBazelBuild) {
        buildListenerBuilder.onBazelBuild(onBazelBuild);
        return this;
    }

    /**
     * Registers a callback function to be invoked when an sbt build is
     * encountered.
     *
     * @param onSbtBuild the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public BuildProcessorBuilder onSbtBuild(Consumer<SbtBuild> onSbtBuild) {
        buildListenerBuilder.onSbtBuild(onSbtBuild);
        return this;
    }

    /**
     * Registers a callback function to be invoked when a build is retrieved
     * from a {@link ProcessorCache}.
     *
     * @param onCachedBuild the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public BuildProcessorBuilder onCachedBuild(Consumer<CachedBuildEvent> onCachedBuild) {
        processListenerBuilder.onCachedBuild(onCachedBuild);
        return this;
    }

    /**
     * Registers a callback function to be invoked when a build is fetched from
     * the API.
     *
     * @param onFetchedBuild the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public BuildProcessorBuilder onFetchedBuild(Consumer<FetchedBuildEvent> onFetchedBuild) {
        processListenerBuilder.onFetchedBuild(onFetchedBuild);
        return this;
    }

    /**
     * Registers a callback function to be invoked when the discovery phase of
     * build processing starts.
     *
     * @param onDiscoveryStarted the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public BuildProcessorBuilder onDiscoveryStarted(Consumer<DiscoveryStartedEvent> onDiscoveryStarted) {
        processListenerBuilder.onDiscoveryStarted(onDiscoveryStarted);
        return this;
    }

    /**
     * Registers a callback function to be invoked when the discovery phase of
     * build processing finishes.
     *
     * @param onDiscoveryFinished the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public BuildProcessorBuilder onDiscoveryFinished(Consumer<DiscoveryFinishedEvent> onDiscoveryFinished) {
        processListenerBuilder.onDiscoveryFinished(onDiscoveryFinished);
        return this;
    }

    /**
     * Registers a callback function to be invoked when build processing starts.
     *
     * @param onProcessingStarted the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public BuildProcessorBuilder onProcessingStarted(Consumer<ProcessingStartedEvent> onProcessingStarted) {
        processListenerBuilder.onProcessingStarted(onProcessingStarted);
        return this;
    }

    /**
     * Registers a callback function to be invoked when build processing finishes.
     *
     * @param onProcessingFinished the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public BuildProcessorBuilder onProcessingFinished(Consumer<ProcessingFinishedEvent> onProcessingFinished) {
        processListenerBuilder.onProcessingFinished(onProcessingFinished);
        return this;
    }

    /**
     * Adds the specified build models required by the constructed
     * {@link BuildProcessor}. This information is used by the
     * {@link BuildProcessor} to know which build models to request.
     *
     * <p>Calling this method more than once will add the supplied build models
     * to the set of required build models. Duplicate build models are ignored.
     *
     * @param buildModels the build models required by the constructed listener
     * @return this builder instance for fluent configuration
     */
    public BuildProcessorBuilder withRequiredBuildModels(BuildModel... buildModels) {
        buildListenerBuilder.requiredBuildModels(buildModels);
        return this;
    }

    /**
     * Sets the {@link ProcessorCache} to be used for caching builds.
     *
     * @param processorCache the cache instance to use
     * @return this builder instance for fluent configuration
     */
    public BuildProcessorBuilder withProcessorCache(ProcessorCache processorCache) {
        this.processorCache = processorCache;
        return this;
    }

    /**
     * Sets the maximum number of builds to fetch per request.
     *
     * <p>As the number of required build models increases, the longer calls to
     * the API will take. It's important to set this value to a reasonable value
     * to avoid the API prematurely ending the connection.
     *
     * <p>By default, the maximum number of builds per request is 100.
     *
     * @param maxBuildsPerRequest the maximum builds per request
     * @return this builder instance for fluent configuration
     */
    public BuildProcessorBuilder withMaxBuildsPerRequest(int maxBuildsPerRequest) {
        this.maxBuildsPerRequest = maxBuildsPerRequest;
        return this;
    }

    // todo explain how this is different than a retry
    /**
     * Sets the maximum number of times to back off when processing encounters a
     * response from the server that indicates a request took too long.
     *
     * <p>Each time a back off occurs, the constructed build processor reduce
     * the maximum number of builds fetched per request. The factor by which the
     * maximum is reduced can be configured with
     * {@link BuildProcessorBuilder#withBackOffFactor(double)}
     *
     * <p> By default, the backoff limit is 8.
     *
     * @param backOffLimit the maximum number of times to back off
     * @return this builder instance for fluent configuration
     */
    public BuildProcessorBuilder withBackOffLimit(int backOffLimit) {
        this.backOffLimit = backOffLimit;
        return this;
    }

    /**
     * Sets the factor by which to decrease the maximum number of builds per
     * request when a back off occurs.
     *
     * <p>This factor is used in conjunction with the back off limit to determine
     * the maximum number of builds per request. For example, a backoff factor
     * of .5 means the maximum number will halve with each back off. The back
     * off limit can be configured with
     * {@link BuildProcessorBuilder#withBackOffLimit(int)}
     *
     * <p>By default, the back off factor is .75.
     *
     * @param backOffFactor the factor by which to reduce the maximum number of
     *                      builds per request
     * @return this builder instance for fluent configuration
     */
    public BuildProcessorBuilder withBackOffFactor(double backOffFactor) {
        this.backOffFactor = backOffFactor;
        return this;
    }

    // todo explain how this is different than a back off
    /**
     * Sets the maximum number of times to retry a failed request to fetch
     * builds.
     *
     * <p>If the API request to fetch builds fails, the build processor will retry
     * up to this number of times before giving up.
     *
     * <p>By default, the retry limit is 5.
     *
     * @param retryLimit the maximum number of retry attempts
     * @return this builder instance for fluent configuration
     */
    public BuildProcessorBuilder withRetryLimit(int retryLimit) {
        this.retryLimit = retryLimit;
        return this;
    }

    /**
     * Sets the factor by which to increase the retry delay after each failed
     * attempt to fetch builds.
     *
     * <p>This factor is used in conjunction with the retry limit to determine the
     * delay between retry attempts. For example, a retry factor of 2.0 means the
     * delay will double after each failure.
     *
     * <p>By default, the retry factor is 1.5.
     *
     * @param retryFactor the retry delay factor
     * @return this builder instance for fluent configuration
     */
    public BuildProcessorBuilder withRetryFactor(double retryFactor) {
        this.retryFactor = retryFactor;
        return this;
    }

    /**
     * Builds and starts the {@link BuildProcessor}, processing builds since the
     * given date and time.
     *
     * @param since the date and time from which to start processing builds
     * @return the built {@link BuildProcessor}
     */
    public BuildProcessor process(ZonedDateTime since) {
        return process(since.toInstant(), null);
    }

    /**
     * Builds and starts the {@link BuildProcessor}, processing builds since the
     * given date and time.
     *
     * @param since the date and time from which to start processing builds
     * @param query a query string to filter builds
     * @return the built {@link BuildProcessor}
     */
    public BuildProcessor process(ZonedDateTime since, String query) {
        return process(since.toInstant(), query);
    }

    /**
     * Builds and starts the {@link BuildProcessor}, processing builds since the
     * given date and time.
     *
     * @param since the date and time from which to start processing builds
     * @return the built {@link BuildProcessor}
     */
    public BuildProcessor process(OffsetDateTime since) {
        return process(since, null);
    }

    /**
     * Builds and starts the {@link BuildProcessor}, processing builds since the
     * given date and time.
     *
     * @param since the date and time from which to start processing builds
     * @param query a query string to filter builds
     * @return the built {@link BuildProcessor}
     */
    public BuildProcessor process(OffsetDateTime since, String query) {
        return process(since.toInstant(), query);
    }

    /**
     * Builds and starts the {@link BuildProcessor}, processing builds since the
     * given date at the beginning of the day using the system default timezone.
     *
     * @param since the date and time from which to start processing builds
     * @return the built {@link BuildProcessor}
     */
    public BuildProcessor process(LocalDate since) {
        return process(since, null);
    }

    /**
     * Builds and starts the {@link BuildProcessor}, processing builds since the
     * given date at the beginning of the day using the system default timezone.
     *
     * @param since the date and time from which to start processing builds
     * @param query a query string to filter builds
     * @return the built {@link BuildProcessor}
     */
    public BuildProcessor process(LocalDate since, String query) {
        return process(since.atStartOfDay(ZoneId.systemDefault()), query);
    }

    /**
     * Builds and starts the {@link BuildProcessor}, processing builds since the
     * given date and time using the system's default timezone.
     *
     * @param since the date and time from which to start processing builds
     * @return the built {@link BuildProcessor}
     */
    public BuildProcessor process(LocalDateTime since) {
        return process(since, null);
    }

    /**
     * Builds and starts the {@link BuildProcessor}, processing builds since the
     * given date and time using the system's default timezone.
     *
     * @param since the date and time from which to start processing builds
     * @param query a query string to filter builds
     * @return the built {@link BuildProcessor}
     */
    public BuildProcessor process(LocalDateTime since, String query) {
        return process(since.atZone(ZoneId.systemDefault()), query);
    }

    /**
     * Builds and starts the {@link BuildProcessor}, processing builds from now
     * since the provided duration. For example, passing
     * {@code Duration.ofDays(7)} will process all builds from the last 7 days.
     *
     * @param since the date and time from which to start processing builds
     * @return the built {@link BuildProcessor}
     */
    public BuildProcessor process(Duration since) {
        return process(now().minus(since), null);
    }

    /**
     * Builds and starts the {@link BuildProcessor}, processing builds from now
     * since the provided duration. For example, passing
     * {@code Duration.ofDays(7)} will process all builds from the last 7 days.
     *
     * @param since the date and time from which to start processing builds
     * @param query a query string to filter builds
     * @return the built {@link BuildProcessor}
     */
    public BuildProcessor process(Duration since, String query) {
        return process(now().minus(since), query);
    }

    /**
     * Builds and starts the {@link BuildProcessor}, processing builds since the
     * given instant.
     *
     * @param since the instant from which to start processing builds
     * @return the built {@link BuildProcessor}
     */
    public BuildProcessor process(Instant since) {
        return process(since, null);
    }

    /**
     * Builds and starts the {@link BuildProcessor}, processing builds since the
     * given instant.
     *
     * @param since the instant from which to start processing builds
     * @param query a query string to filter builds
     * @return the built {@link BuildProcessor}
     */
    public BuildProcessor process(Instant since, String query) {
        final var processor = build();
        processor.process(since, query);
        return processor;
    }

    /**
     * Constructs a new {@link BuildProcessor} instance with the configured
     * settings.
     *
     * @return a new {@link BuildProcessor} instance
     */
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
                processListeners
        );
    }

}
