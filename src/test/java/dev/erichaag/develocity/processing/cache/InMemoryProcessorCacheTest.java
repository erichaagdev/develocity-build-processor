package dev.erichaag.develocity.processing.cache;

import org.junit.jupiter.api.BeforeEach;

final class InMemoryProcessorCacheTest extends AbstractProcessorCacheTest {

    @BeforeEach
    void beforeEach() {
        this.cache = new InMemoryProcessorCache();
    }

}
