/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spframe;

import javafx.collections.ObservableList;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

/**
 *
 * @author mcc
 */
public interface SPCustomizable {
    
    public void setDataKeeperDetails();
        
    public void useDataKeeperDetails(); 
        
    default public void applyTheme(String path){
        ObservableList<String> sheets = this.getMainPane().getStylesheets();   
        if(sheets.size()>1)
            sheets.remove(1);
        sheets.add(path);  
    }
    
    default public void setNightModeEnabled(boolean enable){
        ObservableList<String> sheets = this.getWindowScene().getStylesheets();
        if(enable){                         
            sheets.remove("rec/styles/LightTheme.css");  
            sheets.remove("rec/styles/DarkTheme.css"); 
            sheets.add("rec/styles/DarkTheme.css");         
        }else{
            sheets.remove("rec/styles/LightTheme.css");  
            sheets.remove("rec/styles/DarkTheme.css"); 
            sheets.add("rec/styles/LightTheme.css");                 
        }        
    }        
    
    public void setUIOrientation(NodeOrientation orientation);
    
    public Pane getMainPane();  
    
     public Scene getWindowScene();
}
