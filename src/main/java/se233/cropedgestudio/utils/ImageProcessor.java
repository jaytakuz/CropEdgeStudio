package se233.cropedgestudio.utils;

import javafx.scene.image.*;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class ImageProcessor {

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