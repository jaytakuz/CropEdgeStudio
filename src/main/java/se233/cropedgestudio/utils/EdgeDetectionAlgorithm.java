package se233.cropedgestudio.utils;

import javafx.scene.image.*;


public interface EdgeDetectionAlgorithm {
    Image apply(Image input, int strength);
}