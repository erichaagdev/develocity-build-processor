package dev.erichaag.develocity.processing.cache;

import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.BuildModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static dev.erichaag.develocity.api.BuildModel.GRADLE_ATTRIBUTES;
import static dev.erichaag.develocity.api.BuildModel.GRADLE_BUILD_CACHE_PERFORMANCE;
import static dev.erichaag.develocity.api.Builds.gradle;
import static dev.erichaag.develocity.api.Builds.gradleAttributes;
import static dev.erichaag.develocity.api.Builds.gradleProjects;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An abstract base class for testing {@link ProcessorCache} implementations.
 *
 * <p>This class provides a set of common test scenarios and helper methods to
 * streamline the testing of different cache implementations. Subclasses must
 * implement the {@link #createCache()} method to provide a concrete cache
 * instance for testing.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class AbstractCacheTest {

    private static final String id = "foobarbazqux1";

    private ProcessorCache cache;

    /**
     * Creates and returns a new instance of the {@link ProcessorCache}
     * implementation under test.
     *
     * @return a new {@link ProcessorCache} instance
     */
    protected abstract ProcessorCache createCache();

    /**
     * Returns the {@link ProcessorCache} instance being tested.
     *
     * <p>Implementations of this abstract class should favor using the
     * provided helper methods and only result to this method in the event a
     * suitable helper method does not exist.
     *
     * @return the {@link ProcessorCache} instance
     */
    protected ProcessorCache cache() {
        return cache;
    }

    /**
     * Prepares the cache for a test scenario where a build already exists in
     * the cache.
     *
     * <p>This method saves the given build to the cache and returns it for
     * further use.
     *
     * @param build the build to save in the cache
     * @return the saved build
     */
    protected Build givenBuildExistsInCache(Build build) {
        cache.save(build);
        return build;
    }

    /**
     * Saves a build to the cache as part of a test scenario.
     *
     * <p>Use this method when you want to test the cache's behavior after a
     * build is saved.
     *
     * @param build the build to save in the cache
     * @return the saved build
     */
    protected Build whenBuildSaved(Build build) {
        cache.save(build);
        return build;
    }

    /**
     * Performs a load operation on the cache, requesting the build with the
     * given ID and models.
     *
     * <p>Use this method to test the cache's behavior in response to a load
     * request.
     *
     * @param id         the ID of the build to load
     * @param buildModels the models associated with the build
     * @return an Optional containing the loaded build, or empty if not found
     */
    protected Optional<Build> whenBuildLoadedFromCache(String id, BuildModel... buildModels) {
        return cache.load(id, buildModels);
    }

    /**
     * Asserts that a build was retrieved successfully from the cache, matching
     * the expected build.
     *
     * @param expected the expected build
     * @param actual   the Optional containing the actual build (or empty if not
     *                 found)
     */
    protected void thenBuildIsRetrievedSuccessfully(Build expected, Optional<Build> actual) {
        assertTrue(actual.isPresent(), "Expected a build to be retrieved successfully, but there wasn't");
        assertEquals(expected, actual.get());
    }

    /**
     * Asserts that no build was retrieved from the cache.
     *
     * @param actual the Optional that should be empty
     */
    protected void thenNoBuildIsRetrieved(Optional<Build> actual) {
        assertTrue(actual.isEmpty(), "Expected no build to be retrieved, but there was");
    }

    @BeforeEach
    void beforeEach() {
        this.cache = createCache();
    }

    @Test
    void givenBuildExistsInCache_whenLoaded_thenBuildIsRetrievedSuccessfully() {
        final var buildInCache = givenBuildExistsInCache(gradle(id, gradleAttributes()));
        final var buildFromCache = whenBuildLoadedFromCache(id, GRADLE_ATTRIBUTES);
        thenBuildIsRetrievedSuccessfully(buildInCache, buildFromCache);
    }

    @Test
    void givenBuildExistsInCache_whenLoadedButForDifferentModels_thenNoBuildIsRetrieved() {
        givenBuildExistsInCache(gradle(id, gradleAttributes()));
        final var buildFromCache = whenBuildLoadedFromCache(id, GRADLE_BUILD_CACHE_PERFORMANCE);
        thenNoBuildIsRetrieved(buildFromCache);
    }

    @Test
    void givenBuildExistsInCacheWithMultipleModels_whenLoadedForOnlyOneModel_thenBuildIsRetrievedSuccessfully() {
        final var buildInCache = givenBuildExistsInCache(gradle(id, gradleAttributes(), gradleProjects()));
        final var buildFromCache = whenBuildLoadedFromCache(id, GRADLE_ATTRIBUTES);
        thenBuildIsRetrievedSuccessfully(buildInCache, buildFromCache);
    }

    @Test
    void givenBuildExistsInCache_whenBuildSavedWithSameId_thenPreviousBuildIsOverwritten() {
        givenBuildExistsInCache(gradle(id));
        final var newBuildInCache = whenBuildSaved(gradle(id, gradleAttributes()));
        final var buildFromCache = whenBuildLoadedFromCache(id);
        thenBuildIsRetrievedSuccessfully(newBuildInCache, buildFromCache);
    }

    @Test
    void givenBuildDoesNotExistInCache_whenLoaded_thenNoBuildIsRetrieved() {
        final var buildFromCache = whenBuildLoadedFromCache(id);
        thenNoBuildIsRetrieved(buildFromCache);
    }

}
