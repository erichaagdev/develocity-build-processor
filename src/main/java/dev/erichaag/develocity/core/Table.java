package dev.erichaag.develocity.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.join;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;

@SuppressWarnings("UnusedReturnValue")
final class Table {

    private final String[] header;
    private final List<String[]> rows = new ArrayList<>();

    private Table(String[] header) {
        this.header = header;
    }

    static Table withHeader(Object... header) {
        return new Table(stream(header).map(String::valueOf).toArray(String[]::new));
    }

    Table row(Object... values) {
        this.rows.add(stream(values).map(String::valueOf).toArray(String[]::new));
        return this;
    }

    @Override
    public String toString() {
        return concat(Stream.of(join(",", header)), rows.stream().map(it -> join(",", it))).collect(joining("\n"));
    }

}
