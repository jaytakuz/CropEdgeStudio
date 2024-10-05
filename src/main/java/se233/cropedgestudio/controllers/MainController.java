package se233.cropedgestudio.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private static MainController instance;

    @FXML
    private VBox contentArea;

    public static MainController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        loadView("/se233/cropedgestudio/views/MainContent.fxml");
    }

    @FXML
    public void handleHome() {
        loadView("/se233/cropedgestudio/views/MainContent.fxml");
    }

    @FXML
    public void handleDetectEdge() {
        loadView("/se233/cropedgestudio/views/EdgeDetectionView.fxml");
    }

    @FXML
    public void handleCrop() {
        loadView("/se233/cropedgestudio/views/CropView.fxml");
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About CropEdge Studio");
        alert.setHeaderText(null);
        alert.setContentText("CropEdge Studio v1.0\n\nDeveloped by:\nYour Names Here");
        alert.showAndWait();
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
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