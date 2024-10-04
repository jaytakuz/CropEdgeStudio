package se233.cropedgestudio.utils;

import javafx.scene.image.*;
import javafx.scene.paint.Color;


public interface EdgeDetectionAlgorithm {
    Image apply(Image input, int strength);
}