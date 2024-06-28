package dev.erichaag.develocity.processing.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FileSystemCacheTest extends AbstractCacheTest {

    Path temporaryCacheDirectory;
    PartitioningFileSystemCacheStrategy cacheStrategy;

    @BeforeEach
    void beforeEach(@TempDir Path temporaryCacheDirectory) {
        this.temporaryCacheDirectory = temporaryCacheDirectory;
        this.cacheStrategy = new PartitioningFileSystemCacheStrategy(temporaryCacheDirectory, 2);
        this.cache = FileSystemCache.withStrategy(cacheStrategy);
    }

    @Test
    void givenCorruptFile_whenLoaded_thenBuildIsNotLoadedFromCacheAndFileIsDeleted() throws IOException {
        final var id = "foobarbazqux1";
        final var corruptCacheFile = cacheStrategy.getPath(id).toFile();
        //noinspection ResultOfMethodCallIgnored
        corruptCacheFile.getParentFile().mkdirs();
        Files.write(corruptCacheFile.toPath(), "corrupt".getBytes());
        final var cachedBuild = cache.load(id);
        assertTrue(cachedBuild.isEmpty());
        assertFalse(corruptCacheFile.exists());
    }

}
