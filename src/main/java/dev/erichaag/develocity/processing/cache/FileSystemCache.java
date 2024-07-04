package dev.erichaag.develocity.processing.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.erichaag.develocity.api.ApiBuild;
import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.BuildModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;

public final class FileSystemCache implements ProcessorCache {

    private static final int defaultGranularity = 2;
    private static final Path defaultRootDirectory = Path.of(System.getProperty("user.home"))
            .resolve(".develocity-failure-insights");

    private final ObjectMapper objectMapper = new JsonMapper();
    private final FileSystemCacheStrategy fileSystemCacheStrategy;

    private FileSystemCache(FileSystemCacheStrategy fileSystemCacheStrategy) {
        this.fileSystemCacheStrategy = fileSystemCacheStrategy;
    }

    public static FileSystemCache withDefaultStrategy() {
        return new FileSystemCache(new PartitioningFileSystemCacheStrategy(defaultRootDirectory, defaultGranularity));
    }

    public static FileSystemCache withStrategy(FileSystemCacheStrategy strategy) {
        return new FileSystemCache(strategy);
    }

    @Override
    public Optional<Build> load(String id, Set<BuildModel> requiredBuildModels) {
        final var cachedBuildFile = fileSystemCacheStrategy.getPath(id).toFile();
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
        final var cachedBuildFile = fileSystemCacheStrategy.getPath(build.getId()).toFile();
        //noinspection ResultOfMethodCallIgnored
        cachedBuildFile.getParentFile().mkdirs();
        try {
            Files.write(cachedBuildFile.toPath(), objectMapper.writeValueAsBytes(build.getBuild()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
