package se233.cropedgestudio.utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class LaplacianAlgorithm implements EdgeDetectionAlgorithm {
    @Override
    public Image apply(Image input, int strength) {
        int width = (int) input.getWidth();
        int height = (int) input.getHeight();
        WritableImage output = new WritableImage(width, height);

        PixelReader reader = input.getPixelReader();
        PixelWriter writer = output.getPixelWriter();

        int[][] kernel;
        if (strength == 3) {
            kernel = new int[][]{{0, -1, 0}, {-1, 4, -1}, {0, -1, 0}};
        } else {
            kernel = new int[][]{{-1, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1},
                    {-1, -1, 24, -1, -1},
                    {-1, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1}};
        }

        int offset = strength / 2;

        for (int y = offset; y < height - offset; y++) {
            for (int x = offset; x < width - offset; x++) {
                double sum = 0;
                for (int i = -offset; i <= offset; i++) {
                    for (int j = -offset; j <= offset; j++) {
                        Color c = reader.getColor(x + j, y + i);
                        sum += c.getBrightness() * kernel[i + offset][j + offset];
                    }
                }
                sum = Math.abs(sum);
                sum = Math.min(1.0, sum); // Clamp to 0-1 range
                Color edgeColor = Color.gray(1.0 - sum); // Invert for black edges on white background
                writer.setColor(x, y, edgeColor);
            }
        }

        return output;    }
}