package se233.cropedgestudio.utils;

import javafx.scene.image.*;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class ImageProcessor {

//    public static Image detectEdges(Image input, String algorithm, double threshold) {
//        int width = (int) input.getWidth();
//        int height = (int) input.getHeight();
//        WritableImage output = new WritableImage(width, height);
//
//        PixelReader reader = input.getPixelReader();
//        PixelWriter writer = output.getPixelWriter();
//
//        for (int y = 1; y < height - 1; y++) {
//            for (int x = 1; x < width - 1; x++) {
//                Color color = applyEdgeDetection(reader, x, y, algorithm, threshold);
//                writer.setColor(x, y, color);
//            }
//        }
//
//        return output;
//    }
//
//    private static Color applyEdgeDetection(PixelReader reader, int x, int y, String algorithm, double threshold) {
//        switch (algorithm) {
//            case "Roberts Cross":
//                return robertsCross(reader, x, y, threshold);
//            case "Sobel":
//                return sobel(reader, x, y, threshold);
//            case "Laplacian":
//                return laplacian(reader, x, y, threshold);
//            default:
//                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
//        }
//    }
//
//    private static Color robertsCross(PixelReader reader, int x, int y, double threshold) {
//        double gx = intensity(reader.getColor(x+1, y+1)) - intensity(reader.getColor(x, y));
//        double gy = intensity(reader.getColor(x+1, y)) - intensity(reader.getColor(x, y+1));
//        return createEdgeColor(gx, gy, threshold);
//    }
//
//    private static Color sobel(PixelReader reader, int x, int y, double threshold) {
//        double gx = intensity(reader.getColor(x+1, y-1)) + 2*intensity(reader.getColor(x+1, y)) + intensity(reader.getColor(x+1, y+1))
//                - intensity(reader.getColor(x-1, y-1)) - 2*intensity(reader.getColor(x-1, y)) - intensity(reader.getColor(x-1, y+1));
//        double gy = intensity(reader.getColor(x-1, y+1)) + 2*intensity(reader.getColor(x, y+1)) + intensity(reader.getColor(x+1, y+1))
//                - intensity(reader.getColor(x-1, y-1)) - 2*intensity(reader.getColor(x, y-1)) - intensity(reader.getColor(x+1, y-1));
//        return createEdgeColor(gx, gy, threshold);
//    }
//
//    private static Color laplacian(PixelReader reader, int x, int y, double threshold) {
//        double laplacian = 4 * intensity(reader.getColor(x, y))
//                - intensity(reader.getColor(x+1, y))
//                - intensity(reader.getColor(x-1, y))
//                - intensity(reader.getColor(x, y+1))
//                - intensity(reader.getColor(x, y-1));
//        return Color.grayRgb((int) Math.min(Math.abs(laplacian) * 255, 255));
//    }
//
//    private static double intensity(Color color) {
//        return (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
//    }
//
//    private static Color createEdgeColor(double gx, double gy, double threshold) {
//        double magnitude = Math.sqrt(gx*gx + gy*gy);
//        int intensity = (magnitude > threshold) ? 255 : 0;
//        return Color.grayRgb(intensity);
//    }

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

    public static Image applyRobertsCross(Image input, int strength) {
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

        return output;
    }

    public static Image applySobel(Image input) {
        int width = (int) input.getWidth();
        int height = (int) input.getHeight();
        WritableImage output = new WritableImage(width, height);

        PixelReader reader = input.getPixelReader();
        PixelWriter writer = output.getPixelWriter();

        int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

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
                double magnitude = Math.sqrt(gx * gx + gy * gy);
                magnitude = Math.min(1.0, magnitude); // Clamp to 0-1 range
                Color edgeColor = Color.gray(1.0 - magnitude); // Invert for black edges on white background
                writer.setColor(x, y, edgeColor);
            }
        }

        return output;
    }

    public static Image applyLaplacian(Image input, int maskSize) {
        int width = (int) input.getWidth();
        int height = (int) input.getHeight();
        WritableImage output = new WritableImage(width, height);

        PixelReader reader = input.getPixelReader();
        PixelWriter writer = output.getPixelWriter();

        int[][] kernel;
        if (maskSize == 3) {
            kernel = new int[][]{{0, -1, 0}, {-1, 4, -1}, {0, -1, 0}};
        } else {
            kernel = new int[][]{{-1, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1},
                    {-1, -1, 24, -1, -1},
                    {-1, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1}};
        }

        int offset = maskSize / 2;

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

        return output;
    }
}