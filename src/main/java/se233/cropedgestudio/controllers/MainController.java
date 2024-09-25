package se233.cropedgestudio.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import se233.cropedgestudio.components.DragAndDropPane;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainController {

    @FXML
    private MenuItem detectEdgeMenuItem;

    @FXML
    private MenuItem cropMenuItem;

    @FXML
    private StackPane contentArea;

    private DragAndDropPane dragAndDropPane;

    @FXML
    public void initialize() {
        dragAndDropPane = new DragAndDropPane(this::handleDroppedFiles);
        contentArea.getChildren().add(dragAndDropPane);
    }

    @FXML
    private void handleDetectEdge() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/se233/cropedgestudio/views/EdgeDetectionView.fxml"));
            Parent edgeDetectionView = loader.load();
            contentArea.getChildren().setAll(edgeDetectionView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCrop() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/se233/cropedgestudio/views/CropView.fxml"));
            Parent cropView = loader.load();
            contentArea.getChildren().setAll(cropView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDroppedFiles(List<File> files) {
        for (File file : files) {
            if (isValidFile(file)) {
                System.out.println("Dropped file: " + file.getAbsolutePath());
                // TODO: Process the file (either add to a list for batch processing or process immediately)
            } else {
                System.out.println("Invalid file: " + file.getName());
                // TODO: Show an error message to the user
            }
        }
    }

    private boolean isValidFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".zip");
    }
}