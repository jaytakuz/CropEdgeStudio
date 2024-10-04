package se233.cropedgestudio.utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class RobertsCrossAlgorithm implements EdgeDetectionAlgorithm {
    @Override
    public Image apply(Image input, int strength) {
        int width = (int) input.getWidth();
        int height = (int) input.getHeight();
        WritableImage output = new WritableImage(width, height);

        PixelReader reader = input.getPixelReader();
        PixelWriter writer = output.getPixelWriter();

        double strengthFactor = strength / 50.0; // Normalize strength to 0-2 range

        for (int y = 0; y < height - 1; y++) {
            for (int x = 0; x < width - 1; x++) {
                Color c00 = reader.getColor(x, y);
                Color c01 = reader.getColor(x, y + 1);
                Color c10 = reader.getColor(x + 1, y);
                Color c11 = reader.getColor(x + 1, y + 1);

                double gx = c11.getBrightness() - c00.getBrightness();
                double gy = c10.getBrightness() - c01.getBrightness();

                double magnitude = Math.sqrt(gx * gx + gy * gy) * strengthFactor;
                magnitude = Math.min(1.0, magnitude);

                Color edgeColor = Color.gray(1.0 - magnitude);
                writer.setColor(x, y, edgeColor);
            }
        }

        return output;    }
}