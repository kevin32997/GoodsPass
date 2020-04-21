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
import java.sql.Date;
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
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
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

    private Stage stage;
    private SQLDatabase db;

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

        setupListData();
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
                btnPrint.setOnAction(e -> {
                    btnPrint.setVisible(false);
                    progress.setVisible(true);
                    printPass(pass, progress);
                });
                list_view.getItems().add(parent);

            } catch (IOException ex) {
                Logger.getLogger(PrintPassesCtrl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void printPass(Goodspass pass, ProgressIndicator progress) {
        new Thread(() -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/layout_to_print_auto.fxml"));
                AnchorPane toPrint = fxmlLoader.load();
                Text control_number = (Text) toPrint.lookup("#control_no");

                Text business_name = (Text) toPrint.lookup("#business_name");
                Text business_address = (Text) toPrint.lookup("#business_address");
                Text plate_no = (Text) toPrint.lookup("#plate_no");
                Text description = (Text) toPrint.lookup("#description");

                Text designation1 = (Text) toPrint.lookup("#designation1");
                Text designation2 = (Text) toPrint.lookup("#designation2");

                control_number.setText(pass.getGpNo());

                business_name.setText(pass.getBusinessName());
                autoResizeField(business_name);

                business_address.setText(pass.getBusinessAddress());
                autoResizeField(business_address);

                plate_no.setText(pass.getVehiclePlateNo());
                autoResizeField(plate_no);

                description.setText(pass.getVehicleDesc());
                autoResizeField(description);

                Platform.runLater(() -> {
                    save(toPrint, pass.getGpNo(), progress);
                });
            } catch (IOException ex) {
                Logger.getLogger(PrintPassesCtrl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();

    }

    private void save(AnchorPane pane, String title, ProgressIndicator progress) {

        WritableImage img = pixelScaleAwareCanvasSnapshot(pane, 3);
        // File fileToSave = chooser.getSelectedFile();//Remove this line.

        // BufferedImage img2 = SwingFXUtils.fromFXImage(img, null);
        try {
            File fileToSave = new File(MainActivityController.settings.getFolderPath(), title + ".png");
            ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", fileToSave);
            progress.setVisible(false);
            // this.stage.close();
        } catch (IOException ex) {
            Logger.getLogger(PrintPassesCtrl.class.getName()).log(Level.SEVERE, null, ex);

        } catch (NullPointerException ex) {
            Logger.getLogger(PrintPassesCtrl.class.getName()).log(Level.SEVERE, null, ex);
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

    }

    @FXML
    void onPrintAll(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("printimage/layout/dialog_printall_progress_layout.fxml"));
            AnchorPane progressDialog = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle("PRINTING");
            stage.setResizable(false);
            Scene scene = new Scene(progressDialog, 326, 150);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);

            stage.show();
            startPrinting(stage, progressDialog);
            // Fetch progress layout
        } catch (IOException ex) {
            Logger.getLogger(PrintPassesCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void startPrinting(Stage progressStage, AnchorPane dialogPane) {
        final int printed_count[] = {0};
        final int error_count[] = {0};

        int max_count = this.listToPrint.size();
        final int printed[] = {0};
        ProgressBar progress = (ProgressBar) dialogPane.lookup("#progress");
        Label progressCount = (Label) dialogPane.lookup("#print_count");
        progressCount.setText(printed[0] + "/" + max_count);
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
                            // File fileToSave = chooser.getSelectedFile();//Remove this line.

                            // BufferedImage img2 = SwingFXUtils.fromFXImage(img, null);
                            File fileToSave = new File(MainActivityController.settings.getFolderPath(), pass.getGpNo() + ".png");
                            ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", fileToSave);
                            updatePassInfo(pass);
                            printed[0]++;
                            printed_count[0]++;
                            progressCount.setText(printed[0] + "/" + max_count);
                            // this.stage.close();
                        } catch (IOException ex) {
                            Logger.getLogger(PrintPassesCtrl.class.getName()).log(Level.SEVERE, null, ex);
                            error_count[0]++;
                        } catch (NullPointerException ex) {
                            Logger.getLogger(PrintPassesCtrl.class.getName()).log(Level.SEVERE, null, ex);
                            error_count[0]++;
                        }
                    });

                    Thread.sleep(250);

                } catch (IOException ex) {
                    Logger.getLogger(PrintPassesCtrl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PrintPassesCtrl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            // End
            Platform.runLater(() -> {
                progressStage.close();
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Passes Printed!");
                alert.setContentText("Success: " + printed_count[0] + " - Unsuccessfull: " + error_count[0]);

                alert.showAndWait();
                db.updateDB();
            });

        }).start();
    }

    private void updatePassInfo(Goodspass pass) {
        new Thread(() -> {
            if (!pass.getStatus().equals("1")) {
                pass.setStatus("1");
                java.util.Date dateToday = new java.util.Date();
                pass.setDate_sql(new Date(dateToday.getTime()));
                db.updatePassInfo(pass);
            }
        }).start();
    }

    public void print(ObservableList<AnchorPane> pagesToPrint) {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null) {

            PageLayout pageLayout = printerJob.getPrinter().createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, 0, 0, 0, 0);

            for (AnchorPane pane : pagesToPrint) {
                printerJob.printPage(pageLayout, pane);
            }

        }
    }

}