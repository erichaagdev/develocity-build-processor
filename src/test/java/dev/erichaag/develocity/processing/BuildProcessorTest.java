package dev.erichaag.develocity.processing;

import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.DevelocityClient;
import dev.erichaag.develocity.api.DevelocityClientStub;
import dev.erichaag.develocity.processing.cache.InMemoryCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.erichaag.develocity.api.Builds.bazel;
import static dev.erichaag.develocity.api.Builds.gradle;
import static dev.erichaag.develocity.api.Builds.maven;
import static dev.erichaag.develocity.api.Builds.sbt;
import static java.time.Instant.ofEpochMilli;
import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class BuildProcessorTest {

    private final AtomicInteger overallBuildsEncountered = new AtomicInteger();
    private final AtomicInteger gradleBuildsEncountered = new AtomicInteger();
    private final AtomicInteger mavenBuildsEncountered = new AtomicInteger();
    private final AtomicInteger bazelBuildsEncountered = new AtomicInteger();
    private final AtomicInteger sbtBuildsEncountered = new AtomicInteger();

    private final AtomicInteger discoveryStartedCalled = new AtomicInteger();
    private final AtomicInteger discoveryFinishedCalled = new AtomicInteger();
    private final AtomicInteger processingStartedCalled = new AtomicInteger();
    private final AtomicInteger cachedBuildCalled = new AtomicInteger();
    private final AtomicInteger fetchedBuildCalled = new AtomicInteger();
    private final AtomicInteger processingFinishedCalled = new AtomicInteger();

    private final BuildListener countingBuildListener = BuildListener.builder()
            .onBuild(__ -> overallBuildsEncountered.incrementAndGet())
            .onGradleBuild(__ -> gradleBuildsEncountered.incrementAndGet())
            .onMavenBuild(__ -> mavenBuildsEncountered.incrementAndGet())
            .onBazelBuild(__ -> bazelBuildsEncountered.incrementAndGet())
            .onSbtBuild(__ -> sbtBuildsEncountered.incrementAndGet())
            .build();

    private final ProcessListener countingProcessListener = ProcessListener.builder()
            .onDiscoveryStarted(__ -> discoveryStartedCalled.incrementAndGet())
            .onDiscoveryFinished(__ -> discoveryFinishedCalled.incrementAndGet())
            .onProcessingStarted(__ -> processingStartedCalled.incrementAndGet())
            .onCachedBuild(__ -> cachedBuildCalled.incrementAndGet())
            .onFetchedBuild(__ -> fetchedBuildCalled.incrementAndGet())
            .onProcessingFinished(__ -> processingFinishedCalled.incrementAndGet())
            .build();

    private final List<Build> builds = List.of(
            gradle(it -> it.availableAt(100L)),
            maven (it -> it.availableAt(200L)),
            bazel (it -> it.availableAt(300L)),
            sbt   (it -> it.availableAt(400L)),
            gradle(it -> it.availableAt(500L)),
            gradle(it -> it.availableAt(600L)),
            maven (it -> it.availableAt(700L)),
            gradle(it -> it.availableAt(800L)),
            sbt   (it -> it.availableAt(900L)),
            gradle(it -> it.availableAt(1000L)),
            bazel (it -> it.availableAt(1100L)),
            gradle(it -> it.availableAt(1200L))
    );

    private final DevelocityClient develocity = DevelocityClientStub.withBuilds(builds);

    private final BuildProcessorBuilder buildProcessor = BuildProcessor.forClient(develocity)
            .register(countingBuildListener)
            .register(countingProcessListener);

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 100})
    void givenBuildsSince0_whenProcessed_thenBuildsSince0AreProcessed(int maxBuildsPerRequest) {
        buildProcessor
                .withMaxBuildsPerRequest(maxBuildsPerRequest)
                .build()
                .process(ofEpochMilli(0));
        assertOverallBuildsEncountered(12);
        assertGradleBuildsEncountered(6);
        assertMavenBuildsEncountered(2);
        assertBazelBuildsEncountered(2);
        assertSbtBuildsEncountered(2);
    }

    @Test
    void givenBuildsSince500_whenProcessed_thenBuildsSince500AreProcessed() {
        buildProcessor
                .withMaxBuildsPerRequest(1)
                .build()
                .process(ofEpochMilli(500));
        assertOverallBuildsEncountered(8);
        assertGradleBuildsEncountered(5);
        assertMavenBuildsEncountered(1);
        assertBazelBuildsEncountered(1);
        assertSbtBuildsEncountered(1);
    }

    @Test
    void givenBuildsSince1200_whenProcessed_thenBuildsSince1200AreProcessed() {
        buildProcessor
                .withMaxBuildsPerRequest(1)
                .build()
                .process(ofEpochMilli(1200));
        assertOverallBuildsEncountered(1);
        assertGradleBuildsEncountered(1);
        assertMavenBuildsEncountered(0);
        assertBazelBuildsEncountered(0);
        assertSbtBuildsEncountered(0);
    }

    @Test
    void givenBuildsSince1201_whenProcessed_thenNoBuildsAreProcessed() {
        buildProcessor
                .withMaxBuildsPerRequest(1)
                .build()
                .process(ofEpochMilli(1201));
        assertOverallBuildsEncountered(0);
        assertGradleBuildsEncountered(0);
        assertMavenBuildsEncountered(0);
        assertBazelBuildsEncountered(0);
        assertSbtBuildsEncountered(0);
    }

    @Test
    void givenEmptyCache_whenProcessed_thenAllBuildsAreFetched() {
        buildProcessor
                .withMaxBuildsPerRequest(1)
                .build()
                .process(ofEpochMilli(0));
        assertDiscoveryStartedCalledOnce();
        assertDiscoveryFinishedCalledOnce();
        assertProcessingStartedCalledOnce();
        assertFetchedBuildCalled(12);
        assertProcessingFinishedCalledOnce();
    }

    @Test
    void givenSomeBuildsAreCached_whenProcessed_thenCachedBuildsAreReusedAndRemainingFetched() {
        final var inMemoryCache = InMemoryCache.withDefaultSize();
        range(0, 3).forEach(i -> inMemoryCache.save(builds.get(i)));
        inMemoryCache.save(builds.get(0));
        inMemoryCache.save(builds.get(1));
        inMemoryCache.save(builds.get(2));
        buildProcessor
                .withMaxBuildsPerRequest(1)
                .withProcessorCache(inMemoryCache)
                .build()
                .process(ofEpochMilli(0));
        assertDiscoveryStartedCalledOnce();
        assertDiscoveryFinishedCalledOnce();
        assertProcessingStartedCalledOnce();
        assertFetchedBuildCalled(9);
        assertCachedBuildCalled(3);
        assertProcessingFinishedCalledOnce();
    }

    @Test
    void givenAllBuildsAreCached_whenProcessed_thenAllCachedBuildsAreReusedAndNoneFetched() {
        final var inMemoryCache = InMemoryCache.withDefaultSize();
        builds.forEach(inMemoryCache::save);
        buildProcessor
                .withMaxBuildsPerRequest(1)
                .withProcessorCache(inMemoryCache)
                .build()
                .process(ofEpochMilli(0));
        assertDiscoveryStartedCalledOnce();
        assertDiscoveryFinishedCalledOnce();
        assertProcessingStartedCalledOnce();
        assertFetchedBuildCalled(0);
        assertCachedBuildCalled(12);
        assertProcessingFinishedCalledOnce();
    }

    private void assertOverallBuildsEncountered(int expected) {
        assertBuildsEncountered(expected, overallBuildsEncountered, "overall");
    }

    private void assertGradleBuildsEncountered(int expected) {
        assertBuildsEncountered(expected, gradleBuildsEncountered, "Gradle");
    }

    private void assertMavenBuildsEncountered(int expected) {
        assertBuildsEncountered(expected, mavenBuildsEncountered, "Maven");
    }

    private void assertBazelBuildsEncountered(int expected) {
        assertBuildsEncountered(expected, bazelBuildsEncountered, "Bazel");
    }

    private void assertSbtBuildsEncountered(int expected) {
        assertBuildsEncountered(expected, sbtBuildsEncountered, "sbt");
    }

    private static void assertBuildsEncountered(int expected, AtomicInteger actual, String type) {
        assertEquals(expected, actual.get(), () -> "Expected " + expected + " " + type + " " + pluralize("build", expected) + " but encountered " + actual.get());
    }

    private void assertDiscoveryStartedCalledOnce() {
        assertProcessListenerMethodCalled(1, discoveryStartedCalled, "discovery started");
    }

    private void assertDiscoveryFinishedCalledOnce() {
        assertProcessListenerMethodCalled(1, discoveryFinishedCalled, "discovery finished");
    }

    private void assertProcessingStartedCalledOnce() {
        assertProcessListenerMethodCalled(1, processingStartedCalled, "processing started");
    }

    private void assertCachedBuildCalled(int expected) {
        assertProcessListenerMethodCalled(expected, cachedBuildCalled, "cached build");
    }

    private void assertFetchedBuildCalled(int expected) {
        assertProcessListenerMethodCalled(expected, fetchedBuildCalled, "fetched build");
    }

    private void assertProcessingFinishedCalledOnce() {
        assertProcessListenerMethodCalled(1, processingFinishedCalled, "processing finished");
    }

    private static void assertProcessListenerMethodCalled(int expected, AtomicInteger actual, String type) {
        assertEquals(expected, actual.get(), () -> "Expected " + expected + " " + type + " " + pluralize("call", expected) + " but encountered " + actual.get());
    }

    private static String pluralize(String value, int i) {
        return i == 1 ? value : value + "s";
    }

}
