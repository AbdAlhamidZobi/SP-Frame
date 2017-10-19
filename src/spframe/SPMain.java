
package spframe;

import java.io.IOException;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.stage.Stage;

public class SPMain extends Application {  
    private static HostServices hostService;
    
    public static HostServices getAppHostServices(){
        return hostService;
    }
                   
    public static void main(String[] args){               
        launch(args);
    }
        
    @Override
    public void start(Stage primaryStage) throws IOException {    
        System.setProperty("prism.text", "t2k");
        System.setProperty("prism.lcdtext", "false");   
        hostService = getHostServices();
        SPSettings.initialize(); 
        if(SPSettings.isFirstRun()){            
            SPConfigWizard.showConfigWizard();
        }
        SPShooter.initialize();
        SPPrimary.initialize();
        SPCapture.initialize();   
        SPAbout.initialize();   
        SPSaver.initialize();        
    }    
}