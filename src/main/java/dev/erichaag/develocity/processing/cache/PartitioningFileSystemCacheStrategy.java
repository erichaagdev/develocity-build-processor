package dev.erichaag.develocity.processing.cache;

import java.nio.file.Path;

public final class PartitioningFileSystemCacheStrategy implements FileSystemCacheStrategy {

    private final Path rootDirectory;
    private final int granularity;

    public PartitioningFileSystemCacheStrategy(Path rootDirectory, int granularity) {
        this.rootDirectory = rootDirectory;
        this.granularity = granularity;
    }

    @Override
    public Path getPath(String id) {
        return rootDirectory.resolve(id.substring(0, granularity)).resolve(id + ".json");
    }

}
