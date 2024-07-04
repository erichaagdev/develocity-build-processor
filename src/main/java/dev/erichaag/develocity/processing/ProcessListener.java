package dev.erichaag.develocity.processing;

import dev.erichaag.develocity.processing.cache.ProcessorCache;
import dev.erichaag.develocity.processing.event.CachedBuildEvent;
import dev.erichaag.develocity.processing.event.DiscoveryFinishedEvent;
import dev.erichaag.develocity.processing.event.DiscoveryStartedEvent;
import dev.erichaag.develocity.processing.event.FetchedBuildEvent;
import dev.erichaag.develocity.processing.event.ProcessingFinishedEvent;
import dev.erichaag.develocity.processing.event.ProcessingStartedEvent;

/**
 * A listener interface for receiving notifications about events during the
 * build processing lifecycle.
 *
 * <p>Implement this interface to receive callbacks while a
 * {@link BuildProcessor} is running, such as cache interactions,
 * build fetching, discovery, and overall processing start/finish. A
 * {@link ProcessListenerBuilder} provides a convenient way to create custom
 * listener instances without needing to write a dedicated class that implements
 * this interface
 *
 * <p>Note that it can sometimes be useful for an implementation to also
 * implement {@link BuildListener} if it needs to be notified when builds are
 * encountered.
 *
 * <p>By default, all methods have empty implementations. Implement only the
 * methods relevant to your use case.
 *
 * @see BuildListener
 * @see ProcessListenerBuilder
 */
public interface ProcessListener {

    /**
     * Creates a new {@link ProcessListenerBuilder} to construct a
     * {@link ProcessListener} instance.
     *
     * <p>This can be convenient to quickly construct listeners without needing
     * to write a dedicated class that implements this interface.
     *
     * @return a new builder instance
     */
    static ProcessListenerBuilder builder() {
        return new ProcessListenerBuilder();
    }

    /**
     * Callback method invoked when a build is retrieved from a
     * {@link ProcessorCache}.
     *
     * @param event details about the cached build event
     */
    default void onCachedBuild(CachedBuildEvent event) {
    }

    /**
     * Callback method invoked when a build is fetched from the API.
     *
     * @param event details about the fetched build event
     */
    default void onFetchedBuild(FetchedBuildEvent event) {
    }

    /**
     * Callback method invoked when the discovery phase of build processing
     * starts.
     *
     * @param event details about the discovery started event
     */
    default void onDiscoveryStarted(DiscoveryStartedEvent event) {
    }

    /**
     * Callback method invoked when the discovery phase of build processing
     * finishes.
     *
     * @param event details about the discovery finished event
     */
    default void onDiscoveryFinished(DiscoveryFinishedEvent event) {
    }

    /**
     * Callback method invoked when build processing starts.
     *
     * @param event details about the processing started event
     */
    default void onProcessingStarted(ProcessingStartedEvent event) {
    }

    /**
     * Callback method invoked when build processing finishes.
     *
     * @param event details about the processing finished event
     */
    default void onProcessingFinished(ProcessingFinishedEvent event) {
    }

}
