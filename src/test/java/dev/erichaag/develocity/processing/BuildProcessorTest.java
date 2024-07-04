package dev.erichaag.develocity.processing;

import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.DevelocityClientException;
import dev.erichaag.develocity.api.DevelocityClientStub;
import dev.erichaag.develocity.processing.cache.InMemoryCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static dev.erichaag.develocity.api.BuildModel.GRADLE_ATTRIBUTES;
import static dev.erichaag.develocity.api.Builds.bazel;
import static dev.erichaag.develocity.api.Builds.gradle;
import static dev.erichaag.develocity.api.Builds.gradleAttributes;
import static dev.erichaag.develocity.api.Builds.maven;
import static dev.erichaag.develocity.api.Builds.sbt;
import static java.time.Instant.ofEpochMilli;
import static java.util.Collections.emptyMap;
import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class BuildProcessorTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 100})
    void givenBuildsAvailable_whenProcessed_thenExpectedBuildsAreProcessed(int maxBuildsPerRequest) {
        buildProcessor().withMaxBuildsPerRequest(maxBuildsPerRequest).process(ofEpochMilli(0));
        assertOverallBuildsEncountered(12);
        assertGradleBuildsEncountered(6);
        assertMavenBuildsEncountered(2);
        assertBazelBuildsEncountered(2);
        assertSbtBuildsEncountered(2);
        assertAllBuildsEncounteredInOrder();

        buildProcessor().withMaxBuildsPerRequest(maxBuildsPerRequest).process(ofEpochMilli(500));
        assertOverallBuildsEncountered(8);
        assertGradleBuildsEncountered(5);
        assertMavenBuildsEncountered(1);
        assertBazelBuildsEncountered(1);
        assertSbtBuildsEncountered(1);

        buildProcessor().withMaxBuildsPerRequest(maxBuildsPerRequest).process(ofEpochMilli(1200));
        assertOverallBuildsEncountered(1);
        assertGradleBuildsEncountered(1);
        assertMavenBuildsEncountered(0);
        assertBazelBuildsEncountered(0);
        assertSbtBuildsEncountered(0);

        buildProcessor().withMaxBuildsPerRequest(maxBuildsPerRequest).build().process(ofEpochMilli(1201));
        assertOverallBuildsEncountered(0);
        assertGradleBuildsEncountered(0);
        assertMavenBuildsEncountered(0);
        assertBazelBuildsEncountered(0);
        assertSbtBuildsEncountered(0);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 100})
    void givenEmptyCache_whenProcessed_thenAllBuildsAreFetched(int maxBuildsPerRequest) {
        final var inMemoryCache = InMemoryCache.withDefaultSize();
        buildProcessor()
                .withMaxBuildsPerRequest(maxBuildsPerRequest)
                .withProcessorCache(inMemoryCache)
                .process(ofEpochMilli(0));
        assertDiscoveryStartedCalledOnce();
        assertDiscoveryFinishedCalledOnce();
        assertProcessingStartedCalledOnce();
        assertFetchedBuildCalled(12);
        assertCachedBuildCalled(0);
        assertProcessingFinishedCalledOnce();
        assertAllBuildsEncounteredInOrder();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 100})
    void givenSomeBuildsAreCached_whenProcessed_thenCachedBuildsAreReusedAndRemainingFetched(int maxBuildsPerRequest) {
        final var inMemoryCache = InMemoryCache.withDefaultSize();
        IntStream.of(0, 4, 7, 8, 10).mapToObj(builds::get).forEach(inMemoryCache::save);
        buildProcessor()
                .withRequiredBuildModels(GRADLE_ATTRIBUTES)
                .withMaxBuildsPerRequest(maxBuildsPerRequest)
                .withProcessorCache(inMemoryCache)
                .process(ofEpochMilli(0));
        assertDiscoveryStartedCalledOnce();
        assertDiscoveryFinishedCalledOnce();
        assertProcessingStartedCalledOnce();
        assertFetchedBuildCalled(7);
        assertCachedBuildCalled(5);
        assertProcessingFinishedCalledOnce();
        assertAllBuildsEncounteredInOrder();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 100})
    void givenAllBuildsAreCached_whenProcessed_thenAllCachedBuildsAreReusedAndNoneFetched(int maxBuildsPerRequest) {
        final var inMemoryCache = InMemoryCache.withDefaultSize();
        builds.forEach(inMemoryCache::save);
        buildProcessor()
                .withRequiredBuildModels(GRADLE_ATTRIBUTES)
                .withMaxBuildsPerRequest(maxBuildsPerRequest)
                .withProcessorCache(inMemoryCache)
                .process(ofEpochMilli(0));
        assertDiscoveryStartedCalledOnce();
        assertDiscoveryFinishedCalledOnce();
        assertProcessingStartedCalledOnce();
        assertFetchedBuildCalled(0);
        assertCachedBuildCalled(12);
        assertProcessingFinishedCalledOnce();
        assertAllBuildsEncounteredInOrder();
    }

    @Test
    void whenProcessingButSomeCallsTimesOut_thenAllBuildsCanStillBeProcessed() {
        final var builds = range(0, 100).mapToObj(i -> (Build) gradle("foobarbazqux" + i)).toList();
        final var develocity = DevelocityClientStub.withBuilds(builds);
        BuildProcessor.forClient(develocity)
                .withMaxBuildsPerRequest(10)
                .register(trackingBuildListener)
                .register(trackingProcessListener)
                .onProcessingStarted(__ -> develocity.thenThrow(5, newDevelocityClientException()))
                .process(ofEpochMilli(0));
        assertDiscoveryStartedCalledOnce();
        assertDiscoveryFinishedCalledOnce();
        assertProcessingStartedCalledOnce();
        assertOverallBuildsEncountered(100);
        assertFetchedBuildCalled(100);
        assertCachedBuildCalled(0);
        assertProcessingFinishedCalledOnce();
    }

    private final List<Build> builds = List.of(
            gradle("foobarbazqu12", it -> it.availableAt(1200L), gradleAttributes()),
            bazel ("foobarbazqu11", it -> it.availableAt(1100L)),
            gradle("foobarbazqu10", it -> it.availableAt(1000L), gradleAttributes()),
            sbt   ("foobarbazqux9", it -> it.availableAt(900L)),
            gradle("foobarbazqux8", it -> it.availableAt(800L), gradleAttributes()),
            maven ("foobarbazqux7", it -> it.availableAt(700L)),
            gradle("foobarbazqux6", it -> it.availableAt(600L), gradleAttributes()),
            gradle("foobarbazqux5", it -> it.availableAt(500L), gradleAttributes()),
            sbt   ("foobarbazqux4", it -> it.availableAt(400L)),
            bazel ("foobarbazqux3", it -> it.availableAt(300L)),
            maven ("foobarbazqux2", it -> it.availableAt(200L)),
            gradle("foobarbazqux1", it -> it.availableAt(100L), gradleAttributes())
    );

    private TrackingBuildListener trackingBuildListener = new TrackingBuildListener();
    private TrackingProcessListener trackingProcessListener = new TrackingProcessListener();

    private BuildProcessorBuilder buildProcessor() {
        this.trackingBuildListener = new TrackingBuildListener();
        this.trackingProcessListener = new TrackingProcessListener();
        return BuildProcessor.forClient(DevelocityClientStub.withBuilds(builds))
                .register(trackingBuildListener)
                .register(trackingProcessListener);
    }

    private void assertOverallBuildsEncountered(int expected) {
        assertBuildsEncountered(expected, trackingBuildListener.overallBuildsEncountered(), "overall");
    }

    private void assertGradleBuildsEncountered(int expected) {
        assertBuildsEncountered(expected, trackingBuildListener.gradleBuildsEncountered(), "Gradle");
    }

    private void assertMavenBuildsEncountered(int expected) {
        assertBuildsEncountered(expected, trackingBuildListener.mavenBuildsEncountered(), "Maven");
    }

    private void assertBazelBuildsEncountered(int expected) {
        assertBuildsEncountered(expected, trackingBuildListener.bazelBuildsEncountered(), "Bazel");
    }

    private void assertSbtBuildsEncountered(int expected) {
        assertBuildsEncountered(expected, trackingBuildListener.sbtBuildsEncountered(), "sbt");
    }

    private static void assertBuildsEncountered(int expected, int actual, String type) {
        assertEquals(expected, actual, () -> "Expected " + expected + " " + type + " " + pluralize("build", expected) + " but encountered " + actual);
    }

    private void assertDiscoveryStartedCalledOnce() {
        assertProcessListenerMethodCalled(1, trackingProcessListener.discoveryStartedCalled(), "discovery started");
    }

    private void assertDiscoveryFinishedCalledOnce() {
        assertProcessListenerMethodCalled(1, trackingProcessListener.discoveryFinishedCalled(), "discovery finished");
    }

    private void assertProcessingStartedCalledOnce() {
        assertProcessListenerMethodCalled(1, trackingProcessListener.processingStartedCalled(), "processing started");
    }

    private void assertCachedBuildCalled(int expected) {
        assertProcessListenerMethodCalled(expected, trackingProcessListener.cachedBuildCalled(), "cached build");
    }

    private void assertFetchedBuildCalled(int expected) {
        assertProcessListenerMethodCalled(expected, trackingProcessListener.fetchedBuildCalled(), "fetched build");
    }

    private void assertProcessingFinishedCalledOnce() {
        assertProcessListenerMethodCalled(1, trackingProcessListener.processingFinishedCalled(), "processing finished");
    }

    private static void assertProcessListenerMethodCalled(int expected, int actual, String type) {
        assertEquals(expected, actual, () -> "Expected " + expected + " " + type + " " + pluralize("call", expected) + " but encountered " + actual);
    }

    private void assertAllBuildsEncounteredInOrder() {
        assertEquals(builds, trackingBuildListener.encounteredBuilds());
    }

    private static String pluralize(String value, int i) {
        return i == 1 ? value : value + "s";
    }

    private static Supplier<DevelocityClientException> newDevelocityClientException() {
        return () -> new DevelocityClientException(URI.create("https://example.com"), 504, emptyMap());
    }

}
