package dev.erichaag.develocity.core;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.lang.String.valueOf;
import static java.time.Duration.ZERO;
import static java.time.Duration.between;
import static java.time.Duration.ofMillis;
import static java.time.ZoneId.systemDefault;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;
import static java.util.Map.Entry.comparingByKey;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.iterate;

@SuppressWarnings("Convert2MethodRef")
public final class IncidentReport {

    private final ZonedDateTime since;
    private final ZonedDateTime until;
    private final Resolution resolution;
    private final List<Incident> incidents;
    private final Map<ZonedDateTime, List<Incident>> incidentsPartitioned;

    private String ciOverall;
    private String ciPerProject;
    private String ciPerProjectRequested;
    private String localOverall;
    private String localPerProject;
    private String localPerUser;
    private String localPerUserProject;
    private String overall;

    private String meanTrendsCiOverall;
    private String meanTrendsCiPerProject;
    private String meanTrendsCiPerProjectRequested;
    private String meanTrendsLocalOverall;
    private String meanTrendsLocalPerProject;
    private String meanTrendsLocalPerUser;
    private String meanTrendsLocalPerUserProject;
    private String meanTrendsOverall;

    private String p50TrendsCiOverall;
    private String p50TrendsCiPerProject;
    private String p50TrendsCiPerProjectRequested;
    private String p50TrendsLocalOverall;
    private String p50TrendsLocalPerProject;
    private String p50TrendsLocalPerUser;
    private String p50TrendsLocalPerUserProject;
    private String p50TrendsOverall;

    private String p95TrendsCiOverall;
    private String p95TrendsCiPerProject;
    private String p95TrendsCiPerProjectRequested;
    private String p95TrendsLocalOverall;
    private String p95TrendsLocalPerProject;
    private String p95TrendsLocalPerUser;
    private String p95TrendsLocalPerUserProject;
    private String p95TrendsOverall;

    private String failuresTrendsCiOverall;
    private String failuresTrendsCiPerProject;
    private String failuresTrendsCiPerProjectRequested;
    private String failuresTrendsLocalOverall;
    private String failuresTrendsLocalPerProject;
    private String failuresTrendsLocalPerUser;
    private String failuresTrendsLocalPerUserProject;
    private String failuresTrendsOverall;

    public IncidentReport(IncidentTracker tracker, ZonedDateTime since, ZonedDateTime until, Integer excludeAbovePercentile) {
        this.since = since;
        this.until = until;
        this.resolution = Resolution.from(between(since, until));
        this.incidents = sortChronologicallyAndApplyExclusions(tracker.getResolvedIncidents(), excludeAbovePercentile);
        this.incidentsPartitioned = partition(incidents);
        initializeCiOverall();
        initializeCiPerProject();
        initializeCiPerProjectRequested();
        initializeLocalOverall();
        initializeLocalPerProject();
        initializeLocalPerUser();
        initializeLocalPerUserProject();
        initializeOverall();
    }

    private void initializeOverall() {
        this.overall = computeOverall(allBuilds());
        this.meanTrendsOverall = computeOverallTrends(allBuilds(), it -> format(it.getMean()));
        this.p50TrendsOverall = computeOverallTrends(allBuilds(), it -> format(it.getPercentile(50)));
        this.p95TrendsOverall = computeOverallTrends(allBuilds(), it -> format(it.getPercentile(95)));
        this.failuresTrendsOverall = computeOverallTrends(allBuilds(), it -> it.getN());
    }

    private void initializeCiOverall() {
        this.ciOverall = computeOverall(onlyCiBuilds());
        this.meanTrendsCiOverall = computeOverallTrends(onlyCiBuilds(), it -> format(it.getMean()));
        this.p50TrendsCiOverall = computeOverallTrends(onlyCiBuilds(), it -> format(it.getPercentile(50)));
        this.p95TrendsCiOverall = computeOverallTrends(onlyCiBuilds(), it -> format(it.getPercentile(95)));
        this.failuresTrendsCiOverall = computeOverallTrends(onlyCiBuilds(), it -> it.getN());
    }

