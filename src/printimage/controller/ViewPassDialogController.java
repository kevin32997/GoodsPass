/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package printimage.controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
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
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import printimage.helpers.SQLDatabase;
import printimage.models.BusinessInfo;
import printimage.models.Crew;
import printimage.models.Goodspass;
import printimage.models.Passes;

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
    private TableColumn<Crew, String> tableColumn_name;

    @FXML
    private TableColumn<Crew, String> tableColumn_designation;

    @FXML
    private ListView dialog_search_list;

    private Goodspass info;
    private ObservableList<Crew> crews;
    private SQLDatabase db;
    private Stage stage;
    private BusinessInfo businessInfo;
    private ViewBusinessInfoController ctrl;

    private ObservableList<BusinessInfo> searchedBusinessList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
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

        businessInfo = db.getBusinessInfoById(Integer.parseInt(info.getBusinessId()));
        if (businessInfo == null) {
            businessInfo = new BusinessInfo();
        }

        crews = db.getAllCrewByGPNo(info.getGpNo());

        setFields();
        setupTable();
    }

    private void setFields() {
        this.dialog_passNo.setText(info.getGpNo());
        this.dialog_vehicleDesc.setText(info.getVehicleDesc());
        this.dialog_plateNo.setText(info.getVehiclePlateNo());
        this.dialog_businessName.setText(businessInfo.getBusinessName());
        this.dialog_address.setText(businessInfo.getAddress());
        checkIfPrinted();
    }

    public void setCtrl(ViewBusinessInfoController ctrl) {
        this.ctrl = ctrl;
    }

    private int viewtype = 0;

    private void checkIfPrinted() {
        if (info.getDate_printed() != null) {
            btnEdit.setVisible(false);
            btnEdit.setDisable(true);

            btnDelete.setVisible(false);
            btnDelete.setDisable(true);

            btnHold.setVisible(false);
            btnHold.setDisable(true);

            btnAdd.setVisible(false);
            btnAdd.setDisable(true);
            viewtype = ViewCrewDialogController.FORM_VIEW;
            dialog_date_printed.setText(info.getDate_printed());
        } else {
            viewtype = ViewCrewDialogController.FORM_EDIT;
            dialog_date_printed.setText("NOT PRINTED.");
        }
    }

    private void setupTable() {
        this.tableColumn_name.setCellValueFactory(new PropertyValueFactory<Crew, String>("fullname"));
        this.tableColumn_designation.setCellValueFactory(new PropertyValueFactory<Crew, String>("designation"));
        loadTableData();
        main_table.setRowFactory(tv -> {
            TableRow<Crew> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && (!row.isEmpty())) {
                    openViewCrewInfoDialog(row.getItem());
                }
            });

            return row;
        });
    }

    private void loadTableData() {
        this.main_table.getItems().clear();
        this.main_table.getItems().addAll(crews);
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

    public void updatePass() {
        info.setStatus("" + MainActivityController.STATUS_PRINTED);
        java.util.Date date_today = new java.util.Date();
        Date sqlDate = new Date(date_today.getTime());
        info.setDate_sql(sqlDate);
        info.setDate_printed(sqlDate.toString());
        db.updatePassInfo(info);
        db.updateDB();

        this.checkIfPrinted();
        if (ctrl != null) {
            ctrl.loadData();
        }

    }

    @FXML
    void onAddCrew(ActionEvent event) {
        if (crews.size() == 2) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Operation Unable");
            alert.setHeaderText("Unable to add! Crew limit is 2.");
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

            info.setVehicleDesc(dialog_vehicleDesc.getText());
            info.setVehiclePlateNo(dialog_plateNo.getText());
            info.setBusinessId("" + businessInfo.getId());

            // Save to db
            if (db.updatePassInfo(info)) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Update Success");
                alert.setHeaderText("Information Updated!");
                alert.showAndWait();
                db.updateDB();

            }
            btnEdit.setText("Edit");
            btnDelete.setText("Delete");
            fieldsEnable(false);
            if (ctrl != null) {
                ctrl.loadData();
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
                    db.updateCrewInfo(crew);
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
            // Cancel
            this.btnDelete.setText("Delete");
            this.btnEdit.setText("Edit");
            this.setFields();
            this.fieldsEnable(false);

        }

    }

    @FXML
    void onHold(ActionEvent event) {

    }

    private void openIDPreview(Passes pass) {
        AnchorPane root;
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/layout_to_print.fxml"));
        try {
            root = loader.load();
            Scene scene = new Scene(root, 849, 714);
            Stage stage = new Stage();
            stage.setTitle("Preview");
            stage.setScene(scene);

            // Hide this current window (if this is what you want)
            PrintLayoutController ctrl = (PrintLayoutController) loader.getController();
            ctrl.setPasses(pass);
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

            Scene scene = new Scene(parent, 400, 321);
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

            Scene scene = new Scene(parent, 434, 385);
            Stage stage = new Stage();
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

}
