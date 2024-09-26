package se233.cropedgestudio.controllers;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import se233.cropedgestudio.controllers.ResizableRectangle;


public class Crop {

    private final ImageView imageView;
    private final BorderPane imagePane;
    private final ScrollPane imageScroll;
    private ResizableRectangle selectionRectangle;
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

    // In CropHandler.java

    public void startCrop() {
        isCroppingActive = true;
        imageScroll.setPannable(false);
        removeExistingSelection();

        // Use the actual dimensions of the displayed image, considering scaling
        double imageWidth = imageView.getBoundsInParent().getWidth();  // Adjusted to get the displayed bounds
        double imageHeight = imageView.getBoundsInParent().getHeight(); // Adjusted to get the displayed bounds

        // Ensure these dimensions match the imageView's actual displayed image
        if (imageWidth == 0 || imageHeight == 0) {
            imageWidth = imageView.getImage().getWidth();
            imageHeight = imageView.getImage().getHeight();
        }

        double rectWidth = imageWidth / 2;
        double rectHeight = imageHeight / 2;
        double rectX = (imageWidth - rectWidth) / 2;
        double rectY = (imageHeight - rectHeight) / 2;

        // Create the resizable rectangle using proper scaling
        // selectionRectangle = new ResizableRectangle(rectX, rectY, rectWidth, rectHeight, imagePane, this::updateDarkArea);
        selectionRectangle = new ResizableRectangle(rectX, rectY, rectWidth, rectHeight, imagePane);
        isAreaSelected = true;
        // updateDarkArea();
        imagePane.requestFocus();
    }


    public void confirmCrop() {
        if (isAreaSelected && selectionRectangle != null) {
            imageScroll.setPannable(true);
            cropImage(selectionRectangle.getBoundsInParent());

            removeExistingSelection();
            selectionRectangle = null;
            isAreaSelected = false;
            darkArea.setVisible(false);
            isCroppingActive = false;
        }
    }

    private void cropImage(Bounds bounds) {
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight()));

        WritableImage croppedImageWritable = new WritableImage((int) bounds.getWidth(), (int) bounds.getHeight());
        imageView.snapshot(parameters, croppedImageWritable);

        imageView.setImage(croppedImageWritable);
    }

//    private void updateDarkArea() {
//        if (selectionRectangle != null) {
//            double imageWidth = imageView.getFitWidth();
//            double imageHeight = imageView.getFitHeight();
//            double rectX = selectionRectangle.getX();
//            double rectY = selectionRectangle.getY();
//            double rectWidth = selectionRectangle.getWidth();
//            double rectHeight = selectionRectangle.getHeight();
//
//            darkArea.setWidth(imageWidth);
//            darkArea.setHeight(imageHeight);
//            darkArea.setLayoutX(0);
//            darkArea.setLayoutY(0);
//
//            Rectangle outerRect = new Rectangle(0, 0, imageWidth, imageHeight);
//            Rectangle innerRect = new Rectangle(rectX, rectY, rectWidth, rectHeight);
//            Shape clippedArea = Shape.subtract(outerRect, innerRect);
//
//            darkArea.setClip(clippedArea);
//            darkArea.setVisible(true);
//        }
//    }

    void removeExistingSelection() {
        // Remove the existing selection rectangle if it exists
        if (selectionRectangle != null) {
            selectionRectangle.removeResizeHandles(imagePane);
            imagePane.getChildren().remove(selectionRectangle);
            selectionRectangle = null;  // Set to null to ensure fresh initialization
        }
        isAreaSelected = false;
    }
}
