package se233.cropedgestudio.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ScrollPane;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
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
    private ScrollPane imageScroll;

    @FXML
    Label statusLabel;

    private Crop crop;
    private List<String> inputListView = new ArrayList<String>();

    private volatile boolean cropConfirmed;
    private Rectangle selectionRectangle;
    private ExecutorService batchExecutorService;

    @FXML
    private void initialize() {
        crop = new Crop(imageView, imagePane, imageScroll);
        setupDragAndDrop();
    }

    private void setupDragAndDrop() {
        //drag
        myListView.setOnDragOver (event -> {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        //drop
        myListView.setOnDragDropped(event -> {
            boolean success = false;
            Dragboard dragboard = event.getDragboard();

            if (dragboard.hasFiles()) {
                List<File> files = dragboard.getFiles();
                for (File file : files) {
                    try {
                        if (isImageFile(file)) {
                            inputListView.add(file.getAbsolutePath());
                            myListView.getItems().add(file.getName());
                            imageView.setImage(new Image(file.toURI().toString()));
                            handleClearSelect();
                            setStatus("Droped files" + file.getName() );
                            success = true;
                        } else if (file.getName().toLowerCase().endsWith(".zip")) {
                            try {
                                List<File> extractedFiles = extractZipFile(file);
                                for (File extractedFile : extractedFiles) {
                                    if (isImageFile(extractedFile)) {
                                        inputListView.add(extractedFile.getAbsolutePath());
                                        myListView.getItems().add(extractedFile.getName());
                                        handleClearSelect();
                                        setStatus("ZIP file dropped and extracted. Loaded " + inputListView.size() + " images.");
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
            }

            event.setDropCompleted(success);
            event.consume();
        });

        //double-click on ListView to load the selected image
        myListView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                String selectedFileName = myListView.getSelectionModel().getSelectedItem();
                if (selectedFileName != null) {
                    handleClearSelect();
                    int index = myListView.getSelectionModel().getSelectedIndex();
                    String filePath = inputListView.get(index);
                    File file = new File(filePath);
                    Image image = new Image(file.toURI().toString());
                    imageView.setImage(image);



                    setStatus("File: " + selectedFileName);
                }
            }
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
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg");
    }

    @FXML
    public void handleUploadImage() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("ZIP Files", "*.zip")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile == null) {
            showInformation("Cancel Upload","You have not selected a file");
            setStatus("You have not selected a file, please select again");
            return;
        }
        if (imageView.getImage() != null) {
            handleClearSelect();
        }
        if (selectedFile != null) {
            try {
                if (isImageFile(selectedFile)) {
                    inputListView.add(selectedFile.getAbsolutePath());
                    myListView.getItems().add(selectedFile.getName());
                    setStatus("Image load successfully.");

                    imageView.setImage(new Image(selectedFile.toURI().toString()));

                } else if (selectedFile.getName().toLowerCase().endsWith(".zip")) {
                    try {
                        List<File> extractedFiles = extractZipFile(selectedFile);
                        for (File extractedFile : extractedFiles) {
                            if (isImageFile(extractedFile)) {
                                inputListView.add(extractedFile.getAbsolutePath());
                                myListView.getItems().add(extractedFile.getName());
                                setStatus("ZIP file extracted successfully. Loaded " + inputListView.size() + " images.");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    public void handleSelectArea() {
        if (imageView.getImage() != null) {
            crop.startselect();
            setStatus("Selecting area");

        } else if (imageView.getImage() == null || myListView.getSelectionModel().getSelectedItem() == null){
            showInformation("No Image", "Please drop or select image");
        }
    }

    @FXML
    public void handleClearSelect() {

        if (!crop.isAreaSelected) {
            setStatus("No select area, please select area");
            return;
        }

        if (selectionRectangle != null) {
            imagePane.getChildren().remove(selectionRectangle);
            selectionRectangle = null;
        }
        crop.isAreaSelected = false;

        if (crop.darkArea != null) {
            crop.darkArea.setVisible(false);
        }

        imagePane.requestFocus();
        crop.removeSelection();
        crop = new Crop(imageView, imagePane, imageScroll);
        setStatus("Selection cleared");
    }

    @FXML
    public void handleClearImage() {
        if (batchExecutorService == null && imageView.getImage() == null && myListView.getItems().isEmpty()) {
            showInformation("No Image to clear" , "Please upload or drag image");
            setStatus("No image, please upload or drag image");
            return;
        }
        handleClearSelect();

        if (batchExecutorService != null && !batchExecutorService.isShutdown()) {
            batchExecutorService.shutdownNow();
            batchExecutorService = null;
        }
        if (!inputListView.isEmpty() && !myListView.getItems().isEmpty() && imageView.getImage() != null) {
            inputListView.clear();
            myListView.getItems().clear();
            imageView.setImage(null);
        }

        showInformation("Images Cleared", "All images and batch processes have been cleared.");
        setStatus("Images Cleared");
    }

    @FXML
    private void handleCrop() {
        if (imageView.getImage() == null) {
            statusLabel.setText("Please select area");
            showInformation("Area not found" , "Please select area ");
            return;
        }

        if (!crop.isAreaSelected) {
            statusLabel.setText("Please select an area");
            showInformation("Area not selected", "Please select an area to crop");
            return;
        }
        crop.Cropping();
        cropConfirmed = true;
        setStatus("Image cropped");
    }

    @FXML
    private void handleSave() {
        if( imageView.getImage() == null && inputListView.isEmpty()){
            showInformation("No crop image", "Please crop image first");
            return;
        }

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
                showInformation("Saved Image", "Image saved to " + file.getAbsolutePath());
                setStatus("Selecting area");
            } catch (IOException e) {

                e.printStackTrace();
            }
        } else {
            showInformation("File not save yet" , "You have canceled the save file");
        }
    }


    @FXML
    public void handleBatchProcess(ActionEvent event) {
        if (inputListView.isEmpty()) {
            showInformation("No Images", "Please add images to the list first.");
            setStatus("No Image to be processed, please add");
            return;
        }
        try {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Output Directory");
            File outputDir = directoryChooser.showDialog(imageView.getScene().getWindow());

            if (outputDir == null) {
                throw new IllegalArgumentException("No output directory selected.");
            }

            configureCroppingForAllImages(outputDir);
            setStatus("Batch processing started.");
            System.out.println("Batch processing started");
        } catch (IllegalArgumentException e) {
            showAlert("Invalid Directory", e.getMessage());
        }
    }

    private void configureCroppingForAllImages(File outputDir) {
        configureNextImage(1, outputDir, new ArrayList<>());
    }

    private void configureNextImage(int currentIndex, File outputDir, List<Image> croppedImages) {
        if (currentIndex >= inputListView.size()) {
            startConcurrentProcessing(croppedImages, outputDir);
            return;
        }

        String filePath = inputListView.get(currentIndex);
        File file = new File(filePath);

        try {
            if (isImageFile(file)) {
                Image image = new Image(file.toURI().toString());
                imageView.setImage(image);
                handleClearSelect();
                crop.setOnCropConfirmed(() -> {
                    croppedImages.add(imageView.getImage());
                    configureNextImage(currentIndex + 1, outputDir, croppedImages);
                    System.out.println("Now processing image: " + currentIndex);
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
                    Image processedImage = detectEdges(image);
                    saveProcessedImage(processedImage, outputDir, fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to save processed image: " + fileName);
                }
            });
        }
        for (Runnable task : tasks) {
            executorService.submit(task);
        }
        executorService.shutdown();
        imageView.setImage(null);
        myListView.getItems().clear();
        inputListView.clear();
        showInformation("Batch Processing Complete", "All images have been processed and saved.");
        setStatus("Batch Processing Complete");
    }

    private Image detectEdges(Image croppedImage) {
        return croppedImage;
    }

    private void saveProcessedImage(Image image, File outputDir, String originalFileName) throws IOException {
        String outputFileName = originalFileName.substring(0, originalFileName.lastIndexOf('.')) + "_processed.png";
        File outputFile = new File(outputDir, outputFileName);
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        ImageIO.write(bufferedImage, "png", outputFile);
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

    private void setStatus(String message) {
        statusLabel.setText("Status: " + message);
    }
    public class Crop {

        private final ImageView imageView;
        private final BorderPane imagePane;
        private final ScrollPane imageScroll;
        private Runnable onCropConfirmed;
        private ResizeSquare selectionRectangle;
        Rectangle darkArea;
        boolean isAreaSelected = false;
        private boolean isCroppingActive = false;

        public Crop(ImageView imageView, BorderPane imagePane, ScrollPane imageScroll) {
            this.imageView = imageView;
            this.imagePane = imagePane;
            this.imageScroll = imageScroll;
            setupCropArea();
        }

        private void setupCropArea() {
            darkArea = new Rectangle();
            darkArea.setFill(Color.color(0, 0, 0, 0.5));
            darkArea.setVisible(false);
            imagePane.getChildren().add(darkArea);
        }

        public void startselect() {
            isCroppingActive = true;
            imageScroll.setPannable(false);
            removeSelection();


            Bounds imageBounds = imageView.getBoundsInParent();

            double imageWidth = imageBounds.getWidth();
            double imageHeight = imageBounds.getHeight();
            double rectWidth = imageWidth / 2.5;
            double rectHeight = imageHeight / 2.5;
            double rectX = imageBounds.getMinX() + (imageWidth - rectWidth) / 2;
            double rectY = imageBounds.getMinY() + (imageHeight - rectHeight) / 2;

            selectionRectangle = new ResizeSquare(rectX, rectY, rectWidth, rectHeight, imagePane, this::updateDarkArea);
            selectionRectangle.setImageView(imageView);


            isAreaSelected = true;
            updateDarkArea();
            imagePane.requestFocus();
        }

        public void Cropping() {
            if (isAreaSelected && selectionRectangle != null) {
                imageScroll.setPannable(true);
                cropImage(selectionRectangle.getBoundsInParent());
                removeSelection();
                selectionRectangle = null;
                isAreaSelected = false;
                darkArea.setVisible(false);
                isCroppingActive = false;
            }
            if (onCropConfirmed != null) {
                onCropConfirmed.run();
            }
        }

        public void setOnCropConfirmed(Runnable onCropConfirmed) {
            this.onCropConfirmed = onCropConfirmed;
        }

        private void cropImage(Bounds bounds) {
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            parameters.setViewport(new Rectangle2D(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight()));
            WritableImage croppedImageWritable = new WritableImage((int) bounds.getWidth(), (int) bounds.getHeight());
            imageView.snapshot(parameters, croppedImageWritable);
            imageView.setImage(croppedImageWritable);
        }

        private void updateDarkArea() {
            if (selectionRectangle != null) {
                Bounds imageBounds = imageView.getBoundsInParent();

                darkArea.setWidth(imageBounds.getWidth());
                darkArea.setHeight(imageBounds.getHeight());
                darkArea.setLayoutX(imageBounds.getMinX());
                darkArea.setLayoutY(imageBounds.getMinY());

                Rectangle outerRect = new Rectangle(0, 0, imageBounds.getWidth(), imageBounds.getHeight());
                Rectangle innerRect = new Rectangle(
                        selectionRectangle.getX() - imageBounds.getMinX(),
                        selectionRectangle.getY() - imageBounds.getMinY(),
                        selectionRectangle.getWidth(),
                        selectionRectangle.getHeight()
                );

                Shape clippedArea = Shape.subtract(outerRect, innerRect);

                darkArea.setClip(clippedArea);
                darkArea.setVisible(true);
            }
        }

        public void removeSelection() {
            if (selectionRectangle != null) {
                selectionRectangle.removeResizeHandles(imagePane);
                imagePane.getChildren().remove(selectionRectangle);
                selectionRectangle = null;
            }
            isAreaSelected = false;
        }


    }

}