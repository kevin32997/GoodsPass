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
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrinterJob;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Transform;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import printimage.PrintImage;
import printimage.helpers.Helper;
import printimage.helpers.SQLDatabase;
import printimage.models.Crew;
import printimage.models.Goodspass;
import printimage.models.Remark;

/**
 * FXML Controller class
 *
 * @author Admin
 */
public class PrintPassesCtrl implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private ListView<AnchorPane> list_view;
    private ObservableList<Goodspass> listToPrint;
    private ObservableList<WritableImage> pass_images;
    private ObservableList<AnchorPane> paneToPrint;

    @FXML
    private Label passes_size;

    private Stage stage;
    private SQLDatabase db;

    @FXML
    private Button btnPrint;

    @FXML
    private Button btnPreview;

    @FXML
    private ProgressIndicator progress;

    private boolean isReprinted = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setData(SQLDatabase db, ObservableList<Goodspass> passes) {
        this.db = db;
        this.listToPrint = passes;
        passes_size.setText("Size: " + this.listToPrint.size());
        setupListData();
        // Setups Pages to be printed
        setupPageToPrint();

        // Check if already printed
        checkReprinted();
    }

    private void checkReprinted() {
        new Thread(() -> {
            for (Goodspass pass : listToPrint) {
                if (pass.getStatus().equals("1")) {
                    isReprinted = true;
                }
            }
        }).start();
    }

    private void setupListData() {
        for (Goodspass pass : listToPrint) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/print_view_item_layout.fxml"));
            try {
                AnchorPane parent = fxmlLoader.load();
                Label label = (Label) parent.lookup("#item_label");
                Button btnPrint = (Button) parent.lookup("#item_print");
                ProgressIndicator progress = (ProgressIndicator) parent.lookup("#item_progress");

                label.setText(pass.getGpNo() + " - " + pass.getBusinessName());

                list_view.getItems().add(parent);

            } catch (IOException ex) {
                Logger.getLogger(PrintPassesCtrl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private double default_fontsize = 15;
    private Font defaultFont = Font.font("Consolas", FontWeight.BOLD, default_fontsize);
    private double MAX_TEXT_WIDTH = 298;

    private void autoResizeField(Text text) {
        text.setFont(defaultFont);

        double textWidth = text.getLayoutBounds().getWidth();
        if (textWidth <= this.MAX_TEXT_WIDTH) {
            text.setFont(defaultFont);
        } else {
            double newFontSize = this.default_fontsize * MAX_TEXT_WIDTH / textWidth;
            text.setFont(Font.font(defaultFont.getFamily(), FontWeight.BOLD, newFontSize));
        }

    }

    private WritableImage pixelScaleAwareCanvasSnapshot(AnchorPane pane, double pixelScale) {
        WritableImage writableImage = new WritableImage((int) Math.rint(pixelScale * 551), (int) Math.rint(pixelScale * 714));
        SnapshotParameters spa = new SnapshotParameters();
        spa.setTransform(Transform.scale(pixelScale, pixelScale));
        return pane.snapshot(spa, writableImage);
    }

    private Image createQrCode(String qrcode) throws FileNotFoundException, IOException {
        QrCode qr0 = QrCode.encodeText("gpzn@" + Helper.getMd5(PrintImage.salt + qrcode + PrintImage.pepper), QrCode.Ecc.MEDIUM);
        BufferedImage img = qr0.toImage(5, 5);
        ImageIO.write(img, "png", new File("qr-code.png"));
        FileInputStream input = new FileInputStream("qr-code.png");
        Image image = new Image(input);
        return image;
    }

    @FXML
    void onCancel(ActionEvent event) {
        this.stage.close();
    }

    @FXML
    void onPreview(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/print_preview_dialog_layout.fxml"));
            Parent parent;
            parent = fxmlLoader.load();
            PrintPreviewCtrl ctrl = fxmlLoader.getController();

            Stage stage = new Stage();
            stage.setTitle("PREVIEW - PAGE COUNT: " + paneToPrint.size());
            Scene scene = new Scene(parent, 770, 720);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);

            ctrl.setCtrl(this);

            ctrl.setPages(this.paneToPrint);

            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(PrintPassesCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void onPrintAll(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/dialog_printall_progress_layout.fxml"));
            AnchorPane progressDialog = fxmlLoader.load();

            Stage printDialogStage = new Stage();
            printDialogStage.setTitle("PRINTING");
            printDialogStage.setResizable(false);
            Scene scene = new Scene(progressDialog, 325, 146);
            printDialogStage.initModality(Modality.APPLICATION_MODAL);
            printDialogStage.setScene(scene);
            Label pageCount = (Label) progressDialog.lookup("#page_count");
            pageCount.setText("Page count " + paneToPrint.size());

            if (isReprinted) {
                FXMLLoader remarksLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/dialog_add_remarks_layout.fxml"));
                Parent parent = remarksLoader.load();
                Stage remarksStage = new Stage();
                Scene remarksScene = new Scene(parent, 470, 370);

                remarksStage.setTitle("Add Remarks");
                remarksStage.setScene(remarksScene);
                remarksStage.initModality(Modality.APPLICATION_MODAL);
                remarksStage.setResizable(false);

                TextArea descriptionText = (TextArea) parent.lookup("#text_area");
                Button btnSave = (Button) parent.lookup("#btnSave");
                Button btnCancel = (Button) parent.lookup("#btnCancel");

                btnSave.setOnAction(e -> {

                    if (descriptionText.getText().equals("")) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Error");
                        alert.setHeaderText("Please add description to save!");
                        alert.showAndWait();
                    } else {

                        PrinterJob printJob = this.print(this.paneToPrint, printDialogStage);

                        Button dialogBtnCancel = (Button) progressDialog.lookup("#btnCancel");

                        dialogBtnCancel.setOnAction(ev -> {
                            if (printJob != null) {
                                printJob.cancelJob();
                                printJob.endJob();
                                printDialogStage.close();
                            }
                        });

                        printDialogStage.show();

                        addReprintRemarks(descriptionText.getText());
                        remarksStage.close();

                    }
                });

                btnCancel.setOnAction(e -> {
                    remarksStage.close();
                });

                remarksStage.show();

            } else {
                PrinterJob printJob = this.print(this.paneToPrint, printDialogStage);
                Button btnCancel = (Button) progressDialog.lookup("#btnCancel");

                btnCancel.setOnAction(e -> {
                    if (printJob != null) {
                        printJob.cancelJob();
                        printJob.endJob();
                        printDialogStage.close();
                    }
                });
                printDialogStage.show();
            }

            // Fetch progress layout
        } catch (IOException ex) {
            Logger.getLogger(PrintPassesCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addReprintRemarks(String description) {
        new Thread(() -> {
            for (Goodspass pass : listToPrint) {
                if (pass.getStatus().equals("1")) {
                    db.createRemarks(Remark.TARGET_PASS, Remark.REMARK_REPRINTED, MainActivityController.MAIN_USER.getId(), pass.getId(), "REPRINTED - " + description);
                }
            }
            System.out.println("Remarks added!");
        }).start();
    }

    // Builds Passes Images to be printed
    private void setupPageToPrint() {
        // Check pages count first
        pass_images = FXCollections.observableArrayList();

        new Thread(() -> {
            for (Goodspass pass : listToPrint) {
                ObservableList<Crew> crews = db.getAllCrewByGPNo(pass.getGpNo());
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/layout_to_print_auto.fxml"));
                    AnchorPane toPrint = fxmlLoader.load();
                    Text control_number = (Text) toPrint.lookup("#control_no");

                    Text business_name = (Text) toPrint.lookup("#business_name");
                    Text business_address = (Text) toPrint.lookup("#business_address");
                    Text plate_no = (Text) toPrint.lookup("#plate_no");
                    Text description = (Text) toPrint.lookup("#description");

                    ImageView image = (ImageView) toPrint.lookup("#main_image");

                    control_number.setText(pass.getGpNo());

                    business_name.setText(pass.getBusinessName());
                    autoResizeField(business_name);

                    business_address.setText(pass.getBusinessAddress());
                    autoResizeField(business_address);

                    plate_no.setText(pass.getVehiclePlateNo());
                    autoResizeField(plate_no);

                    description.setText(pass.getVehicleDesc());
                    autoResizeField(description);

                    Text designation1 = (Text) toPrint.lookup("#designation1");
                    Text designation2 = (Text) toPrint.lookup("#designation2");

                    if (crews.size() > 0) {
                        designation1.setText(crews.get(0).getFullname() + " - " + crews.get(0).getDesignation());
                        if (crews.size() > 1) {
                            designation2.setText(crews.get(1).getFullname() + " - " + crews.get(1).getDesignation());
                        } else {
                            designation2.setText("NONE");
                        }
                    }

                    autoResizeField(designation1);
                    autoResizeField(designation2);

                    Platform.runLater(() -> {
                        try {
                            image.setImage(createQrCode(pass.getGpNo()));
                            WritableImage img = pixelScaleAwareCanvasSnapshot(toPrint, 3);
                            pass_images.add(img);
                            // File fileToSave = chooser.getSelectedFile();//Remove this line.
                            // BufferedImage img2 = SwingFXUtils.fromFXImage(img, null);
                            // this.stage.close();
                        } catch (IOException ex) {
                            Logger.getLogger(PrintPassesCtrl.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (NullPointerException ex) {
                            Logger.getLogger(PrintPassesCtrl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                    Thread.sleep(100);
                } catch (IOException ex) {
                    Logger.getLogger(PrintPassesCtrl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PrintPassesCtrl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            // End
            setupPages();

        }).start();
    }

    // Builds Pages of Passes to be printed
    private void setupPages() {
        paneToPrint = FXCollections.observableArrayList();
        int pageCount = this.listToPrint.size() / 4;
        if (listToPrint.size() % 4 > 0) {
            pageCount++;
        }
        int passesSize = listToPrint.size();
        int settledPasses = 0;
        for (int i = 0; i < pageCount; i++) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/pass_print_page_layout.fxml"));
            try {
                AnchorPane page = fxmlLoader.load();

                for (int ii = 0; ii < 4; ii++) {

                    if (settledPasses < passesSize) {
                        ImageView img = (ImageView) page.lookup("#pass" + (ii + 1));
                        img.setImage(pass_images.get(settledPasses));
                        img.setVisible(true);
                        settledPasses++;
                    } else {

                    }
                }
                paneToPrint.add(page);
            } catch (IOException ex) {
                Logger.getLogger(PrintPassesCtrl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // print(paneToPrint, stage);

        Platform.runLater(() -> {
            btnPrint.setDisable(false);
            btnPreview.setDisable(false);
            progress.setVisible(false);

        });
    }

    private void updatePassInfo(Goodspass pass) {
        new Thread(() -> {
            if (!pass.getStatus().equals("1")) {
                db.updatePassInfoPrinted(pass);
                db.createRemarks(Remark.TARGET_PASS, Remark.REMARK_PRINTED, MainActivityController.MAIN_USER.getId(), pass.getId(), "Printed by " + MainActivityController.MAIN_USER.getFullname());
            }
        }).start();
    }

    private PrinterJob print(ObservableList<AnchorPane> pagesToPrint, Stage progressStage) {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        new Thread(() -> {
            if (printerJob != null) {
                PageLayout pageLayout = printerJob.getPrinter().createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, 0, 0, 0, 0);
                for (AnchorPane pane : pagesToPrint) {
                    printerJob.printPage(pageLayout, pane);
                }
                printerJob.endJob();
                Platform.runLater(() -> {
                    progressStage.close();
                    updatePasses();
                });

            }
        }).start();
        return printerJob;
    }

    private void updatePasses() {
        for (Goodspass pass : this.listToPrint) {
            updatePassInfo(pass);
        }
        db.updateDB();
    }

}
