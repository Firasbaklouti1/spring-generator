package com.firas.generator.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for creating ZIP archives from directories.
 * 
 * This class provides methods to recursively zip a directory and all its contents
 * into a byte array, which can then be sent as an HTTP response or saved to a file.
 * 
 * @author Firas Baklouti
 * @version 1.0
 * @since 2025-12-01
 */
public class ZipUtils {

    /**
     * Zips a directory and all its contents into a byte array.
     * 
     * This method recursively walks through the directory tree, adding all files
     * to the ZIP archive while preserving the directory structure. The paths in
     * the ZIP file are relative to the parent of the source directory.
     * 
     * @param sourceDir The directory to zip
     * @return Byte array containing the ZIP file content
     * @throws IOException If an error occurs during file reading or ZIP creation
     */
    public static byte[] zipDirectory(File sourceDir) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            Path sourcePath = sourceDir.toPath();
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // Create ZIP entry with path relative to parent directory
                    Path targetFile = sourcePath.getParent().relativize(file);
                    zos.putNextEntry(new ZipEntry(targetFile.toString().replace("\\", "/")));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
            
            zos.finish();
            return baos.toByteArray();
        }
    }
}
