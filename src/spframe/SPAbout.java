/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spframe;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;

/**
 *
 * @author mcc
 */
public class SPAbout {
    private static SPAboutController controller;
    
    protected static void initialize() throws IOException{
         Locale toUseLocale = new Locale.Builder().setLanguage(SPSettings.getLanguageString()).
                        setScript("Arab").build();         ;
         initialize(toUseLocale);                        
     }
     
    protected static void initialize(Locale toUseLocale) throws IOException{                         
        FXMLLoader loader = new FXMLLoader(SPAbout.class.getResource("/spframe/fxml/FXMLAbout.fxml"));                                
        loader.setResources(ResourceBundle.getBundle("rec.language.spframe",toUseLocale));        
        loader.load();        
        controller = loader.getController();            
    }
    
    protected static SPAboutController getController(){
        return controller;
    }  
}
