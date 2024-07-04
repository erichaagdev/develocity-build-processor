package dev.erichaag.develocity.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FileSystemBuildCacheTest extends AbstractBuildCacheTest {

    Path temporaryCacheDirectory;

    @BeforeEach
    void beforeEach(@TempDir Path temporaryCacheDirectory) {
        this.temporaryCacheDirectory = temporaryCacheDirectory;
        this.cache = new FileSystemBuildCache(temporaryCacheDirectory);
    }

    @Test
    void givenCorruptFile_whenLoaded_thenBuildIsNotLoadedFromCacheAndFileIsDeleted() throws IOException {
        final var id = "foobarbazqux1";
        final var corruptCacheFile = temporaryCacheDirectory.resolve(id.substring(0, 2)).resolve(id + ".json").toFile();
        //noinspection ResultOfMethodCallIgnored
        corruptCacheFile.getParentFile().mkdirs();
        Files.write(corruptCacheFile.toPath(), "corrupt".getBytes());
        final var cachedBuild = cache.load(id);
        assertTrue(cachedBuild.isEmpty());
        assertFalse(corruptCacheFile.exists());
    }

}
