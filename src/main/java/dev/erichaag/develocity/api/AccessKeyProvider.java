package dev.erichaag.develocity.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import static java.lang.System.getProperty;
import static java.lang.System.getenv;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public final class AccessKeyProvider {

    private static final String accessKey = "DEVELOCITY_ACCESS_KEY";
    private static final String legacyAccessKey = "GRADLE_ENTERPRISE_ACCESS_KEY";
    private static final String gradleUserHome = "GRADLE_USER_HOME";
    private static final String malformedEnvironmentVariableMessage = "Environment variable '%s' is malformed. " +
            "Expected format: 'server-host=access-key' or 'server-host1=access-key1;server-host2=access-key2;...'";

    private AccessKeyProvider() {
    }

    public static String lookupAccessKey(URI serverUrl) {
        return fromEnvVar(accessKey, serverUrl)
                .or(() -> fromEnvVar(legacyAccessKey, serverUrl))
                .or(() -> fromGradleHome("develocity", serverUrl))
                .or(() -> fromMavenHome("develocity", serverUrl))
                .or(() -> fromGradleHome("enterprise", serverUrl))
                .or(() -> fromMavenHome("gradle-enterprise", serverUrl))
                .orElseThrow(() -> new RuntimeException("No access key found for server " + serverUrl.getHost()));
    }

    private static Optional<String> fromGradleHome(String baseDir, URI serverUrl) {
        Path accessKeysFile = getGradleUserHomeDirectory().resolve(baseDir + "/keys.properties");
        return ofNullable(loadFromFile(accessKeysFile).getProperty(serverUrl.getHost()));
    }

    private static Path getGradleUserHomeDirectory() {
        if (isNullOrEmpty(getenv(gradleUserHome))) return Paths.get(getProperty("user.home"), ".gradle");
        return Paths.get(getenv(gradleUserHome));
    }

    private static Optional<String> fromMavenHome(String dir, URI serverUrl) {
        Path accessKeysFile = getMavenStorageDirectory(dir).resolve("keys.properties");
        return ofNullable(loadFromFile(accessKeysFile).getProperty(serverUrl.getHost()));
    }

    private static Path getMavenStorageDirectory(String dir) {
        String defaultLocation = getProperty("user.home") + "/.m2/." + dir;
        return Paths.get(getProperty("develocity.storage.directory", getProperty("gradle.enterprise.storage.directory", defaultLocation)));
    }

    private static Properties loadFromFile(Path accessKeysFile) {
        Properties accessKeys = new Properties();
        if (Files.isRegularFile(accessKeysFile)) {
            try (BufferedReader in = Files.newBufferedReader(accessKeysFile)) {
                accessKeys.load(in);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return accessKeys;
    }

    private static Optional<String> fromEnvVar(String env, URI serverUrl) {
        Properties accessKeys = new Properties();
        String value = getenv(env);
        if (isNullOrEmpty(value)) return empty();
        String[] entries = value.split(";");
        for (String entry : entries) {
            if (entry == null) throw buildMalformedException(env);
            String[] parts = entry.split("=", 2);
            if (parts.length < 2) throw buildMalformedException(env);
            String joinedServers = parts[0].trim();
            String accessKey = parts[1].trim();
            if (joinedServers.isEmpty() || isNullOrEmpty(accessKey)) throw buildMalformedException(env);
            for (String server : joinedServers.split(",")) {
                server = server.trim();
                if (server.isEmpty()) throw buildMalformedException(env);
                accessKeys.put(server, accessKey);
            }
        }
        return ofNullable(accessKeys.getProperty(serverUrl.getHost()));
    }

    private static RuntimeException buildMalformedException(String envVar) {
        return new RuntimeException(String.format(malformedEnvironmentVariableMessage, envVar));
    }

    private static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

}
