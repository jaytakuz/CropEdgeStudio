package se233.cropedgestudio.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ScrollPane;
import javafx.embed.swing.SwingFXUtils;

import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class CropController {


    @FXML
    private ListView<String> myListView;

    @FXML
    private ImageView imageView;


    @FXML
    private BorderPane imagePane;

    @FXML
    ScrollPane imageScroll;


    private Crop cropHandler;
    private List<String> inputListView = new ArrayList<String>();
    private int currentIndex = 0;
    private volatile boolean cropConfirmed;
    private Rectangle selectionRectangle;
    private ExecutorService batchExecutorService;

    @FXML
    private void initialize() {
        cropHandler = new Crop(imageView, imagePane, imageScroll);
        setupDragAndDrop();
        // updateNavigationButtons();  // Initial update to disable navigation buttons if no images
    }

    private void setupDragAndDrop() {
        //drag
        myListView.setOnDragOver (event -> {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasFiles()) {
                File file = dragboard.getFiles().get(0);
                try {
                    if (isImageFile(file) || file.getName().toLowerCase().endsWith(".zip")) {
                        event.acceptTransferModes(TransferMode.COPY);  // Accept copy mode
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            event.consume();
        });

        //  drop
        myListView.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;

            if (dragboard.hasFiles()) {
                File file = dragboard.getFiles().get(0);
                try {
                    if (isImageFile(file)) {
                        inputListView.add(file.getAbsolutePath());
                        myListView.getItems().add(file.getName());
                        // Optionally display the image immediately in ImageView
                       imageView.setImage(new Image(file.toURI().toString()));
                        success = true;
                    } else if (file.getName().toLowerCase().endsWith(".zip")) {
                        try {
                            List<File> extractedFiles = extractZipFile(file);
                            for (File extractedFile : extractedFiles) {
                                if (isImageFile(extractedFile)) {
                                    inputListView.add(extractedFile.getAbsolutePath());
                                    myListView.getItems().add(extractedFile.getName());
                                }
                            }
                            success = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });

        // Handle double-click on ListView to load the selected image
        myListView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) { // Double-click
                String selectedFileName = myListView.getSelectionModel().getSelectedItem();
                if (selectedFileName != null) {
                    int index = myListView.getSelectionModel().getSelectedIndex();
                    String filePath = inputListView.get(index);

                    // Display the selected image in ImageView
                    File file = new File(filePath);
                    Image image = new Image(file.toURI().toString());
                    imageView.setImage(image);
                }
            }
        });

        imageView.setOnDragOver (event -> {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasFiles()) {
                File file = dragboard.getFiles().get(0);
                try {
                    if (isImageFile(file) || file.getName().toLowerCase().endsWith(".zip")) {
                        event.acceptTransferModes(TransferMode.COPY);  // Accept copy mode
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            event.consume();
        });

        //  drop
        imageView.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;

            if (dragboard.hasFiles()) {
                File file = dragboard.getFiles().get(0);
                try {
                    if (isImageFile(file)) {
                        inputListView.add(file.getAbsolutePath());
                        myListView.getItems().add(file.getName());
                        // Optionally display the image immediately in ImageView
                        imageView.setImage(new Image(file.toURI().toString()));
                        success = true;
                    } else if (file.getName().toLowerCase().endsWith(".zip")) {
                        try {
                            List<File> extractedFiles = extractZipFile(file);
                            for (File extractedFile : extractedFiles) {
                                if (isImageFile(extractedFile)) {
                                    inputListView.add(extractedFile.getAbsolutePath());
                                    myListView.getItems().add(extractedFile.getName());
                                }
                            }
                            success = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });


    }

    private List<File> extractZipFile(File zipFile) throws IOException {
        List<File> extractedFiles = new ArrayList<>();
        File tempDir = new File("extracted");
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                File newFile = new File(tempDir, zipEntry.getName());
                if (!newFile.getParentFile().exists()) {
                    newFile.getParentFile().mkdirs();
                }

                if (!zipEntry.isDirectory()) {
                    Files.copy(zis, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    extractedFiles.add(newFile);
                }
                zis.closeEntry();
            }
        }

        return extractedFiles;
    }

    private boolean isImageFile(File file) throws IOException {
        String fileName = file.getName().toLowerCase(); // Make case-insensitive
        return fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg");
    }


    @FXML
    private void resetCropHandler() {
        if (cropHandler != null) {
            cropHandler.removeExistingSelection();
            cropHandler = new Crop(imageView, imagePane, imageScroll);
        }
    }

    @FXML
    public void handleSelectArea() {
        if (imageView.getImage() != null) {
            cropHandler.startCrop();
        } else if (imageView.getImage() == null || myListView.getSelectionModel().getSelectedItem() == null){
            showInformation("No Image" , "Please drop or select image");
        }
    }
    @FXML
    public void handleClearSelect() {

        if (selectionRectangle != null) {
            imagePane.getChildren().remove(selectionRectangle);
            selectionRectangle = null;
        }
        cropHandler.isAreaSelected = false;

        if (cropHandler.darkArea != null) {
            cropHandler.darkArea.setVisible(false);
        }


        imagePane.requestFocus();
        resetCropHandler();
  if (cropHandler.darkArea == null) {
            showInformation("No select area" , "Please select area");
        }
    }

    @FXML
    public void handleClearImage(ActionEvent event) {
        if (batchExecutorService != null && !batchExecutorService.isShutdown()) {
            batchExecutorService.shutdownNow(); // Stop all running tasks immediately
            batchExecutorService = null;        // Reset the executor for future use
        }

        // 2. Clear the list of images and selections
        if (!inputListView.isEmpty()) {
            inputListView.clear();
        }

        if (!myListView.getItems().isEmpty()) {
            myListView.getItems().clear();
        }

        // 3. Clear the ImageView
        if (imageView.getImage() != null) {
            imageView.setImage(null);
        }

        // 4. Clear any cropping selections
        handleClearSelect();

        showInformation("Images Cleared", "All images and batch processes have been cleared.");


    }

    @FXML
    private void handleCrop() {
        // TODO: Implement cropping logic
        if (imageView.getImage() != null && cropHandler != null) {
            cropHandler.confirmCrop();
            cropConfirmed = true;
        } else if (!cropConfirmed && cropHandler == null){
            showInformation("Crop Error" , "Please select area ");
        }
    }

    @FXML
    public void handleBatchProcess(ActionEvent event) {
        if (inputListView.isEmpty()) {
            showInformation("No Images", "Please add images to the list first.");
            return;
        }

        // Directory chooser for output
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Output Directory");
        File outputDir = directoryChooser.showDialog(imageView.getScene().getWindow());

        if (outputDir == null) {
            showInformation("No Directory", "Please select an output directory.");
            return;
        }

        // Start processing the first image in the list
        //processNextImage(0, outputDir);

        // Configure cropping for all images
        configureCroppingForAllImages(outputDir);
    }

    private void configureCroppingForAllImages(File outputDir) {
        configureNextImage(0, outputDir, new ArrayList<>());
    }

    private void configureNextImage(int currentIndex, File outputDir, List<Image> croppedImages) {
        if (currentIndex >= inputListView.size()) {
            // After all images are configured, start concurrent processing
            startConcurrentProcessing(croppedImages, outputDir);
            return;
        }

        // Load the current image
        String filePath = inputListView.get(currentIndex);
        File file = new File(filePath);

        try {
            if (isImageFile(file)) {
                // Load the image into the ImageView
                Image image = new Image(file.toURI().toString());
                imageView.setImage(image);

                // Start cropping for this image
                cropHandler.startCrop();

                // Wait for user to confirm cropping
                cropHandler.setOnCropConfirmed(() -> {
                    // Store cropped image
                    croppedImages.add(imageView.getImage());

                    // Proceed to configure the next image
                    configureNextImage(currentIndex + 1, outputDir, croppedImages);
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void startConcurrentProcessing(List<Image> croppedImages, File outputDir) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);  // Adjust thread count as needed

        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < croppedImages.size(); i++) {
            Image image = croppedImages.get(i);
            String fileName = new File(inputListView.get(i)).getName();

            tasks.add(() -> {
                try {
                    // Apply edge detection
                    Image processedImage = detectEdges(image);

                    // Save the processed image
                    saveProcessedImage(processedImage, outputDir, fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to save processed image: " + fileName);
                }
            });
        }

        // Submit all tasks to the executor
        for (Runnable task : tasks) {
            executorService.submit(task);
        }

        // Shutdown the executor once all tasks are submitted
        executorService.shutdown();

        showInformation("Batch Processing Complete", "All images have been processed and saved.");
    }



    private Image detectEdges(Image croppedImage) {
        // TODO: Apply edge detection logic here (use OpenCV or custom algorithm)
        // For now, just return the cropped image as a placeholder
        return croppedImage;
    }

    // Save the processed image
    private void saveProcessedImage(Image image, File outputDir, String originalFileName) throws IOException {
        // Prepare the output file path
        String outputFileName = originalFileName.substring(0, originalFileName.lastIndexOf('.')) + "_processed.png";
        File outputFile = new File(outputDir, outputFileName);

        // Convert Image to BufferedImage
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

        // Save the buffered image as a file (you can choose the format, e.g., PNG)
        ImageIO.write(bufferedImage, "png", outputFile);
    }


    @FXML
    private void handleSave() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Save as png", "*.png"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Save as jpg", "*.jpg"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Save as jpeg", "*.jpeg"));
        File file = fileChooser.showSaveDialog(imageView.getScene().getWindow());

        if (file != null) {
            try {
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                ImageIO.write(bufferedImage, "png", file);
                ImageIO.write(bufferedImage, "jpg", file);
                ImageIO.write(bufferedImage, "jpeg", file);
            } catch (IOException e) {

                e.printStackTrace();
            }
        } else {
            showInformation("File not save yet" , "You have canceled the save file");
        }
    }

    private void showInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();

    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();

    }




    // TODO: Add method to set the image to be cropped
}