package se233.cropedgestudio.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import se233.cropedgestudio.utils.ImageProcessor;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class EdgeDetectionController {

    @FXML private ComboBox<String> algorithmChoice;
    @FXML private ImageView inputImageView;
    @FXML private ImageView outputImageView;
    @FXML private VBox adjustmentBox;
    @FXML private HBox robertsBox;
    @FXML private Slider robertsStrengthSlider;
    @FXML private Label robertsStrengthLabel;
    @FXML private HBox laplacianBox;
    @FXML private RadioButton radio3x3;
    @FXML private RadioButton radio5x5;

    private Image originalImage;

    @FXML
    public void initialize() {
        algorithmChoice.getItems().addAll("Roberts Cross", "Sobel", "Laplacian");
        robertsStrengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            robertsStrengthLabel.setText(String.format("%.0f", newVal.doubleValue()));
        });
    }

    @FXML
    private void handleAlgorithmChange() {
        String selectedAlgorithm = algorithmChoice.getValue();
        adjustmentBox.setVisible(true);
        adjustmentBox.setManaged(true);

        robertsBox.setVisible(false);
        robertsBox.setManaged(false);
        laplacianBox.setVisible(false);
        laplacianBox.setManaged(false);

        switch (selectedAlgorithm) {
            case "Roberts Cross":
                robertsBox.setVisible(true);
                robertsBox.setManaged(true);
                break;
            case "Laplacian":
                laplacianBox.setVisible(true);
                laplacianBox.setManaged(true);
                break;
            case "Sobel":
                // No adjustments for Sobel
                break;
            default:
                adjustmentBox.setVisible(false);
                adjustmentBox.setManaged(false);
        }
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
            inputImageView.setImage(originalImage);
        }
    }

    @FXML
    private void applyEdgeDetection() {
        if (originalImage == null) {
            showAlert("Error", "Please load an image first.", Alert.AlertType.WARNING);
            return;
        }

        String algorithm = algorithmChoice.getValue();
        if (algorithm == null) {
            showAlert("Error", "Please select an algorithm.", Alert.AlertType.WARNING);
            return;
        }

        try {
            Image processedImage;
            switch (algorithm) {
                case "Roberts Cross":
                    int strength = (int) robertsStrengthSlider.getValue();
                    processedImage = ImageProcessor.applyRobertsCross(originalImage, strength);
                    break;
                case "Sobel":
                    processedImage = ImageProcessor.applySobel(originalImage);
                    break;
                case "Laplacian":
                    int maskSize = radio5x5.isSelected() ? 5 : 3;
                    processedImage = ImageProcessor.applyLaplacian(originalImage, maskSize);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
            }
            outputImageView.setImage(processedImage);
        } catch (Exception e) {
            showAlert("Error", "Failed to apply edge detection: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBatchProcessing() {
        if (algorithmChoice.getValue() == null) {
            showAlert("Error", "Please select an algorithm first.", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            DirectoryChooser outputChooser = new DirectoryChooser();
            outputChooser.setTitle("Select Output Directory");
            File outputDir = outputChooser.showDialog(null);

            if (outputDir != null) {
                processBatch(selectedFiles, outputDir);
            }
        }
    }

    private void processBatch(List<File> files, File outputDir) {
        String algorithm = algorithmChoice.getValue();
        int strength = (int) robertsStrengthSlider.getValue();
        int maskSize = radio5x5.isSelected() ? 5 : 3;

        for (File file : files) {
            try {
                Image image = new Image(file.toURI().toString());
                Image processedImage;

                switch (algorithm) {
                    case "Roberts Cross":
                        processedImage = ImageProcessor.applyRobertsCross(image, strength);
                        break;
                    case "Sobel":
                        processedImage = ImageProcessor.applySobel(image);
                        break;
                    case "Laplacian":
                        processedImage = ImageProcessor.applyLaplacian(image, maskSize);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
                }

                File outputFile = new File(outputDir, "processed_" + file.getName());
                ImageIO.write(ImageProcessor.fromFXImage(processedImage), "png", outputFile);
            } catch (IOException e) {
                showAlert("Error", "Failed to process " + file.getName() + ": " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
        showAlert("Success", "Batch processing completed.", Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}