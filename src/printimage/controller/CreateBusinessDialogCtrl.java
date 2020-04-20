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
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import printimage.helpers.SQLDatabase;
import printimage.models.BusinessInfo;

/**
 * FXML Controller class
 *
 * @author Admin
 */
public class CreateBusinessDialogCtrl implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private TextField business_name;

    @FXML
    private TextField owner;

    @FXML
    private TextField permit_number;

    @FXML
    private TextField address;

    @FXML
    private TextField contact_person;

    @FXML
    private TextField contact_number;

    private Stage stage;
    private SQLDatabase db;
    private MainActivityController ctrl;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void setData(Stage stage, SQLDatabase db, MainActivityController ctrl, String businessName) {
        this.stage = stage;
        this.db = db;
        this.ctrl = ctrl;
        setFields(businessName);
    }

    private void setFields(String businessName) {
        this.business_name.setText(businessName);
        this.owner.requestFocus();
        owner.setOnAction(e -> {
            permit_number.requestFocus();
        });

        permit_number.setOnAction(e -> {
            address.requestFocus();
        });

        address.setOnAction(e -> {
            contact_person.requestFocus();
        });

        contact_person.setOnAction(e -> {
            contact_number.requestFocus();
        });

        contact_number.setOnAction(e -> {
            saveBusinessInfo();
        });
    }

    private void saveBusinessInfo() {
        BusinessInfo info = new BusinessInfo();
        info.setBusinessName(this.business_name.getText());
        info.setOwner(this.owner.getText());
        info.setPermitNo(this.permit_number.getText());
        info.setAddress(this.address.getText());
        info.setContactPerson(this.contact_person.getText());
        info.setContact(this.contact_number.getText());

        db.createBusinessInfo(info);
        int last_id = db.getLastBusinessInfoId();
        if (last_id != 0) {
            info.setId(last_id);
            ctrl.setBusinessInfo(info);
            this.stage.close();
        } else {
            ///////////////////////////////////////////////////
        }
    }

    @FXML
    void onCancel(ActionEvent event) {
        this.stage.close();
    }

    @FXML
    void onSave(ActionEvent event) {
        saveBusinessInfo();
    }

}
