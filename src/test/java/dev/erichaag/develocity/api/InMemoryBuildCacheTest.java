package dev.erichaag.develocity.api;

import org.junit.jupiter.api.BeforeEach;

final class InMemoryBuildCacheTest extends AbstractBuildCacheTest {

    @BeforeEach
    void beforeEach() {
        this.cache = new InMemoryBuildCache();
    }

}
