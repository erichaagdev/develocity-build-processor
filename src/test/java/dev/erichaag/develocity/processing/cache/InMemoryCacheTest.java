package dev.erichaag.develocity.processing.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.erichaag.develocity.api.Builds.gradleBuild;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class InMemoryCacheTest extends AbstractCacheTest {

    @BeforeEach
    void beforeEach() {
        this.cache = InMemoryCache.withDefaultSize();
    }

    @Test
    void givenFullCache_whenSaved_thenEntryIsPurged() {
        final var firstBuild = gradleBuild("foobarbazqux1");
        final var secondBuild = gradleBuild("foobarbazqux2");
        final var thirdBuild = gradleBuild("foobarbazqux3");
        final var cache = InMemoryCache.withSize(2);
        cache.save(firstBuild);
        cache.save(secondBuild);
        cache.save(thirdBuild);
        assertTrue(cache.load(firstBuild.getId()).isEmpty());
    }

}
