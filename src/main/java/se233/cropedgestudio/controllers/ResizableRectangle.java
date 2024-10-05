package se233.cropedgestudio.controllers;

import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class ResizableRectangle extends Rectangle {

    private static final int RESIZER_SQUARE_SIDE = 10;
    private Paint resizerSquareColor = Color.valueOf("#3498db");
    private Paint rectangleStrokeColor = Color.valueOf("#3498db");
    private double mouseClickX;
    private double mouseClickY;

    private final List<Rectangle> resizeHandles = new ArrayList<>();
    private Pane parentPane;
    private Runnable DarkArea;
    private ImageView imageView;

    public ResizableRectangle(double x, double y, double width, double height, Pane pane, Runnable DarkArea) {
        super(x, y, width, height);
        this.parentPane = pane;
        this.DarkArea = DarkArea;
        pane.getChildren().add(this);
        super.setStroke(rectangleStrokeColor);
        super.setStrokeWidth(2);
        super.setFill(Color.color(0, 0, 0, 0));

        makeSWResizerSquare(pane);
        makeSCResizerSquare(pane);
        makeSEResizerSquare(pane);
        makeCWResizerSquare(pane);
        makeCEResizerSquare(pane);
        makeNWResizerSquare(pane);
        makeNCResizerSquare(pane);
        makeNEResizerSquare(pane);

        this.setOnMousePressed(event -> {
            mouseClickX = event.getX();
            mouseClickY = event.getY();
            getParent().setCursor(Cursor.MOVE);
        });

        this.setOnMouseDragged(event -> {
            double offsetX = event.getX() - mouseClickX;
            double offsetY = event.getY() - mouseClickY;
            double newX = getX() + offsetX;
            double newY = getY() + offsetY;

            Bounds imageBounds = imageView.getBoundsInParent();

            if (newX >= imageBounds.getMinX() && newX + getWidth() <= imageBounds.getMaxX()) {
                setX(newX);
            }
            if (newY >= imageBounds.getMinY() && newY + getHeight() <= imageBounds.getMaxY()) {
                setY(newY);
            }

            mouseClickX = event.getX();
            mouseClickY = event.getY();
            DarkArea.run();
        });
        this.setOnMouseReleased(event -> getParent().setCursor(Cursor.DEFAULT));
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public void removeResizeHandles(Pane pane) {
        for (Rectangle handle : resizeHandles) {
            pane.getChildren().remove(handle);
        }
        resizeHandles.clear();
    }


    private void makeNWResizerSquare(Pane pane) {
        Rectangle squareNW = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);
        squareNW.xProperty().bind(super.xProperty().subtract(squareNW.widthProperty().divide(2.0)));
        squareNW.yProperty().bind(super.yProperty().subtract(squareNW.heightProperty().divide(2.0)));
        pane.getChildren().add(squareNW);
        resizeHandles.add(squareNW);

        squareNW.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareNW.getParent().setCursor(Cursor.NW_RESIZE));
        ResizerSquare(squareNW);

        squareNW.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            double offsetX = event.getX() - getX();
            double offsetY = event.getY() - getY();

            if (getWidth() - offsetX > 0) {
                setX(event.getX());
                setWidth(getWidth() - offsetX);
            }

            if (getHeight() - offsetY > 0) {
                setY(event.getY());
                setHeight(getHeight() - offsetY);
            }

            DarkArea.run();
        });
    }

    private void makeCWResizerSquare(Pane pane) {
        Rectangle squareCW = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);
        squareCW.xProperty().bind(super.xProperty().subtract(squareCW.widthProperty().divide(2.0)));
        squareCW.yProperty().bind(super.yProperty().add(super.heightProperty().divide(2.0).subtract(
                squareCW.heightProperty().divide(2.0))));
        pane.getChildren().add(squareCW);
        resizeHandles.add(squareCW);
        ResizerSquare(squareCW);

        squareCW.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareCW.getParent().setCursor(Cursor.W_RESIZE));
        ResizerSquare(squareCW);

        squareCW.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            double offsetX = event.getX() - getX();
            double newX = getX() + offsetX;

            if (newX >= 0 && newX <= getX() + getWidth() - 5) {
                setX(newX);
                setWidth(getWidth() - offsetX);
            }
            DarkArea.run();
        });
    }

    private void makeSWResizerSquare(Pane pane) {
        Rectangle squareSW = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);
        squareSW.xProperty().bind(super.xProperty().subtract(squareSW.widthProperty().divide(2.0)));
        squareSW.yProperty().bind(super.yProperty().add(super.heightProperty().subtract(
                squareSW.heightProperty().divide(2.0))));
        pane.getChildren().add(squareSW);
        resizeHandles.add(squareSW);

        squareSW.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareSW.getParent().setCursor(Cursor.SW_RESIZE));
        ResizerSquare(squareSW);

        squareSW.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            double offsetX = event.getX() - getX();
            double offsetY = event.getY() - getY();
            double newX = getX() + offsetX;

            if (newX >= 0 && newX <= getX() + getWidth() - 5) {
                setX(newX);
                setWidth(getWidth() - offsetX);
            }
            if (offsetY >= 0 && offsetY <= getY() + getHeight() - 5) {
                setHeight(offsetY);
            }

            DarkArea.run();
        });
    }

    private void makeSCResizerSquare(Pane pane) {
        Rectangle squareSC = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);
        squareSC.xProperty().bind(super.xProperty().add(super.widthProperty().divide(2.0).subtract(
                squareSC.widthProperty().divide(2.0))));
        squareSC.yProperty().bind(super.yProperty().add(super.heightProperty().subtract(
                squareSC.heightProperty().divide(2.0))));
        pane.getChildren().add(squareSC);
        resizeHandles.add(squareSC);

        squareSC.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareSC.getParent().setCursor(Cursor.S_RESIZE));
        ResizerSquare(squareSC);

        squareSC.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {

            double offsetY = event.getY() - getY();

            if (offsetY >= 0 && offsetY <= getY() + getHeight() - 5) {
                setHeight(offsetY);
            }

            DarkArea.run();
        });
    }

    private void makeSEResizerSquare(Pane pane) {
        Rectangle squareSE = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);
        squareSE.xProperty().bind(super.xProperty().add(super.widthProperty()).subtract(
                squareSE.widthProperty().divide(2.0)));
        squareSE.yProperty().bind(super.yProperty().add(super.heightProperty().subtract(
                squareSE.heightProperty().divide(2.0))));
        pane.getChildren().add(squareSE);
        resizeHandles.add(squareSE);

        squareSE.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareSE.getParent().setCursor(Cursor.SE_RESIZE));
        ResizerSquare(squareSE);

        squareSE.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            double offsetX = event.getX() - getX();
            double offsetY = event.getY() - getY();

            if (offsetX >= 0 && offsetX <= getX() + getWidth() - 5) {
                setWidth(offsetX);
            }

            if (offsetY >= 0 && offsetY <= getY() + getHeight() - 5) {
                setHeight(offsetY);
            }

            DarkArea.run();
        });
    }

    private void makeCEResizerSquare(Pane pane) {
        Rectangle squareCE = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);
        squareCE.xProperty().bind(super.xProperty().add(super.widthProperty()).subtract(
                squareCE.widthProperty().divide(2.0)));
        squareCE.yProperty().bind(super.yProperty().add(super.heightProperty().divide(2.0).subtract(
                squareCE.heightProperty().divide(2.0))));
        pane.getChildren().add(squareCE);
        resizeHandles.add(squareCE);

        squareCE.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareCE.getParent().setCursor(Cursor.E_RESIZE));
        ResizerSquare(squareCE);

        squareCE.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            double offsetX = event.getX() - getX();
            if (offsetX >= 0 && offsetX <= getX() + getWidth() - 5) {
                setWidth(offsetX);
            }

            DarkArea.run();
        });
    }

    private void makeNEResizerSquare(Pane pane) {
        Rectangle squareNE = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);
        squareNE.xProperty().bind(super.xProperty().add(super.widthProperty()).subtract(
                squareNE.widthProperty().divide(2.0)));
        squareNE.yProperty().bind(super.yProperty().subtract(squareNE.heightProperty().divide(2.0)));
        pane.getChildren().add(squareNE);
        resizeHandles.add(squareNE);

        squareNE.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareNE.getParent().setCursor(Cursor.NE_RESIZE));
        ResizerSquare(squareNE);

        squareNE.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            double offsetX = event.getX() - getX();
            double offsetY = event.getY() - getY();
            double newY = getY() + offsetY;

            if (offsetX >= 0 && offsetX <= getX() + getWidth() - 5) {
                setWidth(offsetX);
            }

            if (newY >= 0 && newY <= getY() + getHeight() - 5) {
                setY(newY);
                setHeight(getHeight() - offsetY);
            }

            DarkArea.run();
        });
    }

    private void makeNCResizerSquare(Pane pane) {
        Rectangle squareNC = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);
        squareNC.xProperty().bind(super.xProperty().add(super.widthProperty().divide(2.0).subtract(
                squareNC.widthProperty().divide(2.0))));
        squareNC.yProperty().bind(super.yProperty().subtract(
                squareNC.heightProperty().divide(2.0)));
        pane.getChildren().add(squareNC);
        resizeHandles.add(squareNC);

        squareNC.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareNC.getParent().setCursor(Cursor.N_RESIZE));
        ResizerSquare(squareNC);

        squareNC.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            double offsetY = event.getY() - getY();
            double newY = getY() + offsetY;

            if (newY >= 0 && newY <= getY() + getHeight()) {
                setY(newY);
                setHeight(getHeight() - offsetY);
            }

            DarkArea.run();
        });
    }

    private void ResizerSquare(Rectangle ResizeRectangle) {
        ResizeRectangle.setFill(resizerSquareColor);
        ResizeRectangle.addEventHandler(MouseEvent.MOUSE_EXITED, event ->
                ResizeRectangle.getParent().setCursor(Cursor.DEFAULT));
    }


}
