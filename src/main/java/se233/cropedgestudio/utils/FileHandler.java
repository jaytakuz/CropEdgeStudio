package se233.cropedgestudio.utils;

import se233.cropedgestudio.models.ImageFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileHandler {
    public static List<ImageFile> processFiles(List<File> files) throws IOException {
        List<ImageFile> processedFiles = new ArrayList<>();

        for (File file : files) {
            if (file.getName().toLowerCase().endsWith(".zip")) {
                processedFiles.addAll(extractZip(file));
            } else if (isImageFile(file.getName())) { // Changed this line
                processedFiles.add(new ImageFile(file));
            }
        }

        return processedFiles;
    }

    private static List<ImageFile> extractZip(File zipFile) throws IOException {
        List<ImageFile> extractedFiles = new ArrayList<>();
        Path tempDir = Files.createTempDirectory("cropedgestudio_");

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile.toPath()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && isImageFile(entry.getName())) {
                    Path filePath = tempDir.resolve(entry.getName());
                    Files.copy(zis, filePath);
                    extractedFiles.add(new ImageFile(filePath.toFile()));
                }
            }
        }

        return extractedFiles;
    }

    private static boolean isImageFile(String fileName) {
        String lowercaseName = fileName.toLowerCase();
        return lowercaseName.endsWith(".jpg") || lowercaseName.endsWith(".jpeg") || lowercaseName.endsWith(".png");
    }
}