/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package printimage.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import printimage.PrintImage;
import printimage.helpers.Helper;
import printimage.helpers.SQLDatabase;
import printimage.models.User;

/**
 * FXML Controller class
 *
 * @author Admin
 */
public class LoginActivityController implements Initializable {

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    private Stage stage;

    private SQLDatabase db;

    private MainActivityController ctrl;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        setFields();
    }

    private void setFields() {
        this.username.setOnAction(e -> {
            this.password.requestFocus();
        });

        this.password.setOnAction(e -> {
            this.tryLogin();
        });
    }

    public void setDB(SQLDatabase db) {
        this.db = db;
    }

    public void setStage(Stage stage) {
        this.stage = stage;

        this.stage.setOnHiding(e -> {
            ctrl.stopRunning();
            Platform.exit();
        });
    }

    public void setCtrl(MainActivityController ctrl) {
        this.ctrl = ctrl;
    }

    @FXML
    void onLogin(ActionEvent event) {
        tryLogin();
    }

    private void tryLogin() {

        User user = db.checkUser(username.getText(), Helper.getMd5(password.getText()));
        System.out.println("Password hash: " + Helper.getMd5(password.getText()));
        if (user != null) {
            if (user.getActive() == 1) {

                // open program
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/main_activity_layout.fxml"));
                AnchorPane root = null;
                try {
                    root = (AnchorPane) loader.load();
                } catch (IOException ex) {
                    Logger.getLogger(PrintImage.class.getName()).log(Level.SEVERE, null, ex);
                }

                Scene scene = new Scene(root);

                stage.setScene(scene);
                stage.setMaximized(true);
                stage.setTitle("GOODS PASS REGISTRY");

                MainActivityController ctrl = (MainActivityController) loader.getController();
                ctrl.setUser(user);
                ctrl.setStage(stage);
                ctrl.startUpdateThread();
                stage.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid");
                alert.setHeaderText("User has been Deactivated!");
                alert.setContentText("Contact admin for activation.");
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid");
            alert.setHeaderText("Wrong Username or Password!");
            alert.setContentText("Please input valid user.");
            alert.showAndWait();
        }
    }

}
