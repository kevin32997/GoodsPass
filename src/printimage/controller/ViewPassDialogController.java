/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package printimage.controller;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import printimage.helpers.Helper;
import printimage.helpers.SQLDatabase;
import printimage.models.BusinessInfo;
import printimage.models.Crew;
import printimage.models.Goodspass;
import printimage.models.Passes;
import printimage.models.Remark;
import printimage.models.User;

/**
 * FXML Controller class
 *
 * @author Admin
 */
public class ViewPassDialogController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private TextField dialog_passNo;
    
    @FXML
    private TextField dialog_address;
    
    @FXML
    private TextField dialog_vehicleDesc;
    
    @FXML
    private TextField dialog_plateNo;
    
    @FXML
    private TextField dialog_dateCreated;
    
    @FXML
    private TextField dialog_businessName;
    
    @FXML
    private TextField dialog_date_printed;
    
    @FXML
    private Button btnClose;
    
    @FXML
    private Button btnEdit;
    
    @FXML
    private Button btnPreview;
    
    @FXML
    private Button btnDelete;
    
    @FXML
    private Button btnHold;
    
    @FXML
    private Button btnAdd;
    
    @FXML
    private TableView<Crew> main_table;
    
    @FXML
    private TableView<Remark> remarks_table;
    
    @FXML
    private TableColumn<Crew, String> tableColumn_name;
    
    @FXML
    private TableColumn<Crew, String> tableColumn_designation;
    
    @FXML
    private TableColumn<Remark, String> remarkColumn_description;
    
    @FXML
    private TableColumn<Remark, String> remarkColumn_date;
    
    @FXML
    private ListView dialog_search_list;
    
    private Goodspass info;
    
    private SQLDatabase db;
    private Stage stage;
    private BusinessInfo businessInfo;
    private ViewBusinessInfoController ctrl;
    
    private ObservableList<BusinessInfo> searchedBusinessList;
    private ObservableList<Crew> crews;
    private ObservableList<Remark> remarks;
    
    private SimpleDateFormat df;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO

        df = new SimpleDateFormat("MMMMM dd, yyyy - hh:mm:ss a");
        initFields();
    }
    
    private void initFields() {

        // On Edit Search business
        searchedBusinessList = FXCollections.observableArrayList();
        dialog_businessName.setOnKeyReleased(e -> {
            if (dialog_businessName.isEditable()) {
                if (e.getCode() != KeyCode.ENTER) {
                    //selected_business_id = 0;
                    searchedBusinessList.clear();
                    if (!dialog_businessName.getText().toString().equalsIgnoreCase("") && dialog_businessName.getText() != null) {
                        dialog_search_list.getItems().clear();
                        searchedBusinessList = db.searchBusinessInfoLimit(dialog_businessName.getText().toString(), 5);
                        if (searchedBusinessList.size() > 0) {
                            this.dialog_search_list.setVisible(true);
                            for (BusinessInfo info : searchedBusinessList) {
                                this.dialog_search_list.getItems().add(info.getBusinessName());
                            }
                        } else {
                            searchedBusinessList.clear();
                            this.dialog_search_list.setVisible(false);
                        }
                    } else {
                        this.dialog_search_list.setVisible(false);
                        searchedBusinessList.clear();
                    }
                }
            }
        });

        // Listview Click Event
        this.dialog_search_list.setOnMouseClicked(e -> {
            businessInfo = searchedBusinessList.get(dialog_search_list.getSelectionModel().getSelectedIndex());
            this.dialog_search_list.setVisible(false);
            this.dialog_businessName.setText(businessInfo.getBusinessName());
            this.dialog_address.setText(businessInfo.getAddress());
            searchedBusinessList.clear();
        });
        
        dialog_businessName.setOnAction(e -> {
            this.dialog_search_list.setVisible(false);
            if (this.dialog_search_list.getItems().size() > 0) {
                
                this.dialog_businessName.setText(searchedBusinessList.get(0).getBusinessName());
                businessInfo = searchedBusinessList.get(0);
                searchedBusinessList.clear();
            } else {
                searchedBusinessList.clear();
            }
        });
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
        info = db.getPassInfoById(id);
        
        System.out.println("Date Printed: " + info.getSqlDatePrinted());
        
        businessInfo = db.getBusinessInfoById(Integer.parseInt(info.getBusinessId()));
        if (businessInfo == null) {
            businessInfo = new BusinessInfo();
        }
        
        crews = db.getAllCrewByGPNo(info.getGpNo());
        remarks = db.getAllRemarkByRemarksId(this.info.getId());
        
        setFields();
        setupTable();
    }
    
    private void setFields() {
        this.dialog_passNo.setText(info.getGpNo());
        this.dialog_vehicleDesc.setText(info.getVehicleDesc());
        this.dialog_plateNo.setText(info.getVehiclePlateNo());
        this.dialog_businessName.setText(businessInfo.getBusinessName());
        this.dialog_address.setText(businessInfo.getAddress());
        this.dialog_dateCreated.setText(df.format(info.getSqlDateCreated()));
        checkPassData();
    }
    
    public void setCtrl(ViewBusinessInfoController ctrl) {
        this.ctrl = ctrl;
    }
    
    private int viewtype = 0;
    
    private void checkPassData() {
        
        if (info.getStatus().equals("" + MainActivityController.STATUS_PRINTED) || info.getStatus().equals("" + MainActivityController.STATUS_HOLD) && info.getSqlDatePrinted() != null) {
            long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;
            java.util.Date dateToday = new java.util.Date();
            java.util.Date datePrinted = new java.util.Date(info.getSqlDatePrinted().getTime());

            //Calendar calPrinted = Calendar.getInstance();
            //Calendar calToday = Calendar.getInstance();
            //calPrinted.setTime(datePrinted);
            //calToday.setTime(dateToday);
            // boolean samedate = calPrinted.get(Calendar.DAY_OF_YEAR) == calToday.get(Calendar.DAY_OF_YEAR)
            //       && calPrinted.get(Calendar.YEAR) == calToday.get(Calendar.YEAR);
            boolean moreThanDay = Math.abs(dateToday.getTime() - datePrinted.getTime()) > MILLIS_PER_DAY;
            if (!moreThanDay) {
                viewtype = ViewCrewDialogController.FORM_EDIT;
            } else {
                btnEdit.setVisible(false);
                btnEdit.setDisable(true);
                
                btnDelete.setVisible(false);
                btnDelete.setDisable(true);
                
                btnAdd.setVisible(false);
                btnAdd.setDisable(true);
                viewtype = ViewCrewDialogController.FORM_VIEW;
            }
            
            if (info.getStatus().equals("" + MainActivityController.STATUS_HOLD)) {
                btnHold.setText("Approve");
                dialog_date_printed.setText(this.df.format(info.getSqlDatePrinted()) + " - CANCELLED");
            } else {
                btnHold.setText("Cancel Pass");
                
                dialog_date_printed.setText(this.df.format(info.getSqlDatePrinted()));
            }
            
        } else if (info.getStatus().equals("" + MainActivityController.STATUS_HOLD)) {
            
            viewtype = ViewCrewDialogController.FORM_EDIT;
            btnHold.setText("Approve");
            if (info.getSqlDatePrinted() == null) {
                dialog_date_printed.setText("NOT PRINTED.");
            } else {
                
                dialog_date_printed.setText(this.df.format(info.getSqlDatePrinted()) + " - CANCELLED");
                
            }
            
        } else {
            btnHold.setText("Cancel Pass");
            viewtype = ViewCrewDialogController.FORM_EDIT;
            dialog_date_printed.setText("NOT PRINTED.");
        }
    }
    
    private void setupTable() {
        this.tableColumn_name.setCellValueFactory(new PropertyValueFactory<Crew, String>("fullname"));
        this.tableColumn_designation.setCellValueFactory(new PropertyValueFactory<Crew, String>("designation"));
        
        main_table.setRowFactory(tv -> {
            TableRow<Crew> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && (!row.isEmpty())) {
                    openViewCrewInfoDialog(row.getItem());
                }
            });
            
            return row;
        });
        
        this.remarkColumn_description.setCellValueFactory(new PropertyValueFactory<Remark, String>("description"));
        this.remarkColumn_date.setCellValueFactory(new PropertyValueFactory<Remark, String>("dateCreated"));
        
        remarks_table.setRowFactory(tv -> {
            TableRow<Remark> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && (!row.isEmpty())) {
                    openRemarksInformationDialog(row.getItem());
                }
            });
            
            return row;
        });
        
        loadTableData();
        
    }
    
    private void loadTableData() {
        this.main_table.getItems().clear();
        this.main_table.getItems().addAll(crews);
        
        this.remarks_table.getItems().clear();
        this.remarks_table.getItems().addAll(remarks);
    }
    
    private void fieldsEnable(boolean enable) {
        //this.dialog_passNo.setEditable(enable);
        //this.dialog_address.setEditable(enable);
        this.dialog_vehicleDesc.setEditable(enable);
        this.dialog_plateNo.setEditable(enable);
        this.dialog_businessName.setEditable(enable);
    }
    
    public void refreshCrewList() {
        crews = db.getAllCrewByGPNo(info.getGpNo());
        loadTableData();
    }
    
    public void refreshRemarksList() {
        remarks = db.getAllRemarkByRemarksId(info.getId());
        loadTableData();
    }
    
    public void updatePass() {
        
        if (!info.getStatus().equals("1")) {
            db.updatePassInfoPrinted(info);
            db.updateDB();
        }
        
        this.checkPassData();

        // if this dialog came from business dialog
        if (ctrl != null) {
            ctrl.loadData();
        }
    }
    
    public void cancelPass() {
        
        info.setStatus("" + MainActivityController.STATUS_HOLD);
        if (db.updatePassInfo(info)) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Update Success");
            alert.setHeaderText("Goodspass has been canceled.");
            alert.showAndWait();
            db.updateDB();
            this.refreshRemarksList();
        }
        this.checkPassData();

        // if this dialog came from business dialog
        if (ctrl != null) {
            ctrl.loadData();
        }
    }
    
    public void approvePass() {
        
        if (info.getSqlDatePrinted() != null) {
            info.setStatus("" + MainActivityController.STATUS_PRINTED);
            if (db.updatePassInfo(info)) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Update Successfull");
                alert.setHeaderText("Goodspass has been approved.");
                alert.showAndWait();
                db.updateDB();
                this.refreshRemarksList();
            }
        } else {
            info.setStatus("" + MainActivityController.STATUS_PENDING);
            if (db.updatePassInfo(info)) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Update Successfull");
                alert.setHeaderText("Goodspass has been approved.");
                alert.showAndWait();
                db.updateDB();
            }
        }
        
        this.checkPassData();

        // if this dialog came from business dialog
        if (ctrl != null) {
            ctrl.loadData();
        }
    }
    
    private void openIDPreview(Passes pass) {
        AnchorPane root;
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/layout_to_print.fxml"));
        try {
            root = loader.load();
            Scene scene = new Scene(root, 849, 714);
            Stage stage = new Stage();
            stage.getIcons().add(Helper.getMaterialDesignIcon(MaterialDesignIcon.TOOLTIP_OUTLINE));
            stage.setTitle("Preview");
            stage.setScene(scene);

            // Hide this current window (if this is what you want)
            PrintLayoutController ctrl = (PrintLayoutController) loader.getController();
            ctrl.setPasses(pass,db.getAllCrewByGPNo(pass.getCtrlNo()));
            ctrl.setStage(stage);
            ctrl.setCtrl(this);

            // Show Window
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    private void openAddCrewInformationDialog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/create_crew_dialog_layout.fxml"));
            Parent parent = fxmlLoader.load();
            CreateCrewDialogCtrl ctrl = (CreateCrewDialogCtrl) fxmlLoader.getController();
            
            Scene scene = new Scene(parent, 385, 295);
            Stage stage = new Stage();
            stage.setTitle("Add Crew");
            stage.setResizable(false);
            
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            
            ctrl.setStage(stage);
            ctrl.setData(db, info);
            ctrl.setCtrl(this);
            
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void openViewCrewInfoDialog(Crew crew) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/dialog_view_crew_layout.fxml"));
            Parent parent = fxmlLoader.load();
            ViewCrewDialogController ctrl = (ViewCrewDialogController) fxmlLoader.getController();
            
            Scene scene = new Scene(parent, 430, 375);
            Stage stage = new Stage();
            
            stage.getIcons().add(Helper.getMaterialDesignIcon(MaterialDesignIcon.ACCOUNT_OUTLINE));
            stage.setTitle(this.info.getGpNo() + " - " + crew.getFullname());
            stage.setResizable(false);
            
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            
            ctrl.setData(stage, db, crew.getId());
            ctrl.setViewType(viewtype);
            ctrl.setCtrl(this);
            
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void openRemarksInformationDialog(Remark remark) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/dialog_view_remarks_layout.fxml"));
            Parent parent = fxmlLoader.load();
            
            Scene scene = new Scene(parent, 478, 342);
            Stage stage = new Stage();
            stage.getIcons().add(Helper.getMaterialDesignIcon(MaterialDesignIcon.MESSAGE_TEXT_OUTLINE));
            stage.setTitle("Remarks - " + info.getGpNo());
            stage.setResizable(false);
            
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            
            Label createdBy = (Label) parent.lookup("#created_by");
            
            Label remarksOf = (Label) parent.lookup("#remarks_of");
            Label dateCreated = (Label) parent.lookup("#date_created");
            TextArea textArea = (TextArea) parent.lookup("#text_area");
            Button btnClose = (Button) parent.lookup("#btnClose");
            
            Label remarksType = (Label) parent.lookup("#remark_type");
            remarksType.setText("Type: " + remark.getRemarkType());
            
            User user = db.getUserById(remark.getUser_id());
            
            if (user != null) {
                createdBy.setText("Remarks by: " + user.getFullname());
            } else {
                createdBy.setText("Remarks by: Unknown");
            }
            remarksOf.setText("Remarks of: " + info.getGpNo());
            dateCreated.setText("Date Create: " + remark.getDateCreated());
            textArea.setText(remark.getDescription());
            
            btnClose.setOnAction(e -> {
                stage.close();
            });
            
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    void onAddCrew(ActionEvent event) {
        if (crews.size() >= 5) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Operation Unable");
            alert.setHeaderText("Unable to add! Crew limit is 5.");
            alert.showAndWait();
        } else {

            // Add Crew Dialog
            openAddCrewInformationDialog();
        }
    }
    
    @FXML
    void onClose(ActionEvent event) {
        this.stage.close();
    }
    
    @FXML
    void onEdit(ActionEvent event) {
        // Edit
        if (btnEdit.getText().toString().endsWith("Edit")) {
            btnEdit.setText("Save");
            btnDelete.setText("Cancel");
            fieldsEnable(true);
            dialog_passNo.requestFocus();
        } // Save
        else {
            try {
                Stage stage = new Stage();
                
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/dialog_add_remarks_layout.fxml"));
                Parent parent = fxmlLoader.load();
                
                Scene scene = new Scene(parent, 470, 370);
                stage.setTitle("Update Goodspass");
                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setResizable(false);
                
                TextArea descriptionText = (TextArea) parent.lookup("#text_area");
                Button btnSave = (Button) parent.lookup("#btnSave");
                Button btnCancel = (Button) parent.lookup("#btnCancel");
                
                btnSave.setOnAction(e -> {

                    // Update pass
                    info.setVehicleDesc(dialog_vehicleDesc.getText());
                    info.setVehiclePlateNo(dialog_plateNo.getText());
                    info.setBusinessId("" + businessInfo.getId());
                    info.setBusinessName(businessInfo.getBusinessName());

                    // Save to db
                    if (db.updatePassInfo(info)) {
                        
                        if (descriptionText.getText().equals("")) {
                            db.createRemarks(Remark.TARGET_PASS, Remark.REMARK_UPDATE, MainActivityController.MAIN_USER.getId(), this.info.getId(), "PASS INFORMATION UPDATED");
                        } else {
                            db.createRemarks(Remark.TARGET_PASS, Remark.REMARK_UPDATE, MainActivityController.MAIN_USER.getId(), this.info.getId(), descriptionText.getText());
                        }
                        db.updateDB();
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Update Success");
                        alert.setHeaderText("Information Updated!");
                        alert.showAndWait();
                        stage.close();
                        this.refreshRemarksList();
                    }
                    
                    btnEdit.setText("Edit");
                    btnDelete.setText("Delete");
                    fieldsEnable(false);
                    if (ctrl != null) {
                        ctrl.loadData();
                    }
                });
                
                btnCancel.setOnAction(e -> {
                    stage.close();
                });
                
                stage.show();
                
            } catch (IOException ex) {
                Logger.getLogger(ViewPassDialogController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @FXML
    void onPreview(ActionEvent event) {
        Passes pass = new Passes();
        
        pass.setBusinessName(this.businessInfo.getBusinessName());
        pass.setAddress(this.businessInfo.getAddress());
        
        pass.setDescription(info.getVehicleDesc().toUpperCase());
        
        if (crews.size() > 0) {
            pass.setDesignation_1(crews.get(0).getFullname().toUpperCase() + " - " + crews.get(0).getDesignation().toUpperCase());
            if (crews.size() > 1) {
                pass.setDesignation_2(crews.get(1).getFullname().toUpperCase() + " - " + crews.get(1).getDesignation().toUpperCase());
            } else {
                pass.setDesignation_2("NONE");
            }
            
        }
        
        pass.setQrCode(this.info.getGpNo().toUpperCase());
        pass.setCtrlNo(this.info.getGpNo().toUpperCase());
        pass.setPlateNo(info.getVehiclePlateNo().toUpperCase());
        
        openIDPreview(pass);
    }
    
    @FXML
    void onDelete(ActionEvent event) {
        
        if (btnDelete.getText().equals("Delete")) {
            // Delete
            if (info.getSqlDatePrinted() == null) {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Delete");
                alert.setHeaderText("Delete Data?");
                alert.setContentText("Are you sure with this? this cannot be undone.");
                
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    // ... user chose OK
                    db.removePass(info.getId());
                    
                    for (Crew crew : crews) {
                        crew.setGpNo("");
                        db.removeCrewInfo(crew.getId());
                    }
                    Alert alertSuccess = new Alert(AlertType.INFORMATION);
                    alertSuccess.setTitle("Deleted");
                    alertSuccess.setHeaderText("Data Successfully Deleted!");
                    alertSuccess.showAndWait();
                    
                    db.updateDB();
                    stage.close();
                } else {
                    // ... user chose CANCEL or closed the dialog
                }
            } else {
                Alert alertSuccess = new Alert(AlertType.ERROR);
                alertSuccess.setTitle("Operation Unable");
                alertSuccess.setHeaderText("Cannot delete Pass Info!\nPass already been printed.");
                alertSuccess.showAndWait();
            }
        } else {
            // Cancel
            this.btnDelete.setText("Delete");
            this.btnEdit.setText("Edit");
            this.setFields();
            this.fieldsEnable(false);
        }
        
    }
    
    @FXML
    void onHold(ActionEvent event) {
        if (btnHold.getText().equals("Cancel Pass")) {
            try {
                // Cancel

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/dialog_add_remarks_layout.fxml"));
                Parent parent = fxmlLoader.load();
                Stage stage = new Stage();
                Scene scene = new Scene(parent, 470, 370);
                
                stage.setTitle("Add Remarks");
                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setResizable(false);
                
                TextArea descriptionText = (TextArea) parent.lookup("#text_area");
                Button btnSave = (Button) parent.lookup("#btnSave");
                Button btnCancel = (Button) parent.lookup("#btnCancel");
                
                btnSave.setOnAction(e -> {
                    
                    if (descriptionText.getText().equals("")) {
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Error");
                        alert.setHeaderText("Please add description to save!");
                        alert.showAndWait();
                    } else {
                        if (db.createRemarks(Remark.TARGET_PASS, Remark.REMARK_CANCEL, MainActivityController.MAIN_USER.getId(), this.info.getId(), descriptionText.getText())) {
                            cancelPass();
                            stage.close();
                        }
                    }
                });
                
                btnCancel.setOnAction(e -> {
                    stage.close();
                });
                
                stage.show();
            } catch (IOException ex) {
                Logger.getLogger(ViewPassDialogController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } else {
            try {
                // Approve

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/dialog_add_remarks_layout.fxml"));
                Parent parent = fxmlLoader.load();
                Stage stage = new Stage();
                Scene scene = new Scene(parent, 470, 370);
                
                stage.setTitle("Add Remarks");
                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setResizable(false);
                
                TextArea descriptionText = (TextArea) parent.lookup("#text_area");
                Button btnSave = (Button) parent.lookup("#btnSave");
                Button btnCancel = (Button) parent.lookup("#btnCancel");
                
                btnSave.setOnAction(e -> {
                    
                    if (descriptionText.getText().equals("")) {
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Error");
                        alert.setHeaderText("Please add description to save!");
                        alert.showAndWait();
                    } else {
                        if (db.createRemarks(Remark.TARGET_PASS, Remark.REMARK_APPROVE, MainActivityController.MAIN_USER.getId(), this.info.getId(), descriptionText.getText())) {
                            approvePass();
                            stage.close();
                        }
                    }
                });
                
                btnCancel.setOnAction(e -> {
                    stage.close();
                });
                
                stage.show();
            } catch (IOException ex) {
                Logger.getLogger(ViewPassDialogController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @FXML
    void onPrint(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/print_passes_dialog_layout.fxml"));
            Parent parent = fxmlLoader.load();
            PrintPassesCtrl ctrl = (PrintPassesCtrl) fxmlLoader.getController();
            
            Scene scene = new Scene(parent, 395, 416);
            Stage stage = new Stage();
            stage.getIcons().add(Helper.getMaterialDesignIcon(MaterialDesignIcon.PRINTER));
            stage.setTitle("PRINT PASSES");
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            
            ctrl.setStage(stage);
            
            ObservableList<Goodspass> passes = FXCollections.observableArrayList();
            info.setBusinessAddress(businessInfo.getAddress());
            passes.add(info);
            ctrl.setData(db, passes);
            
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    @FXML
    void onAddRemarks(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/dialog_add_remarks_layout.fxml"));
            Parent parent = fxmlLoader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(parent, 470, 370);
            
            stage.setTitle("Add Remarks");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            
            TextArea descriptionText = (TextArea) parent.lookup("#text_area");
            Button btnSave = (Button) parent.lookup("#btnSave");
            Button btnCancel = (Button) parent.lookup("#btnCancel");
            
            btnSave.setOnAction(e -> {
                
                if (descriptionText.getText().equals("")) {
                    
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Error");
                    alert.setHeaderText("Please add description to save!");
                    alert.showAndWait();
                } else {
                    if (db.createRemarks(Remark.TARGET_PASS, Remark.REMARK_CUSTOM, MainActivityController.MAIN_USER.getId(), this.info.getId(), descriptionText.getText())) {
                        db.updateDB();
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Updated");
                        alert.setHeaderText("Remarks Added!");
                        alert.showAndWait();
                        stage.close();
                        this.refreshRemarksList();
                    }
                }
            });
            
            btnCancel.setOnAction(e -> {
                stage.close();
            });
            
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(ViewPassDialogController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
