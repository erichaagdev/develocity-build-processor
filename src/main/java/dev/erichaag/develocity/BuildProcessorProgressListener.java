package dev.erichaag.develocity;

import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.BuildProcessorListener;
import dev.erichaag.develocity.api.CachedBuildEvent;
import dev.erichaag.develocity.api.DiscoveryFinishedEvent;
import dev.erichaag.develocity.api.DiscoveryStartedEvent;
import dev.erichaag.develocity.api.FetchedBuildEvent;
import dev.erichaag.develocity.api.ProcessingFinishedEvent;
import dev.erichaag.develocity.api.ProcessingStartedEvent;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static dev.erichaag.develocity.core.Durations.format;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static java.time.Instant.now;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.time.temporal.ChronoUnit.SECONDS;

final class BuildProcessorProgressListener implements BuildProcessorListener {

    private static final Duration progressInterval = ofSeconds(10);
    private static final DateTimeFormatter formatter = ofPattern("LLL d uuuu HH:mm z");

    private final URI serverUrl;

    private Thread progressThread;
    private AtomicBoolean inProgress;
    private Instant intervalStartedOn;
    private Instant processingStartedOn;
    private int total = 0;
    private int processed = 0;
    private int cached = 0;
    private int fetched = 0;

    public BuildProcessorProgressListener(URI serverUrl) {
        this.serverUrl = serverUrl;
    }

    @Override
    public void onDiscoveryStarted(DiscoveryStartedEvent event) {
        print("Processing builds from %s%n", serverUrl);
        print("Discovering builds since %s%n", event.since().format(formatter));
    }

    @Override
    public void onDiscoveryFinished(DiscoveryFinishedEvent event) {
        total = event.builds().size();
        print("Discovered %s builds%n", total);
    }

    @Override
    public void onProcessingStarted(ProcessingStartedEvent event) {
        print("Processing builds%n");
        processingStartedOn = now();
        intervalStartedOn = now();
        startProgressThread();
    }

    @Override
    public void onProcessingFinished(ProcessingFinishedEvent event) {
        finishProgressThread();
        print("%d processed, 100%% complete, %d builds from cache, %d builds fetched%n", processed, cached, fetched);
    }

    @Override
    public void onBuild(Build build) {
        processed++;
    }

    @Override
    public void onCachedBuild(CachedBuildEvent event) {
        cached++;
    }

    @Override
    public void onFetchedBuild(FetchedBuildEvent event) {
        fetched++;
    }

    private void printProgress(Instant now) {
        final var fetchingRatePerSecond = fetched / (double) Duration.between(processingStartedOn, now).toSeconds();
        final var estimatedTimeRemaining = Duration.ofSeconds((long) ((total - processed) / fetchingRatePerSecond));
        print("%d processed, %d%% complete, %d from cache, %d fetched, %s remaining%n", processed, (processed * 100) / total, cached, fetched, fetched > 0 ? format(estimatedTimeRemaining) : "--");
    }

    private void startProgressThread() {
        inProgress = new AtomicBoolean(true);
        progressThread = Thread.startVirtualThread(() -> {
            while (inProgress.get()) {
                final var now = now();
                if (Duration.between(intervalStartedOn, now).compareTo(progressInterval) >= 0) {
                    intervalStartedOn = now;
                    printProgress(now);
                }
                sleep(ofMillis(100));
            }
        });
    }

    private void finishProgressThread() {
        inProgress.set(false);
        try {
            progressThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sleep(Duration duration) {
        try {
            TimeUnit.MILLISECONDS.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void print(String format, Object... args) {
        System.out.printf(ISO_LOCAL_TIME.format(LocalTime.now().truncatedTo(SECONDS)) + " - " + format, args);
    }

}
