package se233.cropedgestudio.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

public class CropController {

    @FXML
    private ImageView imageView;

    @FXML
    private Rectangle cropRectangle;

    @FXML
    private TextField xTextField;

    @FXML
    private TextField yTextField;

    @FXML
    private TextField widthTextField;

    @FXML
    private TextField heightTextField;

    @FXML
    public void initialize() {
        // TODO: Set up drag listeners for the crop rectangle
        // TODO: Bind text fields to crop rectangle properties
    }

    @FXML
    private void handleCrop() {
        // TODO: Implement cropping logic
    }

    @FXML
    private void handleSave() {
        // TODO: Save cropped image
    }

    // TODO: Add method to set the image to be cropped
}