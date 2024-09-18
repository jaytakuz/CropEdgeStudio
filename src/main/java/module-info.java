module se233.changescreentest {
    requires javafx.controls;
    requires javafx.fxml;


    opens se233.changescreentest to javafx.fxml;
    exports se233.changescreentest;
}