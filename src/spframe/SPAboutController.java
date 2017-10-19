
package spframe;

import java.util.ResourceBundle;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;

/**
 *
 * only a class showing and hiding the about section
 * 
 * @author mcc
 */
public class SPAboutController implements SPCustomizable{
    
    private CustomStage windowManager;
    @FXML private ResourceBundle resources;
    @FXML private GridPane mainPane;   
    @FXML private Label verLabel;
    @FXML private Hyperlink visitProfileLink,sourceLink,licenseLink;
           
    @FXML private void initialize(){        
        this.windowManager = new CustomStage(this.mainPane);            
        this.windowManager.setCustomResizable(false);
        this.windowManager.initCustomModality(Modality.APPLICATION_MODAL);
        this.windowManager.setTitle(this.resources.getString("ABOUT_TITLE")); 
        this.windowManager.getIcons().addAll(new Image("/rec/icons/transparentlogo/512.png"),
                                          new Image("/rec/icons/transparentlogo/256.png"),
                                          new Image("/rec/icons/transparentlogo/128.png"),
                                          new Image("/rec/icons/transparentlogo/64.png"),
                                          new Image("/rec/icons/transparentlogo/32.png"),
                                          new Image("/rec/icons/transparentlogo/24.png"),
                                          new Image("/rec/icons/transparentlogo/16.png")); 
        this.verLabel.setText("1.0");        
        this.applyTheme(SPSettings.getThemePath());        
        this.setNightModeEnabled(SPSettings.getNightMode());
        String usedOrie = this.resources.getString("NODE_ORIENTATION");        
        this.setUIOrientation(usedOrie.equals("ltr")? 
                NodeOrientation.LEFT_TO_RIGHT:NodeOrientation.RIGHT_TO_LEFT);  
        
        this.visitProfileLink.setOnAction(value -> showWebPage("https://github.com/AbdAlhamidZobi"));
        this.sourceLink.setOnAction(value -> showWebPage("https://github.com/AbdAlhamidZobi/SP-Frame/"));
        this.licenseLink.setOnAction(value -> showWebPage("https://github.com/AbdAlhamidZobi/SP-Frame/wiki/Liscense"));
    }    
    
    @FXML private void showWebPage(String url){
        HostServices host = SPMain.getAppHostServices();
        host.showDocument(url);
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
        this.mainPane.setNodeOrientation(orientation);
        if(orientation.equals(NodeOrientation.LEFT_TO_RIGHT))
            this.windowManager.setTitlebarorientation(NodeOrientation.RIGHT_TO_LEFT);                    
        else
            this.windowManager.setTitlebarorientation(NodeOrientation.LEFT_TO_RIGHT);            
                    
    }
    
    protected void show(){
        this.windowManager.show();
    }    
        
    @Override
    public void setDataKeeperDetails() {}

    @Override
    public void useDataKeeperDetails() {}

}
