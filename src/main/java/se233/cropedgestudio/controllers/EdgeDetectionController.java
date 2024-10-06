package se233.cropedgestudio.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import se233.cropedgestudio.utils.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Label;
import javafx.geometry.Insets;

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
    @FXML private ProgressBar progressBar;
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private TextField sobelThresholdField;
    @FXML private HBox sobelBox;

    private Map<String, EdgeDetectionAlgorithm> algorithms;
    private List<Image> imagesList = new ArrayList<>();
    private int currentIndex = 0;

    @FXML
    public void initialize() {
        algorithms = new HashMap<>();
        algorithms.put("Roberts Cross", new RobertsCrossAlgorithm());
        algorithms.put("Sobel", new SobelAlgorithm());
        algorithms.put("Laplacian", new LaplacianAlgorithm());
        algorithmChoice.getItems().addAll("Roberts Cross", "Sobel", "Laplacian");
        robertsStrengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            robertsStrengthLabel.setText(String.format("%.0f", newVal.doubleValue()));
        });
        setupDragAndDrop();

        updateNavigationButtons();

        // Load CSS after the scene is fully loaded
        Platform.runLater(() -> {
            String cssPath = "/se233/cropedgestudio/styles/edge-detection.css";
            String css = getClass().getResource(cssPath).toExternalForm();
            inputImageView.getScene().getStylesheets().add(css);
        });

        sobelThresholdField = new TextField("50");
        sobelThresholdField.setPromptText("between 0-100 %)");
        sobelBox = new HBox(10, new Label("Sobel Threshold Percentage:"), sobelThresholdField);
        sobelBox.setAlignment(Pos.CENTER);
        sobelBox.setVisible(false);
        sobelBox.setManaged(false);

        adjustmentBox.getChildren().add(sobelBox);
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
        dragDropLabel.setVisible(true);
        currentIndex = 0;
        updateNavigationButtons();
        setStatus("All images cleared");
    }

    private void displayCurrentImage() {
        if (!imagesList.isEmpty()) {
            Image currentImage = imagesList.get(currentIndex);
            inputImageView.setImage(currentImage);
            outputImageView.setImage(null);
            dragDropLabel.setVisible(false);
            setStatus("Showing image " + (currentIndex + 1) + " of " + imagesList.size());
        } else {
            inputImageView.setImage(null);
            outputImageView.setImage(null);
            dragDropLabel.setVisible(true);
            setStatus("No images loaded");
        }
        updateNavigationButtons();
    }

    private void updateNavigationButtons() {
        boolean hasMultipleImages = imagesList.size() > 1;
        previousButton.setVisible(hasMultipleImages);
        nextButton.setVisible(hasMultipleImages);
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
        sobelBox.setVisible(false);
        sobelBox.setManaged(false);

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
                sobelBox.setVisible(true);
                sobelBox.setManaged(true);
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
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && isImageFile(entry.getName())) {
                    // Create a temporary file for each image in the zip
                    File tempFile = File.createTempFile("temp", "." + getFileExtension(entry.getName()));
                    tempFile.deleteOnExit();

                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }

                    Image image = new Image(tempFile.toURI().toString());
                    if (image.getWidth() > 0 && image.getHeight() > 0) {
                        imagesList.add(image);
                    }
                }
                zis.closeEntry();
            }
            if (!imagesList.isEmpty()) {
                currentIndex = 0;
                displayCurrentImage();
            }
            setStatus("ZIP file processed successfully. Loaded " + imagesList.size() + " images.");
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
        currentIndex = imagesList.size() - 1;
        displayCurrentImage();
        dragDropLabel.setVisible(false);
    }

    @FXML
    private void applyEdgeDetection() {
        if (imagesList.isEmpty() || currentIndex < 0 || currentIndex >= imagesList.size()) {
            showAlert("Error", "Please load an image first.", Alert.AlertType.WARNING);
            return;
        }

        Image currentImage = imagesList.get(currentIndex);

        String algorithmName = algorithmChoice.getValue();
        if (algorithmName == null) {
            showAlert("Error", "Please select an algorithm.", Alert.AlertType.WARNING);
            return;
        }

        Task<Image> task = new Task<>() {
            @Override
            protected Image call() throws Exception {
                updateProgress(0, 100);
                EdgeDetectionAlgorithm algorithm = algorithms.get(algorithmName);
                if (algorithm == null) {
                    throw new ImageProcessingException("Unknown algorithm: " + algorithmName);
                }
                int strength = (int) robertsStrengthSlider.getValue();
                int maskSize = radio5x5.isSelected() ? 5 : 3;
                int threshold = 50;

                if (algorithmName.equals("Sobel")) {
                    try {
                        threshold = Integer.parseInt(sobelThresholdField.getText());
                        if (threshold < 0 || threshold > 100) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException e) {
                        throw new ImageProcessingException("Invalid Sobel threshold. Please enter a number between 0 and 100.");
                    }
                }

                updateProgress(50, 100);
                Image processedImage = algorithm.apply(currentImage,
                        algorithmName.equals("Laplacian") ? maskSize :
                                (algorithmName.equals("Sobel") ? threshold : strength));
                updateProgress(100, 100);
                return processedImage;
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());

        task.setOnSucceeded(e -> {
            outputImageView.setImage(task.getValue());
            setStatus("Edge detection applied successfully");
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
        });

        task.setOnFailed(e -> {
            showAlert("Error", task.getException().getMessage(), Alert.AlertType.ERROR);
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
        });

        new Thread(task).start();
    }

    @FXML
    private void handleBatchProcessing() {
        if (imagesList.isEmpty()) {
            showAlert("Error", "No images to process. Please load images first.", Alert.AlertType.WARNING);
            return;
        }

        String algorithm = algorithmChoice.getValue();
        if (algorithm == null) {
            showAlert("Error", "Please select an edge detection algorithm.", Alert.AlertType.WARNING);
            return;
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Output Directory");
        File outputDir = directoryChooser.showDialog(null);
        if (outputDir != null) {
            processBatchMultithreaded(outputDir, algorithm);
        }
    }

    private void processBatchMultithreaded(File outputDir, String algorithm) {
        Task<Void> batchTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int total = imagesList.size();
                EdgeDetectionAlgorithm edgeAlgorithm = algorithms.get(algorithm);
                if (edgeAlgorithm == null) {
                    throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
                }

                int numThreads = Runtime.getRuntime().availableProcessors();
                ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

                AtomicInteger completedTasks = new AtomicInteger(0);

                List<Future<?>> futures = new ArrayList<>();

                for (int i = 0; i < total; i++) {
                    final int index = i;
                    futures.add(executorService.submit(() -> {
                        try {
                            Image originalImage = imagesList.get(index);
                            int strength = (int) robertsStrengthSlider.getValue();
                            int maskSize = radio5x5.isSelected() ? 5 : 3;
                            int threshold = algorithm.equals("Sobel") ? Integer.parseInt(sobelThresholdField.getText()) : 0;

                            Image processedImage = edgeAlgorithm.apply(originalImage,
                                    algorithm.equals("Laplacian") ? maskSize :
                                            (algorithm.equals("Sobel") ? threshold : strength));

                            // Extract original filename and create new filename
                            String originalFilename = new File(originalImage.getUrl().substring(5)).getName();
                            String baseName = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
                            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
                            String newFilename = baseName + "_detected" + extension;

                            File outputFile = new File(outputDir, newFilename);

                            // Handle naming conflicts
                            int counter = 1;
                            while (outputFile.exists()) {
                                newFilename = baseName + "_detected_" + counter + extension;
                                outputFile = new File(outputDir, newFilename);
                                counter++;
                            }

                            ImageIO.write(ImageProcessor.fromFXImage(processedImage), "png", outputFile);

                            int completed = completedTasks.incrementAndGet();
                            updateProgress(completed, total);
                            updateMessage(String.format("Processed %d%% (%d of %d images)",
                                    (completed * 100) / total, completed, total));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }));
                }

                for (Future<?> future : futures) {
                    future.get(); // Wait for each task to complete
                }

                executorService.shutdown();
                return null;
            }
        };

        Stage progressStage = new Stage();
        progressStage.setTitle("Batch Processing");
        ProgressBar progressBar = new ProgressBar();
        progressBar.progressProperty().bind(batchTask.progressProperty());
        Label statusLabel = new Label();
        statusLabel.textProperty().bind(batchTask.messageProperty());
        VBox vbox = new VBox(10, new Label("Processing images..."), progressBar, statusLabel);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        progressStage.setScene(new Scene(vbox, 300, 150));
        progressStage.show();

        batchTask.setOnSucceeded(e -> {
            progressStage.close();
            showAlert("Success", "Batch processing completed. Images saved to " + outputDir.getAbsolutePath(), Alert.AlertType.INFORMATION);
        });

        batchTask.setOnFailed(e -> {
            progressStage.close();
            showAlert("Error", "Batch processing failed: " + batchTask.getException().getMessage(), Alert.AlertType.ERROR);
        });

        new Thread(batchTask).start();
    }

    private void processBatch(File outputDir, String algorithm) {
        Task<Void> batchTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int total = imagesList.size();
                EdgeDetectionAlgorithm edgeAlgorithm = algorithms.get(algorithm);
                if (edgeAlgorithm == null) {
                    throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
                }

                for (int i = 0; i < total; i++) {
                    Image originalImage = imagesList.get(i);
                    int strength = (int) robertsStrengthSlider.getValue();
                    int maskSize = radio5x5.isSelected() ? 5 : 3;

                    Image processedImage = edgeAlgorithm.apply(originalImage, algorithm.equals("Laplacian") ? maskSize : strength);

                    File outputFile = new File(outputDir, "processed_image_" + (i + 1) + ".png");
                    ImageIO.write(ImageProcessor.fromFXImage(processedImage), "png", outputFile);

                    updateProgress(i + 1, total);
                    updateMessage("Processed image " + (i + 1) + " of " + total);
                }
                return null;
            }
        };

        Stage progressStage = new Stage();
        progressStage.setTitle("Batch Processing");

        ProgressBar progressBar = new ProgressBar();
        progressBar.progressProperty().bind(batchTask.progressProperty());

        Label statusLabel = new Label();
        statusLabel.textProperty().bind(batchTask.messageProperty());

        VBox vbox = new VBox(10, new Label("Processing images..."), progressBar, statusLabel);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        progressStage.setScene(new Scene(vbox, 300, 150));
        progressStage.show();

        batchTask.setOnSucceeded(e -> {
            progressStage.close();
            showAlert("Success", "Batch processing completed. Images saved to " + outputDir.getAbsolutePath(), Alert.AlertType.INFORMATION);
        });

        batchTask.setOnFailed(e -> {
            progressStage.close();
            showAlert("Error", "Batch processing failed: " + batchTask.getException().getMessage(), Alert.AlertType.ERROR);
        });

        new Thread(batchTask).start();
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