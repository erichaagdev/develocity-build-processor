package dev.erichaag.develocity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import static java.lang.Integer.parseInt;
import static java.time.ZoneId.systemDefault;

final class Configuration {

    private static final String configurationFile = "config.properties";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("LLL d uuuu kk:mm");
    private static final int defaultMaxBuildsPerRequest = 100;

    private Configuration() {
    }

    static ConfigurationProperties load() {
        final var properties = new java.util.Properties();
        try (InputStream input = new FileInputStream(configurationFile)) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration from '%s' file".formatted(configurationFile), e);
        }
        return new ConfigurationProperties(
                URI.create(properties.getProperty("serverUrl")),
                LocalDateTime.parse(properties.getProperty("since"), formatter).atZone(systemDefault()),
                getOrDefault("maxBuildsPerRequest", it -> parseInt(properties.getProperty(it)), defaultMaxBuildsPerRequest),
                getOrDefault("excludeAbovePercentile", it -> parseInt(properties.getProperty(it)), null)
        );
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> T getOrDefault(String key, Function<String, T> getValue, T defaultValue) {
        try {
            final var value = getValue.apply(key);
            return value == null ? defaultValue : value;
        } catch (Exception e) {
            System.out.println("Failed to parse property '" + key + "'. Using default value of '" + defaultValue + "'.");
            return defaultValue;
        }
    }

    record ConfigurationProperties(
            URI serverUrl,
            ZonedDateTime since,
            int maxBuildsPerRequest,
            Integer excludeAbovePercentile) {
    }

}
