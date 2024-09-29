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
                       // imageView.setImage(new Image(file.toURI().toString()));
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
                        //imageView.setImage(new Image(file.toURI().toString()));
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

//        selectionRectangle = new Rectangle(0, 0, 0, 0);
//        selectionRectangle.setFill(Color.TRANSPARENT);
//        selectionRectangle.setStroke(Color.GREEN);
//        ((Pane) imageView.getParent()).getChildren().add(selectionRectangle);

        // Mouse events for cropping
//        imageView.setOnMousePressed(this::startSelection);
//        imageView.setOnMouseDragged(this::updateSelection);
//        imageView.setOnMouseReleased(this::completeSelection);

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
        if (imageView.getImage() != null && myListView.getSelectionModel().getSelectedItem() != null) {
            myListView.getItems().clear();
            imageView.setImage(null);
            handleClearSelect();
        } else {
            showInformation("No Image" , "Please drop or select image");
        }


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