    private void initializeLocalOverall() {
        this.localOverall = computeOverall(onlyLocalBuilds());
        this.meanTrendsLocalOverall = computeOverallTrends(onlyLocalBuilds(), it -> format(it.getMean()));
        this.p50TrendsLocalOverall = computeOverallTrends(onlyLocalBuilds(), it -> format(it.getPercentile(50)));
        this.p95TrendsLocalOverall = computeOverallTrends(onlyLocalBuilds(), it -> format(it.getPercentile(95)));
        this.failuresTrendsLocalOverall = computeOverallTrends(onlyLocalBuilds(), it -> it.getN());
    }

    private void initializeCiPerProject() {
        record Key(String projectName) { }
        final var headers = List.of("Project");
        final var groupingBy = groupBy(it -> new Key(it.projectName()));
        final var keyExtractor = extractKey(Key.class, it -> Stream.of(it.projectName()));
        this.ciPerProject = computeGroupedBy(headers, onlyCiBuilds(), groupingBy, keyExtractor);
        this.meanTrendsCiPerProject = computeGroupedByTrends(headers, onlyCiBuilds(), it -> format(it.getMean()), groupingBy, keyExtractor);
        this.p50TrendsCiPerProject = computeGroupedByTrends(headers, onlyCiBuilds(), it -> format(it.getPercentile(50)), groupingBy, keyExtractor);
        this.p95TrendsCiPerProject = computeGroupedByTrends(headers, onlyCiBuilds(), it -> format(it.getPercentile(95)), groupingBy, keyExtractor);
        this.failuresTrendsCiPerProject = computeGroupedByTrends(headers, onlyCiBuilds(), it -> it.getN(), groupingBy, keyExtractor);
    }

    private void initializeCiPerProjectRequested() {
        record Key(String projectName, Collection<String> requested) { }
        final var headers = List.of("Project", "Requested tasks/goals");
        final var groupingBy = groupBy(it -> new Key(it.projectName(), it.requested()));
        final var keyExtractor = extractKey(Key.class, it -> Stream.of(it.projectName(), String.join(" ", it.requested())));
        this.ciPerProjectRequested = computeGroupedBy(headers, onlyCiBuilds(), groupingBy, keyExtractor);
        this.meanTrendsCiPerProjectRequested = computeGroupedByTrends(headers, onlyCiBuilds(), it -> format(it.getMean()), groupingBy, keyExtractor);
        this.p50TrendsCiPerProjectRequested = computeGroupedByTrends(headers, onlyCiBuilds(), it -> format(it.getPercentile(50)), groupingBy, keyExtractor);
        this.p95TrendsCiPerProjectRequested = computeGroupedByTrends(headers, onlyCiBuilds(), it -> format(it.getPercentile(95)), groupingBy, keyExtractor);
        this.failuresTrendsCiPerProjectRequested = computeGroupedByTrends(headers, onlyCiBuilds(), it -> it.getN(), groupingBy, keyExtractor);
    }

    private void initializeLocalPerUser() {
        record Key(String username) { }
        final var headers = List.of("User");
        final var groupingBy = groupBy(it -> new Key(it.username()));
        final var keyExtractor = extractKey(Key.class, it -> Stream.of(it.username()));
        this.localPerUser = computeGroupedBy(headers, onlyLocalBuilds(), groupingBy, keyExtractor);
        this.meanTrendsLocalPerUser = computeGroupedByTrends(headers, onlyLocalBuilds(), it -> format(it.getMean()), groupingBy, keyExtractor);
        this.p50TrendsLocalPerUser = computeGroupedByTrends(headers, onlyLocalBuilds(), it -> format(it.getPercentile(50)), groupingBy, keyExtractor);
        this.p95TrendsLocalPerUser = computeGroupedByTrends(headers, onlyLocalBuilds(), it -> format(it.getPercentile(95)), groupingBy, keyExtractor);
        this.failuresTrendsLocalPerUser = computeGroupedByTrends(headers, onlyLocalBuilds(), it -> it.getN(), groupingBy, keyExtractor);
    }

