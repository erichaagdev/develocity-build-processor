package dev.erichaag.develocity.processing;

import dev.erichaag.develocity.processing.event.CachedBuildEvent;
import dev.erichaag.develocity.processing.event.DiscoveryFinishedEvent;
import dev.erichaag.develocity.processing.event.DiscoveryStartedEvent;
import dev.erichaag.develocity.processing.event.FetchedBuildEvent;
import dev.erichaag.develocity.processing.event.ProcessingFinishedEvent;
import dev.erichaag.develocity.processing.event.ProcessingStartedEvent;

import java.util.concurrent.atomic.AtomicInteger;

public final class TrackingProcessListener implements ProcessListener {

    private final AtomicInteger discoveryStartedCalled = new AtomicInteger();
    private final AtomicInteger discoveryFinishedCalled = new AtomicInteger();
    private final AtomicInteger processingStartedCalled = new AtomicInteger();
    private final AtomicInteger cachedBuildCalled = new AtomicInteger();
    private final AtomicInteger fetchedBuildCalled = new AtomicInteger();
    private final AtomicInteger processingFinishedCalled = new AtomicInteger();

    @Override
    public void onDiscoveryStarted(DiscoveryStartedEvent event) {
        discoveryStartedCalled.incrementAndGet();
    }

    @Override
    public void onDiscoveryFinished(DiscoveryFinishedEvent event) {
        discoveryFinishedCalled.incrementAndGet();
    }

    @Override
    public void onProcessingStarted(ProcessingStartedEvent event) {
        processingStartedCalled.incrementAndGet();
    }

    @Override
    public void onCachedBuild(CachedBuildEvent event) {
        cachedBuildCalled.incrementAndGet();
    }

    @Override
    public void onFetchedBuild(FetchedBuildEvent event) {
        fetchedBuildCalled.incrementAndGet();
    }

    @Override
    public void onProcessingFinished(ProcessingFinishedEvent event) {
        processingFinishedCalled.incrementAndGet();
    }

    public int discoveryStartedCalled() {
        return discoveryStartedCalled.get();
    }

    public int discoveryFinishedCalled() {
        return discoveryFinishedCalled.get();
    }

    public int processingStartedCalled() {
        return processingStartedCalled.get();
    }

    public int cachedBuildCalled() {
        return cachedBuildCalled.get();
    }

    public int fetchedBuildCalled() {
        return fetchedBuildCalled.get();
    }

    public int processingFinishedCalled() {
        return processingFinishedCalled.get();
    }

}
