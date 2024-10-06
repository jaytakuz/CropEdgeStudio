package se233.cropedgestudio.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private static MainController instance;

    @FXML private VBox contentArea;
    @FXML private Menu featuresMenu;

    public static MainController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        loadView("/se233/cropedgestudio/views/MainContent.fxml");
        updateFeaturesMenu("home");
    }

    @FXML
    public void handleHome() {
        loadView("/se233/cropedgestudio/views/MainContent.fxml");
        updateFeaturesMenu("home");
    }

    @FXML
    public void handleDetectEdge() {
        loadView("/se233/cropedgestudio/views/EdgeDetectionView.fxml");
        updateFeaturesMenu("edge");
    }

    @FXML
    public void handleCrop() {
        loadView("/se233/cropedgestudio/views/CropView.fxml");
        updateFeaturesMenu("crop");
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
        alert.setContentText("CropEdge Studio v1.0\n\nDeveloped by:\nPongpiphat Kalasuk\nWatcharapong Wanna");
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

    private void updateFeaturesMenu(String currentPage) {
        featuresMenu.getItems().clear();
        MenuItem refreshItem = new MenuItem();
        MenuItem edgeDetectionItem = new MenuItem();
        MenuItem cropItem = new MenuItem();

        switch (currentPage) {
            case "home":
                refreshItem.setText("Refresh Home");
                refreshItem.setOnAction(e -> handleHome());
                edgeDetectionItem.setText("Go to Edge Detection page");
                edgeDetectionItem.setOnAction(e -> handleDetectEdge());
                cropItem.setText("Go to Crop Image page");
                cropItem.setOnAction(e -> handleCrop());
                break;
            case "edge":
                refreshItem.setText("Refresh Edge Detection");
                refreshItem.setOnAction(e -> handleDetectEdge());
                edgeDetectionItem.setText("Go to Home page");
                edgeDetectionItem.setOnAction(e -> handleHome());
                cropItem.setText("Go to Crop Image page");
                cropItem.setOnAction(e -> handleCrop());
                break;
            case "crop":
                refreshItem.setText("Refresh Crop Image");
                refreshItem.setOnAction(e -> handleCrop());
                edgeDetectionItem.setText("Go to Home page");
                edgeDetectionItem.setOnAction(e -> handleHome());
                cropItem.setText("Go to Edge Detection page");
                cropItem.setOnAction(e -> handleDetectEdge());
                break;
        }

        featuresMenu.getItems().addAll(refreshItem, edgeDetectionItem, cropItem);
    }
}