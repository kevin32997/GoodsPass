/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package printimage.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import printimage.custom.ZoomableScrollPane;

/**
 * FXML Controller class
 *
 * @author Admin
 */
public class PrintPreviewCtrl implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private AnchorPane main_anchor;
    
    @FXML
    private Label page_count;
    
    private PrintPassesCtrl ctrl;
    
    private ObservableList<AnchorPane> pages;
    
    private ZoomableScrollPane scrollpane;
    
    private HBox main_container;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        main_container = new HBox();
        main_container.setSpacing(15);
        scrollpane = new ZoomableScrollPane(main_container);
        // TODO
        main_anchor.getChildren().add(scrollpane);
        main_anchor.setTopAnchor(scrollpane, 55.0);
        main_anchor.setLeftAnchor(scrollpane, 0.0);
        main_anchor.setTopAnchor(scrollpane, 0.0);
        main_anchor.setRightAnchor(scrollpane, 0.0);
        
    }
    
    public void setCtrl(PrintPassesCtrl ctrl) {
        this.ctrl = ctrl;
    }
    
    public void setPages(ObservableList<AnchorPane> pages) {
        this.pages = pages;
        loadPagesToView();
    }
    
    private void loadPagesToView() {
        for (AnchorPane page : pages) {
            main_container.getChildren().add(page);
        }
        
    }
    
    @FXML
    void onPrint(ActionEvent event) {
        
    }
    
}
