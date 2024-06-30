package dev.erichaag.develocity.processing.cache;

import org.junit.jupiter.api.Test;

import static dev.erichaag.develocity.api.Builds.gradle;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class InMemoryCacheTest extends AbstractCacheTest {

    @Override
    protected ProcessorCache createCache() {
        return InMemoryCache.withDefaultSize();
    }

    @Test
    void givenFullCache_whenSaved_thenEntryIsPurged() {
        final var firstBuild = gradle("foobarbazqux1");
        final var secondBuild = gradle("foobarbazqux2");
        final var thirdBuild = gradle("foobarbazqux3");
        final var cache = InMemoryCache.withSize(2);
        cache.save(firstBuild);
        cache.save(secondBuild);
        cache.save(thirdBuild);
        assertTrue(cache.load(firstBuild.getId()).isEmpty());
    }

}
