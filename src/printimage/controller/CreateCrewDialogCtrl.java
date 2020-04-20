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
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import printimage.helpers.SQLDatabase;
import printimage.models.Crew;
import printimage.models.Goodspass;

/**
 * FXML Controller class
 *
 * @author Admin
 */
public class CreateCrewDialogCtrl implements Initializable {

    @FXML
    private TextField pass_no;

    @FXML
    private TextField full_name;

    @FXML
    private TextField address;

    @FXML
    private TextField designation;

    @FXML
    private TextField presented_id;

    @FXML
    private TextField id_number;

    private Stage stage;
    private ViewPassDialogController ctrl;
    private SQLDatabase db;
    private Goodspass pass;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void setStage(Stage stage) {
        this.stage = stage;

    }

    public void setData(SQLDatabase db, Goodspass pass) {
        this.db = db;
        this.pass = pass;
        setupFields();
    }

    public void setCtrl(ViewPassDialogController ctrl) {
        this.ctrl = ctrl;
    }

    private void setupFields() {
        pass_no.setText(pass.getGpNo());

        full_name.setOnAction(e -> {
            address.requestFocus();
        });

        address.setOnAction(e -> {
            designation.requestFocus();
        });

        designation.setOnAction(e -> {
            presented_id.requestFocus();
        });

        presented_id.setOnAction(e -> {
            id_number.requestFocus();
        });

        id_number.setOnAction(e -> {
            // Add
            saveCrew();
        });

        full_name.requestFocus();

    }

    private void saveCrew() {
        if (!full_name.getText().equals("") && !designation.getText().equals("")) {
            Crew crew = new Crew();
            crew.setGpNo(pass.getGpNo());
            crew.setFullname(full_name.getText());
            crew.setDesignation(designation.getText());
            crew.setAddress(address.getText());
            crew.setIdPresented(presented_id.getText());
            crew.setIdNumber(id_number.getText());

            if (db.createCrewInfo(crew)) {
                db.updateDB();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Create Success");
                alert.setHeaderText("Crew Added!");
                alert.showAndWait();
                stage.close();

                ctrl.refreshCrewList();
            }

        } else {
            // Error
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Create Error");
            alert.setHeaderText("Please fill up Full name and Designation field.");
            alert.showAndWait();
        }
    }

    @FXML
    void onAddCrew(ActionEvent event) {
        saveCrew();
    }

    @FXML
    void onCancel(ActionEvent event) {
        stage.close();
    }

}
