
package spframe;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;

public class SPCapture {
    private static SPCaptureController controller;
        
    static void initialize() throws IOException{
         Locale toUseLocale = new Locale.Builder().setLanguage(SPSettings.getLanguageString()).
                        setScript("Arab").build(); 
         initialize(toUseLocale);                        
     }
        
    static void initialize(Locale toUseLocale) throws IOException{
        boolean useDataKeeper = false;
        if(controller != null)
            useDataKeeper = true;
        FXMLLoader loader = new FXMLLoader(SPCapture.class.getResource("/rec/fxml/FXMLCapture.fxml"));        
        loader.setResources(ResourceBundle.getBundle("rec.language.spframe",toUseLocale));                     
        loader.load();
        controller = loader.getController();  
        if(useDataKeeper)
            controller.useDataKeeperDetails();          
    }
    
    static void showSPCaptureController(Image img){             
        controller.show();
        controller.setCapturedImage(img,true);  
    }
 
    static SPCaptureController getController(){
        return controller;
    }
    
}
