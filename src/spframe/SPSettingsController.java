package spframe;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;

public class SPSettingsController implements SPCustomizable{    
    private CustomStage windowManager;       
    @FXML private ResourceBundle resources;
    @FXML private FlowPane mainPane;
    @FXML private GridPane appGrid,screenGrid;
    @FXML private CheckBox reminderCheck,nightModeCheck,instrucCheck,quickDoneCheck;    
    @FXML private ChoiceBox langBox,themeBox,backgroundBox,doneBox;    
    @FXML private Label doneLabel;
    @FXML private RadioButton lastPathRadio,specRadio;
    @FXML private TextField pathFiled;
    @FXML private Button browseButton;
    @FXML private ToggleGroup savePathGroup;
    private ObservableList<Object> unmodifedSettings;     
    private DirectoryChooser dirChooser;
        
    @FXML private void initialize(){    
        this.dirChooser = new DirectoryChooser();
        this.initWindow();  
        this.initBoxes();
                      
        this.unmodifedSettings = FXCollections.observableArrayList();      
        this.themeBox.getSelectionModel().select(SPSettings.getTheme()); 
        this.langBox.getSelectionModel().select(SPSettings.getLanguage());  
        this.reminderCheck.setSelected(SPSettings.getSaveDialogShow());
        this.nightModeCheck.setSelected(SPSettings.getNightMode()); 
        this.instrucCheck.setSelected(SPSettings.getInstructionShow());
        
        this.backgroundBox.getSelectionModel().select(SPSettings.getImageBackground());
        this.quickDoneCheck.setSelected(SPSettings.getQuickDialogShow());
        this.doneBox.getSelectionModel().select(SPSettings.getWhenQuickDone());
        this.savePathGroup.selectToggle((SPSettings.getQuickPathType() == 0)? lastPathRadio:specRadio);
        this.refreshSettings();
                        
        this.setNightModeEnabled(SPSettings.getNightMode());
        this.applyTheme(SPSettings.getThemePath());
        String usedOrie = this.resources.getString("NODE_ORIENTATION");
        this.setUIOrientation(usedOrie.equals("ltr")? 
                NodeOrientation.LEFT_TO_RIGHT:NodeOrientation.RIGHT_TO_LEFT);
        
        this.initListeners();   
        if(SPPrimary.getController() != null)
            setSettingsPosition();
    }  
    
    private void initBoxes(){
        this.themeBox.setItems(FXCollections.observableArrayList(
                this.resources.getString("SETTINGS_THEME_ORANGE_ITEM"),
                this.resources.getString("SETTINGS_THEME_GREEN_ITEM")));  
        
        this.langBox.setItems(FXCollections.observableArrayList(
                this.resources.getString("SETTINGS_LANGUAGE_ENGLISH_ITEM"),
                this.resources.getString("SETTINGS_LANGUAGE_ARABIC_ITEM"))); 
        this.backgroundBox.setItems(FXCollections.observableArrayList(
                this.resources.getString("SETTINGS_TRANSPARENT_ITEM"),
                this.resources.getString("SETTINGS_WHITE_ITEM"),
                this.resources.getString("SETTINGS_BLACK_ITEM")));    
        this.doneBox.setItems(FXCollections.observableArrayList(
                this.resources.getString("SETTINGS_ASKME_ITEM"),
                this.resources.getString("SETTINGS_EXIT_ITEM"),
                this.resources.getString("SETTINGS_OPENFOLDER_ITEM"),
                this.resources.getString("SETTINGS_DONOTHING_ITEM")));           
    }
    
    private void initWindow(){
        this.windowManager = new CustomStage(this.mainPane);
        this.windowManager.setCustomResizable(false); 
        this.windowManager.setTitle(this.resources.getString("SETTINGS_TITLE")); 
        this.windowManager.getIcons().addAll(new Image("/rec/icons/transparentlogo/512.png"),
                                          new Image("/rec/icons/transparentlogo/256.png"),
                                          new Image("/rec/icons/transparentlogo/128.png"),
                                          new Image("/rec/icons/transparentlogo/64.png"),
                                          new Image("/rec/icons/transparentlogo/32.png"),
                                          new Image("/rec/icons/transparentlogo/24.png"),
                                          new Image("/rec/icons/transparentlogo/16.png")); 
        this.windowManager.initWindowModality(Modality.APPLICATION_MODAL);        
    }
    
