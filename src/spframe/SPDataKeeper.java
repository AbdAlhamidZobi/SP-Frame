
package spframe;

import javafx.collections.ObservableList;

/**
 * this class is used to keep some data while changing GUI language
 * @author mcc
 */
public class SPDataKeeper {
    protected static ObservableList settingsDetails = null;
    protected static ObservableList primaryDetails = null;    
    protected static ObservableList captureDetails = null; 
    
    protected static void clearDetails(){
        settingsDetails = null;
        primaryDetails = null; 
        captureDetails = null;
    }
}
