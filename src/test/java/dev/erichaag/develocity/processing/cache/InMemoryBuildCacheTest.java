package dev.erichaag.develocity.processing.cache;

import org.junit.jupiter.api.BeforeEach;

final class InMemoryBuildCacheTest extends AbstractBuildCacheTest {

    @BeforeEach
    void beforeEach() {
        this.cache = new InMemoryBuildCache();
    }

}
