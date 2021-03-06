/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package printimage.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import printimage.helpers.SQLDatabase;
import printimage.models.BusinessInfo;
import printimage.models.Goodspass;
import printimage.models.Passes;

/**
 * FXML Controller class
 *
 * @author Admin
 */
public class DialogPassGenerateReportCtrl implements Initializable {

    @FXML
    private DatePicker date_from;

    @FXML
    private DatePicker date_until;

    @FXML
    private ProgressIndicator progress;

    @FXML
    private Label label_info;

    @FXML
    private Button btnGenerate;

    private SQLDatabase db;
    private Stage stage;

    private String sortType = "DESC";

    ////////////////////////////////////////////
    @FXML
    private CheckBox cb_id;

    @FXML
    private CheckBox cb_proprietor;

    @FXML
    private CheckBox cb_businessName;

    @FXML
    private CheckBox cb_passId;

    @FXML
    private CheckBox cb_address;

    @FXML
    private CheckBox cb_dateReleased;

    @FXML
    private CheckBox cb_vehicleDesc;

    @FXML
    private CheckBox cb_plateNumber;

    // Dates
    private ZoneId defaultZoneId;
    private SimpleDateFormat df;
    private Workbook workbook;

    private ObservableList<Goodspass> passes;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // TODO
        date_from.valueProperty().addListener((ov, oldValue, newValue) -> {
            checkDates();
        });

        date_until.valueProperty().addListener((ov, oldValue, newValue) -> {
            checkDates();
        });

        // Date
        defaultZoneId = ZoneId.systemDefault();
        df = new SimpleDateFormat("yyyy-MM-dd");

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setData(SQLDatabase db) {
        this.db = db;
    }

    private void checkDates() {
        if (date_from.getValue() == null && date_until.getValue() == null) {

        } else if (date_from.getValue() != null && date_until.getValue() == null) {
            this.btnGenerate.setDisable(false);
            loadData(1);
        } else if (date_from.getValue() == null && date_until.getValue() != null) {
            this.btnGenerate.setDisable(false);
            loadData(2);
        } else if (date_from.getValue().equals(date_until.getValue())) {
            System.out.println("Has same date");
            this.btnGenerate.setDisable(false);
            loadData(0);
        } else if (date_from.getValue().compareTo(date_until.getValue()) > 0) {
            System.out.println("Date From is greater than Date Until: Wrong!!");
            setLabelInfo("Date Selection is invalid!", false, true);
            this.btnGenerate.setDisable(true);
        } else if (date_from.getValue().compareTo(date_until.getValue()) < 0) {
            System.out.println("Date From is lesser than Date Until: Correct!!");
            this.btnGenerate.setDisable(false);
            loadData(0);
        }
    }

    private void loadData(int search_type) {

        passes = FXCollections.observableArrayList();
        if (search_type == 0) {

            LocalDate dateFrom = date_from.getValue();
            LocalDate dateUntil = date_until.getValue().plusDays(1);
            String text_date_from = df.format(Date.from(dateFrom.atStartOfDay(defaultZoneId).toInstant()));
            String text_date_until = df.format(Date.from(dateUntil.atStartOfDay(defaultZoneId).toInstant()));
            passes = db.getPassByDate(text_date_from, text_date_until, this.sortType);
        } else if (search_type == 1) {

            LocalDate dateFrom = date_from.getValue();
            String text_date_from = df.format(Date.from(dateFrom.atStartOfDay(defaultZoneId).toInstant()));
            passes = db.getPassByDateOperation(text_date_from, ">=", this.sortType);
        } else if (search_type == 2) {

            LocalDate dateUntil = date_until.getValue();
            String text_date_until = df.format(Date.from(dateUntil.atStartOfDay(defaultZoneId).toInstant()));
            passes = db.getPassByDateOperation(text_date_until, "<=", this.sortType);
        }
        System.out.println("Pass size is " + passes.size());
    }

