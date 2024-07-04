package dev.erichaag.develocity.processing.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FileSystemCacheTest extends AbstractCacheTest {

    private static final String id = "foobarbazqux1";

    @TempDir private Path temporaryCacheDirectory;

    private PartitioningFileSystemCacheStrategy cacheStrategy;

    @Override
    protected ProcessorCache createCache() {
        this.cacheStrategy = new PartitioningFileSystemCacheStrategy(temporaryCacheDirectory, 2);
        return FileSystemCache.withStrategy(cacheStrategy);
    }

    @Test
    void givenCorruptCacheFile_whenLoaded_thenBuildIsNotRetrievedFromCacheAndFileIsDeleted() throws IOException {
        final var corruptCacheFile = cacheStrategy.getPath(id).toFile();
        //noinspection ResultOfMethodCallIgnored
        corruptCacheFile.getParentFile().mkdirs();
        Files.write(corruptCacheFile.toPath(), "corrupt".getBytes());
        final var cachedBuild = whenBuildLoadedFromCache(id);
        assertTrue(cachedBuild.isEmpty(), "Expected no build to be loaded from the corrupt cache file");
        assertFalse(corruptCacheFile.exists(), "Expected the corrupt cache file to be deleted");
    }

}
