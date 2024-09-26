package se233.cropedgestudio.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import se233.cropedgestudio.utils.ImageProcessor;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class CropController {

    @FXML
    private ImageView imageView;

    @FXML
    private Rectangle cropRectangle;

    @FXML
    private TextField xTextField;

    @FXML
    private TextField yTextField;

    @FXML
    private TextField widthTextField;

    @FXML
    private TextField heightTextField;

    private Image originalImage;

    @FXML
    public void initialize() {
        setupCropRectangle();
        bindTextFieldsToCropRectangle();
    }

    private void setupCropRectangle() {
        cropRectangle.setMouseTransparent(false);
        cropRectangle.setOnMousePressed(event -> {
            cropRectangle.setX(event.getX());
            cropRectangle.setY(event.getY());
            cropRectangle.setWidth(0);
            cropRectangle.setHeight(0);
        });
        cropRectangle.setOnMouseDragged(event -> {
            cropRectangle.setWidth(event.getX() - cropRectangle.getX());
            cropRectangle.setHeight(event.getY() - cropRectangle.getY());
        });
    }

    private void bindTextFieldsToCropRectangle() {
        xTextField.textProperty().bind(cropRectangle.xProperty().asString("%.0f"));
        yTextField.textProperty().bind(cropRectangle.yProperty().asString("%.0f"));
        widthTextField.textProperty().bind(cropRectangle.widthProperty().asString("%.0f"));
        heightTextField.textProperty().bind(cropRectangle.heightProperty().asString("%.0f"));
    }

    @FXML
    private void handleOpenImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            originalImage = new Image(selectedFile.toURI().toString());
            imageView.setImage(originalImage);
            resetCropRectangle();
        }
    }

    private void resetCropRectangle() {
        cropRectangle.setX(0);
        cropRectangle.setY(0);
        cropRectangle.setWidth(0);
        cropRectangle.setHeight(0);
    }

    @FXML
    private void handleCrop() {
        if (originalImage != null) {
            int x = (int) cropRectangle.getX();
            int y = (int) cropRectangle.getY();
            int width = (int) cropRectangle.getWidth();
            int height = (int) cropRectangle.getHeight();

            WritableImage croppedImage = new WritableImage(originalImage.getPixelReader(), x, y, width, height);
            imageView.setImage(croppedImage);
        }
    }

    @FXML
    private void handleSave() {
        if (imageView.getImage() != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png")
            );
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try {
                    ImageIO.write(ImageProcessor.fromFXImage(imageView.getImage()), "png", file);
                } catch (IOException e) {
                    e.printStackTrace();
                    // TODO: Show error dialog
                }
            }
        }
    }
}