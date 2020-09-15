/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package printimage;

import java.io.File;
import printimage.controller.MainActivityController;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 *
 * @author Admin
 */
public class PrintImage extends Application {

    public static final String salt = "ojhX4B1zFQ0H2Pyw";
    public static final String pepper = "eVW2J/*/ZQW";

    @Override
    public void start(Stage primaryStage) {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("layout/main_activity_layout.fxml"));
        AnchorPane root = null;
        try {
            root = (AnchorPane) loader.load();
        } catch (IOException ex) {
            Logger.getLogger(PrintImage.class.getName()).log(Level.SEVERE, null, ex);
        }

        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setTitle("GOODS PASS REGISTRY");

        File app_icon = new File("res/app_icon.png");
        System.out.println("File path: " + app_icon.getAbsolutePath());
        if (app_icon.exists()) {
            System.out.println("Icon exist");
        } else {
            System.out.println("Icon dont exist");
        }

        primaryStage.getIcons().add(new Image(app_icon.toURI().toString()));
        MainActivityController ctrl = (MainActivityController) loader.getController();
        ctrl.setStage(primaryStage);

        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
