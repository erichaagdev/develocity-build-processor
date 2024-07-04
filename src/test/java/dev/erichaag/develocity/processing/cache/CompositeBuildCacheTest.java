package dev.erichaag.develocity.processing.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static dev.erichaag.develocity.api.Builds.gradleBuild;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CompositeBuildCacheTest extends AbstractBuildCacheTest {

    InMemoryBuildCache inMemoryBuildCache;
    FileSystemBuildCache fileSystemBuildCache;

    @BeforeEach
    void beforeEach(@TempDir Path temporaryCacheDirectory) {
        this.inMemoryBuildCache = new InMemoryBuildCache();
        this.fileSystemBuildCache = new FileSystemBuildCache(temporaryCacheDirectory);
        this.cache = CompositeBuildCache.firstChecking(inMemoryBuildCache)
                .followedBy(fileSystemBuildCache);
    }

    @Test
    void whenSavedToCompositeCache_thenBuildIsSavedToAllCaches() {
        final var id = "foobarbazqux1";
        final var savedBuild = gradleBuild(id);
        cache.save(savedBuild);
        final var inMemoryBuild = inMemoryBuildCache.load(id);
        final var fileSystemBuild = fileSystemBuildCache.load(id);
        assertTrue(inMemoryBuild.isPresent());
        assertEquals(savedBuild, inMemoryBuild.get());
        assertTrue(fileSystemBuild.isPresent());
        assertEquals(savedBuild, fileSystemBuild.get());
    }

    @Test
    void givenBuildExistsInMemoryButNotOnFileSystem_whenLoaded_thenBuildIsLoadedButBuildIsNotPersistedToFileSystem() {
        final var id = "foobarbazqux1";
        final var inMemoryBuild = gradleBuild(id);
        inMemoryBuildCache.save(inMemoryBuild);
        final var compositeBuild = cache.load(id);
        final var fileSystemBuild = fileSystemBuildCache.load(id);
        assertTrue(compositeBuild.isPresent());
        assertEquals(inMemoryBuild, compositeBuild.get());
        assertTrue(fileSystemBuild.isEmpty());
    }

    @Test
    void givenBuildExistsOnFileSystemButNotInMemory_whenLoaded_thenBuildIsLoadedAndBuildIsPersistedToMemory() {
        final var id = "foobarbazqux1";
        final var fileSystemBuild = gradleBuild(id);
        fileSystemBuildCache.save(fileSystemBuild);
        final var compositeBuild = cache.load(id);
        final var inMemoryBuild = inMemoryBuildCache.load(id);
        assertTrue(compositeBuild.isPresent());
        assertEquals(fileSystemBuild, compositeBuild.get());
        assertTrue(inMemoryBuild.isPresent());
        assertEquals(fileSystemBuild, inMemoryBuild.get());
    }

}
