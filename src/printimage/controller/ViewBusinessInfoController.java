/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package printimage.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import printimage.helpers.SQLDatabase;
import printimage.models.BusinessInfo;
import printimage.models.Goodspass;
import printimage.models.Remark;

/**
 * FXML Controller class
 *
 * @author Admin
 */
public class ViewBusinessInfoController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private TextField dialog_owner;

    @FXML
    private TextField dialog_businessname;

    @FXML
    private TextField dialog_address;

    @FXML
    private TextField dialog_permit_no;

    @FXML
    private TextField dialog_contact_no;

    @FXML
    private TextField dialog_contact_person;

    @FXML
    private Button btnClose;

    @FXML
    private Button btnEdit;

    @FXML
    private TableView<Goodspass> dialog_main_table;

    @FXML
    private TableColumn<Goodspass, String> dialog_tablerow_id;

    @FXML
    private TableColumn<Goodspass, String> dialog_tablerow_name;

    @FXML
    private TableColumn<Goodspass, String> dialog_tablerow_printed;

    private Stage stage;
    private BusinessInfo businessInfo;
    private SQLDatabase db;

    private ObservableList<Goodspass> passes;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        initTable();
    }

    private void initTable() {
        this.dialog_tablerow_id.setCellValueFactory(new PropertyValueFactory<Goodspass, String>("gpNo"));
        this.dialog_tablerow_name.setCellValueFactory(new PropertyValueFactory<Goodspass, String>("vehicleDesc"));
        this.dialog_tablerow_printed.setCellValueFactory(new PropertyValueFactory<Goodspass, String>("date_printed"));
        this.dialog_main_table.setRowFactory(tv -> {
            TableRow<Goodspass> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && (!row.isEmpty())) {
                    openViewDriverDialog(row.getItem());
                }
            });

            return row;
        });
    }

    private void openViewDriverDialog(Goodspass pass) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/dialog_view_member_layout.fxml"));
            Parent parent = fxmlLoader.load();
            ViewPassDialogController ctrl = (ViewPassDialogController) fxmlLoader.getController();
            Stage stage = new Stage();
            Scene scene = new Scene(parent, 866, 397);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setTitle(this.businessInfo.getBusinessName() + " - " + pass.getGpNo());
            stage.setResizable(false);

            ctrl.setData(stage, db, pass.getId());
            ctrl.setCtrl(this);

            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        businessInfo = db.getBusinessInfoById(id);
        loadData();
        setFields();
    }

    public void loadData() {
        this.dialog_main_table.getItems().clear();
        passes = db.getAllPassesByBusinessId(businessInfo.getId());

        for (Goodspass driver : passes) {
            if (driver.getDate_printed() == null || driver.getDate_printed().equals("")) {
                driver.setDate_printed("Not Printed");
            }
        }
        this.dialog_main_table.setItems(passes);
    }

    private void setFields() {
        this.dialog_owner.setText(businessInfo.getOwner());
        this.dialog_businessname.setText(businessInfo.getBusinessName());
        this.dialog_address.setText(businessInfo.getAddress());
        this.dialog_permit_no.setText(businessInfo.getPermitNo());
        this.dialog_contact_no.setText(businessInfo.getContact());
        this.dialog_contact_person.setText(businessInfo.getContactPerson());

    }

    private void fieldsEnable(boolean enable) {
        this.dialog_owner.setEditable(enable);
        this.dialog_businessname.setEditable(enable);
        this.dialog_address.setEditable(enable);
        this.dialog_permit_no.setEditable(enable);
        this.dialog_contact_no.setEditable(enable);
        this.dialog_contact_person.setEditable(enable);

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

        if (db.getPassInfoByBusinessId(businessInfo.getId()).size() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Delete Warning!");
            alert.setHeaderText("Can't Delete Data!");
            alert.setContentText("There are other data that is associated with this data, this could make the app crash!");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {

            } else {
                // ... user chose CANCEL or closed the dialog
            }
        } else {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete");
            alert.setHeaderText("Delete Data?");
            alert.setContentText("Are you sure with this? this cannot be undone.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                // ... user chose OK
                db.removeBusinessInfo(this.businessInfo.getId());
                Alert alertSuccess = new Alert(Alert.AlertType.INFORMATION);
                alertSuccess.setTitle("Deleted");
                alertSuccess.setHeaderText("Data Successfully Deleted!");
                alertSuccess.showAndWait();

                db.updateDB();
                stage.close();
            } else {
                // ... user chose CANCEL or closed the dialog
            }
        }
    }

    @FXML
    void onEdit(ActionEvent event) {
        // Edit
        if (btnEdit.getText().toString().endsWith("Edit")) {
            btnEdit.setText("Save");
            btnClose.setText("Cancel");
            fieldsEnable(true);

            dialog_owner.requestFocus();
        } // Save
        else {
            try {
                Stage stage = new Stage();

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/dialog_add_remarks_layout.fxml"));
                Parent parent = fxmlLoader.load();

                Scene scene = new Scene(parent, 477, 370);
                stage.setTitle("Update Business Information");
                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setResizable(false);

                TextArea descriptionText = (TextArea) parent.lookup("#text_area");
                Button btnSave = (Button) parent.lookup("#btnSave");
                Button btnCancel = (Button) parent.lookup("#btnCancel");

                btnSave.setOnAction(e -> {

                    // Update business Info
                    businessInfo.setOwner(dialog_owner.getText());
                    businessInfo.setBusinessName(dialog_businessname.getText());
                    businessInfo.setAddress(dialog_address.getText());
                    businessInfo.setPermitNo(dialog_permit_no.getText());
                    businessInfo.setContact(dialog_contact_no.getText());
                    businessInfo.setContactPerson(dialog_contact_person.getText());

                    // Save to db
                    if (db.updateBusinessInfo_boolean(businessInfo)) {
                        if (descriptionText.getText().equals("")) {
                            db.createRemarks(Remark.REMARK_BUSINESS, businessInfo.getId(), "BUSINESS INFORMATION UPDATED");
                        } else {
                            db.createRemarks(Remark.REMARK_BUSINESS, businessInfo.getId(), descriptionText.getText());
                        }

                        db.updateDB();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Update Success");
                        alert.setHeaderText("Information Updated!");
                        alert.showAndWait();
                        stage.close();

                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Update Error");
                        alert.setHeaderText("Something went wrong!");
                        alert.setContentText("Please check server connection!");
                        alert.showAndWait();
                    }
                    btnEdit.setText("Edit");
                    btnClose.setText("Close");
                    fieldsEnable(false);
                });

                btnCancel.setOnAction(e -> {
                    stage.close();
                });

                stage.show();

            } catch (IOException ex) {
                Logger.getLogger(ViewBusinessInfoController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