    private void initializeLocalPerProject() {
        record Key(String projectName) { }
        final var headers = List.of("Project");
        final var groupingBy = groupBy(it -> new Key(it.projectName()));
        final var keyExtractor = extractKey(Key.class, it -> Stream.of(it.projectName()));
        this.localPerProject = computeGroupedBy(headers, onlyLocalBuilds(), groupingBy, keyExtractor);
        this.meanTrendsLocalPerProject = computeGroupedByTrends(headers, onlyLocalBuilds(), it -> format(it.getMean()), groupingBy, keyExtractor);
        this.p50TrendsLocalPerProject = computeGroupedByTrends(headers, onlyLocalBuilds(), it -> format(it.getPercentile(50)), groupingBy, keyExtractor);
        this.p95TrendsLocalPerProject = computeGroupedByTrends(headers, onlyLocalBuilds(), it -> format(it.getPercentile(95)), groupingBy, keyExtractor);
        this.failuresTrendsLocalPerProject = computeGroupedByTrends(headers, onlyLocalBuilds(), it -> it.getN(), groupingBy, keyExtractor);
    }

    private void initializeLocalPerUserProject() {
        record Key(String username, String projectName) { }
        final var headers = List.of("User", "Project");
        final var groupingBy = groupBy(it -> new Key(it.username(), it.projectName()));
        final var keyExtractor = extractKey(Key.class, it -> Stream.of(it.username(), it.projectName()));
        this.localPerUserProject = computeGroupedBy(headers, onlyLocalBuilds(), groupingBy, keyExtractor);
        this.meanTrendsLocalPerUserProject = computeGroupedByTrends(headers, onlyLocalBuilds(), it -> format(it.getMean()), groupingBy, keyExtractor);
        this.p50TrendsLocalPerUserProject = computeGroupedByTrends(headers, onlyLocalBuilds(), it -> format(it.getPercentile(50)), groupingBy, keyExtractor);
        this.p95TrendsLocalPerUserProject = computeGroupedByTrends(headers, onlyLocalBuilds(), it -> format(it.getPercentile(95)), groupingBy, keyExtractor);
        this.failuresTrendsLocalPerUserProject = computeGroupedByTrends(headers, onlyLocalBuilds(), it -> it.getN(), groupingBy, keyExtractor);
    }

    public ZonedDateTime since() {
        return since;
    }

    public ZonedDateTime until() {
        return until;
    }

    public String ciOverall() {
        return ciOverall;
    }

    public String ciPerProject() {
        return ciPerProject;
    }

    public String ciPerProjectRequested() {
        return ciPerProjectRequested;
    }

    public String localOverall() {
        return localOverall;
    }

    public String localPerProject() {
        return localPerProject;
    }

    public String localPerUser() {
        return localPerUser;
    }

    public String localPerUserProject() {
        return localPerUserProject;
    }

    public String overall() {
        return overall;
    }

    public String meanTrendsCiOverall() {
        return meanTrendsCiOverall;
    }

    public String meanTrendsCiPerProject() {
        return meanTrendsCiPerProject;
    }

    public String meanTrendsCiPerProjectRequested() {
        return meanTrendsCiPerProjectRequested;
    }

    public String meanTrendsLocalOverall() {
        return meanTrendsLocalOverall;
    }

    public String meanTrendsLocalPerProject() {
        return meanTrendsLocalPerProject;
    }

    public String meanTrendsLocalPerUser() {
        return meanTrendsLocalPerUser;
    }

    public String meanTrendsLocalPerUserProject() {
        return meanTrendsLocalPerUserProject;
    }

    public String meanTrendsOverall() {
        return meanTrendsOverall;
    }

    public String p50TrendsCiOverall() {
        return p50TrendsCiOverall;
    }

    public String p50TrendsCiPerProject() {
        return p50TrendsCiPerProject;
    }

    public String p50TrendsCiPerProjectRequested() {
        return p50TrendsCiPerProjectRequested;
    }

    public String p50TrendsLocalOverall() {
        return p50TrendsLocalOverall;
    }

    public String p50TrendsLocalPerProject() {
        return p50TrendsLocalPerProject;
    }

    public String p50TrendsLocalPerUser() {
        return p50TrendsLocalPerUser;
    }

    public String p50TrendsLocalPerUserProject() {
        return p50TrendsLocalPerUserProject;
    }

    public String p50TrendsOverall() {
        return p50TrendsOverall;
    }

    public String p95TrendsCiOverall() {
        return p95TrendsCiOverall;
    }

    public String p95TrendsCiPerProject() {
        return p95TrendsCiPerProject;
    }

    public String p95TrendsCiPerProjectRequested() {
        return p95TrendsCiPerProjectRequested;
    }

    public String p95TrendsLocalOverall() {
        return p95TrendsLocalOverall;
    }

    public String p95TrendsLocalPerProject() {
        return p95TrendsLocalPerProject;
    }

