/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package printimage.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import printimage.helpers.Helper;
import printimage.helpers.SQLDatabase;
import printimage.models.User;

/**
 * FXML Controller class
 *
 * @author Admin
 */
public class ViewUserDialogController implements Initializable {

    @FXML
    private TextField fullname;

    @FXML
    private TextField username;

    @FXML
    private ChoiceBox<String> usertype;

    @FXML
    private ChoiceBox<String> isActive;

    @FXML
    private PasswordField new_password;

    @FXML
    private PasswordField confirm_pass;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnEdit;

    private User user;

    private SQLDatabase db;

    private Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO

        this.usertype.getItems().addAll("Encoder", "Administrator");
        this.isActive.getItems().addAll("Active", "Inactive");

    }

    public void setData(SQLDatabase db, int id) {
        this.db = db;
        this.user = db.getUserById(id);
        setFields();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void setFields() {
        this.fullname.setText(user.getFullname());
        this.username.setText(user.getUsername());

        if (this.user.getUsertype() == User.USER_ADMIN) {
            usertype.getSelectionModel().select(1);
        } else {
            usertype.getSelectionModel().select(0);
        }

        if (this.user.getActive() == 1) {
            this.isActive.getSelectionModel().select(0);
        } else {
            this.isActive.getSelectionModel().select(1);
        }

    }

    private void saveUser() {
        this.user.setFullname(fullname.getText());
        this.user.setUsername(username.getText());

        if (usertype.getSelectionModel().getSelectedIndex() == 0) {
            this.user.setUsertype(User.USER_ENCODER);
        } else {
            this.user.setUsertype(User.USER_ADMIN);
        }

        if (isActive.getSelectionModel().getSelectedIndex() == 0) {
            this.user.setActive(1);
        } else {
            this.user.setActive(0);
        }

        if (db.updateUser(user)) {
            db.updateDB();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Updated");
            alert.setHeaderText("User successfully updated!");
            alert.showAndWait();

            btnEdit.setText("Edit");
            btnCancel.setDisable(true);
            this.fullname.setEditable(false);
            this.username.setEditable(false);
            this.usertype.setDisable(true);
            this.isActive.setDisable(true);
            this.setFields();
        }
    }

    @FXML
    void onCancel(ActionEvent event) {
        btnEdit.setText("Edit");
        btnCancel.setDisable(true);
        this.fullname.setEditable(false);
        this.username.setEditable(false);
        this.usertype.setDisable(true);
        this.isActive.setDisable(true);
        this.setFields();
    }

    @FXML
    void onChangePass(ActionEvent event) {

        if (!this.new_password.getText().equals("")) {
            if (this.new_password.getText().equals(this.confirm_pass.getText())) {

                String newPass = Helper.getMd5(this.new_password.getText());
                System.out.println("New Pass: " + newPass);

                if (db.updateUserPassword(user.getId(), newPass)) {
                    user.setPassword(newPass);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText("Passwords has been change!");
                    alert.setContentText(null);
                    alert.show();

                    new_password.clear();
                    confirm_pass.clear();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid");
                alert.setHeaderText("Passwords did not match!");
                alert.setContentText(null);
                alert.showAndWait();
                this.confirm_pass.setText("");
                this.confirm_pass.requestFocus();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid");
            alert.setHeaderText("Please fill out missing fields!");
            alert.setContentText(null);
            alert.showAndWait();
        }

    }

    @FXML
    void onClose(ActionEvent event) {
        this.stage.close();
    }

    @FXML
    void onEdt(ActionEvent event) {
        if (btnEdit.getText().equals("Edit")) {
            // On Edit
            btnEdit.setText("Save");
            btnCancel.setDisable(false);
            this.fullname.setEditable(true);
            this.username.setEditable(true);
            this.usertype.setDisable(false);
            this.isActive.setDisable(false);

        } else {
            // On Save
            if (!this.fullname.getText().equals("") && !this.username.getText().equals("")) {
                this.saveUser();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Not Saved");
                alert.setHeaderText("Some fields are empty!");
                alert.setContentText("Please fill up all fields.");
                alert.showAndWait();
            }
        }
    }
}
