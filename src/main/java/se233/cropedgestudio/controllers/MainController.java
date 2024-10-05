package se233.cropedgestudio.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class MainController {

    @FXML
    private MenuItem detectEdgeMenuItem;

    @FXML
    private MenuItem cropMenuItem;

    @FXML
    private VBox root;

    @FXML
    private void handleDetectEdge() {
        loadView("/se233/cropedgestudio/views/EdgeDetectionView.fxml");
    }

    @FXML
    private void handleCrop() {
        loadView("/se233/cropedgestudio/views/CropView.fxml");
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About CropEdge Studio");
        alert.setHeaderText(null);
        alert.setContentText("CropEdge Studio v1.0\n\nDeveloped by:\nPongpiphat Kalasuk\nWatcharapong Wanna");
        alert.showAndWait();
    }

    @FXML
    private void handleHome() {
        loadView("/se233/cropedgestudio/views/MainView.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            root.getChildren().set(1, view);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Failed to load view: " + e.getMessage());
        }
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}