package dev.erichaag.develocity.processing;

import dev.erichaag.develocity.processing.event.CachedBuildEvent;
import dev.erichaag.develocity.processing.event.DiscoveryFinishedEvent;
import dev.erichaag.develocity.processing.event.DiscoveryStartedEvent;
import dev.erichaag.develocity.processing.event.FetchedBuildEvent;
import dev.erichaag.develocity.processing.event.ProcessingFinishedEvent;
import dev.erichaag.develocity.processing.event.ProcessingStartedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class ProcessListenerBuilder {

    private final List<ProcessListener> listeners = new ArrayList<>();

    ProcessListenerBuilder() {
    }

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

    public ProcessListenerBuilder onCachedBuild(Consumer<CachedBuildEvent> onCachedBuild) {
        listeners.add(new ProcessListener() {
            @Override
            public void onCachedBuild(CachedBuildEvent event) {
                onCachedBuild.accept(event);
            }
        });
        return this;
    }

    public ProcessListenerBuilder onFetchedBuild(Consumer<FetchedBuildEvent> onFetchedBuild) {
        listeners.add(new ProcessListener() {
            @Override
            public void onFetchedBuild(FetchedBuildEvent event) {
                onFetchedBuild.accept(event);
            }
        });
        return this;
    }

    public ProcessListenerBuilder onDiscoveryStarted(Consumer<DiscoveryStartedEvent> onDiscoveryStarted) {
        listeners.add(new ProcessListener() {
            @Override
            public void onDiscoveryStarted(DiscoveryStartedEvent event) {
                onDiscoveryStarted.accept(event);
            }
        });
        return this;
    }

    public ProcessListenerBuilder onDiscoveryFinished(Consumer<DiscoveryFinishedEvent> onDiscoveryFinished) {
        listeners.add(new ProcessListener() {
            @Override
            public void onDiscoveryFinished(DiscoveryFinishedEvent event) {
                onDiscoveryFinished.accept(event);
            }
        });
        return this;
    }

    public ProcessListenerBuilder onProcessingStarted(Consumer<ProcessingStartedEvent> onProcessingStarted) {
        listeners.add(new ProcessListener() {
            @Override
            public void onProcessingStarted(ProcessingStartedEvent event) {
                onProcessingStarted.accept(event);
            }
        });
        return this;
    }

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
