package se233.cropedgestudio.utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class SobelAlgorithm implements EdgeDetectionAlgorithm {
    @Override
    public Image apply(Image input, int threshold) {
        int width = (int) input.getWidth();
        int height = (int) input.getHeight();
        WritableImage output = new WritableImage(width, height);
        PixelReader reader = input.getPixelReader();
        PixelWriter writer = output.getPixelWriter();

        int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

        double maxGradient = 0;

        // First pass to find the maximum gradient
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double gx = 0, gy = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        Color c = reader.getColor(x + j, y + i);
                        double brightness = c.getBrightness();
                        gx += brightness * sobelX[i + 1][j + 1];
                        gy += brightness * sobelY[i + 1][j + 1];
                    }
                }
                double gradient = Math.sqrt(gx * gx + gy * gy);
                if (gradient > maxGradient) {
                    maxGradient = gradient;
                }
            }
        }

        // Second pass to apply the threshold
        double thresholdValue = (threshold / 100.0) * maxGradient;
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double gx = 0, gy = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        Color c = reader.getColor(x + j, y + i);
                        double brightness = c.getBrightness();
                        gx += brightness * sobelX[i + 1][j + 1];
                        gy += brightness * sobelY[i + 1][j + 1];
                    }
                }
                double gradient = Math.sqrt(gx * gx + gy * gy);
                Color edgeColor = gradient > thresholdValue ? Color.BLACK : Color.WHITE;
                writer.setColor(x, y, edgeColor);
            }
        }

        return output;
    }
}