/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package printimage.controller;

import io.nayuki.qrcodegen.QrCode;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import printimage.models.Passes;
import printimage.PrintImage;
import javafx.scene.text.Text;
import printimage.helpers.Helper;
import printimage.models.Crew;

/**
 * FXML Controller class
 *
 * @author Admin
 */
public class PrintLayoutController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private AnchorPane main_pane;

    @FXML
    private AnchorPane sub_pane;

    @FXML
    private Label ctrl_number;
    @FXML
    private Text business_name;

    @FXML
    private Text business_address;

    @FXML
    private Text vehicle_plate_number;

    @FXML
    private Text vehicle_description;

    @FXML
    private VBox crew_list;

    @FXML
    private ImageView main_image;

    @FXML
    private TextField pxBName;

    @FXML
    private TextField pxBAddress;

    @FXML
    private TextField pxVehicleNo;

    @FXML
    private TextField pxDescription;

    @FXML
    private TextField pxDes1;

    @FXML
    private TextField pxDes2;

    private Passes pass;

    private Stage stage;

    private ViewPassDialogController driverInfoCtrl;

    private double default_fontsize = 15;
    private Font defaultFont = Font.font("Consolas", FontWeight.BOLD, default_fontsize);
    private double MAX_TEXT_WIDTH = 298;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        setEvents();

    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.getScene().setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.close();
            }
        });
    }

    public void setCtrl(ViewPassDialogController driverInfoCtrl) {
        this.driverInfoCtrl = driverInfoCtrl;
    }

    public void setPasses(Passes pass, ObservableList<Crew> crews) {

        this.pass = pass;

        // Auto resize labels on Pass
        // Business Name Field
        this.business_name.setText(pass.getBusinessName());
        autoResizeField(business_name, pxBName);

        ////////////////////////////////////////////////////
        // Business 
        this.business_address.setText(pass.getAddress());
        autoResizeField(business_address, this.pxBAddress);
        ////////////////////////////////////////////////////
        this.vehicle_plate_number.setText(pass.getPlateNo());
        autoResizeField(vehicle_plate_number, this.pxVehicleNo);

        this.vehicle_description.setText(pass.getDescription());
        autoResizeField(vehicle_description, this.pxDescription);

        // Load crew List
        System.out.println("Crew size is " + crews.size());
        for (Crew crew : crews) {
            try {
                FXMLLoader crewListItemFXMLLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/print_pass_crew_item.fxml"));
                AnchorPane crew_item_layout = crewListItemFXMLLoader.load();

                Text designation_text = (Text) crew_item_layout.lookup("#designation");
                designation_text.setText(crew.getFullname() + " - " + crew.getDesignation());

                autoResizeField(designation_text, null);
                crew_list.getChildren().add(crew_item_layout);
            } catch (IOException ex) {
                Logger.getLogger(PrintLayoutController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        this.ctrl_number.setText(pass.getCtrlNo());
        try {
            this.main_image.setImage(createQrCode(pass.getQrCode()));
        } catch (IOException ex) {
            Logger.getLogger(PrintLayoutController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void autoResizeField(Text text, TextField tf) {
        DecimalFormat df = new DecimalFormat("#.00");

        text.setFont(defaultFont);

        double textWidth = text.getLayoutBounds().getWidth();
        if (textWidth <= this.MAX_TEXT_WIDTH) {
            text.setFont(defaultFont);
        } else {
            double newFontSize = this.default_fontsize * MAX_TEXT_WIDTH / textWidth;
            text.setFont(Font.font(defaultFont.getFamily(), FontWeight.BOLD, newFontSize));
            if (tf != null) {
                tf.setText(df.format(newFontSize));
            }
        }
    }

    @FXML
    void onPrint(ActionEvent event) {

    }

    @FXML
    void saveOnclick(ActionEvent event) {
        main_pane.getChildren().remove(this.sub_pane);
        stage.setMaxWidth(568);
        stage.setMaxWidth(568);
        main_pane.setPrefWidth(568);
        main_pane.maxWidth(568);
        main_pane.minWidth(568);

        new Thread(() -> {
            try {
                Thread.sleep(500);
                Platform.runLater(() -> {
                    save();
                });

            } catch (InterruptedException ex) {
                Logger.getLogger(PrintLayoutController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }).start();

    }

    private void save() {
        WritableImage img = pixelScaleAwareCanvasSnapshot(this.main_pane, 3);
        // File fileToSave = chooser.getSelectedFile();//Remove this line.
        BufferedImage img2 = SwingFXUtils.fromFXImage(img, null);
        try {
            File fileToSave = new File(MainActivityController.settings.getFolderPath(), pass.getCtrlNo() + ".png");
            ImageIO.write(img2, "png", fileToSave);
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Saved");
            alert.setHeaderText("Image Saved!");
            alert.setContentText("File Path: " + fileToSave.getAbsolutePath());
            alert.showAndWait();
            driverInfoCtrl.updatePass();
            this.stage.close();
        } catch (IOException ex) {
            Logger.getLogger(MainActivityController.class.getName()).log(Level.SEVERE, null, ex);
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Save Error");
            alert.setHeaderText("Image not saved! Please check save file path.");
            alert.setContentText("Error: " + ex.getMessage());
            alert.showAndWait();
        } catch (NullPointerException ex) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Save Error");
            alert.setHeaderText("Image not saved!\nPlease check save file path.");
            alert.setContentText("Error: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    public void setEvents() {
        pxBName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equalsIgnoreCase("") && newValue != null) {
                this.business_name.setStyle("-fx-font-size:" + newValue + ";"
                        + "-fx-font-weight: bold;");
            }
        });

        pxBAddress.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equalsIgnoreCase("") && newValue != null) {
                this.business_address.setStyle("-fx-font-size:" + newValue + ";"
                        + "-fx-font-weight: bold;");
            }
        });

        pxVehicleNo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equalsIgnoreCase("") && newValue != null) {
                this.vehicle_plate_number.setStyle("-fx-font-size:" + newValue + ";"
                        + "-fx-font-weight: bold;");
            }
        });

        pxDescription.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equalsIgnoreCase("") && newValue != null) {
                this.vehicle_description.setStyle("-fx-font-size:" + newValue + ";"
                        + "-fx-font-weight: bold;");
            }
        });

    }

    public static WritableImage pixelScaleAwareCanvasSnapshot(AnchorPane pane, double pixelScale) {
        WritableImage writableImage = new WritableImage((int) Math.rint(pixelScale * pane.getWidth()), (int) Math.rint(pixelScale * pane.getHeight()));
        SnapshotParameters spa = new SnapshotParameters();
        spa.setTransform(Transform.scale(pixelScale, pixelScale));
        return pane.snapshot(spa, writableImage);
    }

    public Image createQrCode(String qrcode) throws FileNotFoundException, IOException {
        QrCode qr0 = QrCode.encodeText("gpzn@" + Helper.getMd5(PrintImage.salt + qrcode + PrintImage.pepper), QrCode.Ecc.MEDIUM);
        BufferedImage img = qr0.toImage(5, 5);
        ImageIO.write(img, "png", new File("qr-code.png"));
        FileInputStream input = new FileInputStream("qr-code.png");
        Image image = new Image(input);

        return image;
    }

}