    private void initListeners(){   
        this.browseButton.setOnAction(value ->{
            showDirectoryChooser();
        });
        this.langBox.getSelectionModel().selectedIndexProperty().addListener(
            (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {                                                                                                        
                this.setApplicationLanguage((int)newValue);
            
        });
        
        this.themeBox.getSelectionModel().selectedIndexProperty().addListener(
            (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {                                                                                               
                this.setApplicationTheme((int)newValue);
            }); 
        
        this.reminderCheck.selectedProperty().addListener(listener ->{
            SPSettings.setSaveDialogShow(this.reminderCheck.isSelected());
        });        
        this.nightModeCheck.selectedProperty().addListener(listener ->{
            boolean enable = this.nightModeCheck.isSelected();
            SPSettings.setNightMode(enable);
            this.setNightModeEnabled(enable);
            SPPrimary.getController().setNightModeEnabled(enable);
            SPCapture.getController().setNightModeEnabled(enable);
            SPAbout.getController().setNightModeEnabled(enable);            
        });    
        this.instrucCheck.selectedProperty().addListener(listener ->{
            boolean enable = this.instrucCheck.isSelected();
            SPSettings.setInstructionShow(enable);                        
        });        
        this.backgroundBox.getSelectionModel().selectedIndexProperty().addListener(
            (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {  
                SPSettings.setImageBackground((int)newValue);
        });
        this.quickDoneCheck.selectedProperty().addListener(listener ->{
            SPSettings.setQuickDialogShow(quickDoneCheck.isSelected());
            refreshSettings();
        });
        this.doneBox.getSelectionModel().selectedIndexProperty().addListener(
            (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {  
                SPSettings.setWhenQuickDone((int)newValue);
        });        
        this.savePathGroup.selectedToggleProperty().addListener(
                (ObservableValue<? extends Toggle> Observable,Toggle oldValue,Toggle newValue) ->{
                    SPSettings.setQuickPathType((newValue == lastPathRadio) ? 0 : 1);
                    refreshSettings();                        
        });
    }
    
    protected void setApplicationLanguage(int lang){
        try {
            SPSettings.setLanguage(lang);
            SPPrimary.getController().setDataKeeperDetails();
            SPCapture.getController().setDataKeeperDetails();
            this.setDataKeeperDetails();
            SPPrimary.initialize();
            SPCapture.initialize();
            SPSettings.initialize();
            SPAbout.initialize();
            SPSaver.initialize();
                        
            SPDataKeeper.clearDetails();
        } catch (IOException ex) {
            Logger.getLogger(SPSettingsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    protected void setApplicationTheme(int theme){
        SPSettings.setTheme(theme);
        this.applyTheme(SPSettings.getThemePath());
        SPPrimary.getController().applyTheme(SPSettings.getThemePath());
        SPCapture.getController().applyTheme(SPSettings.getThemePath());
        SPAbout.getController().applyTheme(SPSettings.getThemePath());                
    }
    
    private void showDirectoryChooser(){   
        this.dirChooser.setTitle(resources.getString("SETTINGS_FOLDER_CHOOSER_TITLE"));
        this.dirChooser.setInitialDirectory(new File(pathFiled.getText()));
        File selection = this.dirChooser.showDialog(windowManager);
        if(selection != null){
            this.pathFiled.setText(selection.getAbsolutePath());
            SPSettings.setSpecificQuickPath(selection.getAbsolutePath());
        }
    }
    
    protected void refreshSettings(){
        reminderCheck.setSelected(SPSettings.getSaveDialogShow());
        doneBox.setDisable(!this.quickDoneCheck.isSelected());
        doneLabel.setDisable(!this.quickDoneCheck.isSelected());
        if(SPSettings.getQuickPathType() == 0){
            this.browseButton.setDisable(true);         
            this.pathFiled.setText(SPSettings.getSavePath());
        }else{
            this.browseButton.setDisable(false);         
            this.pathFiled.setText(SPSettings.getSpecificQuickPath());            
        }
    }
    
    @FXML private void cancelSetting(){
        int unmodLang = (int)this.unmodifedSettings.get(0);
        int unmodTheme = (int)this.unmodifedSettings.get(1);
        boolean showDialog = (boolean)this.unmodifedSettings.get(2);
        boolean nightMode = (boolean)this.unmodifedSettings.get(3); 
        boolean instrcShow = (boolean)this.unmodifedSettings.get(4);
        int background = (int)this.unmodifedSettings.get(5);
        boolean quickDialog = (boolean)this.unmodifedSettings.get(6);
        int whenDone = (int)this.unmodifedSettings.get(7);
        int quickPathType = (int)this.unmodifedSettings.get(8);
        String specificPath = (String)this.unmodifedSettings.get(9);
        this.windowManager.close();    
        
        this.langBox.getSelectionModel().select(unmodLang);
        this.themeBox.getSelectionModel().select(unmodTheme);
        this.nightModeCheck.setSelected(nightMode);        
        this.reminderCheck.setSelected(showDialog);  
        this.instrucCheck.setSelected(instrcShow);
        SPSettings.setLanguage(unmodLang);
        SPSettings.setTheme(unmodTheme);
        SPSettings.setSaveDialogShow(showDialog);
        SPSettings.setNightMode(nightMode);
        
        this.backgroundBox.getSelectionModel().select(background);
        this.quickDoneCheck.setSelected(quickDialog);
        this.doneBox.getSelectionModel().select(whenDone);
        this.savePathGroup.selectToggle(quickPathType == 0 ? lastPathRadio:specRadio);
        SPSettings.setImageBackground(background);
        SPSettings.setQuickDialogShow(quickDialog);
        SPSettings.setWhenQuickDone(whenDone);
        SPSettings.setQuickPathType(quickPathType);
        SPSettings.setSpecificQuickPath(specificPath);        
    }
    
    @FXML private void setSettings(){        
        this.windowManager.close();    
    }    
    @Override()
    public Pane getMainPane(){
        return this.mainPane;
    }    
    @Override
    public Scene getWindowScene(){
        return this.windowManager.getScene();
    }             
    @Override
    public void setUIOrientation(NodeOrientation orientation) {  
        if(orientation.equals(NodeOrientation.LEFT_TO_RIGHT))
            this.windowManager.setTitlebarorientation(NodeOrientation.RIGHT_TO_LEFT);
        else
            this.windowManager.setTitlebarorientation(NodeOrientation.LEFT_TO_RIGHT);
        this.mainPane.setNodeOrientation(orientation);
    }        
    @Override
    public void setDataKeeperDetails() {
        SPDataKeeper.settingsDetails = FXCollections.observableArrayList(
                this.windowManager.isShowing(),
                this.windowManager.getCustomX(),
                this.windowManager.getCustomY(),
                this.unmodifedSettings);    
        this.windowManager.close();         
    }
    @Override
    public void useDataKeeperDetails() {
        ObservableList details = SPDataKeeper.settingsDetails;
        boolean show = (boolean)details.get(0);
        double x = (double)details.get(1);
        double y = (double)details.get(2);   
        ObservableList<Object> setList = (ObservableList<Object>)details.get(3);
        this.unmodifedSettings = setList;        
        this.windowManager.setCustomX(x);this.windowManager.setCustomY(y);
        
        if(show)
            this.windowManager.show();
    }       
    
    private void setSettingsPosition(){
        Rectangle2D vBounds = Screen.getPrimary().getVisualBounds();
        double toSetX,toSetY;
        if(SPPrimary.getController().isShowing()){            
            double priX = SPSettings.getPrimaryX();
            double priY = SPSettings.getPrimaryY();
            toSetX = priX-440; toSetY = priY;            
            if(toSetX <= 0)
                toSetX = priX + 325;        
        }
        else{
            toSetX = vBounds.getMaxX()- 530;
            toSetY = 60;
        }
        windowManager.setX(toSetX);windowManager.setY(toSetY);        
    }
    
    protected void show(){ 
        refreshSettings();
        this.windowManager.show();       
        setSettingsPosition();
        this.unmodifedSettings.setAll(
                SPSettings.getLanguage(),
                SPSettings.getTheme(),
                SPSettings.getSaveDialogShow(),
                SPSettings.getNightMode(),
                SPSettings.getInstructionShow(),
                SPSettings.getImageBackground(),
                SPSettings.getQuickDialogShow(),
                SPSettings.getWhenQuickDone(),
                SPSettings.getQuickPathType(),
                SPSettings.getSpecificQuickPath());
    }          
}