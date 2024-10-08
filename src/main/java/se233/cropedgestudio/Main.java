// 662115032 Pongpiphat Kalasuk
// 662115047 Watcharapong Wanna

package se233.cropedgestudio;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/se233/cropedgestudio/views/MainView.fxml"));
        primaryStage.setTitle("CropEdgeStudio");
        primaryStage.setScene(new Scene(root, 800, 690));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}