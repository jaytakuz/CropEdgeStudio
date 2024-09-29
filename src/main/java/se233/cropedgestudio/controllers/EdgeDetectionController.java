package se233.cropedgestudio.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import se233.cropedgestudio.utils.ImageProcessor;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class EdgeDetectionController {

    @FXML private ComboBox<String> algorithmChoice;
    @FXML private StackPane inputImageStack;
    @FXML private ImageView inputImageView;
    @FXML private ImageView outputImageView;
    @FXML private Label dragDropLabel;
    @FXML private VBox adjustmentBox;
    @FXML private HBox robertsBox;
    @FXML private Slider robertsStrengthSlider;
    @FXML private Label robertsStrengthLabel;
    @FXML private HBox laplacianBox;
    @FXML private RadioButton radio3x3;
    @FXML private RadioButton radio5x5;
    @FXML private Label statusLabel;

    @FXML private Button previousButton;
    @FXML private Button nextButton;

    private List<Image> imagesList = new ArrayList<>();
    private int currentIndex = 0;

    @FXML
    public void initialize() {
        algorithmChoice.getItems().addAll("Roberts Cross", "Sobel", "Laplacian");
        robertsStrengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            robertsStrengthLabel.setText(String.format("%.0f", newVal.doubleValue()));
        });
        setupDragAndDrop();
    }

    @FXML
    private void handlePrevious() {
        if (currentIndex > 0) {
            currentIndex--;
            displayCurrentImage();
        }
    }

    @FXML
    private void handleNext() {
        if (currentIndex < imagesList.size() - 1) {
            currentIndex++;
            displayCurrentImage();
        }
    }

    @FXML
    private void handleClear() {
        imagesList.clear();
        inputImageView.setImage(null);
        outputImageView.setImage(null);
        currentIndex = 0;
        updateNavigationButtons();
        setStatus("All images cleared");
    }

    private void displayCurrentImage() {
        if (!imagesList.isEmpty()) {
            inputImageView.setImage(imagesList.get(currentIndex));
            outputImageView.setImage(null);
            setStatus("Showing image " + (currentIndex + 1) + " of " + imagesList.size());
        }
        updateNavigationButtons();
    }

    private void updateNavigationButtons() {
        previousButton.setDisable(currentIndex <= 0);
        nextButton.setDisable(currentIndex >= imagesList.size() - 1);
    }

    private void setStatus(String message) {
        statusLabel.setText("Status: " + message);
    }

    private void setupDragAndDrop() {
        inputImageStack.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
            }
            event.consume();
        });

        inputImageStack.setOnDragDropped(event -> {
            boolean success = false;
            if (event.getDragboard().hasFiles()) {
                List<File> files = event.getDragboard().getFiles();
                for (File file : files) {
                    if (file.getName().toLowerCase().endsWith(".zip")) {
                        processZipFile(file);
                        success = true;
                    } else if (isImageFile(file)) {
                        loadImage(file);
                        success = true;
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private boolean isImageFile(String fileName) {
        String name = fileName.toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
    }

    private boolean isImageFile(File file) {
        return isImageFile(file.getName());
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
                break;
            default:
                adjustmentBox.setVisible(false);
                adjustmentBox.setManaged(false);
        }
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("ZIP Files", "*.zip")
        );
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            if (selectedFile.getName().toLowerCase().endsWith(".zip")) {
                processZipFile(selectedFile);
            } else {
                loadImage(selectedFile);
            }
        }
    }

    private void processZipFile(File zipFile) {
        Set<String> processedNames = new HashSet<>();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (!entry.isDirectory() && isImageFile(entryName) && !processedNames.contains(entryName)) {
                    processedNames.add(entryName);
                    File tempFile = File.createTempFile("temp", "." + getFileExtension(entryName));
                    tempFile.deleteOnExit();

                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }

                    loadImage(tempFile);
                }
                zis.closeEntry();
            }
            if (!imagesList.isEmpty()) {
                currentIndex = 0;
                displayCurrentImage();
            }

            setStatus("ZIP file processed successfully. Loaded " + processedNames.size() + " images.");
        } catch (IOException e) {
            showAlert("Error", "Failed to process ZIP file: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return fileName.substring(lastIndexOf + 1);
    }

    private void loadImage(File file) {
        Image image = new Image(file.toURI().toString());
        imagesList.add(image);
        if (imagesList.size() == 1) {
            currentIndex = 0;
        } else {
            currentIndex = imagesList.size() - 1;
        }
        displayCurrentImage();
        dragDropLabel.setVisible(false);
        updateNavigationButtons();
    }

    @FXML
    private void applyEdgeDetection() {
        if (imagesList.isEmpty() || currentIndex < 0 || currentIndex >= imagesList.size()) {
            showAlert("Error", "Please load an image first.", Alert.AlertType.WARNING);
            return;
        }

        Image currentImage = imagesList.get(currentIndex);

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
                    processedImage = ImageProcessor.applyRobertsCross(currentImage, strength);
                    break;
                case "Sobel":
                    processedImage = ImageProcessor.applySobel(currentImage);
                    break;
                case "Laplacian":
                    int maskSize = radio5x5.isSelected() ? 5 : 3;
                    processedImage = ImageProcessor.applyLaplacian(currentImage, maskSize);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
            }
            outputImageView.setImage(processedImage);
            setStatus("Edge detection applied successfully");
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

    @FXML
    private void handleSave() {
        if (outputImageView.getImage() == null) {
            showAlert("Error", "No processed image to save.", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                ImageIO.write(ImageProcessor.fromFXImage(outputImageView.getImage()), "png", file);
                setStatus("Image saved successfully");
            } catch (IOException e) {
                showAlert("Error", "Failed to save image: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleReset() {
        if (!imagesList.isEmpty()) {
            outputImageView.setImage(null);
            displayCurrentImage();
            setStatus("Image reset");
        } else {
            showAlert("Error", "No image to reset", Alert.AlertType.WARNING);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}