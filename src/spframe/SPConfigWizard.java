/*
 * Copyright (C) 2017 mcc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package spframe;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;


/**
 * the first time configuration dialog class manager .
 * @author Abd Alhamid
 */
public class SPConfigWizard implements SPCustomizable{
    
    protected static SPConfigWizard controller;
    
    protected static void showConfigWizard(){  
        Locale toUseLocale = new Locale.Builder().setLanguage(SPSettings.getLanguageString()).setScript("Arab").build();
        FXMLLoader FXLoader = new FXMLLoader(SPSettings.class.getResource("/rec/fxml/FXMLConfigWizard.fxml"));            
        FXLoader.setResources(ResourceBundle.getBundle("rec.language.spframe",toUseLocale));              
        try {
            FXLoader.load();
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, ex.getLocalizedMessage(), ButtonType.OK).show();
        }
        controller = FXLoader.getController();        
    } 
      
    private CustomStage stage;
    @FXML private AnchorPane mainPane;
    @FXML private Button okBtn;
    @FXML private ChoiceBox langBox;
    @FXML private ResourceBundle resources;
    
    @FXML private void initialize(){          
        this.stage = new CustomStage(mainPane);
        this.stage.setCustomResizable(false);               
        this.stage.getIcons().addAll(new Image("/rec/icons/transparentlogo/512.png"),
                                          new Image("/rec/icons/transparentlogo/256.png"),
                                          new Image("/rec/icons/transparentlogo/128.png"),
                                          new Image("/rec/icons/transparentlogo/64.png"),
                                          new Image("/rec/icons/transparentlogo/32.png"),
                                          new Image("/rec/icons/transparentlogo/24.png"),
                                          new Image("/rec/icons/transparentlogo/16.png"));        
        this.stage.setTitle(this.resources.getString("PRIMARY_TITLE"));  
        this.stage.initCustomModality(Modality.APPLICATION_MODAL); 
        this.stage.setCustomHeight(mainPane.getPrefHeight()+40);
        
        this.langBox.setItems(FXCollections.observableArrayList(
                this.resources.getString("SETTINGS_LANGUAGE_ENGLISH_ITEM"),
                this.resources.getString("SETTINGS_LANGUAGE_ARABIC_ITEM"))); 
        this.langBox.getSelectionModel().select(SPSettings.getLanguage());
        
        this.setNightModeEnabled(SPSettings.getNightMode());    
        this.applyTheme(SPSettings.getThemePath());
        String usedOrie = this.resources.getString("NODE_ORIENTATION");
        this.setUIOrientation(usedOrie.equals("ltr")? 
                NodeOrientation.LEFT_TO_RIGHT:NodeOrientation.RIGHT_TO_LEFT);                
        this.stage.show();   
        
        this.langBox.getSelectionModel().selectedIndexProperty().addListener(
            (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {                                                                                                        
                SPSettings.getController().setApplicationLanguage((int)newValue);            
        });              
    }
        
    @FXML private void onSelected(){ 
        this.close();
        SPSettings.setFirstRun(false);
        SPPrimary.getController().show();               
    }
        
    protected void close(){
        this.stage.close();
    }
        
    @Override
    public void setUIOrientation(NodeOrientation orientation) {
        if(orientation.equals(NodeOrientation.LEFT_TO_RIGHT))
            this.stage.setTitlebarorientation(NodeOrientation.RIGHT_TO_LEFT);
        else
            this.stage.setTitlebarorientation(NodeOrientation.LEFT_TO_RIGHT);
        this.mainPane.setNodeOrientation(orientation);        
    }

    @Override
    public Pane getMainPane() {
        return this.mainPane;
    }
    @Override
    public Scene getWindowScene() {
       return this.stage.getScene();
    }
    
    @Override
    public void setDataKeeperDetails() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void useDataKeeperDetails() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
            
}
