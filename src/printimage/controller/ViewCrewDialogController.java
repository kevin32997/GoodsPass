/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package printimage.controller;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import printimage.helpers.SQLDatabase;
import printimage.models.Crew;

/**
 * FXML Controller class
 *
 * @author Admin
 */
public class ViewCrewDialogController implements Initializable {

    @FXML
    private TextField gp_no;

    @FXML
    private TextField business_name;

    @FXML
    private TextField full_name;

    @FXML
    private TextField address;

    @FXML
    private TextField designation;

    @FXML
    private TextField id_presented;

    @FXML
    private TextField id_number;

    @FXML
    private Button btnClose;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnRemove;

    private ViewPassDialogController ctrl;
    private Stage stage;
    private Crew crew;
    private SQLDatabase db;

    public static final int FORM_VIEW = 0;
    public static final int FORM_EDIT = 1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void setCtrl(ViewPassDialogController ctrl) {
        this.ctrl = ctrl;
    }

    public void setData(Stage stage, SQLDatabase db, int id) {
        this.stage = stage;

        this.stage.getScene().setOnKeyReleased(e -> {
            KeyCode code = e.getCode();
            if (code == KeyCode.ESCAPE) {
                stage.close();
            }
        });

        this.db = db;
        crew = db.getCrewInfoById(id);
        setFields();
    }

    private void setFields() {
        this.gp_no.setText(crew.getGpNo());
        this.business_name.setText(db.getPassInfoByPassNo(crew.getGpNo()).getBusinessName());
        this.full_name.setText(crew.getFullname());
        this.address.setText(crew.getAddress());
        this.designation.setText(crew.getDesignation());
        this.id_presented.setText(crew.getIdPresented());
        this.id_number.setText(crew.getIdNumber());
    }

    public void setViewType(int viewtype) {
        if (viewtype == FORM_VIEW) {
            // View only
            btnRemove.setVisible(false);
            btnRemove.setDisable(true);

            btnEdit.setVisible(false);
            btnEdit.setDisable(true);
        }
    }

    private void fieldsEnable(boolean enable) {
        this.full_name.setEditable(enable);
        this.address.setEditable(enable);
        this.designation.setEditable(enable);
        this.id_presented.setEditable(enable);
        this.id_number.setEditable(enable);

    }

    @FXML
    void onClose(ActionEvent event) {
        // Close
        if (btnClose.getText().toString().equals("Close")) {
            this.stage.close();
        } // Cancel
        else {
            this.btnEdit.setText("Edit");
            this.btnClose.setText("Close");
            this.setFields();
            this.fieldsEnable(false);
        }
    }

    @FXML
    void onDelete(ActionEvent event) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Operation");
        alert.setHeaderText("Remove Crew from Pass?");
        alert.setContentText("Are you sure with this? this cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            // ... user chose OK
            db.removeCrewInfo(this.crew.getId());
            db.updateDB();
            Alert alertSuccess = new Alert(Alert.AlertType.INFORMATION);
            alertSuccess.setTitle("Removed");
            alertSuccess.setHeaderText("Data Successfully Removed!");
            alertSuccess.showAndWait();
            ctrl.refreshCrewList();
            stage.close();
        } else {
            // ... user chose CANCEL or closed the dialog
        }

    }

    @FXML
    void onEdit(ActionEvent event) {

        // Edit
        if (btnEdit.getText().toString().endsWith("Edit")) {
            btnEdit.setText("Save");
            btnClose.setText("Cancel");
            fieldsEnable(true);
            this.full_name.requestFocus();
        } // Save
        else {

            crew.setGpNo(this.gp_no.getText());
            crew.setFullname(this.full_name.getText());
            crew.setAddress(this.address.getText());
            crew.setDesignation(this.designation.getText());
            crew.setIdPresented(this.id_presented.getText());
            crew.setIdNumber(this.id_number.getText());

            // Save to db
            if (db.updateCrewInfo(crew)) {
                db.updateDB();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Update Success");
                alert.setHeaderText("Information Updated!");
                alert.showAndWait();
                ctrl.refreshCrewList();
            }

            btnEdit.setText("Edit");
            btnClose.setText("Close");
            fieldsEnable(false);
        }

    }

}
