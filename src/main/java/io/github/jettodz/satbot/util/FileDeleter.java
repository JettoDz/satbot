package io.github.jettodz.satbot.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * File visitor cuya unica razon de existir es borrar los archivos de un directorio y el directorio mismo
 * @author Fernando
 * @since 0.0.1
 */
public class FileDeleter extends SimpleFileVisitor<Path> {

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
        Files.delete(path);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
        Files.delete(path);
        return FileVisitResult.TERMINATE;
    }
}
