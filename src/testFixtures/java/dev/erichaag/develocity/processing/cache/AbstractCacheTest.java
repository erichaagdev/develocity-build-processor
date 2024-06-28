package dev.erichaag.develocity.processing.cache;

import org.junit.jupiter.api.Test;

import static dev.erichaag.develocity.api.BuildModel.GRADLE_ATTRIBUTES;
import static dev.erichaag.develocity.api.BuildModel.GRADLE_BUILD_CACHE_PERFORMANCE;
import static dev.erichaag.develocity.api.Builds.gradleAttributes;
import static dev.erichaag.develocity.api.Builds.gradleBuildWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractCacheTest {

    ProcessorCache cache;

    @Test
    void givenCachedBuild_whenLoaded_thenBuildIsLoadedFromCache() {
        final var id = "foobarbazqux1";
        final var savedBuild = gradleBuildWith(id, gradleAttributes());
        cache.save(savedBuild);
        final var cachedBuild = cache.load(id, GRADLE_ATTRIBUTES);
        assertTrue(cachedBuild.isPresent());
        assertEquals(savedBuild, cachedBuild.get());
    }

    @Test
    void givenCachedBuild_whenLoadedForDifferentModels_thenBuildIsNotLoadedFromCache() {
        final var id = "foobarbazqux1";
        cache.save(gradleBuildWith(id, gradleAttributes()));
        final var cachedBuild = cache.load(id, GRADLE_BUILD_CACHE_PERFORMANCE);
        assertTrue(cachedBuild.isEmpty());
    }

}
