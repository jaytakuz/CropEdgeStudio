module se233.cropedgestudio {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;

    opens se233.cropedgestudio to javafx.fxml;
    opens se233.cropedgestudio.controllers to javafx.fxml;
    exports se233.cropedgestudio;
}