package dev.erichaag.develocity.processing.cache;

import dev.erichaag.develocity.api.Build;
import org.junit.jupiter.api.Test;

import static dev.erichaag.develocity.api.Builds.gradle;

final class CompositeCacheTest extends AbstractCacheTest {

    private static final String id = "foobarbazqux1";

    private final InMemoryCache primaryCache = InMemoryCache.withDefaultSize();
    private final InMemoryCache secondaryCache = InMemoryCache.withDefaultSize();

    @Override
    protected ProcessorCache createCache() {
        return CompositeCache.firstChecking(primaryCache).followedBy(secondaryCache);
    }

    @Test
    void whenSavedToCompositeCache_thenBuildIsReplicatedToAllCaches() {
        final var buildSavedToCompositeCache = whenBuildSaved(gradle(id));
        thenBuildIsRetrievedSuccessfully(buildSavedToCompositeCache, primaryCache.load(id));
        thenBuildIsRetrievedSuccessfully(buildSavedToCompositeCache, secondaryCache.load(id));
    }

    @Test
    void givenBuildExistsInPrimaryCacheOnly_whenLoaded_thenBuildIsNotReplicatedToSecondaryCache() {
        final var buildInPrimaryCache = givenBuildExistsInPrimaryCache(gradle(id));
        final var buildFromCompositeCache = whenBuildLoadedFromCache(id);
        thenBuildIsRetrievedSuccessfully(buildInPrimaryCache, buildFromCompositeCache);
        thenNoBuildIsRetrieved(secondaryCache.load(id));
    }

    @Test
    void givenBuildExistsInSecondaryCacheOnly_whenLoaded_thenBuildIsReplicatedToPrimaryCache() {
        final var buildInSecondaryCache = givenBuildExistsInSecondaryCache(gradle(id));
        final var buildFromCompositeCache = whenBuildLoadedFromCache(id);
        thenBuildIsRetrievedSuccessfully(buildInSecondaryCache, buildFromCompositeCache);
        thenBuildIsRetrievedSuccessfully(buildInSecondaryCache, primaryCache.load(id));
    }

    private Build givenBuildExistsInPrimaryCache(Build build) {
        primaryCache.save(build);
        return build;
    }

    private Build givenBuildExistsInSecondaryCache(Build build) {
        secondaryCache.save(build);
        return build;
    }

}