    private void setupPassToExcel(ObservableList<Goodspass> passes) {

        workbook = new HSSFWorkbook();
        Sheet spreadsheet = workbook.createSheet("Goodspass Report");

        spreadsheet.setColumnWidth(0, 10 * 255);
        spreadsheet.setColumnWidth(1, 15 * 255);
        spreadsheet.setColumnWidth(2, 50 * 255);
        spreadsheet.setColumnWidth(3, 50 * 255);
        spreadsheet.setColumnWidth(4, 50 * 255);
        spreadsheet.setColumnWidth(5, 18 * 255);
        spreadsheet.setColumnWidth(6, 20 * 255);
        spreadsheet.setColumnWidth(7, 20 * 255);

        Row row = spreadsheet.createRow(0);
        CellStyle columnStyle = row.getSheet().getWorkbook().createCellStyle();
        columnStyle.setAlignment(CellStyle.ALIGN_CENTER);

        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setFontName("Calibri");
        font.setBold(true);
        columnStyle.setFont(font);

        row.setHeight((short) 500);

        int columnCount = 0;

        if (this.cb_id.isSelected()) {

            Cell cell_id = row.createCell(columnCount);
            cell_id.setCellValue("ID");
            cell_id.setCellStyle(columnStyle);
            columnCount++;
        }

        if (this.cb_passId.isSelected()) {
            Cell cell_gpno = row.createCell(columnCount);
            cell_gpno.setCellValue("GPASS NO.");
            cell_gpno.setCellStyle(columnStyle);
            columnCount++;
        }

        if (this.cb_businessName.isSelected()) {
            Cell business_name = row.createCell(columnCount);
            business_name.setCellValue("BUSINESS NAME");
            business_name.setCellStyle(columnStyle);
            columnCount++;
        }

        if (this.cb_proprietor.isSelected()) {
            Cell business_owner = row.createCell(columnCount);
            business_owner.setCellValue("PROPRIETOR");
            business_owner.setCellStyle(columnStyle);
            columnCount++;
        }

        if (this.cb_address.isSelected()) {
            Cell address = row.createCell(columnCount);
            address.setCellValue("ADDRESS");
            address.setCellStyle(columnStyle);
            columnCount++;
        }

        if (this.cb_dateReleased.isSelected()) {
            Cell date_released = row.createCell(columnCount);
            date_released.setCellValue("DATE RELEASED");
            date_released.setCellStyle(columnStyle);
            columnCount++;
        }

        if (this.cb_vehicleDesc.isSelected()) {
            Cell vehicle_desc = row.createCell(columnCount);
            vehicle_desc.setCellValue("VEHICLE DESC");
            vehicle_desc.setCellStyle(columnStyle);
            columnCount++;
        }

        if (this.cb_plateNumber.isSelected()) {
            Cell plate_num = row.createCell(columnCount);
            plate_num.setCellValue("PLATE NUMBER");
            plate_num.setCellStyle(columnStyle);
        }

        for (int i = 1; i <= passes.size(); i++) {

            Goodspass pass = passes.get(i - 1);
            BusinessInfo bInfo = db.getBusinessInfoById(Integer.parseInt(pass.getBusinessId()));
            Row new_row = spreadsheet.createRow(i);

            CellStyle cellStyle = new_row.getSheet().getWorkbook().createCellStyle();
            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
            int cellCount = 0;

            if (this.cb_id.isSelected()) {
                new_row.createCell(cellCount++).setCellValue(pass.getId());
            }
            if (this.cb_passId.isSelected()) {
                new_row.createCell(cellCount++).setCellValue(pass.getGpNo());
            }

            if (this.cb_businessName.isSelected()) {
                new_row.createCell(cellCount++).setCellValue(bInfo.getBusinessName());
            }

            if (this.cb_proprietor.isSelected()) {
                new_row.createCell(cellCount++).setCellValue(bInfo.getOwner());
            }

            if (this.cb_address.isSelected()) {
                new_row.createCell(cellCount++).setCellValue(bInfo.getAddress());
            }

            if (this.cb_dateReleased.isSelected()) {
                if (pass.getStatus().equals("" + MainActivityController.STATUS_PRINTED)) {
                    System.out.println("ID is " + pass.getGpNo());
                    if (pass.getSqlDatePrinted() != null) {
                        new_row.createCell(cellCount).setCellValue(df.format(pass.getSqlDatePrinted()));
                        System.out.println(pass.getId() + " created Successfully");
                    } else {
                        new_row.createCell(cellCount).setCellValue("Printed: no_date");
                        System.out.println(pass.getId() + " created Unsuccessful");
                    }

                } else {
                    new_row.createCell(cellCount).setCellValue("Not Released");
                }
                cellCount++;
            }

            if (this.cb_vehicleDesc.isSelected()) {
                new_row.createCell(cellCount++).setCellValue(pass.getVehicleDesc());
            }

            if (this.cb_plateNumber.isSelected()) {
                new_row.createCell(cellCount).setCellValue(pass.getVehiclePlateNo());
            }
        }

    }

    private void openSaveWindow() {
        FileChooser fileChooser = new FileChooser();
        //Set extension filter for text files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("EXCEL files (*.xls)", "*.xls");
        fileChooser.getExtensionFilters().add(extFilter);
        //Show save file dialog
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            FileOutputStream fileOut = null;
            try {
                fileOut = new FileOutputStream(file);
                workbook.write(fileOut);
                fileOut.close();

                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.setHeaderText("Report Successfully created!");
                alert.setContentText("File Path: " + file.getAbsolutePath());
                alert.showAndWait();

            } catch (FileNotFoundException ex) {
                Logger.getLogger(DialogPassGenerateReportCtrl.class.getName()).log(Level.SEVERE, null, ex);

                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Save Error");
                alert.setHeaderText("File not saved!");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();

            } catch (IOException ex) {
                Logger.getLogger(DialogPassGenerateReportCtrl.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    fileOut.close();
                } catch (IOException ex) {
                    Logger.getLogger(DialogPassGenerateReportCtrl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void startExport() {
        this.progress.setVisible(true);
        new Thread(() -> {
            this.setupPassToExcel(passes);
            Platform.runLater(() -> {
                this.btnGenerate.setDisable(false);
                this.progress.setVisible(false);
                setLabelInfo("Ready to Generate!", true, true);
                openSaveWindow();
            });
        }).start();

    }

    private void setLabelInfo(String label, boolean correct, boolean visibility) {
        this.label_info.setVisible(visibility);
        if (correct) {
            this.label_info.setText(label);
            this.label_info.setStyle("-fx-text-fill: darkgreen;");
        } else {
            this.label_info.setText(label);
            this.label_info.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    void onExportExcel(ActionEvent event) {
        startExport();
    }

    @FXML
    void onAscending(ActionEvent event) {
        sortType = "ASC";
        this.checkDates();
    }

    @FXML
    void onDescending(ActionEvent event) {
        sortType = "DESC";
        this.checkDates();
    }

}
