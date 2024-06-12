package dev.erichaag.develocity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.walk;
import static java.nio.file.Files.writeString;
import static java.util.Comparator.reverseOrder;


final class Archive implements AutoCloseable {

    private final Path archivePath;
    private final Path tempDirectory = createTempDirectory();

    public Archive(Path archivePath) {
        this.archivePath = archivePath;
    }

    public Path getPath() {
        return archivePath;
    }

    public void write(String path, String content) {
        try {
            writeString(tempDirectory.resolve(path), content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path create() {
        //noinspection ResultOfMethodCallIgnored
        archivePath.getParent().toFile().mkdirs();
        try (final var outputStream = new ZipOutputStream(newOutputStream(archivePath));
             final var files = walk(tempDirectory)) {
            files.filter(Files::isRegularFile).forEach(file -> {
                try {
                    outputStream.putNextEntry(new ZipEntry(tempDirectory.relativize(file).toString()));
                    copy(file, outputStream);
                    outputStream.closeEntry();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return archivePath;
    }

    @Override
    public void close() {
        deleteTempDirectory();
    }

    private static Path createTempDirectory() {
        try {
            return Files.createTempDirectory(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteTempDirectory() {
        try (final var files = walk(tempDirectory)) {
            //noinspection ResultOfMethodCallIgnored
            files.sorted(reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
