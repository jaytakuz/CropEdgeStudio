package se233.cropedgestudio.utils;

import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BatchProcessor {

    public static List<File> processFiles(List<File> files) throws IOException {
        List<File> processedFiles = new ArrayList<>();

        for (File file : files) {
            if (file.getName().toLowerCase().endsWith(".zip")) {
                processedFiles.addAll(extractZip(file));
            } else if (isImageFile(file)) {
                processedFiles.add(file);
            }
        }

        return processedFiles;
    }

    private static List<File> extractZip(File zipFile) throws IOException {
        List<File> extractedFiles = new ArrayList<>();
        Path tempDir = Files.createTempDirectory("cropedgestudio_");

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile.toPath()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && isImageFile(entry.getName())) {
                    Path filePath = tempDir.resolve(entry.getName());
                    Files.copy(zis, filePath);
                    extractedFiles.add(filePath.toFile());
                }
            }
        }

        return extractedFiles;
    }

    private static boolean isImageFile(File file) {
        return isImageFile(file.getName());
    }

    private static boolean isImageFile(String fileName) {
        String lowercaseName = fileName.toLowerCase();
        return lowercaseName.endsWith(".jpg") || lowercaseName.endsWith(".jpeg") || lowercaseName.endsWith(".png");
    }
}