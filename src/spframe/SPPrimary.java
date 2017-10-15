package spframe;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;

public class SPPrimary {
    private static SPPrimaryController controller;
    
    protected static void initialize() throws IOException{
         Locale toUseLocale = new Locale.Builder().setLanguage(SPSettings.getLanguageString()).
                        setScript("Arab").build(); 
         initialize(toUseLocale);                        
     }
     
    protected static void initialize(Locale toUseLocale) throws IOException{ 
        boolean useDataKeeper = false;
        if(controller!= null)
            useDataKeeper = true;                
        ResourceBundle bundle = ResourceBundle.getBundle("rec.language.spframe",toUseLocale);
        FXMLLoader PrimaryLoader = new FXMLLoader(SPPrimary.class.getResource("/spframe/fxml/FXMLPrimary.fxml"),bundle);                                        
        PrimaryLoader.load();        
        controller = PrimaryLoader.getController();    
        if(useDataKeeper)
            controller.useDataKeeperDetails();
        else
            controller.show();
    }
    
    protected static SPPrimaryController getController(){
        return controller;
    }  
}
