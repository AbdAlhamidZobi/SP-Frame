
package spframe;

import java.io.IOException;
import javafx.application.Application;
import javafx.stage.Stage;

public class SPMain extends Application {     
                   
    public static void main(String[] args){               
        launch(args);
    }
        
    @Override
    public void start(Stage primaryStage) throws IOException {    
        System.setProperty("prism.text", "t2k");
        System.setProperty("prism.lcdtext", "false");         
        SPSettings.initialize(); 
        SPShooter.initialize();
        SPPrimary.initialize();
        SPCapture.initialize();   
        SPAbout.initialize();   
        SPSaver.initialize();
    }    
}