    public String p95TrendsLocalPerUser() {
        return p95TrendsLocalPerUser;
    }

    public String p95TrendsLocalPerUserProject() {
        return p95TrendsLocalPerUserProject;
    }

    public String p95TrendsOverall() {
        return p95TrendsOverall;
    }

    public String failuresTrendsCiOverall() {
        return failuresTrendsCiOverall;
    }

    public String failuresTrendsCiPerProject() {
        return failuresTrendsCiPerProject;
    }

    public String failuresTrendsCiPerProjectRequested() {
        return failuresTrendsCiPerProjectRequested;
    }

    public String failuresTrendsLocalOverall() {
        return failuresTrendsLocalOverall;
    }

    public String failuresTrendsLocalPerProject() {
        return failuresTrendsLocalPerProject;
    }

    public String failuresTrendsLocalPerUser() {
        return failuresTrendsLocalPerUser;
    }

    public String failuresTrendsLocalPerUserProject() {
        return failuresTrendsLocalPerUserProject;
    }

    public String failuresTrendsOverall() {
        return failuresTrendsOverall;
    }

    private String computeOverall(Predicate<Incident> filter) {
        final var row = calculateStatistics(incidents.stream().filter(filter).toList());
        final var table = Table.withHeader("Failures", "Mean", "Median", "Min", "Max", "P5", "P25", "P75", "P95");
        addRow(table, row);
        return table.toString();
    }

    private <Key> String computeGroupedBy(
            List<String> headers,
            Predicate<Incident> filter,
            Function<Incident, Key> groupingBy,
            Function<Key, Stream<String>> keyExtractor) {
        final var rows = incidents
                .stream()
                .filter(filter)
                .collect(groupingBy(groupingBy))
                .entrySet()
                .stream()
                .collect(toMap(Entry::getKey, it -> calculateStatistics(it.getValue())))
                .entrySet()
                .stream()
                .sorted(reverseOrder(comparing(it -> it.getValue().getN())))
                .toList();
        final var defaultHeaders = List.of("Failures", "Mean", "Median", "Min", "Max", "P5", "P25", "P75", "P95");
        final var table = Table.withHeader(concat(headers.stream(), defaultHeaders.stream()).toArray());
        rows.forEach(s -> addRow(table, s.getValue(), keyExtractor.apply(s.getKey()).toArray()));
        return table.toString();
    }

    private String computeOverallTrends(Predicate<Incident> filter, Function<DescriptiveStatistics, Object> getStatistic) {
        final var row = incidentsPartitioned
                .entrySet()
                .stream()
                .collect(toMap(Entry::getKey, it -> calculateStatistics(it.getValue().stream().filter(filter).toList())))
                .entrySet()
                .stream()
                .sorted(comparingByKey())
                .toList();
        final var table = Table.withHeader(concat(Stream.of("Failures"), row.stream().map(Entry::getKey).map(resolution::format)).toArray());
        final var failures = (int) row.stream().mapToDouble(value -> value.getValue().getN()).sum();
        table.row(concat(Stream.of(failures), row.stream().map(it -> valueOf(getStatistic.apply(it.getValue())))).toArray());
        return table.toString();
    }

    private <Key> String computeGroupedByTrends(
            List<String> headers,
            Predicate<Incident> filter,
            Function<DescriptiveStatistics, Object> getStatistic,
            Function<Incident, Key> groupingBy,
            Function<Key, Stream<String>> keyExtractor) {
        record Row<Key>(Key key, int failures, Map<ZonedDateTime, DescriptiveStatistics> columns) { }
        final var ticks = incidentsPartitioned.keySet().stream().sorted().toList();
        final var rows = transpose(incidentsPartitioned
                .entrySet()
                .stream()
                .collect(groupingByKeyAndCalculatingStatistics(filter, groupingBy)))
                .entrySet()
                .stream()
                .map(it -> new Row<>(it.getKey(), countFailures(it), it.getValue()))
                .sorted(reverseOrder(comparing(Row::failures)))
                .toList();
        final var table = Table.withHeader(concat(concat(headers.stream(), Stream.of("Failures")), ticks.stream().map(resolution::format)).toArray());
        rows.forEach(row -> table.row(concat(concat(
                keyExtractor.apply(row.key()),
                Stream.of(row.failures)),
                ticks.stream().map(it -> row.columns().containsKey(it) ? valueOf(getStatistic.apply(row.columns().get(it))) : "--")).toArray()));
        return table.toString();
    }

