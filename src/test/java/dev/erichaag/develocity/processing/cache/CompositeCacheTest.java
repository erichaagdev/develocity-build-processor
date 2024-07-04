package dev.erichaag.develocity.processing.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static dev.erichaag.develocity.api.Builds.gradleBuild;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CompositeCacheTest extends AbstractCacheTest {

    InMemoryCache inMemoryCache;
    FileSystemCache fileSystemCache;

    @BeforeEach
    void beforeEach(@TempDir Path temporaryCacheDirectory) {
        this.inMemoryCache = InMemoryCache.withDefaultSize();
        this.fileSystemCache = FileSystemCache.withStrategy(new PartitioningFileSystemCacheStrategy(temporaryCacheDirectory, 2));
        this.cache = CompositeCache.firstChecking(inMemoryCache)
                .followedBy(fileSystemCache);
    }

    @Test
    void whenSavedToCompositeCache_thenBuildIsSavedToAllCaches() {
        final var id = "foobarbazqux1";
        final var savedBuild = gradleBuild(id);
        cache.save(savedBuild);
        final var inMemoryBuild = inMemoryCache.load(id);
        final var fileSystemBuild = fileSystemCache.load(id);
        assertTrue(inMemoryBuild.isPresent());
        assertEquals(savedBuild, inMemoryBuild.get());
        assertTrue(fileSystemBuild.isPresent());
        assertEquals(savedBuild, fileSystemBuild.get());
    }

    @Test
    void givenBuildExistsInMemoryButNotOnFileSystem_whenLoaded_thenBuildIsLoadedButBuildIsNotPersistedToFileSystem() {
        final var id = "foobarbazqux1";
        final var inMemoryBuild = gradleBuild(id);
        inMemoryCache.save(inMemoryBuild);
        final var compositeBuild = cache.load(id);
        final var fileSystemBuild = fileSystemCache.load(id);
        assertTrue(compositeBuild.isPresent());
        assertEquals(inMemoryBuild, compositeBuild.get());
        assertTrue(fileSystemBuild.isEmpty());
    }

    @Test
    void givenBuildExistsOnFileSystemButNotInMemory_whenLoaded_thenBuildIsLoadedAndBuildIsPersistedToMemory() {
        final var id = "foobarbazqux1";
        final var fileSystemBuild = gradleBuild(id);
        fileSystemCache.save(fileSystemBuild);
        final var compositeBuild = cache.load(id);
        final var inMemoryBuild = inMemoryCache.load(id);
        assertTrue(compositeBuild.isPresent());
        assertEquals(fileSystemBuild, compositeBuild.get());
        assertTrue(inMemoryBuild.isPresent());
        assertEquals(fileSystemBuild, inMemoryBuild.get());
    }

}
