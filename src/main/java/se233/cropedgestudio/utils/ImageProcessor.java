package se233.cropedgestudio.utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class ImageProcessor {

    public static Image detectEdges(Image input, String algorithm) {
        int width = (int) input.getWidth();
        int height = (int) input.getHeight();
        WritableImage output = new WritableImage(width, height);

        PixelReader reader = input.getPixelReader();
        PixelWriter writer = output.getPixelWriter();

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                Color color = applyEdgeDetection(reader, x, y, algorithm);
                writer.setColor(x, y, color);
            }
        }

        return output;
    }

    private static Color applyEdgeDetection(PixelReader reader, int x, int y, String algorithm) {
        switch (algorithm) {
            case "Roberts Cross":
                return robertsCross(reader, x, y);
            case "Sobel":
                return sobel(reader, x, y);
            case "Laplacian":
                return laplacian(reader, x, y);
            default:
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
    }

    private static Color robertsCross(PixelReader reader, int x, int y) {
        double gx = intensity(reader.getColor(x+1, y+1)) - intensity(reader.getColor(x, y));
        double gy = intensity(reader.getColor(x+1, y)) - intensity(reader.getColor(x, y+1));
        return createEdgeColor(gx, gy);
    }

    private static Color sobel(PixelReader reader, int x, int y) {
        double gx = intensity(reader.getColor(x+1, y-1)) + 2*intensity(reader.getColor(x+1, y)) + intensity(reader.getColor(x+1, y+1))
                - intensity(reader.getColor(x-1, y-1)) - 2*intensity(reader.getColor(x-1, y)) - intensity(reader.getColor(x-1, y+1));
        double gy = intensity(reader.getColor(x-1, y+1)) + 2*intensity(reader.getColor(x, y+1)) + intensity(reader.getColor(x+1, y+1))
                - intensity(reader.getColor(x-1, y-1)) - 2*intensity(reader.getColor(x, y-1)) - intensity(reader.getColor(x+1, y-1));
        return createEdgeColor(gx, gy);
    }

    private static Color laplacian(PixelReader reader, int x, int y) {
        double laplacian = 4 * intensity(reader.getColor(x, y))
                - intensity(reader.getColor(x+1, y))
                - intensity(reader.getColor(x-1, y))
                - intensity(reader.getColor(x, y+1))
                - intensity(reader.getColor(x, y-1));
        return Color.grayRgb((int) Math.min(Math.abs(laplacian) * 255, 255));
    }

    private static double intensity(Color color) {
        return (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
    }

    private static Color createEdgeColor(double gx, double gy) {
        double magnitude = Math.sqrt(gx*gx + gy*gy);
        int intensity = (int) Math.min(magnitude * 255, 255);
        return Color.grayRgb(intensity);
    }
}