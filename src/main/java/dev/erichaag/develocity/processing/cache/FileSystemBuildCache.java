package dev.erichaag.develocity.processing.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.erichaag.develocity.api.ApiBuild;
import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.BuildModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;

public final class FileSystemBuildCache implements BuildCache {

    private static final Path defaultCacheDirectory = Path.of(System.getProperty("user.home"))
            .resolve(".develocity-failure-insights");

    private final ObjectMapper objectMapper = new JsonMapper();
    private final Path cacheDirectory;

    public FileSystemBuildCache() {
        this.cacheDirectory = defaultCacheDirectory;
    }

    public FileSystemBuildCache(Path cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }

    @Override
    public Optional<Build> load(String id, Set<BuildModel> requiredBuildModels) {
        final var cachedBuildFile = getFile(id);
        try {
            if (cachedBuildFile.exists()) {
                return Optional.of(objectMapper.readValue(cachedBuildFile, ApiBuild.class))
                        .map(Build::from)
                        .filter(it -> it.getAvailableBuildModels().containsAll(requiredBuildModels));
            }
        } catch (IOException ignored) {
            //noinspection ResultOfMethodCallIgnored
            cachedBuildFile.delete();
        }
        return empty();
    }

    @Override
    public void save(Build build) {
        final var cachedBuildFile = getFile(build.getId());
        //noinspection ResultOfMethodCallIgnored
        cachedBuildFile.getParentFile().mkdirs();
        try {
            Files.write(cachedBuildFile.toPath(), objectMapper.writeValueAsBytes(build.getBuild()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getFile(String id) {
        return cacheDirectory.resolve(id.substring(0, 2)).resolve(id + ".json").toFile();
    }

}
