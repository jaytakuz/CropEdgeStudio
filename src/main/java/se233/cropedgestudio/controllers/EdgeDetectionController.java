package se233.cropedgestudio.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import se233.cropedgestudio.utils.ImageProcessor;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class EdgeDetectionController {

    @FXML private ImageView originalImageView;
    @FXML private ImageView processedImageView;
    @FXML private ComboBox<String> algorithmComboBox;
    @FXML private VBox parametersVBox;
    @FXML private Slider thresholdSlider;
    @FXML private Label thresholdLabel;

    private Image originalImage;

    @FXML
    public void initialize() {
        algorithmComboBox.getItems().addAll("Roberts Cross", "Sobel", "Laplacian");
        algorithmComboBox.getSelectionModel().selectFirst();
        algorithmComboBox.setOnAction(e -> updateParametersUI());

        thresholdSlider = new Slider(0, 255, 128);
        thresholdSlider.setShowTickLabels(true);
        thresholdSlider.setShowTickMarks(true);
        thresholdSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            thresholdLabel.setText(String.format("Threshold: %.0f", newValue));
            applyEdgeDetection();
        });

        thresholdLabel = new Label("Threshold: 128");

        updateParametersUI();
    }

    private void updateParametersUI() {
        parametersVBox.getChildren().clear();
        parametersVBox.getChildren().addAll(thresholdLabel, thresholdSlider);
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
            double threshold = thresholdSlider.getValue();
            Image processedImage = ImageProcessor.detectEdges(originalImage, selectedAlgorithm, threshold);
            processedImageView.setImage(processedImage);
        }
    }

    @FXML
    private void handleSave() {
        if (processedImageView.getImage() != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png")
            );
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try {
                    ImageIO.write(ImageProcessor.fromFXImage(processedImageView.getImage()), "png", file);
                } catch (IOException e) {
                    e.printStackTrace();
                    // TODO: Show error dialog
                }
            }
        }
    }
}