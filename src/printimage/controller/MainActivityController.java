/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package printimage.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

// QRCode Generator
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import io.nayuki.qrcodegen.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import printimage.PrintImage;
import printimage.helpers.SQLDatabase;
import printimage.models.BusinessInfo;
import printimage.models.Goodspass;
import printimage.models.Passes;
import printimage.models.Setting;
import static printimage.controller.MainActivityController.settings;
import printimage.helpers.Helper;
import printimage.models.Crew;
import printimage.models.Remark;
import printimage.models.User;

/**
 * FXML Controller class
 *
 * @author Admin
 */
public class MainActivityController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private AnchorPane mainAnchor;

    @FXML
    private TabPane mainTabPane;

    @FXML
    private TextField ctrlNo;

    @FXML
    private TextField bName;

    @FXML
    private TextField bAddress;

    @FXML
    private TextField plateNo;

    @FXML
    private TextField description;

    @FXML
    private TextField des1;

    @FXML
    private TextField des2;

    @FXML
    private TextField qrCode;

    // Initialize Important Classes
    private SQLDatabase db;
    private Passes pass;
    private Stage stage;
    private String db_update = "";
    public static Setting settings;

    private boolean appRunning = true;
    private boolean connected = false;

    public static User MAIN_USER;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        testConnection();

        //Test for Debugging
    }

    private void testConnection() {
        try {
            // TODO
            settings = readSettings();
            System.out.println("Settings url: " + settings.getServerAddress());
            System.out.println("Settings user: " + settings.getUsername());
            System.out.println("Settings password: " + settings.getPassword());

            this.db = new SQLDatabase(settings.getServerAddress(), settings.getUsername(), settings.getPassword());
            connected = true;

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Connection Error");
            alert.setHeaderText("Please Check Connection to Server!");
            alert.setContentText("Edit on Settings Tab.");
            alert.showAndWait();

            setSettingFields();
        }

        initAll();
    }

    private void openLoginForm(Stage stage) {
        AnchorPane root;
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/dialog_login_form_layout.fxml"));
        try {
            root = loader.load();
            Scene scene = new Scene(root, 325, 185);

            stage.setMaximized(false);
            stage.setResizable(false);
            stage.setTitle("Goodspass Registry Login");
            stage.setScene(scene);
            stage.show();
            // Hide this current window (if this is what you want)
            LoginActivityController ctrl = (LoginActivityController) loader.getController();
            ctrl.setDB(this.db);
            ctrl.setStage(stage);
            ctrl.setCtrl(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initAll() {
        if (connected) {
            initPassInfoTable();
            initPassInfoField();

            initBusiInfoTable();
            initBusiInfoField();

            initCrewInfoTable();
            initCrewInfoField();

            initUsersInfoTable();
            initUsersInfoField();

            setSettingFields();

            runTableUpdateThread();
        }
    }

    public void setUser(User user) {
        MAIN_USER = user;

        // Check if user is ADMIN or ENCODER
        if (user.getUsertype() == User.USER_ENCODER) {
            mainTabPane.getTabs().remove(5);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;

        this.stage.setOnHiding(e -> {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Logout");
            alert.setHeaderText("Logout Application");
            alert.setContentText(null);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                // ... user chose OK
                appRunning = false;
                this.openLoginForm(new Stage());
            } else {
                // ... user chose CANCEL or closed the dialog
            }

            //Platform.exit();
        });

        // Set Key Events on Stage
        this.stage.getScene().setOnKeyReleased(e -> {
            KeyCode code = e.getCode();
            if (code == KeyCode.F1) {
                mainTabPane.getSelectionModel().select(0);
            } else if (code == KeyCode.F2) {
                mainTabPane.getSelectionModel().select(1);
            } else if (code == KeyCode.F3) {
                mainTabPane.getSelectionModel().select(2);
            } else if (code == KeyCode.F4) {
                mainTabPane.getSelectionModel().select(3);
            } else if (code == KeyCode.F5) {
                mainTabPane.getSelectionModel().select(4);

            } else if (code == KeyCode.F6) {
                mainTabPane.getSelectionModel().select(5);
            } else if (code == KeyCode.ESCAPE) {

            }
        });

        if (connected && MAIN_USER == null) {
            this.openLoginForm(stage);
        }

    }

    @FXML
    void printImage(ActionEvent event) {
        Passes pass = new Passes();
        pass.setBusinessName(bName.getText());
        pass.setAddress(bAddress.getText());
        pass.setDescription(description.getText());
        pass.setDesignation_1(des1.getText());
        pass.setDesignation_2(des2.getText());
        pass.setQrCode(qrCode.getText());
        pass.setCtrlNo(ctrlNo.getText());
        pass.setPlateNo(plateNo.getText());
        openIDPreview(pass);
    }

    public void openIDPreview(Passes pass) {
        AnchorPane root;
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/layout_to_print.fxml"));
        try {
            root = loader.load();
            Scene scene = new Scene(root, 849, 714);
            Stage stage = new Stage();
            stage.setTitle("Preview");
            stage.setScene(scene);
            stage.show();
            // Hide this current window (if this is what you want)
            PrintLayoutController ctrl = (PrintLayoutController) loader.getController();
            ctrl.setPasses(pass);
            ctrl.setStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Image createQrCode(String qrcode) throws FileNotFoundException, IOException {
        QrCode qr0 = QrCode.encodeText("Hello, world!", QrCode.Ecc.MEDIUM);
        BufferedImage img = qr0.toImage(4, 10);
        ImageIO.write(img, "png", new File("qr-code.png"));
        FileInputStream input = new FileInputStream("qr-code.png");
        Image image = new Image(input);
        return image;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                       VIEW PASS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @FXML
    private TableView<Goodspass> pass_info_mainTable;

    @FXML
    private TableColumn<Goodspass, String> tablecolumn_id;

    @FXML
    private TableColumn<Goodspass, String> tablecolumn_pass_no;

    @FXML
    private TableColumn<Goodspass, String> tablecolumn_address;

    @FXML
    private TableColumn<Goodspass, String> tablecolumn_vehicle_desc;

    @FXML
    private TableColumn<Goodspass, String> tablecolumn_plateNo;

    @FXML
    private TableColumn<Goodspass, String> tablecolumn_businessName;

    @FXML
    private TextField create_passNo;

    @FXML
    private TextField create_fullname;

    @FXML
    private TextField create_address;

    @FXML
    private TextField create_issued_id;

    @FXML
    private TextField create_idNumber;

    @FXML
    private TextField create_designation;

    @FXML
    private TextField create_vehicle_desc;

    @FXML
    private TextField create_plate_no;

    @FXML
    private TextField create_business_name;

    @FXML
    private TextField create_fullname2;

    @FXML
    private TextField create_address2;

    @FXML
    private TextField create_issued_id2;
    @FXML
    private TextField create_idNumber2;

    @FXML
    private TextField create_designation2;

    @FXML
    private ListView<String> search_businessName_listview;

    private ObservableList<BusinessInfo> searchedBusinessList;

    @FXML
    private TextField driverInfo_pageLimit;

    @FXML
    private Pagination driverInfo_pagination;

    @FXML
    private TextField driverInfo_search;

    private int table_pass_current_page = 0;

    @FXML
    private ChoiceBox<String> driverInfo_seachby;

    private void initPassInfoTable() {

        this.tablecolumn_id.setCellValueFactory(new PropertyValueFactory<Goodspass, String>("id"));
        this.tablecolumn_id.setMaxWidth(50);
        this.tablecolumn_id.setPrefWidth(50);
        this.tablecolumn_id.setMinWidth(50);

        this.tablecolumn_pass_no.setCellValueFactory(new PropertyValueFactory<Goodspass, String>("gpNo"));
        this.tablecolumn_pass_no.setMaxWidth(100);
        this.tablecolumn_pass_no.setPrefWidth(100);
        this.tablecolumn_pass_no.setMinWidth(100);

        this.tablecolumn_address.setCellValueFactory(new PropertyValueFactory<Goodspass, String>("businessAddress"));
        this.tablecolumn_vehicle_desc.setCellValueFactory(new PropertyValueFactory<Goodspass, String>("vehicleDesc"));
        this.tablecolumn_plateNo.setCellValueFactory(new PropertyValueFactory<Goodspass, String>("vehiclePlateNo"));
        this.tablecolumn_businessName.setCellValueFactory(new PropertyValueFactory<Goodspass, String>("businessName"));

        this.pass_info_mainTable.getSelectionModel().setSelectionMode(
                SelectionMode.MULTIPLE
        );

        pass_info_mainTable.setRowFactory(row -> new TableRow<Goodspass>() {
            @Override
            public void updateItem(Goodspass item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                } else {

                    switch (item.getStatus()) {
                        case "0":
                            // Pending
                            this.getStyleClass().add("statusPending");
                            break;
                        case "1":
                            // Printed
                            this.getStyleClass().add("statusPrinted");
                            break;
                        case "2":
                            // Hold/Canceled
                            this.getStyleClass().add("statusHold");
                            break;
                        default:
                            break;
                    }
                }

                setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2 && (!isEmpty())) {
                        openViewPassDialog(getItem());
                    }
                });
            }
        });

        ContextMenu cm = new ContextMenu();

        pass_info_mainTable.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent t) {
                if (t.getButton() == MouseButton.SECONDARY) {
                    cm.getItems().clear();

                    if (pass_info_mainTable.getSelectionModel().getSelectedItems().size() > 1) {
                        MenuItem printAll = new MenuItem("Print All");
                        printAll.setOnAction(e -> {
                            openMultiplePrint(pass_info_mainTable.getSelectionModel().getSelectedItems());
                        });
                        cm.getItems().add(printAll);

                        cm.show(pass_info_mainTable, t.getScreenX(), t.getScreenY());
                    } else {
                        MenuItem view = new MenuItem("View");
                        view.setOnAction(e -> {
                            openViewPassDialog(pass_info_mainTable.getSelectionModel().getSelectedItem());
                        });
                        cm.getItems().add(view);
                        MenuItem preview = new MenuItem("Print");
                        preview.setOnAction(e -> {
                            openMultiplePrint(pass_info_mainTable.getSelectionModel().getSelectedItems());
                        });
                        cm.getItems().add(preview);

                        cm.show(pass_info_mainTable, t.getScreenX(), t.getScreenY());
                    }
                }

                if (t.getButton() == MouseButton.PRIMARY) {

                    cm.hide();
                }
            }
        });

    }

    public void initPassInfoField() {
        // Search Business field
        searchedBusinessList = FXCollections.observableArrayList();

        create_passNo.setOnAction(e -> {
            create_business_name.requestFocus();
        });

        create_business_name.setOnKeyReleased(e -> {
            if (e.getCode() != KeyCode.ENTER) {
                selected_business_id = 0;
                searchedBusinessList.clear();
                if (!create_business_name.getText().toString().equalsIgnoreCase("") && create_business_name.getText() != null) {

                    search_businessName_listview.getItems().clear();
                    searchedBusinessList = db.searchBusinessInfoLimit(create_business_name.getText().toString(), 5);
                    if (searchedBusinessList.size() > 0) {
                        this.search_businessName_listview.setVisible(true);
                        for (BusinessInfo info : searchedBusinessList) {
                            this.search_businessName_listview.getItems().add(info.getBusinessName());
                        }
                    } else {
                        searchedBusinessList.clear();
                        this.search_businessName_listview.setVisible(false);
                    }
                } else {
                    this.search_businessName_listview.setVisible(false);
                    searchedBusinessList.clear();
                }
            }
        });

        create_business_name.setOnAction(e -> {
            this.search_businessName_listview.setVisible(false);
            if (this.search_businessName_listview.getItems().size() > 0) {

                this.create_business_name.setText(searchedBusinessList.get(0).getBusinessName());
                selected_business_id = searchedBusinessList.get(0).getId();
                searchedBusinessList.clear();
                create_plate_no.requestFocus();
            } else {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/create_business_dialog_layout.fxml"));
                    Parent parent = fxmlLoader.load();
                    CreateBusinessDialogCtrl ctrl = (CreateBusinessDialogCtrl) fxmlLoader.getController();

                    Scene scene = new Scene(parent, 405, 336);
                    Stage stage = new Stage();
                    stage.setTitle("Create Business Info");
                    stage.setResizable(false);

                    ctrl.setData(stage, db, this, create_business_name.getText());
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setScene(scene);
                    stage.showAndWait();

                } catch (IOException ex) {
                    Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
                }
                searchedBusinessList.clear();
            }
        });

        // Listview Click Event
        this.search_businessName_listview.setOnMouseClicked(e -> {
            this.search_businessName_listview.setVisible(false);
            this.create_business_name.setText(searchedBusinessList.get(search_businessName_listview.getSelectionModel().getSelectedIndex()).getBusinessName());
            selected_business_id = searchedBusinessList.get(search_businessName_listview.getSelectionModel().getSelectedIndex()).getId();
            searchedBusinessList.clear();
        });

        create_plate_no.setOnAction(e -> {
            create_vehicle_desc.requestFocus();
        });

        create_vehicle_desc.setOnAction(e -> {
            create_fullname.requestFocus();
        });

        create_fullname.setOnAction(e -> {
            create_address.requestFocus();
        });

        create_address.setOnAction(e -> {
            create_issued_id.requestFocus();
        });

        create_issued_id.setOnAction(e -> {
            create_idNumber.requestFocus();
        });

        create_idNumber.setOnAction(e -> {
            create_designation.requestFocus();
        });

        create_designation.setOnAction(e -> {
            create_fullname2.requestFocus();
        });

        create_fullname2.setOnAction(e -> {
            create_address2.requestFocus();
        });

        create_address2.setOnAction(e -> {
            create_issued_id2.requestFocus();
        });

        create_issued_id2.setOnAction(e -> {
            create_idNumber2.requestFocus();
        });

        create_idNumber2.setOnAction(e -> {
            create_designation2.requestFocus();
        });

        create_designation2.setOnAction(e -> {
            savePassInfo();
        });

        // Choice box 
        ObservableList<String> choiceList = FXCollections.observableArrayList();
        choiceList.add("Pass No.");
        choiceList.add("Business Name");
        choiceList.add("Vehicle Description");
        choiceList.add("Plate No.");

        driverInfo_seachby.getItems().addAll(choiceList);
        driverInfo_seachby.getSelectionModel().select(0);

        // Search Field
        driverInfo_search.textProperty().addListener((observable, oldValue, newValue) -> {
            if (driverInfo_search.getText().toString().equals("")) {
                this.loadPassesTable();
            }
        });

        driverInfo_search.setOnAction(e -> {
            if (!driverInfo_search.getText().toString().equals("")) {

                switch (driverInfo_seachby.getSelectionModel().getSelectedIndex()) {
                    case 0:
                        // Pass no.
                        setPassInfoTableData(db.searchPassInfoByGivenColumnLimit("gp_no", "ZNGP-" + driverInfo_search.getText().toString(), settings.getTableRowSize()));
                        break;

                    case 1:
                        // Business name.
                        setPassInfoTableData(db.searchPassInfoByGivenColumnLimit("business_name", driverInfo_search.getText().toString(), settings.getTableRowSize()));
                        break;
                    case 2:
                        // Vehicle Description
                        setPassInfoTableData(db.searchPassInfoByGivenColumnLimit("vehicle_desc", driverInfo_search.getText().toString(), settings.getTableRowSize()));

                        break;
                    case 3:
                        // Plate no.
                        setPassInfoTableData(db.searchPassInfoByGivenColumnLimit("vehicle_plate_no", driverInfo_search.getText().toString(), settings.getTableRowSize()));
                        break;

                }

            } else {

                this.loadPassesTable();
            }

        });
        // Page Limit Textfield
        driverInfo_pageLimit.setText("" + settings.getTableRowSize());

        driverInfo_pageLimit.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!driverInfo_pageLimit.getText().toString().equals("")) {
                try {
                    settings.setTableRowSize(Integer.parseInt(driverInfo_pageLimit.getText()));
                    Platform.runLater(() -> {
                        notifyTables();
                    });
                    saveSettings();
                } catch (NumberFormatException e) {
                }
            }
        });
        setLatestGPNo();
        // Pagination
        setupPassesPagination();

    }

    // Sets business info when create via dialog
    public void setBusinessInfo(BusinessInfo info) {
        this.create_business_name.setText(info.getBusinessName());
        selected_business_id = info.getId();
        searchedBusinessList.clear();
        create_plate_no.requestFocus();
    }

    // Setup Pagination in Passes tab
    private void setupPassesPagination() {
        // Pagination
        int pageCount = db.getDriverCount() / settings.getTableRowSize();

        if ((db.getDriverCount() % settings.getTableRowSize()) > 0) {
            pageCount++;
        }
        driverInfo_pagination.setCurrentPageIndex(this.table_pass_current_page);
        driverInfo_pagination.setMaxPageIndicatorCount(pageCount);
        driverInfo_pagination.setPageCount(pageCount);

        driverInfo_pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            driverInfo_pagination.setCurrentPageIndex(newIndex.intValue());
            this.table_pass_current_page = newIndex.intValue();

            if (this.sortype == 0) {
                setPassInfoTableData(db.getPassInfoByBusinessLimitAsc(newIndex.intValue() * settings.getTableRowSize(), settings.getTableRowSize()));
            } else {
                setPassInfoTableData(db.getPassInfoByBusinessLimitDesc(newIndex.intValue() * settings.getTableRowSize(), settings.getTableRowSize()));
            }
        });
    }

    private void setPassInfoTableData(ObservableList<Goodspass> list) {
        pass_info_mainTable.getItems().clear();
        for (Goodspass pass : list) {
            BusinessInfo businessInfo = db.getBusinessInfoById(Integer.parseInt(pass.getBusinessId()));
            pass.setBusinessAddress(businessInfo.getAddress());
            if (businessInfo != null) {
                pass.setBusinessName(businessInfo.getBusinessName());
            } else {
                pass.setBusinessName("N/A");
            }

            this.pass_info_mainTable.getItems().add(pass);

        }
        pass_info_mainTable.refresh();
    }

    private void loadPassesTable() {
        pass_info_mainTable.getItems().clear();
        ObservableList<Goodspass> list = FXCollections.observableArrayList();
        if (this.sortype == 0) {
            list = db.getPassInfoByBusinessLimitAsc(this.table_pass_current_page * settings.getTableRowSize(), settings.getTableRowSize());
        } else {
            list = db.getPassInfoByBusinessLimitDesc(this.table_pass_current_page * settings.getTableRowSize(), settings.getTableRowSize());
        }

        for (Goodspass pass : list) {
            BusinessInfo businessInfo = db.getBusinessInfoById(Integer.parseInt(pass.getBusinessId()));
            pass.setBusinessAddress(businessInfo.getAddress());
            if (businessInfo != null) {
                pass.setBusinessName(businessInfo.getBusinessName());
            } else {
                pass.setBusinessName("N/A");
            }

            this.pass_info_mainTable.getItems().add(pass);

        }
        pass_info_mainTable.refresh();
    }

    // Error handler <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    private void setLatestGPNo() {
        try {

            String latest_no = db.getLatestGPNo();
            if (!latest_no.equals("")) {

                String[] split_no = latest_no.split("-");
                int gp_no = Integer.parseInt(split_no[1]);
                gp_no++;
                DecimalFormat df = new DecimalFormat("0000");
                create_passNo.setPromptText("ZNGP-" + df.format(gp_no));
            } else {
                create_passNo.setPromptText("ZNGP-0001");
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            create_passNo.setPromptText("Please input valid Pass no. Example: 'ZNGP-0123'");
        }

    }

    private String getLatestGPNo() {
        String latest_no = db.getLatestGPNo();
        if (!latest_no.equals("")) {

            String[] split_no = latest_no.split("-");

            int gp_no = Integer.parseInt(split_no[1]);
            gp_no++;

            DecimalFormat df = new DecimalFormat("0000");
            return "ZNGP-" + df.format(gp_no);
        } else {
            return "ZNGP-0001";
        }
    }

    private int selected_business_id = 0;

    private void savePassInfo() {
        BusinessInfo businessInfo = db.getBusinessInfoById(selected_business_id);
        if (businessInfo != null) {
            Goodspass pass = new Goodspass();
            Crew crew1 = new Crew();
            Crew crew2 = new Crew();

            if (!create_passNo.getPromptText().equalsIgnoreCase("Please input valid Pass no. Example: 'ZNGP-0123'")) {
                if (create_passNo.getText().toString().equals("")) {
                    pass.setGpNo(this.getLatestGPNo());
                } else {
                    // Verify pass no. 

                    try {
                        String[] split_pass = create_passNo.getText().split("-");
                        if (split_pass[0].equalsIgnoreCase("ZNGP")) {
                            Integer.parseInt(split_pass[1]);
                            pass.setGpNo(this.create_passNo.getText());
                        } else {
                            Alert alert = new Alert(AlertType.WARNING);
                            alert.setTitle("Unable to save!");
                            alert.setHeaderText("Pass no. format is Wrong! Must start with \"ZNGP-\".");
                            alert.setContentText("Example: ZNGP-1234");
                            alert.showAndWait();
                            System.out.println("Error 1");
                            return;
                            // Error Wrong id head
                        }
                    } catch (IndexOutOfBoundsException ex) {
                        Alert alert = new Alert(AlertType.WARNING);
                        alert.setTitle("Unable to save!");
                        alert.setHeaderText("Pass no. format is Wrong! Must start with \"ZNGP-\".");
                        alert.setContentText("Example: ZNGP-1234");
                        alert.showAndWait();
                        System.out.println("Error 2");
                        ex.printStackTrace();
                        return;
                    } catch (NumberFormatException ex) {
                        Alert alert = new Alert(AlertType.WARNING);
                        alert.setTitle("Unable to save!");
                        alert.setHeaderText("Pass no. format is Wrong! Must start with \"ZNGP-\".");
                        alert.setContentText("Example: ZNGP-1234");
                        alert.showAndWait();
                        System.out.println("Error 3");
                        ex.printStackTrace();
                        return;
                    }
                }
                // Pass info
                pass.setVehicleDesc(this.create_vehicle_desc.getText());
                pass.setVehiclePlateNo(this.create_plate_no.getText());
                pass.setBusinessId("" + businessInfo.getId());
                pass.setBusinessName(businessInfo.getBusinessName());
                pass.setStatus("" + STATUS_PENDING);

                // Crew info 1
                crew1.setGpNo(pass.getGpNo());
                crew1.setFullname(this.create_fullname.getText());
                crew1.setAddress(this.create_address.getText());
                crew1.setIdPresented(this.create_issued_id.getText());
                crew1.setIdNumber(this.create_idNumber.getText());
                crew1.setDesignation(this.create_designation.getText());

                crew2.setGpNo(pass.getGpNo());
                crew2.setFullname(this.create_fullname2.getText());
                crew2.setAddress(this.create_address2.getText());
                crew2.setIdPresented(this.create_issued_id2.getText());
                crew2.setIdNumber(this.create_idNumber2.getText());
                crew2.setDesignation(this.create_designation2.getText());

                // Add Pass to DB
                if (this.db.createPass(pass)) {
                    db.createRemarks(Remark.REMARK_PASS, MAIN_USER.getId(), db.getLastPassInfoId(), "Added by " + MAIN_USER.getFullname());
                }

                // Check if crew fields has data
                if (!crew1.getFullname().equals("")) {

                    // Save crew 1
                    this.db.createCrewInfo(crew1);
                }

                if (!crew2.getFullname().equals("")) {

                    // Save crew 2
                    this.db.createCrewInfo(crew2);
                }

                this.create_passNo.clear();
                this.create_fullname.clear();
                this.create_address.clear();
                this.create_issued_id.clear();
                this.create_designation.clear();
                this.create_idNumber.clear();
                this.create_vehicle_desc.clear();
                this.create_plate_no.clear();
                this.create_business_name.clear();

                this.create_fullname2.clear();
                this.create_address2.clear();
                this.create_issued_id2.clear();
                this.create_idNumber2.clear();
                this.create_designation2.clear();
                create_business_name.requestFocus();
                db.updateDB();
                setLatestGPNo();

            } else {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Unable to save!");
                alert.setHeaderText("Please input Pass id first!");
                alert.setContentText("There is an error while getting latest id.");
                alert.showAndWait();
                create_business_name.requestFocus();
            }
        } else {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Unable to save!");
            alert.setHeaderText("Please search Business Name.");
            alert.setContentText("Click item on search list or hit enter.");
            alert.showAndWait();
            create_business_name.requestFocus();
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

    private void openMultiplePrint(ObservableList<Goodspass> passes) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/print_passes_dialog_layout.fxml"));
            Parent parent = fxmlLoader.load();
            PrintPassesCtrl ctrl = (PrintPassesCtrl) fxmlLoader.getController();

            Scene scene = new Scene(parent, 395, 416);
            Stage stage = new Stage();
            stage.setTitle("PRINT PASSES");
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);

            ctrl.setStage(stage);
            ctrl.setData(db, passes);

            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void saveDriverInfo(ActionEvent event) {
        savePassInfo();
    }

    private int sortype = 0;

    @FXML
    void onSortypeAscending(ActionEvent event) {
        sortype = 0;
        Platform.runLater(() -> {
            notifyTables();
        });

    }

    @FXML
    void onSortypeDescending(ActionEvent event) {
        sortype = 1;
        Platform.runLater(() -> {
            notifyTables();
        });
    }

    @FXML
    void onGenerateReport(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/dialog_pass_generatereport_layout.fxml"));
            Parent parent = fxmlLoader.load();

            Scene scene = new Scene(parent, 327, 272);
            Stage stage = new Stage();

            stage.setTitle("GENERATE PASS REPORT");
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);

            DialogPassGenerateReportCtrl ctrl = (DialogPassGenerateReportCtrl) fxmlLoader.getController();
            ctrl.setData(db);
            stage.showAndWait();

        } catch (IOException ex) {
            Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                       VIEW BUSINESS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @FXML
    private TableView<BusinessInfo> business_main_table;

    @FXML
    private TableColumn<BusinessInfo, String> tableColumn_bus_id;

    @FXML
    private TableColumn<BusinessInfo, String> tableColumn_bus_owner;

    @FXML
    private TableColumn<BusinessInfo, String> tableColumn_bus_name;

    @FXML
    private TableColumn<BusinessInfo, String> tableColumn_bus_permit;

    @FXML
    private TableColumn<BusinessInfo, String> tableColumn_bus_address;

    @FXML
    private TableColumn<BusinessInfo, String> tableColumn_bus_number;

    @FXML
    private TableColumn<BusinessInfo, String> tableColumn_bus_contactPerson;

    @FXML
    private TextField createBus_owner;

    @FXML
    private TextField createBus_name;

    @FXML
    private TextField createBus_permit;

    @FXML
    private TextField createBus_address;

    @FXML
    private TextField createBus_contact;

    @FXML
    private TextField createBus_person;

    @FXML
    private TextField businessInfo_search;

    @FXML
    private Pagination businessInfo_pagination;

    @FXML
    private ChoiceBox businessInfo_seachby;

    private int table_business_current_page = 0;

    private void initBusiInfoTable() {

        this.tableColumn_bus_id.setCellValueFactory(new PropertyValueFactory<BusinessInfo, String>("id"));

        tableColumn_bus_id.setMinWidth(50);
        tableColumn_bus_id.setPrefWidth(50);
        tableColumn_bus_id.setMaxWidth(50);
        this.tableColumn_bus_owner.setCellValueFactory(new PropertyValueFactory<BusinessInfo, String>("owner"));
        this.tableColumn_bus_name.setCellValueFactory(new PropertyValueFactory<BusinessInfo, String>("businessName"));
        this.tableColumn_bus_permit.setCellValueFactory(new PropertyValueFactory<BusinessInfo, String>("permitNo"));
        this.tableColumn_bus_address.setCellValueFactory(new PropertyValueFactory<BusinessInfo, String>("address"));
        this.tableColumn_bus_number.setCellValueFactory(new PropertyValueFactory<BusinessInfo, String>("contact"));
        this.tableColumn_bus_contactPerson.setCellValueFactory(new PropertyValueFactory<BusinessInfo, String>("contactPerson"));

        this.business_main_table.setRowFactory(tv -> {

            TableRow<BusinessInfo> row = new TableRow<>();

            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && (!row.isEmpty())) {
                    openViewBusinessInfoDialog(row.getItem());
                }
            });

            return row;
        });

        setupBusinessPagination();

    }

    private void initBusiInfoField() {
        // Search Field

        businessInfo_search.textProperty().addListener((observable, oldValue, newValue) -> {
            if (businessInfo_search.getText().toString().equals("")) {
                this.loadBusinessInfoTable();
            }
        });

        businessInfo_search.setOnAction(e -> {
            if (!businessInfo_search.getText().toString().equals("")) {
                switch (businessInfo_seachby.getSelectionModel().getSelectedIndex()) {
                    case 0:
                        // Owner
                        setBusinessInfoTable(db.searchBusinessInfoByColumnLimit("owner", businessInfo_search.getText().toString(), settings.getTableRowSize()));
                        break;
                    case 1:
                        // Business name
                        setBusinessInfoTable(db.searchBusinessInfoLimit(businessInfo_search.getText().toString(), settings.getTableRowSize()));
                        break;
                    case 2:
                        // Contact person
                        setBusinessInfoTable(db.searchBusinessInfoByColumnLimit("contact", businessInfo_search.getText().toString(), settings.getTableRowSize()));
                        break;
                }

            } else {
                this.loadBusinessInfoTable();
            }

        });

        createBus_owner.setOnAction(e -> {
            createBus_name.requestFocus();
        });

        createBus_name.setOnAction(e -> {
            createBus_permit.requestFocus();
        });

        createBus_permit.setOnAction(e -> {
            createBus_address.requestFocus();
        });

        createBus_address.setOnAction(e -> {
            createBus_contact.requestFocus();
        });
        createBus_contact.setOnAction(e -> {
            createBus_person.requestFocus();
        });

        createBus_person.setOnAction(e -> {
            saveBusinessInfo();
            this.createBus_address.clear();
            this.createBus_contact.clear();
            this.createBus_name.clear();
            this.createBus_owner.clear();
            this.createBus_permit.clear();
            this.createBus_person.clear();
        });

        // Choice Box
        ObservableList<String> list = FXCollections.observableArrayList();
        list.add("Owner");
        list.add("Business name");
        list.add("Contact person");

        businessInfo_seachby.getItems().addAll(list);
        businessInfo_seachby.getSelectionModel().select(1);

    }

    private void setupBusinessPagination() {
        // Pagination
        int pageCount = db.getBusinessCount() / settings.getTableRowSize();

        if ((db.getBusinessCount() % settings.getTableRowSize()) > 0) {
            pageCount++;
        }

        businessInfo_pagination.setCurrentPageIndex(this.table_business_current_page);
        businessInfo_pagination.setMaxPageIndicatorCount(pageCount);
        businessInfo_pagination.setPageCount(pageCount);

        businessInfo_pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            businessInfo_pagination.setCurrentPageIndex(newIndex.intValue());
            this.table_business_current_page = newIndex.intValue();

            if (this.sortype == 0) {
                setBusinessInfoTable(db.getBusinessInfoLimitAsc(newIndex.intValue() * settings.getTableRowSize(), settings.getTableRowSize()));
            } else {
                setBusinessInfoTable(db.getBusinessInfoLimitDesc(newIndex.intValue() * settings.getTableRowSize(), settings.getTableRowSize()));
            }
        });
    }

    private void setBusinessInfoTable(ObservableList<BusinessInfo> list) {
        business_main_table.getItems().clear();
        for (BusinessInfo business : list) {

            this.business_main_table.getItems().add(business);
        }
        business_main_table.refresh();
    }

    private void loadBusinessInfoTable() {
        business_main_table.getItems().clear();
        ObservableList<BusinessInfo> list = FXCollections.observableArrayList();
        if (this.sortype == 0) {
            list = db.getBusinessInfoLimitAsc(this.table_business_current_page * settings.getTableRowSize(), settings.getTableRowSize());
        } else {
            list = db.getBusinessInfoLimitDesc(this.table_business_current_page * settings.getTableRowSize(), settings.getTableRowSize());
        }

        for (BusinessInfo info : list) {

            this.business_main_table.getItems().add(info);

        }
        business_main_table.refresh();
    }

    private void saveBusinessInfo() {
        if (!this.createBus_name.getText().equals("")) {

            if (!this.createBus_address.getText().equals("")) {
                BusinessInfo info = new BusinessInfo();

                info.setOwner(this.createBus_owner.getText());
                info.setBusinessName(this.createBus_name.getText());
                info.setPermitNo(this.createBus_permit.getText());
                info.setAddress(this.createBus_address.getText());
                info.setContact(this.createBus_contact.getText());
                info.setContactPerson(this.createBus_person.getText());
                
                
                if(this.db.createBusinessInfo(info)){
                    db.createRemarks(Remark.REMARK_BUSINESS,MAIN_USER.getId(),db.getLastBusinessInfoId(),"Added by "+MAIN_USER.getFullname());
                }

                db.updateDB();
                createBus_owner.requestFocus();
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Failed");
                alert.setHeaderText("Can't save without Business Address!");
                alert.setContentText("Please fill out missing fields.");
                alert.showAndWait();
            }

        } else {

            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Failed");
            alert.setHeaderText("Can't save without Business Name!");
            alert.setContentText("Please fill out missing fields.");
            alert.showAndWait();

        }

    }

    @FXML
    void saveBusinessInfo(ActionEvent event) {
        saveBusinessInfo();
        this.createBus_address.clear();
        this.createBus_contact.clear();
        this.createBus_name.clear();
        this.createBus_owner.clear();
        this.createBus_permit.clear();
        this.createBus_person.clear();
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                       VIEW CREW INFORMATION
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @FXML
    private TableView<Crew> crew_main_table;

    @FXML
    private TableColumn<Crew, String> tableColumn_crew_id;

    @FXML
    private TableColumn<Crew, String> tableColumn_crew_passNo;

    @FXML
    private TableColumn<Crew, String> tableColumn_crew_name;

    @FXML
    private TableColumn<Crew, String> tableColumn_crew_address;

    @FXML
    private TableColumn<Crew, String> tableColumn_crew_designation;

    @FXML
    private TableColumn<Crew, String> tableColumn_crew_idPresented;

    @FXML
    private TableColumn<Crew, String> tableColumn_crew_idNumber;

    @FXML
    private TextField crewInfo_search;

    @FXML
    private Pagination crewInfo_pagination;

    @FXML
    private ChoiceBox<String> crewInfo_seachby;

    private int table_crew_current_page = 0;

    private void initCrewInfoTable() {
        this.tableColumn_crew_id.setCellValueFactory(new PropertyValueFactory<Crew, String>("id"));
        tableColumn_crew_id.setMinWidth(50);
        tableColumn_crew_id.setPrefWidth(50);
        tableColumn_crew_id.setMaxWidth(50);

        this.tableColumn_crew_passNo.setCellValueFactory(new PropertyValueFactory<Crew, String>("gpNo"));
        tableColumn_crew_passNo.setMaxWidth(100);
        tableColumn_crew_passNo.setPrefWidth(100);
        tableColumn_crew_passNo.setMinWidth(100);

        this.tableColumn_crew_name.setCellValueFactory(new PropertyValueFactory<Crew, String>("fullname"));
        this.tableColumn_crew_address.setCellValueFactory(new PropertyValueFactory<Crew, String>("address"));
        this.tableColumn_crew_designation.setCellValueFactory(new PropertyValueFactory<Crew, String>("designation"));
        this.tableColumn_crew_idPresented.setCellValueFactory(new PropertyValueFactory<Crew, String>("idPresented"));
        this.tableColumn_crew_idNumber.setCellValueFactory(new PropertyValueFactory<Crew, String>("idNumber"));
        this.crew_main_table.setRowFactory(tv -> {

            TableRow<Crew> row = new TableRow<>();

            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && (!row.isEmpty())) {
                    openViewCrewInfoDialog(row.getItem());
                }
            });

            return row;
        });

        setupCrewPagination();

    }

    private void initCrewInfoField() {
        // Search Field

        crewInfo_search.textProperty().addListener((observable, oldValue, newValue) -> {
            if (crewInfo_search.getText().toString().equals("")) {
                this.loadCrewInfoTable();
            }
        });

        crewInfo_search.setOnAction(e -> {
            if (!crewInfo_search.getText().toString().equals("")) {
                switch (crewInfo_seachby.getSelectionModel().getSelectedIndex()) {
                    case 0:
                        // GP No
                        setCrewInfoTable(db.searchCrewInfoByColumnLimit("gp_no", "ZNGP-" + crewInfo_search.getText().toString(), settings.getTableRowSize()));
                        break;
                    case 1:
                        // Full name
                        setCrewInfoTable(db.searchCrewInfoByColumnLimit("full_name", crewInfo_search.getText().toString(), settings.getTableRowSize()));
                        break;
                }

            } else {
                this.loadCrewInfoTable();
            }
        });

        // Choice Box
        ObservableList<String> list = FXCollections.observableArrayList();
        list.add("Pass no");
        list.add("Name");

        crewInfo_seachby.getItems().addAll(list);
        crewInfo_seachby.getSelectionModel().select(1);

    }

    private void setupCrewPagination() {
        // Pagination
        int crewCount = db.getCrewCount();

        int pageCount = crewCount / settings.getTableRowSize();

        if ((crewCount % settings.getTableRowSize()) > 0) {
            pageCount++;
        }

        crewInfo_pagination.setCurrentPageIndex(this.table_crew_current_page);

        crewInfo_pagination.setPageCount(pageCount);

        if (pageCount > 50) {
            crewInfo_pagination.setMaxPageIndicatorCount(50);
        } else {
            crewInfo_pagination.setMaxPageIndicatorCount(pageCount);
        }

        crewInfo_pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            crewInfo_pagination.setCurrentPageIndex(newIndex.intValue());
            this.table_crew_current_page = newIndex.intValue();

            if (this.sortype == 0) {
                setCrewInfoTable(db.getCrewInfoLimitAsc(newIndex.intValue() * settings.getTableRowSize(), settings.getTableRowSize()));
            } else {
                setCrewInfoTable(db.getCrewInfoLimitDesc(newIndex.intValue() * settings.getTableRowSize(), settings.getTableRowSize()));
            }
        });
    }

    private void setCrewInfoTable(ObservableList<Crew> list) {
        crew_main_table.getItems().clear();
        for (Crew crew : list) {

            this.crew_main_table.getItems().add(crew);
        }
        crew_main_table.refresh();
    }

    private void loadCrewInfoTable() {

        crew_main_table.getItems().clear();
        ObservableList<Crew> list = FXCollections.observableArrayList();
        if (this.sortype == 0) {
            list = db.getCrewInfoLimitAsc(this.table_crew_current_page * settings.getTableRowSize(), settings.getTableRowSize());
        } else {
            list = db.getCrewInfoLimitDesc(this.table_crew_current_page * settings.getTableRowSize(), settings.getTableRowSize());
        }

        for (Crew info : list) {

            this.crew_main_table.getItems().add(info);

        }
        crew_main_table.refresh();
    }

    private void openViewCrewInfoDialog(Crew crew) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/dialog_view_crew_layout.fxml"));
            Parent parent = fxmlLoader.load();
            ViewCrewDialogController ctrl = (ViewCrewDialogController) fxmlLoader.getController();
            Stage stage = new Stage();
            Scene scene = new Scene(parent, 434, 385);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setTitle(crew.getFullname());
            stage.setResizable(false);

            ctrl.setData(stage, db, crew.getId());
            ctrl.setViewType(ViewCrewDialogController.FORM_VIEW);

            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                       VIEW SETTINGS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_PRINTED = 1;
    public static final int STATUS_HOLD = 2;

    @FXML
    private TextField settings_url_field;

    @FXML
    private TextField settings_username_field;

    @FXML
    private PasswordField settings_pass_field;

    @FXML
    private TextArea settings_error_field;

    @FXML
    private Label settings_folder_path;

    private void setSettingFields() {
        if (this.settings != null) {
            settings_url_field.setText(settings.getServerAddress());
            settings_username_field.setText(settings.getUsername());
            settings_pass_field.setText(settings.getPassword());
            settings_folder_path.setText("Folder Path: " + settings.getFolderPath());
        }
        settings_error_field.setWrapText(true);
    }

    private void saveSettings() {
        this.settings.setServerAddress(settings_url_field.getText());
        this.settings.setUsername(this.settings_username_field.getText());

        this.settings.setPassword(settings_pass_field.getText());

        PrintWriter writer;
        try {
            writer = new PrintWriter("config.ini");
            writer.print("");
            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
            settings_error_field.setText("On Save Settings File not Found ERROR: " + ex.toString());
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("config.ini", true))) {

            if (settings.getServerAddress() != null) {
                bw.write("SERVER_ADD=" + settings.getServerAddress() + ";");
            }
            bw.newLine();
            if (settings.getUsername().equals("") || settings.getUsername() == null) {
                bw.write("USER= ;");
            } else {
                bw.write("USER=" + settings.getUsername() + ";");
            }
            bw.newLine();

            if (settings.getPassword().equals("") || settings.getPassword() == null) {
                bw.write("PASSWORD= ;");
            } else {
                bw.write("PASSWORD=" + settings.getPassword() + ";");
            }
            bw.newLine();
            if (settings.getFolderPath().equals("") || settings.getFolderPath() == null) {
                bw.write("IMAGEPATH= ;");
            } else {
                bw.write("IMAGEPATH=" + settings.getFolderPath() + ";");
            }

            bw.newLine();
            if (settings.getTableRowSize() != 0 || settings.getFolderPath() == null) {
                bw.write("TABLEROWSIZE=30;");
            } else {
                bw.write("TABLEROWSIZE=" + settings.getTableRowSize() + ";");
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            settings_error_field.setText("On Save Settings IOException ERROR: " + ex.toString());
        }
    }

    @FXML
    void onSaveSettings(ActionEvent event) {
        saveSettings();
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Saved");
        alert.setHeaderText("Successfully Saved!");
        alert.setContentText("If error occur, please restart the program.");
        alert.showAndWait();

        Alert alert_restart = new Alert(AlertType.CONFIRMATION);
        alert_restart.setTitle("Restart Application");
        alert_restart.setHeaderText("Restart Application now?");
        alert_restart.setContentText("Click ok to confirm.");

        Optional<ButtonType> result_restart = alert_restart.showAndWait();
        if (result_restart.get() == ButtonType.OK) {
            this.stage.close();
            Platform.runLater(() -> new PrintImage().start(new Stage()));
        } else {

        }
    }

    @FXML
    private TextField settings_excel_path;

    private File excelFile;

    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        excelFile = fileChooser.showOpenDialog(stage);
        this.settings_excel_path.setText(excelFile.getAbsolutePath());
        //readExcelFile();
    }

    /*   private void readExcelFile() {
        if (excelFile != null) {

            ObservableList<Goodspass> drivers = FXCollections.observableArrayList();
            ObservableList<BusinessInfo> businesses = FXCollections.observableArrayList();

            try {
                XSSFWorkbook workbook = new XSSFWorkbook(this.excelFile);

                Sheet firstSheet = workbook.getSheetAt(0);

                for (Row row : firstSheet) {
                    if (row.getRowNum() != 0) {
                        Goodspass new_driverInfo = new Goodspass();
                        BusinessInfo new_businessInfo = new BusinessInfo();

                        // GP Number
                        System.out.println("GP Number " + row.getCell(1).getStringCellValue());
                        if (row.getCell(1) != null) {
                            new_driverInfo.setPassNo(row.getCell(1).getStringCellValue());
                        } else {
                            new_driverInfo.setPassNo("");
                        }

                        // Fullname
                        System.out.println("Driver Fullname: " + row.getCell(2).getStringCellValue());
                        if (row.getCell(2) != null) {
                            new_driverInfo.setFullname(row.getCell(2).getStringCellValue());
                        } else {
                            new_driverInfo.setFullname("");
                        }

                        // Driver Address
                        System.out.println("Driver Address: " + row.getCell(3).getStringCellValue());
                        if (row.getCell(3) != null) {
                            new_driverInfo.setPersonAddress(row.getCell(3).getStringCellValue());
                        } else {
                            new_driverInfo.setPersonAddress("");
                        }

                        // Issued Id
                        System.out.println("Issued Id: " + row.getCell(4).getStringCellValue());
                        if (row.getCell(4) != null && row.getCell(5) != null) {
                            new_driverInfo.setIssuedId(row.getCell(4).toString() + ", " + row.getCell(5).toString());
                        } else if (row.getCell(4) != null && row.getCell(5) == null) {
                            new_driverInfo.setIssuedId("" + row.getCell(4).toString());
                        } else if (row.getCell(4) == null && row.getCell(5) != null) {
                            new_driverInfo.setIssuedId("" + row.getCell(5).toString());
                        } else {

                            new_driverInfo.setIssuedId("");
                        }

                        // Designation
                        System.out.println("Designation: " + row.getCell(6).getStringCellValue());
                        if (row.getCell(6) != null) {
                            new_driverInfo.setDesignation(row.getCell(6).getStringCellValue());
                        } else {
                            new_driverInfo.setDesignation("");
                        }

                        // Vehicle Description
                        System.out.println("Vehicle Description: " + row.getCell(7).getStringCellValue());
                        if (row.getCell(7) != null) {
                            new_driverInfo.setVehicleDesc(row.getCell(7).getStringCellValue());
                        } else {
                            new_driverInfo.setVehicleDesc("");
                        }

                        // Vehicle Plate no.
                        System.out.println("Vehicle Plate #: " + row.getCell(8).getStringCellValue());
                        if (row.getCell(8) != null) {
                            new_driverInfo.setVehiclePlateNo(row.getCell(8).getStringCellValue());
                        } else {
                            new_driverInfo.setVehiclePlateNo("");
                        }

                        // BUSINESS /////////////////////////////////////////////////////
                        // Owner
                        System.out.println("Business Owner: " + row.getCell(9).getStringCellValue());
                        if (row.getCell(9) != null) {
                            new_businessInfo.setOwner(row.getCell(9).getStringCellValue());
                        } else {
                            new_businessInfo.setOwner("");
                        }

                        // Business name
                        System.out.println("Business Name: " + row.getCell(10).getStringCellValue());
                        if (row.getCell(10) != null) {
                            new_businessInfo.setBusinessName(row.getCell(10).getStringCellValue());
                        } else {
                            new_businessInfo.setBusinessName("");
                        }

                        // Permit no.
                        System.out.println("Permit No#: " + row.getCell(11).toString());
                        if (row.getCell(11) != null) {
                            new_businessInfo.setPermitNo(row.getCell(11).toString());
                            new_driverInfo.setBusinessId(row.getCell(11).toString());
                        } else {
                            new_businessInfo.setPermitNo("");
                            new_driverInfo.setBusinessId("");
                        }

                        // Business Address
                        System.out.println("Business Address: " + row.getCell(12).getStringCellValue());
                        if (row.getCell(12) != null) {
                            new_businessInfo.setAddress(row.getCell(12).getStringCellValue());
                        } else {
                            new_businessInfo.setAddress("");
                        }

                        // Contact Number
                        System.out.println("Contact Number: " + row.getCell(13).getStringCellValue());
                        if (row.getCell(13) != null) {
                            new_businessInfo.setContact(row.getCell(13).getStringCellValue());

                        } else {
                            new_businessInfo.setContact("");
                        }

                        // Contact Person
                        System.out.println("Contact Person: " + row.getCell(14).getStringCellValue());
                        if (row.getCell(14) != null) {
                            new_businessInfo.setContactPerson(row.getCell(14).getStringCellValue());
                        } else {
                            new_businessInfo.setContactPerson("");
                        }

                        System.out.println("Date Printed: " + row.getCell(17).getStringCellValue());
                        if (row.getCell(17) != null) {
                            new_driverInfo.setDate_printed(row.getCell(17).getStringCellValue());
                        } else {
                            new_driverInfo.setDate_printed("");
                        }

                        drivers.add(new_driverInfo);
                        businesses.add(new_businessInfo);

                    }
                    // Save Import
                }
                saveImportData(businesses, drivers);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
                settings_error_field.setText("On Read Excel File not Found ERROR: " + ex.toString());
            } catch (IOException | InvalidFormatException ex) {
                Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
                settings_error_field.setText("On Read Excel IO Exception ERROR: " + ex.toString());
            }

        } else {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("File Error");
            alert.setHeaderText("Please choose Excel File first!");
            alert.showAndWait();
        }
    }
     */
    private void saveImportData(ObservableList<BusinessInfo> businesses, ObservableList<Goodspass> drivers) {
        for (BusinessInfo info : businesses) {
            if (db.getBusinessInfoByPermitNo(info.getPermitNo()) == null) {
                db.createBusinessInfo(info);
            }
        }

        for (Goodspass info : drivers) {
            BusinessInfo businessInfo = db.getBusinessInfoByPermitNo(info.getBusinessId());
            info.setBusinessId("" + businessInfo.getId());

            if (!info.getDatePrinted().equals("")) {
                info.setStatus("" + STATUS_PRINTED);
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

                try {
                    java.util.Date date = dateFormat.parse(info.getDatePrinted());
                    Date sqlDate = new Date(date.getTime());
                    //info.setDate_sql(sqlDate);

                } catch (ParseException ex) {
                    Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                info.setStatus("" + STATUS_PENDING);
            }

            db.createPass(info);
        }

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText("Import Successful!");
        alert.setContentText(null);

        alert.showAndWait();
        db.updateDB();
    }

    public Setting readSettings() {
        Setting settings = new Setting();
        try (BufferedReader reader = new BufferedReader(new FileReader(new File("config.ini")))) {

            String[] url = reader.readLine().replace(";", "").split("=");
            String[] user = reader.readLine().replace(";", "").split("=");
            String[] pass = reader.readLine().replace(";", "").split("=");
            String[] folder_path = reader.readLine().replace(";", "").split("=");
            String[] table_row_size = reader.readLine().replace(";", "").split("=");

            settings.setServerAddress(url[1]);
            settings.setUsername(user[1]);
            if (pass[1].equals(" ")) {
                settings.setPassword("");
            } else {
                settings.setPassword(pass[1]);
            }

            if (folder_path[1].equals(" ")) {
                settings.setFolderPath("");
            } else {
                settings.setFolderPath(folder_path[1]);
            }

            if (table_row_size[1].equals(" ")) {
                settings.setTableRowSize(30);
            } else {
                settings.setTableRowSize(Integer.parseInt(table_row_size[1]));
            }

        } catch (IOException | NullPointerException ex) {
            ex.printStackTrace();
            settings_error_field.setText("Read Settings Error: " + ex.toString());
            return settings;
        }
        return settings;
    }

    @FXML
    void onChangeFolderPath(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        System.out.println("File is " + settings.getFolderPath());
        if (settings.getFolderPath() != null && !settings.getFolderPath().equals("")) {
            File checkFolder = new File(settings.getFolderPath());
            if (checkFolder.exists()) {
                directoryChooser.setInitialDirectory(new File(settings.getFolderPath()));
            }
        } else {

        }

        File selectedDirectory = directoryChooser.showDialog(this.stage);

        if (selectedDirectory != null) {
            settings.setFolderPath(selectedDirectory.getAbsolutePath());
            settings_folder_path.setText(settings.getFolderPath());
            saveSettings();
        }

    }

    @FXML
    void onImportExcel(ActionEvent event) {
        //  readExcelFile();
    }

    @FXML
    void onChooseExcelFile(ActionEvent event) {
        openFileChooser();
    }

    @FXML
    void onTestConnection(ActionEvent event) {
        settings_error_field.clear();
        try {
            new SQLDatabase(this.settings_url_field.getText(), this.settings_username_field.getText(), this.settings_pass_field.getText());
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Connection Successfull");
            alert.setHeaderText(null);
            alert.setContentText("Successfully connected to server!");
            alert.showAndWait();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
            settings_error_field.setText("Test Connection Error : " + ex.toString());
        } catch (SQLException ex) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Connectiion Error");
            alert.setHeaderText("Unable to connect!");
            alert.setContentText("Check Logs for details!");
            alert.showAndWait();
            this.settings_error_field.setText(ex.toString());
            settings_error_field.setText("Test Connection Error : " + ex.toString());
            Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
            settings_error_field.setText("Test Connection Error : " + ex.toString());
        } catch (IllegalAccessException ex) {
            Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
            settings_error_field.setText("Test Connection Error : " + ex.toString());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                       VIEW ADMINISTRATOR
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @FXML
    private TableView<User> users_maintable;

    @FXML
    private TableColumn<User, String> users_tablecolumn_id;

    @FXML
    private TableColumn<User, String> users_tablecolumn_fullname;

    @FXML
    private TableColumn<User, String> users_tablecolumn_username;

    @FXML
    private TableColumn<User, String> users_tablecolumn_usertype;

    @FXML
    private TableColumn<User, String> users_tablecolumn_active;

    @FXML
    private TextField usersInfo_search;

    @FXML
    private TextField users_fullname;

    @FXML
    private TextField users_username;

    @FXML
    private PasswordField users_password;

    @FXML
    private PasswordField users_confirmpass;

    @FXML
    private Pagination usersInfo_pagination;

    @FXML
    private ChoiceBox<String> usersInfo_seachby;

    @FXML
    private ChoiceBox<String> users_usertype;

    private int table_users_current_page = 0;

    private void initUsersInfoTable() {

        this.users_tablecolumn_id.setCellValueFactory(new PropertyValueFactory<User, String>("id"));
        users_tablecolumn_id.setMinWidth(50);
        users_tablecolumn_id.setPrefWidth(50);
        users_tablecolumn_id.setMaxWidth(50);

        this.users_tablecolumn_username.setCellValueFactory(new PropertyValueFactory<User, String>("username"));
        this.users_tablecolumn_fullname.setCellValueFactory(new PropertyValueFactory<User, String>("fullname"));
        this.users_tablecolumn_usertype.setCellValueFactory(new PropertyValueFactory<User, String>("text_usertype"));
        this.users_tablecolumn_active.setCellValueFactory(new PropertyValueFactory<User, String>("isActive"));

        this.users_maintable.setRowFactory(tv -> {

            TableRow<User> row = new TableRow<>();

            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && (!row.isEmpty())) {
                    openViewUsersInfoDialog(row.getItem());
                }
            });

            return row;
        });

        setupUsersPagination();

    }

    private void initUsersInfoField() {
        // Search Field

        usersInfo_search.textProperty().addListener((observable, oldValue, newValue) -> {
            if (usersInfo_search.getText().toString().equals("")) {
                this.loadUsersInfoTable();
            }
        });

        usersInfo_search.setOnAction(e -> {
            if (!usersInfo_search.getText().toString().equals("")) {
                switch (usersInfo_seachby.getSelectionModel().getSelectedIndex()) {
                    case 0:
                        // ID
                        setUsersInfoTable(db.searchUserInfoByColumnLimit("id", usersInfo_search.getText().toString(), settings.getTableRowSize()));
                        break;
                    case 1:
                        // USERNAME
                        setUsersInfoTable(db.searchUserInfoByColumnLimit("username", usersInfo_search.getText().toString(), settings.getTableRowSize()));
                        break;
                    case 2:
                        // FULLNAME
                        setUsersInfoTable(db.searchUserInfoByColumnLimit("full_name", businessInfo_search.getText().toString(), settings.getTableRowSize()));
                        break;
                }

            } else {
                this.loadUsersInfoTable();
            }

        });

        users_fullname.setOnAction(e -> {
            users_username.requestFocus();
        });

        users_username.setOnAction(e -> {
            users_usertype.requestFocus();
        });

        users_usertype.setOnAction(e -> {
            users_password.requestFocus();
        });

        users_password.setOnAction(e -> {
            users_confirmpass.requestFocus();
        });
        users_confirmpass.setOnAction(e -> {
            saveUserInfo();
        });

        // Choice Box
        ObservableList<String> list = FXCollections.observableArrayList();
        list.add("ID");
        list.add("Fullname");
        list.add("Username");

        usersInfo_seachby.getItems().addAll(list);
        usersInfo_seachby.getSelectionModel().select(1);

        users_usertype.getItems().addAll("Encoder", "Administrator");

        users_usertype.getSelectionModel().select(0);
    }

    private void setupUsersPagination() {
        // Pagination
        int pageCount = db.getUsersCount() / settings.getTableRowSize();

        if ((db.getUsersCount() % settings.getTableRowSize()) > 0) {
            pageCount++;
        }

        usersInfo_pagination.setCurrentPageIndex(this.table_users_current_page);
        usersInfo_pagination.setMaxPageIndicatorCount(pageCount);
        usersInfo_pagination.setPageCount(pageCount);

        usersInfo_pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            usersInfo_pagination.setCurrentPageIndex(newIndex.intValue());
            this.table_users_current_page = newIndex.intValue();

            if (this.sortype == 0) {
                setUsersInfoTable(db.getUserLimitOrder(newIndex.intValue() * settings.getTableRowSize(), settings.getTableRowSize(), "id ASC"));
            } else {
                setUsersInfoTable(db.getUserLimitOrder(newIndex.intValue() * settings.getTableRowSize(), settings.getTableRowSize(), "id DESC"));
            }
        });
    }

    private void setUsersInfoTable(ObservableList<User> list) {
        users_maintable.getItems().clear();
        for (User user : list) {
            if (user.getUsertype() == User.USER_ADMIN) {
                user.setText_usertype("Administrator");
            } else {
                user.setText_usertype("Encoder");
            }

            if (user.getActive() == 1) {
                user.setIsActive("Yes");
            } else {
                user.setIsActive("No");
            }
            this.users_maintable.getItems().add(user);
        }
        users_maintable.refresh();
    }

    private void loadUsersInfoTable() {
        users_maintable.getItems().clear();
        ObservableList<User> list = FXCollections.observableArrayList();
        if (this.sortype == 0) {
            list = db.getUserLimitOrder(this.table_business_current_page * settings.getTableRowSize(), settings.getTableRowSize(), "id ASC");
        } else {
            list = db.getUserLimitOrder(this.table_business_current_page * settings.getTableRowSize(), settings.getTableRowSize(), "id DESC");
        }

        for (User user : list) {
            if (user.getUsertype() == User.USER_ADMIN) {
                user.setText_usertype("Administrator");
            } else {
                user.setText_usertype("Encoder");
            }

            if (user.getActive() == 1) {
                user.setIsActive("Yes");
            } else {
                user.setIsActive("No");
            }
            this.users_maintable.getItems().add(user);
        }
        users_maintable.refresh();
    }

    private void saveUserInfo() {
        boolean proceed = true;

        // Check Fields first if not empty
        if (users_fullname.getText().equals("")) {
            proceed = false;
        }
        if (users_username.getText().equals("")) {
            proceed = false;
        }
        if (users_password.getText().equals("")) {
            proceed = false;
        }

        if (proceed) {
            // if fields are not empty
            // check username if already exist
            if (db.checkUsersUsername(users_username.getText()) == null) {
                // check if passwords matches
                if (users_password.getText().equals(users_confirmpass.getText())) {
                    // passwords match
                    // Save user
                    User user = new User();
                    user.setFullname(users_fullname.getText());
                    user.setUsername(users_username.getText());
                    user.setPassword(Helper.getMd5(users_password.getText()));

                    if (users_usertype.getSelectionModel().getSelectedItem().equals("Administrator")) {
                        user.setUsertype(User.USER_ADMIN);
                    } else {
                        user.setUsertype(User.USER_ENCODER);
                    }

                    if (db.createUser(user)) {
                        db.updateDB();
                        users_fullname.clear();
                        users_username.clear();
                        users_password.clear();
                        users_confirmpass.clear();
                        users_usertype.getSelectionModel().select(0);
                        users_fullname.requestFocus();
                    }

                } else {
                    // passwords did not match
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Not Saved");
                    alert.setHeaderText("Passwords did not match!");
                    alert.setContentText("Please check passwords.");
                    alert.showAndWait();
                    users_confirmpass.clear();
                    users_password.requestFocus();
                }

            } else {
                // username already in use
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Not Saved");
                alert.setHeaderText("Username is already in use!");
                alert.setContentText("Please change username.");
                alert.showAndWait();
                users_username.requestFocus();
            }

        } else {
            // if some fields are empty
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Not Saved");
            alert.setHeaderText("Some fields are empty!");
            alert.setContentText("Please fill up all fields.");
            alert.showAndWait();
        }

    }

    @FXML
    void onSaveUserInfo(ActionEvent event) {
        saveUserInfo();
    }

    private void openViewUsersInfoDialog(User info) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/dialog_view_user_layout.fxml"));
            Parent parent = fxmlLoader.load();
            ViewUserDialogController ctrl = (ViewUserDialogController) fxmlLoader.getController();
            Stage stage = new Stage();
            Scene scene = new Scene(parent, 767, 249);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setTitle(info.getUsername());
            stage.setResizable(false);

            ctrl.setData(db, info.getId());
            ctrl.setStage(stage);

            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void stopRunning() {
        appRunning = false;
    }

    private Thread tableUpdateThread;

    private void runTableUpdateThread() {
        tableUpdateThread = new Thread(() -> {
            while (appRunning) {
                String text_dbUpdate = db.getDBUpdated();
                if (!db_update.equals(text_dbUpdate)) {
                    db_update = text_dbUpdate;
                    Platform.runLater(() -> {
                        // Drivers Tab
                        notifyTables();

                    });
                }
                try {
                    setLatestGPNo();
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        tableUpdateThread.start();
    }

    private void notifyTables() {
        if (driverInfo_search.getText().equals("")) {
            setupPassesPagination();
            loadPassesTable();
        }

        if (businessInfo_search.getText().equals("")) {
            setupBusinessPagination();
            loadBusinessInfoTable();
        }

        if (crewInfo_search.getText().equals("")) {
            loadCrewInfoTable();
            setupCrewPagination();
        }
        if (usersInfo_search.getText().equals("")) {
            loadUsersInfoTable();
            setupUsersPagination();
        }

    }

}
