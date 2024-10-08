//package se233.cropedgestudio.components;
//
//import javafx.geometry.Pos;
//import javafx.scene.control.Label;
//import javafx.scene.input.DragEvent;
//import javafx.scene.input.Dragboard;
//import javafx.scene.input.TransferMode;
//import javafx.scene.layout.StackPane;
//import javafx.scene.text.TextAlignment;
//
//import java.io.File;
//import java.util.List;
//import java.util.function.Consumer;
//
//public class DragAndDropPane extends StackPane {
//    private final Label messageLabel;
//    private final Consumer<List<File>> onFilesDroppedHandler;
//
//    public DragAndDropPane(Consumer<List<File>> onFilesDroppedHandler) {
//        this.onFilesDroppedHandler = onFilesDroppedHandler;
//
//        messageLabel = new Label("Drag and drop image files or ZIP archives here");
//        messageLabel.setWrapText(true);
//        messageLabel.setTextAlignment(TextAlignment.CENTER);
//        messageLabel.setAlignment(Pos.CENTER);
//
//        this.getChildren().add(messageLabel);
//        this.setStyle("-fx-border-color: #cccccc; -fx-border-width: 2; -fx-border-style: dashed;");
//
//        setupDragAndDrop();
//    }
//
//    private void setupDragAndDrop() {
//        this.setOnDragOver(this::handleDragOver);
//        this.setOnDragEntered(this::handleDragEntered);
//        this.setOnDragExited(this::handleDragExited);
//        this.setOnDragDropped(this::handleDragDropped);
//    }
//
//    private void handleDragOver(DragEvent event) {
//        if (event.getDragboard().hasFiles()) {
//            event.acceptTransferModes(TransferMode.COPY);
//        }
//        event.consume();
//    }
//
//    private void handleDragEntered(DragEvent event) {
//        this.setStyle("-fx-border-color: #00ff00; -fx-border-width: 2; -fx-border-style: dashed;");
//        event.consume();
//    }
//
//    private void handleDragExited(DragEvent event) {
//        this.setStyle("-fx-border-color: #cccccc; -fx-border-width: 2; -fx-border-style: dashed;");
//        event.consume();
//    }
//
//    private void handleDragDropped(DragEvent event) {
//        Dragboard db = event.getDragboard();
//        boolean success = false;
//        if (db.hasFiles()) {
//            onFilesDroppedHandler.accept(db.getFiles());
//            success = true;
//        }
//        event.setDropCompleted(success);
//        event.consume();
//    }
//}