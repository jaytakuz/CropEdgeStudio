package se233.cropedgestudio.utils;

import javafx.scene.image.*;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class ImageProcessor {

    public static Image detectEdges(Image input, String algorithm, double threshold) {
        int width = (int) input.getWidth();
        int height = (int) input.getHeight();
        WritableImage output = new WritableImage(width, height);

        PixelReader reader = input.getPixelReader();
        PixelWriter writer = output.getPixelWriter();

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                Color color = applyEdgeDetection(reader, x, y, algorithm, threshold);
                writer.setColor(x, y, color);
            }
        }

        return output;
    }

    private static Color applyEdgeDetection(PixelReader reader, int x, int y, String algorithm, double threshold) {
        switch (algorithm) {
            case "Roberts Cross":
                return robertsCross(reader, x, y, threshold);
            case "Sobel":
                return sobel(reader, x, y, threshold);
            case "Laplacian":
                return laplacian(reader, x, y, threshold);
            default:
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
    }

    private static Color robertsCross(PixelReader reader, int x, int y, double threshold) {
        double gx = intensity(reader.getColor(x+1, y+1)) - intensity(reader.getColor(x, y));
        double gy = intensity(reader.getColor(x+1, y)) - intensity(reader.getColor(x, y+1));
        return createEdgeColor(gx, gy, threshold);
    }

    private static Color sobel(PixelReader reader, int x, int y, double threshold) {
        double gx = intensity(reader.getColor(x+1, y-1)) + 2*intensity(reader.getColor(x+1, y)) + intensity(reader.getColor(x+1, y+1))
                - intensity(reader.getColor(x-1, y-1)) - 2*intensity(reader.getColor(x-1, y)) - intensity(reader.getColor(x-1, y+1));
        double gy = intensity(reader.getColor(x-1, y+1)) + 2*intensity(reader.getColor(x, y+1)) + intensity(reader.getColor(x+1, y+1))
                - intensity(reader.getColor(x-1, y-1)) - 2*intensity(reader.getColor(x, y-1)) - intensity(reader.getColor(x+1, y-1));
        return createEdgeColor(gx, gy, threshold);
    }

    private static Color laplacian(PixelReader reader, int x, int y, double threshold) {
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

    private static Color createEdgeColor(double gx, double gy, double threshold) {
        double magnitude = Math.sqrt(gx*gx + gy*gy);
        int intensity = (magnitude > threshold) ? 255 : 0;
        return Color.grayRgb(intensity);
    }

    public static BufferedImage fromFXImage(Image img) {
        int width = (int) img.getWidth();
        int height = (int) img.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        PixelReader pixelReader = img.getPixelReader();
        WritableRaster raster = bufferedImage.getRaster();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                javafx.scene.paint.Color color = pixelReader.getColor(x, y);
                raster.setPixel(x, y, new int[]{
                        (int) (color.getRed() * 255),
                        (int) (color.getGreen() * 255),
                        (int) (color.getBlue() * 255),
                        (int) (color.getOpacity() * 255)
                });
            }
        }

        return bufferedImage;
    }
}