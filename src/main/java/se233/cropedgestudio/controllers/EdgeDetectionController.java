package se233.cropedgestudio.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import se233.cropedgestudio.models.ProcessingJob;
import se233.cropedgestudio.services.BatchProcessorService;
import se233.cropedgestudio.utils.ImageProcessor;
import se233.cropedgestudio.utils.ImageProcessingException;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javafx.concurrent.Task;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import java.util.stream.Collectors;

public class EdgeDetectionController {

    @FXML private TabPane algorithmTabPane;
    @FXML private ComboBox<String> algorithmChoice;
    @FXML private ImageView robertsBeforeImageView;
    @FXML private ImageView robertsAfterImageView;
    @FXML private Slider robertsStrengthSlider;
    @FXML private Label robertsStrengthLabel;
    @FXML private ImageView sobelBeforeImageView;
    @FXML private ImageView sobelAfterImageView;
    @FXML private ImageView laplacianBeforeImageView;
    @FXML private ImageView laplacianAfterImageView;
    @FXML private RadioButton radio3x3;
    @FXML private RadioButton radio5x5;

    @FXML
    private Label statusLabel;

    @FXML
    private ProgressBar progressBar;

    private Image originalImage;

    @FXML
    public void initialize() {
        algorithmChoice.getItems().addAll("Roberts Cross", "Sobel", "Laplacian");
        algorithmChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                algorithmTabPane.getSelectionModel().select(algorithmChoice.getSelectionModel().getSelectedIndex());
            }
        });

        robertsStrengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            robertsStrengthLabel.setText(String.format("%.0f", newVal.doubleValue()));
        });
    }

    @FXML
    private void handleOpenImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                originalImage = new Image(selectedFile.toURI().toString());
                robertsBeforeImageView.setImage(originalImage);
                sobelBeforeImageView.setImage(originalImage);
                laplacianBeforeImageView.setImage(originalImage);
                showAlert("Success", "Image loaded successfully.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Failed to load image: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void applyRobertsCross() {
        if (originalImage != null) {
            try {
                int strength = (int) robertsStrengthSlider.getValue();
                Image processedImage = ImageProcessor.applyRobertsCross(originalImage, strength);
                robertsAfterImageView.setImage(processedImage);
                showAlert("Success", "Roberts Cross applied successfully.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Failed to apply Roberts Cross: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Error", "Please load an image first.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void applySobel() {
        if (originalImage != null) {
            try {
                Image processedImage = ImageProcessor.applySobel(originalImage);
                sobelAfterImageView.setImage(processedImage);
                showAlert("Success", "Sobel filter applied successfully.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Failed to apply Sobel filter: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Error", "Please load an image first.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void applyLaplacian() {
        if (originalImage != null) {
            try {
                int maskSize = radio5x5.isSelected() ? 5 : 3;
                Image processedImage = ImageProcessor.applyLaplacian(originalImage, maskSize);
                laplacianAfterImageView.setImage(processedImage);
                showAlert("Success", "Laplacian filter applied successfully.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Failed to apply Laplacian filter: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Error", "Please load an image first.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleSave() {
        ImageView currentAfterImageView = (ImageView) algorithmTabPane.getSelectionModel().getSelectedItem().getContent().lookup("ImageView");
        if (currentAfterImageView != null && currentAfterImageView.getImage() != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png")
            );
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try {
                    ImageIO.write(ImageProcessor.fromFXImage(currentAfterImageView.getImage()), "png", file);
                    showAlert("Success", "Image saved successfully.", Alert.AlertType.INFORMATION);
                } catch (IOException e) {
                    showAlert("Error", "Failed to save image: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        } else {
            showAlert("Error", "No processed image to save.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleReset() {
        if (originalImage != null) {
            robertsBeforeImageView.setImage(originalImage);
            sobelBeforeImageView.setImage(originalImage);
            laplacianBeforeImageView.setImage(originalImage);
            robertsAfterImageView.setImage(null);
            sobelAfterImageView.setImage(null);
            laplacianAfterImageView.setImage(null);
            showAlert("Success", "Images reset to original.", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Error", "No original image to reset to.", Alert.AlertType.WARNING);
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
                String algorithm = algorithmChoice.getValue();
                int strength = (int) robertsStrengthSlider.getValue();
                int maskSize = radio5x5.isSelected() ? 5 : 3;

                List<ProcessingJob> jobs = selectedFiles.stream()
                        .map(file -> new ProcessingJob(
                                file,
                                new File(outputDir, "processed_" + file.getName()),
                                algorithm,
                                strength,
                                maskSize))
                        .collect(Collectors.toList());

                BatchProcessorService batchService = new BatchProcessorService(jobs, Runtime.getRuntime().availableProcessors());

                progressBar.progressProperty().bind(batchService.progressProperty());
                statusLabel.textProperty().bind(batchService.messageProperty());

                batchService.setOnSucceeded(e -> {
                    showAlert("Success", "Batch processing completed successfully.", Alert.AlertType.INFORMATION);
                    progressBar.progressProperty().unbind();
                    statusLabel.textProperty().unbind();
                });

                batchService.setOnFailed(e -> {
                    showAlert("Error", "Batch processing failed: " + batchService.getException().getMessage(), Alert.AlertType.ERROR);
                    progressBar.progressProperty().unbind();
                    statusLabel.textProperty().unbind();
                });

                batchService.start();
            }
        }
    }

    private void processBatch(File inputDir, File outputDir) {
        File[] imageFiles = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"));

        if (imageFiles == null || imageFiles.length == 0) {
            showAlert("Error", "No image files found in the selected directory.", Alert.AlertType.WARNING);
            return;
        }

        ProgressIndicator progressIndicator = new ProgressIndicator();
        Label progressLabel = new Label("Processing images...");
        VBox progressBox = new VBox(10, progressLabel, progressIndicator);
        progressBox.setAlignment(Pos.CENTER);

        Stage progressStage = new Stage();
        progressStage.initModality(Modality.APPLICATION_MODAL);
        progressStage.setScene(new Scene(progressBox, 250, 100));
        progressStage.show();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int total = imageFiles.length;
                for (int i = 0; i < total; i++) {
                    File file = imageFiles[i];
                    Image image = new Image(file.toURI().toString());
                    Image processedImage;

                    switch (algorithmChoice.getValue()) {
                        case "Roberts Cross":
                            processedImage = ImageProcessor.applyRobertsCross(image, (int) robertsStrengthSlider.getValue());
                            break;
                        case "Sobel":
                            processedImage = ImageProcessor.applySobel(image);
                            break;
                        case "Laplacian":
                            processedImage = ImageProcessor.applyLaplacian(image, radio5x5.isSelected() ? 5 : 3);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + algorithmChoice.getValue());
                    }

                    File outputFile = new File(outputDir, "processed_" + file.getName());
                    ImageIO.write(ImageProcessor.fromFXImage(processedImage), "png", outputFile);

                    updateProgress(i + 1, total);
                    updateMessage("Processing image " + (i + 1) + " of " + total);
                }
                return null;
            }
        };

        progressIndicator.progressProperty().bind(task.progressProperty());
        progressLabel.textProperty().bind(task.messageProperty());

        task.setOnSucceeded(e -> {
            progressStage.close();
            showAlert("Success", "Batch processing completed successfully.", Alert.AlertType.INFORMATION);
        });

        task.setOnFailed(e -> {
            progressStage.close();
            showAlert("Error", "Batch processing failed: " + task.getException().getMessage(), Alert.AlertType.ERROR);
        });

        new Thread(task).start();
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


}