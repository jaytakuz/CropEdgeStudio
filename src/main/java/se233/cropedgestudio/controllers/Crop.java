package se233.cropedgestudio.controllers;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
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

        Bounds viewportBounds = imageScroll.getViewportBounds();

        double viewportWidth = viewportBounds.getWidth();
        double viewportHeight = viewportBounds.getHeight();
        double imageWidth = imageView.getBoundsInParent().getWidth();
        double imageHeight = imageView.getBoundsInParent().getHeight();
        double rectWidth = imageWidth / 2.5;
        double rectHeight = imageHeight / 2.5;
        double rectX = (viewportWidth - rectWidth) / 2.5;
        double rectY = (viewportHeight - rectHeight) / 2.5;

        selectionRectangle = new ResizableRectangle(rectX, rectY, rectWidth, rectHeight, imagePane, this::updateDarkArea);

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
            Bounds viewportBounds = imageScroll.getViewportBounds();

            double viewportWidth = viewportBounds.getWidth();
            double viewportHeight = viewportBounds.getHeight();
            double rectX = selectionRectangle.getX();
            double rectY = selectionRectangle.getY();
            double rectWidth = selectionRectangle.getWidth();
            double rectHeight = selectionRectangle.getHeight();

            darkArea.setWidth(viewportWidth);
            darkArea.setHeight(viewportHeight);
            darkArea.setLayoutX(0);
            darkArea.setLayoutY(0);

            Rectangle outerRect = new Rectangle(0, 0, viewportWidth, viewportHeight);
            Rectangle innerRect = new Rectangle(rectX, rectY, rectWidth, rectHeight);

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
}
