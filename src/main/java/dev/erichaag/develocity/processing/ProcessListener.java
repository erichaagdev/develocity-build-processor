package dev.erichaag.develocity.processing;

import dev.erichaag.develocity.processing.event.CachedBuildEvent;
import dev.erichaag.develocity.processing.event.DiscoveryFinishedEvent;
import dev.erichaag.develocity.processing.event.DiscoveryStartedEvent;
import dev.erichaag.develocity.processing.event.FetchedBuildEvent;
import dev.erichaag.develocity.processing.event.ProcessingFinishedEvent;
import dev.erichaag.develocity.processing.event.ProcessingStartedEvent;

public interface ProcessListener {

    static ProcessListenerBuilder builder() {
        return new ProcessListenerBuilder();
    }

    default void onCachedBuild(CachedBuildEvent event) {
    }

    default void onFetchedBuild(FetchedBuildEvent event) {
    }

    default void onDiscoveryStarted(DiscoveryStartedEvent event) {
    }

    default void onDiscoveryFinished(DiscoveryFinishedEvent event) {
    }

    default void onProcessingStarted(ProcessingStartedEvent event) {
    }

    default void onProcessingFinished(ProcessingFinishedEvent event) {
    }

}
