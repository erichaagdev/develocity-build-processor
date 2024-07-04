package dev.erichaag.develocity.processing;

import dev.erichaag.develocity.processing.cache.ProcessorCache;
import dev.erichaag.develocity.processing.event.CachedBuildEvent;
import dev.erichaag.develocity.processing.event.DiscoveryFinishedEvent;
import dev.erichaag.develocity.processing.event.DiscoveryStartedEvent;
import dev.erichaag.develocity.processing.event.FetchedBuildEvent;
import dev.erichaag.develocity.processing.event.ProcessingFinishedEvent;
import dev.erichaag.develocity.processing.event.ProcessingStartedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A builder class for constructing a {@link ProcessListener} instance.
 *
 * <p>An instance of this builder can be obtained by calling
 * {@link ProcessListener#builder()}.
 *
 * <p>This builder allows you to create a {@link ProcessListener} by specifying
 * the desired callback methods. This can be convenient to quickly construct
 * listeners without needing to write a dedicated class that implements
 * {@link ProcessListener}.
 *
 * <p>The builder provides a fluent interface, allowing method calls to be
 * chained together, to register multiple callbacks for different processing
 * events.
 *
 * @see ProcessListener
 */
public final class ProcessListenerBuilder {

    private final List<ProcessListener> listeners = new ArrayList<>();

    ProcessListenerBuilder() {
    }

    /**
     * Constructs a new {@link ProcessListener} instance with the configured
     * callbacks.
     *
     * <p>The returned listener will invoke all registered listener methods when
     * their corresponding events are encountered.
     *
     * @return a new {@link ProcessListener} instance
     */
    public ProcessListener build() {
        return new ProcessListener() {

            @Override
            public void onCachedBuild(CachedBuildEvent event) {
                listeners.forEach(it -> it.onCachedBuild(event));
            }

            @Override
            public void onFetchedBuild(FetchedBuildEvent event) {
                listeners.forEach(it -> it.onFetchedBuild(event));
            }

            @Override
            public void onDiscoveryStarted(DiscoveryStartedEvent event) {
                listeners.forEach(it -> it.onDiscoveryStarted(event));
            }

            @Override
            public void onDiscoveryFinished(DiscoveryFinishedEvent event) {
                listeners.forEach(it -> it.onDiscoveryFinished(event));
            }

            @Override
            public void onProcessingStarted(ProcessingStartedEvent event) {
                listeners.forEach(it -> it.onProcessingStarted(event));
            }

            @Override
            public void onProcessingFinished(ProcessingFinishedEvent event) {
                listeners.forEach(it -> it.onProcessingFinished(event));
            }

        };
    }

    /**
     * Registers a callback function to be invoked when a build is retrieved
     * from a {@link ProcessorCache}.
     *
     * @param onCachedBuild the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public ProcessListenerBuilder onCachedBuild(Consumer<CachedBuildEvent> onCachedBuild) {
        listeners.add(new ProcessListener() {
            @Override
            public void onCachedBuild(CachedBuildEvent event) {
                onCachedBuild.accept(event);
            }
        });
        return this;
    }

    /**
     * Registers a callback function to be invoked when a build is fetched from
     * the API.
     *
     * @param onFetchedBuild the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public ProcessListenerBuilder onFetchedBuild(Consumer<FetchedBuildEvent> onFetchedBuild) {
        listeners.add(new ProcessListener() {
            @Override
            public void onFetchedBuild(FetchedBuildEvent event) {
                onFetchedBuild.accept(event);
            }
        });
        return this;
    }

    /**
     * Registers a callback function to be invoked when the discovery phase of
     * build processing starts.
     *
     * @param onDiscoveryStarted the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public ProcessListenerBuilder onDiscoveryStarted(Consumer<DiscoveryStartedEvent> onDiscoveryStarted) {
        listeners.add(new ProcessListener() {
            @Override
            public void onDiscoveryStarted(DiscoveryStartedEvent event) {
                onDiscoveryStarted.accept(event);
            }
        });
        return this;
    }

    /**
     * Registers a callback function to be invoked when the discovery phase of
     * build processing finishes.
     *
     * @param onDiscoveryFinished the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public ProcessListenerBuilder onDiscoveryFinished(Consumer<DiscoveryFinishedEvent> onDiscoveryFinished) {
        listeners.add(new ProcessListener() {
            @Override
            public void onDiscoveryFinished(DiscoveryFinishedEvent event) {
                onDiscoveryFinished.accept(event);
            }
        });
        return this;
    }

    /**
     * Registers a callback function to be invoked when build processing starts.
     *
     * @param onProcessingStarted the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public ProcessListenerBuilder onProcessingStarted(Consumer<ProcessingStartedEvent> onProcessingStarted) {
        listeners.add(new ProcessListener() {
            @Override
            public void onProcessingStarted(ProcessingStartedEvent event) {
                onProcessingStarted.accept(event);
            }
        });
        return this;
    }

    /**
     * Registers a callback function to be invoked when build processing finishes.
     *
     * @param onProcessingFinished the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public ProcessListenerBuilder onProcessingFinished(Consumer<ProcessingFinishedEvent> onProcessingFinished) {
        listeners.add(new ProcessListener() {
            @Override
            public void onProcessingFinished(ProcessingFinishedEvent event) {
                onProcessingFinished.accept(event);
            }
        });
        return this;
    }

}
