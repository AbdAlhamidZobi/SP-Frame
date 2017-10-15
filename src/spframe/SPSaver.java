
package spframe;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import javax.imageio.ImageIO;   

/**
 * 
 * this class is used to manage the file chooser,save quick and normal screenshots;
 */
public class SPSaver {
    private static FileChooser fileChooser;
    private static WritableImage currentFrame; 
    private static ResourceBundle bundle;
           
    protected static void initialize() throws IOException{
        Locale toUseLocale = new Locale.Builder().setLanguage(SPSettings.getLanguageString()).
                        setScript("Arab").build(); 
        bundle = ResourceBundle.getBundle("rec.language.spframe",toUseLocale);                                               
     }
       
    protected static void initChooser(WritableImage img,Window owner,String title){ 
        currentFrame = img;
        fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.setInitialDirectory( new File(SPSettings.getSavePath()));
        fileChooser.setInitialFileName(getFrameName());   
        ArrayList<ExtensionFilter> extFilters = getTypes();
        fileChooser.getExtensionFilters().addAll(extFilters);                 
        fileChooser.setSelectedExtensionFilter(extFilters.get(SPSettings.getSaveExtensionIndex()));
                
        fileChooser.selectedExtensionFilterProperty().addListener(
            (ObservableValue<? extends ExtensionFilter> observable, ExtensionFilter oldValue, ExtensionFilter newValue) -> {                                
                SPSettings.setSaveExtensionIndex(extFilters.indexOf(newValue));
        });
        showChooser(owner);
    }
    
    protected static void showChooser(Window _owner){
        File selectedFile = fileChooser.showSaveDialog(_owner);     
        if(selectedFile != null){              
            saveFrame(selectedFile);
        }
    }
            
    private static void saveFrame(File file){      
        try {                   
            ImageIO.write(SwingFXUtils.fromFXImage(currentFrame,null),"png", file);
            // actuallly png is nothing the format is already defined in the file instance           
            SPSettings.setSavePath(file.getParent());  
            SPCapture.getController().setFrameChanged(false);
        } catch (IllegalArgumentException|IOException ex) {
            showSaveErrorDialog(file.getParent());            
        }
    }     
    
    protected static void saveQuickFrame(WritableImage image){
        SPShooter.cancelShooter();
        currentFrame = image;       
        ArrayList<ExtensionFilter> extFilters = getTypes(); 
        String path = SPSettings.getQuickPathType() == 0 ? 
                SPSettings.getSavePath(): SPSettings.getSpecificQuickPath();
        String format = extFilters.get(SPSettings.getSaveExtensionIndex()).getExtensions().get(0).substring(1);
        File quickFile = new File(path+File.separator+getFrameName()+format);        
        try {                   
            boolean write = ImageIO.write(SwingFXUtils.fromFXImage(currentFrame,null),"png", quickFile);
            // png is nothing the format is already defined in the file instance.  
            if(write)  
                quickFinishedHandle(path);
        }catch (IllegalArgumentException|IOException ex) {
            showSaveErrorDialog(path);  
        }
    }  
    
