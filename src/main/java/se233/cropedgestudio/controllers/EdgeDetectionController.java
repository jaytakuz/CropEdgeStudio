package se233.cropedgestudio.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import se233.cropedgestudio.utils.ImageProcessor;

import java.io.File;

public class EdgeDetectionController {

    @FXML
    private ImageView originalImageView;

    @FXML
    private ImageView processedImageView;

    @FXML
    private ComboBox<String> algorithmComboBox;

    @FXML
    private VBox parametersVBox;

    private Image originalImage;

    @FXML
    public void initialize() {
        algorithmComboBox.getItems().addAll("Roberts Cross", "Sobel", "Laplacian");
        algorithmComboBox.getSelectionModel().selectFirst();
        algorithmComboBox.setOnAction(e -> applyEdgeDetection());
    }

    @FXML
    private void handleOpenImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            originalImage = new Image(selectedFile.toURI().toString());
            originalImageView.setImage(originalImage);
            applyEdgeDetection();
        }
    }

    @FXML
    private void applyEdgeDetection() {
        if (originalImage != null) {
            String selectedAlgorithm = algorithmComboBox.getValue();
            Image processedImage = ImageProcessor.detectEdges(originalImage, selectedAlgorithm);
            processedImageView.setImage(processedImage);
        }
    }

    @FXML
    private void handleSave() {
        // TODO: Implement save functionality
    }
}