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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Pagination;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import printimage.helpers.Helper;
import printimage.helpers.SQLDatabase;
import printimage.models.BusinessInfo;
import printimage.models.Goodspass;
import printimage.models.Remark;
import printimage.models.User;

/**
 * FXML Controller class
 *
 * @author Admin
 */
public class ViewUserDialogController implements Initializable {
    
    public static final int VIEWTYPE_USERACCOUNT = 0;
    public static final int VIEWTYPE_ADMINISTRATOR = 1;
    
    private int view_type = VIEWTYPE_ADMINISTRATOR;
    
    @FXML
    private TextField fullname;
    
    @FXML
    private TextField username;
    
    @FXML
    private TextField status;
    
    @FXML
    private ChoiceBox<String> usertype;
    
    @FXML
    private PasswordField new_password;
    
    @FXML
    private PasswordField confirm_pass;
    
    @FXML
    private Button btnCancel;
    
    @FXML
    private Button btnEdit;
    
    @FXML
    private Button btnActivate;
    
    private User user;
    
    private SQLDatabase db;
    
    private Stage stage;
    
    @FXML
    private PasswordField old_password;
    
    @FXML
    private TableView<Remark> remarks_table;
    
    @FXML
    private TableColumn<Remark, String> remarks_type;
    
    @FXML
    private TableColumn<Remark, String> remarks_description;
    
    @FXML
    private TableColumn<Remark, String> remarks_date;
    
    @FXML
    private TableColumn<Remark, String> remarks_of;
    
    @FXML
    private Pagination pagination;
    