    private static <Key> int countFailures(Entry<Key, Map<ZonedDateTime, DescriptiveStatistics>> row) {
        return (int) row.getValue().values().stream().mapToDouble(DescriptiveStatistics::getN).sum();
    }

    private List<Incident> sortChronologicallyAndApplyExclusions(List<Incident> incidents, Integer excludeAbovePercentile) {
        final var sortedIncidents = incidents.stream().sorted(comparing(Incident::startedOn)).filter(it -> it.duration().compareTo(ZERO) > 0);
        if (excludeAbovePercentile != null) {
            final var percentile = calculateStatistics(incidents).getPercentile(excludeAbovePercentile);
            return sortedIncidents.filter(it -> it.duration().compareTo(ofMillis((long) percentile)) <= 0).toList();
        }
        return sortedIncidents.toList();
    }

    private Map<ZonedDateTime, List<Incident>> partition(List<Incident> incidents) {
        final var ticks = iterate(resolution.truncate(since), it -> it.compareTo(until) < 0, it -> it.plus(1, resolution.asChronoUnit())).toList();
        final var partitionedIncidents = new HashMap<>(incidents
                .stream()
                .collect(groupingBy(it -> resolution.truncate(it.startedOn().atZone(systemDefault())))));
        ticks.forEach(it -> partitionedIncidents.computeIfAbsent(it, __ -> emptyList()));
        return partitionedIncidents;
    }

    private static DescriptiveStatistics calculateStatistics(List<Incident> incidents) {
        var s = new DescriptiveStatistics();
        incidents.stream().mapToLong(it -> it.duration().toMillis()).forEach(s::addValue);
        return s;
    }

    private static void addRow(Table table, DescriptiveStatistics s, Object... keys) {
        final var contents = concat(
                stream(keys),
                Stream.of(
                        s.getN(),
                        format(s.getMean()),
                        format(s.getPercentile(50)),
                        format(s.getMin()),
                        format(s.getMax()),
                        format(s.getPercentile(5)),
                        format(s.getPercentile(25)),
                        format(s.getPercentile(75)),
                        format(s.getPercentile(95))
                )).toArray();
        table.row(contents);
    }

    private static <T> Map<T, Map<ZonedDateTime, DescriptiveStatistics>> transpose(Map<ZonedDateTime, Map<T, DescriptiveStatistics>> rows) {
        record Entry(ZonedDateTime outer, Object inner, DescriptiveStatistics value) {}
        //noinspection unchecked
        return (Map<T, Map<ZonedDateTime, DescriptiveStatistics>>) rows
                .entrySet()
                .stream()
                .flatMap(outer -> outer.getValue().entrySet().stream().map(inner -> new Entry(outer.getKey(), inner.getKey(), inner.getValue())))
                .collect(groupingBy(it -> it.inner, toMap(it -> it.outer, it -> it.value)));
    }

    private static <T> Collector<Entry<ZonedDateTime, List<Incident>>, ?, Map<ZonedDateTime, Map<T, DescriptiveStatistics>>> groupingByKeyAndCalculatingStatistics(
            Predicate<Incident> filter,
            Function<Incident, T> groupingBy) {
        return toMap(Entry::getKey, partition -> partition
                .getValue()
                .stream()
                .filter(filter)
                .collect(groupingBy(groupingBy))
                .entrySet()
                .stream()
                .collect(toMap(Entry::getKey, it -> calculateStatistics(it.getValue()))));
    }

    private static Predicate<Incident> allBuilds() {
        return it -> true;
    }

    private static Predicate<Incident> onlyCiBuilds() {
        return Incident::isCI;
    }

    private static Predicate<Incident> onlyLocalBuilds() {
        return not(Incident::isCI);
    }

    private static <Key> Function<Incident, Key> groupBy(Function<Incident, Key> groupingBy) {
        return groupingBy;
    }

    @SuppressWarnings({"unused", "SameParameterValue"})
    private static <Key> Function<Key, Stream<String>> extractKey(Class<Key> type, Function<Key, Stream<String>> keyExtractor) {
        return keyExtractor;
    }

    private static String format(double millis) {
        return Durations.format(ofMillis((long) millis));
    }

}
