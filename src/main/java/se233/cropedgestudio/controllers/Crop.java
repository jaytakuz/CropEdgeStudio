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

public class Crop {

    private final ImageView imageView;
    private final BorderPane imagePane;
    private final ScrollPane imageScroll;
    private Runnable onCropConfirmed;
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

    public void startCrop() {
        isCroppingActive = true;
        imageScroll.setPannable(false);
        removeExistingSelection();


        Bounds imageBounds = imageView.getBoundsInParent();

        double imageWidth = imageBounds.getWidth();
        double imageHeight = imageBounds.getHeight();
        double rectWidth = imageWidth / 2.5;
        double rectHeight = imageHeight / 2.5;
        double rectX = imageBounds.getMinX() + (imageWidth - rectWidth) / 2;
        double rectY = imageBounds.getMinY() + (imageHeight - rectHeight) / 2;

        selectionRectangle = new ResizableRectangle(rectX, rectY, rectWidth, rectHeight, imagePane, this::updateDarkArea);
        selectionRectangle.setImageView(imageView);


        isAreaSelected = true;
        updateDarkArea();
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

    public void removeExistingSelection() {
        if (selectionRectangle != null) {
            selectionRectangle.removeResizeHandles(imagePane);
            imagePane.getChildren().remove(selectionRectangle);
            selectionRectangle = null;
        }
        isAreaSelected = false;
    }

    public void resetDarkArea() {
        if (darkArea != null) {
            darkArea.setVisible(false);
            updateDarkArea();
        }
    }


}