    private int table_row_count = 17;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        this.usertype.getItems().addAll("Encoder", "Administrator");
    }
    
    public void setData(SQLDatabase db, int id) {
        this.db = db;
        this.user = db.getUserById(id);
        setFields();
        setupTable();
        setupPagination();
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
            status.setText("Active");
            this.btnActivate.setText("Deactivate");
        } else {
            status.setText("Deactivated");
            this.btnActivate.setText("Activate");
        }
    }
    
    public void setViewType(int view_type) {
        this.view_type = view_type;
        
        if (this.view_type == VIEWTYPE_ADMINISTRATOR) {
            
        } else {
            this.old_password.setDisable(false);
        }
    }
    
    private void setupTable() {
        this.remarks_table.setRowFactory(tv -> {
            TableRow<Remark> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && (!row.isEmpty())) {
                    if (row.getItem().getTarget_type().equals(Remark.TARGET_PASS)) {
                        this.openViewPassDialog(db.getPassInfoById(row.getItem().getRemarkId()));
                    } else {
                        this.openViewBusinessInfoDialog(db.getBusinessInfoById(row.getItem().getRemarkId()));
                    }
                }
            });
            
            return row;
        });
        
        this.remarks_type.setCellValueFactory(new PropertyValueFactory<Remark, String>("remarkType"));
        this.remarks_description.setCellValueFactory(new PropertyValueFactory<Remark, String>("description"));
        this.remarks_of.setCellValueFactory(new PropertyValueFactory<Remark, String>("remarksOf"));
        this.remarks_date.setCellValueFactory(new PropertyValueFactory<Remark, String>("dateCreated"));
    }
    
    private void setupPagination() {
        int count = db.getRemarksCountByUser(user.getId());
        System.out.println("Remarks count is " + count);
        int page_count = count / this.table_row_count;
        if (count % this.table_row_count > 0) {
            page_count++;
        }
        System.out.println("Page count is " + page_count);
        this.pagination.setCurrentPageIndex(0);
        pagination.setPageCount(page_count);
        pagination.setMaxPageIndicatorCount(page_count);
        
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            pagination.setCurrentPageIndex(newIndex.intValue());
            setRemarksTableData(db.getRemarksLimitUser(newIndex.intValue() * this.table_row_count, this.table_row_count, user.getId()));
        });
        
        setRemarksTableData(db.getRemarksLimitUser(0 * this.table_row_count, this.table_row_count, user.getId()));
    }
    
    private void setRemarksTableData(ObservableList<Remark> list) {
        this.remarks_table.getItems().clear();
        for (Remark remark : list) {
            remarks_table.getItems().add(remark);
            
            if (remark.getTarget_type().equals(Remark.TARGET_PASS)) {
                Goodspass pass = db.getPassInfoById(remark.getRemarkId());
                remark.setRemarksOf("Pass no. " + pass.getGpNo());
            } else if (remark.getTarget_type().equals(Remark.TARGET_BUSINESS)) {
                BusinessInfo businessInfo = db.getBusinessInfoById(remark.getRemarkId());
                remark.setRemarksOf("Business: " + businessInfo.getBusinessName());
            } else {
                remark.setRemarksOf("Unknown");
            }
        }
        remarks_table.refresh();
    }
    
    private void saveUser() {
        this.user.setFullname(fullname.getText());
        this.user.setUsername(username.getText());
        
        if (usertype.getSelectionModel().getSelectedIndex() == 0) {
            this.user.setUsertype(User.USER_ENCODER);
        } else {
            this.user.setUsertype(User.USER_ADMIN);
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
            this.setFields();
        }
    }
    
    private void openViewPassDialog(Goodspass pass) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/dialog_view_member_layout.fxml"));
            Parent parent = fxmlLoader.load();
            ViewPassDialogController ctrl = (ViewPassDialogController) fxmlLoader.getController();
            
            Scene scene = new Scene(parent, 866, 397);
            Stage stage = new Stage();
            stage.setTitle("PASS INFO (" + pass.getGpNo() + ") - " + pass.getBusinessName());
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            
            ctrl.setData(stage, db, pass.getId());
            
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void openViewBusinessInfoDialog(BusinessInfo info) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/dialog_view_business_layout.fxml"));
            Parent parent = fxmlLoader.load();
            ViewBusinessInfoController ctrl = (ViewBusinessInfoController) fxmlLoader.getController();
            Stage stage = new Stage();
            Scene scene = new Scene(parent, 1031, 612);
            
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setTitle(info.getBusinessName());
            stage.setResizable(false);
            
            ctrl.setData(stage, db, info.getId());
            
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    void onCancel(ActionEvent event) {
        btnEdit.setText("Edit");
        btnCancel.setDisable(true);
        this.fullname.setEditable(false);
        this.username.setEditable(false);
        this.usertype.setDisable(true);
        this.setFields();
    }
    
    @FXML
    void onChangePass(ActionEvent event) {
        if (this.view_type == VIEWTYPE_ADMINISTRATOR) {
            
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
                        old_password.clear();
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
        } else {
            if (!this.new_password.getText().equals("")) {
                
                if (Helper.getMd5(this.old_password.getText()).equals(user.getPassword())) {
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
                            old_password.clear();
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
                    alert.setTitle("Wrong Password");
                    alert.setHeaderText("Please enter correct Old Password!");
                    alert.setContentText(null);
                    alert.showAndWait();
                    this.old_password.requestFocus();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid");
                alert.setHeaderText("Please fill out missing fields!");
                alert.setContentText(null);
                alert.showAndWait();
            }
            
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
    
    @FXML
    void onActivate(ActionEvent event) {
        if (this.user.getActive() == 1) {
            // If Active
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Deactivate User");
            alert.setHeaderText("Deactivate this user?");
            alert.setContentText("User cannot login on this application.");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                if (db.updateUserActive(user.getId(), 0)) {
                    db.updateDB();
                    Alert alert_success = new Alert(Alert.AlertType.INFORMATION);
                    alert_success.setTitle("Updated");
                    alert_success.setHeaderText("User has been Deactivated!");
                    alert_success.setContentText("User cannot login on this application.");
                    alert_success.showAndWait();
                    user.setActive(0);
                    this.setFields();
                }
            }
            
        } else {
            // If Deactivated
            // If Active
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Activate User");
            alert.setHeaderText("Activate this user?");
            alert.setContentText("User can login on this application.");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                if (db.updateUserActive(user.getId(), 1)) {
                    db.updateDB();
                    Alert alert_success = new Alert(Alert.AlertType.INFORMATION);
                    alert_success.setTitle("Updated");
                    alert_success.setHeaderText("User has been Activated!");
                    alert_success.setContentText("User can now login on this application.");
                    alert_success.showAndWait();
                    user.setActive(1);
                    this.setFields();
                }
            }
        }
        
    }
}