    private static void quickFinishedHandle(String toPath){      
        if(SPSettings.getQuickDialogShow()){  
            Alert notifDialog = null;
            String message = bundle.getString("SAVER_QUICK_MESSAGE")+"\n"+toPath;
            String title = bundle.getString("SAVER_QUICK_TITLE");
            String header = bundle.getString("SAVER_QUICK_HEADER");
            
            ButtonType openBtn = new ButtonType(bundle.getString("SAVER_QUICK_OPENFOLDER"));               
            ButtonType exitBtn = new ButtonType(bundle.getString("SAVER_QUICK_EXIT"));
            ButtonType closeBtn = new ButtonType(bundle.getString("CLOSE_BUTTON"),ButtonBar.ButtonData.CANCEL_CLOSE);
            
            boolean showPrimary = true;                               
            switch(SPSettings.getWhenQuickDone()){
                case 0:// ask me                         
                    notifDialog = new Alert(Alert.AlertType.INFORMATION, message, closeBtn,exitBtn,openBtn);
                    break;
                case 1:// exit      
                    showPrimary = false;
                    notifDialog = new Alert(Alert.AlertType.INFORMATION, message, closeBtn);
                    break;
                case 2:// open folder                    
                    break;
                case 3:// do nothing                    
                    notifDialog = new Alert(Alert.AlertType.INFORMATION, message, closeBtn);
                    break;
            }
            if(showPrimary)
                SPPrimary.getController().show();
            if(notifDialog != null){
                if(showPrimary){                    
                    double toSetX,toSetY;                                
                    double priX = SPSettings.getPrimaryX();
                    double priY = SPSettings.getPrimaryY();
                    toSetX = priX-150; toSetY = priY+150;            
                    if(toSetX <= 0)
                        toSetX = priX + 150; 
                    notifDialog.setX(toSetX);notifDialog.setY(toSetY);                     
                }
                    
                notifDialog.setTitle(title);notifDialog.setHeaderText(header);
                GridPane pane = (GridPane)notifDialog.getDialogPane().getChildren().get(0); 
                ((Label)notifDialog.getDialogPane().getChildren().get(1)).setStyle("-fx-font-size:13;");
                notifDialog.getDialogPane().setNodeOrientation(getUsedOrientation());
                pane.setPrefHeight(50); 
                Optional<ButtonType> option = notifDialog.showAndWait();
                if(option.get() == openBtn){
                    if(Desktop.isDesktopSupported()){                        
                        Desktop desktop = Desktop.getDesktop();                        
                        try {
                            desktop.open(new File(toPath));
                        } catch (IOException ex) {
                            Logger.getLogger(SPSaver.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }                    
                }else if(option.get() == exitBtn){
                    Platform.exit();
                }                    
            }else{
                if(Desktop.isDesktopSupported()){
                    Desktop desktop = Desktop.getDesktop();
                    try {
                        desktop.open(new File(toPath));
                    } catch (IOException ex) {
                        Logger.getLogger(SPSaver.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }                
            }   
        }
    }
    
    private static ArrayList<ExtensionFilter> getTypes(){
        ArrayList<ExtensionFilter> typeList = new ArrayList<>();        
        typeList.add(new ExtensionFilter("PNG(*.png)","*.png"));        
        typeList.add(new ExtensionFilter("JPEG(*.jpg)", "*.jpg"));     
        typeList.add(new ExtensionFilter("GIF(*.gif)", "*.gif"));        
        return typeList;
    }      
    
    private static String getFrameName(){
       return "SP-"+LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)+"_"
               +LocalTime.now().getHour()+""+LocalTime.now().getMinute()+""+LocalTime.now().getSecond();
    }
    
    private static void showSaveErrorDialog(String toPath){
        ButtonType buttonOk = new ButtonType(bundle.getString("OK_BUTTON"),ButtonBar.ButtonData.OK_DONE);   
        String message = bundle.getString("SAVER_SAVE_FAILED_MESSAGE")+"\n"+toPath;
        String title = bundle.getString("SAVER_SAVE_FAILD_TITLE");
        String header = bundle.getString("SAVER_SAVE_FAILD_HEADER");
        Alert alert = new Alert(Alert.AlertType.ERROR,message,buttonOk);         
        alert.setTitle(title);alert.setHeaderText(header); 
        
        GridPane pane = (GridPane)alert.getDialogPane().getChildren().get(0); 
        ((Label)alert.getDialogPane().getChildren().get(1)).setStyle("-fx-font-size:13;");
        alert.getDialogPane().setNodeOrientation(getUsedOrientation());
        pane.setPrefHeight(50); 
        alert.show();        
    }
    
    private static NodeOrientation getUsedOrientation(){
        String usedOrie = bundle.getString("NODE_ORIENTATION");
        return usedOrie.equals("ltr")? 
                NodeOrientation.LEFT_TO_RIGHT:NodeOrientation.RIGHT_TO_LEFT;
    }

}

