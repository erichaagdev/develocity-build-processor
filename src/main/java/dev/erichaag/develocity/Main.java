package dev.erichaag.develocity;

import dev.erichaag.develocity.processing.BuildProcessor;
import dev.erichaag.develocity.processing.BuildListener;
import dev.erichaag.develocity.processing.cache.FileSystemBuildCache;
import dev.erichaag.develocity.api.HttpClientDevelocityClient;
import dev.erichaag.develocity.core.IncidentReport;
import dev.erichaag.develocity.core.IncidentTracker;

import java.nio.file.Path;

import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.time.temporal.ChronoUnit.SECONDS;

final class Main {

    public static void main(String[] args) {
        final var configuration = Configuration.load();
        final var develocity = new HttpClientDevelocityClient(configuration.serverUrl());
        final var incidentTracker = new IncidentTracker();
        final var fileSystemBuildCache = new FileSystemBuildCache();

        final var listener =  BuildListener.builder()
                .requiredBuildModels()
                .onGradleBuild(it -> System.out.println("Hello, Gradle!"))
                .onMavenBuild(it -> System.out.println("Hello, Maven!"))
                .onBazelBuild(it -> System.out.println("Hello, Bazel!"))
                .onSbtBuild(it -> System.out.println("Hello, sbt!"))
                .build();

        new BuildProcessor(develocity, fileSystemBuildCache, configuration.maxBuildsPerRequest())
                .register(new BuildProcessorProgressListener(configuration.serverUrl()))
                .register(incidentTracker)
                .register(listener)
                .process(configuration.since().toInstant());

        final var incidentReport = new IncidentReport(incidentTracker, configuration.since(), now(), configuration.excludeAbovePercentile());
        try (var archive = getArchive(incidentReport)) {
            archive.write("ci_overall.csv", incidentReport.ciOverall());
            archive.write("ci_per_project.csv", incidentReport.ciPerProject());
            archive.write("ci_per_project_requested.csv", incidentReport.ciPerProjectRequested());
            archive.write("local_overall.csv", incidentReport.localOverall());
            archive.write("local_per_project.csv", incidentReport.localPerProject());
            archive.write("local_per_user.csv", incidentReport.localPerUser());
            archive.write("local_per_user_project.csv", incidentReport.localPerUserProject());
            archive.write("overall.csv", incidentReport.overall());
            archive.write("mean_trends_ci_overall.csv", incidentReport.meanTrendsCiOverall());
            archive.write("mean_trends_ci_per_project.csv", incidentReport.meanTrendsCiPerProject());
            archive.write("mean_trends_ci_per_project_requested.csv", incidentReport.meanTrendsCiPerProjectRequested());
            archive.write("mean_trends_local_overall.csv", incidentReport.meanTrendsLocalOverall());
            archive.write("mean_trends_local_per_project.csv", incidentReport.meanTrendsLocalPerProject());
            archive.write("mean_trends_local_per_user.csv", incidentReport.meanTrendsLocalPerUser());
            archive.write("mean_trends_local_per_user_project.csv", incidentReport.meanTrendsLocalPerUserProject());
            archive.write("mean_trends_overall.csv", incidentReport.meanTrendsOverall());
            archive.write("p50_trends_ci_overall.csv", incidentReport.p50TrendsCiOverall());
            archive.write("p50_trends_ci_per_project.csv", incidentReport.p50TrendsCiPerProject());
            archive.write("p50_trends_ci_per_project_requested.csv", incidentReport.p50TrendsCiPerProjectRequested());
            archive.write("p50_trends_local_overall.csv", incidentReport.p50TrendsLocalOverall());
            archive.write("p50_trends_local_per_project.csv", incidentReport.p50TrendsLocalPerProject());
            archive.write("p50_trends_local_per_user.csv", incidentReport.p50TrendsLocalPerUser());
            archive.write("p50_trends_local_per_user_project.csv", incidentReport.p50TrendsLocalPerUserProject());
            archive.write("p50_trends_overall.csv", incidentReport.p50TrendsOverall());
            archive.write("p95_trends_ci_overall.csv", incidentReport.p95TrendsCiOverall());
            archive.write("p95_trends_ci_per_project.csv", incidentReport.p95TrendsCiPerProject());
            archive.write("p95_trends_ci_per_project_requested.csv", incidentReport.p95TrendsCiPerProjectRequested());
            archive.write("p95_trends_local_overall.csv", incidentReport.p95TrendsLocalOverall());
            archive.write("p95_trends_local_per_project.csv", incidentReport.p95TrendsLocalPerProject());
            archive.write("p95_trends_local_per_user.csv", incidentReport.p95TrendsLocalPerUser());
            archive.write("p95_trends_local_per_user_project.csv", incidentReport.p95TrendsLocalPerUserProject());
            archive.write("p95_trends_overall.csv", incidentReport.p95TrendsOverall());
            archive.write("failures_trends_ci_overall.csv", incidentReport.failuresTrendsCiOverall());
            archive.write("failures_trends_ci_per_project.csv", incidentReport.failuresTrendsCiPerProject());
            archive.write("failures_trends_ci_per_project_requested.csv", incidentReport.failuresTrendsCiPerProjectRequested());
            archive.write("failures_trends_local_overall.csv", incidentReport.failuresTrendsLocalOverall());
            archive.write("failures_trends_local_per_project.csv", incidentReport.failuresTrendsLocalPerProject());
            archive.write("failures_trends_local_per_user.csv", incidentReport.failuresTrendsLocalPerUser());
            archive.write("failures_trends_local_per_user_project.csv", incidentReport.failuresTrendsLocalPerUserProject());
            archive.write("failures_trends_overall.csv", incidentReport.failuresTrendsOverall());
            archive.create();
            System.out.println("\nCreated report archive at " + archive.getPath().toAbsolutePath());
        }
    }

    private static Archive getArchive(IncidentReport incidentReport) {
        final var filename = "reports_"
                + incidentReport.until().truncatedTo(SECONDS).format(ofPattern("uuuuMMdd_HHmmss"))
                + ".zip";
        return new Archive(Path.of("reports").resolve(filename));
    }

}
