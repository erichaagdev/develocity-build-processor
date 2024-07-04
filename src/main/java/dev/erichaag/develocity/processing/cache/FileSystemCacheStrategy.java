package dev.erichaag.develocity.processing.cache;

import java.nio.file.Path;

public interface FileSystemCacheStrategy {

    Path getPath(String id);

}
