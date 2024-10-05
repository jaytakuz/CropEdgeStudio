package se233.cropedgestudio.controllers;

import javafx.fxml.FXML;

public class MainContentController {

    @FXML
    private void handleDetectEdge() {
        // This method will be called when the "Try Edge Detection" button is clicked
        MainController.getInstance().handleDetectEdge();
    }

    @FXML
    private void handleCrop() {
        // This method will be called when the "Try Cropping" button is clicked
        MainController.getInstance().handleCrop();
    }
}