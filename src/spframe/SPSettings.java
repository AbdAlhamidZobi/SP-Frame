package spframe;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javafx.fxml.FXMLLoader;
import javafx.stage.Screen;

public class SPSettings {
    private static SPSettingsController controller;
    private static final Preferences PREFS = Preferences.userNodeForPackage(SPSettings.class);
    private static final String ORANGEPATH = "rec/styles/OrangeStyle.css";          
    private static final String GREENPATH = "rec/styles/GreenStyle.css";
        
    protected static void initialize() throws IOException{  
        Locale toUseLocale = new Locale.Builder().setLanguage(getLanguageString()).setScript("Arab").build();          
        initialize(toUseLocale,getThemePath());
    } 
    
    protected static void initialize(Locale toUseLocale,String theme) throws IOException{   
        boolean useDataKeeper = false;
        if(controller!= null)
            useDataKeeper = true;     
        FXMLLoader FXLoader = new FXMLLoader(SPSettings.class.getResource("/rec/fxml/FXMLSettings.fxml"));                 
        FXLoader.setResources(ResourceBundle.getBundle("rec.language.spframe",toUseLocale));  
        FXLoader.load(); 
        controller = FXLoader.getController();  
        controller.applyTheme(theme);
        if(useDataKeeper)
            controller.useDataKeeperDetails();
    } 
                 
    protected static SPSettingsController getController(){
        return controller;
    }
    
    // app settings
    protected static void setLanguage(int languageIndex){
        PREFS.putInt("Language", languageIndex);  
    }  
    protected static int getLanguage(){
        return PREFS.getInt("Language", 0);
    }       
    protected static String getLanguageString(){
        return (getLanguage() == 0)? "en":"ar";       
    }
        
    protected static void setTheme(int themeIndex){
        PREFS.putInt("UITheme", themeIndex);     
    }  
    protected static int getTheme(){
        return PREFS.getInt("UITheme", 0);
    }   
    protected static String getThemePath(){
        return (getTheme() == 0)? ORANGEPATH:GREENPATH;  
    }
    
    protected static void setNightMode(boolean mode){
        PREFS.putBoolean("NightMode", mode);     
    } 
    protected static boolean getNightMode(){
        return PREFS.getBoolean("NightMode", false);
    } 
           
    protected static void setSaveDialogShow(boolean show){
        PREFS.putBoolean("ShowSaveDialog", show);  
        controller.refreshSettings();
    } 
    protected static boolean getSaveDialogShow(){
        return PREFS.getBoolean("ShowSaveDialog", true);
    }  
    
    protected static void setInstructionShow(boolean show){
        PREFS.putBoolean("InstructionTextDialog", show);          
    } 
    protected static boolean getInstructionShow(){
        return PREFS.getBoolean("InstructionTextDialog", true);
    }  
    
    //screenshot settings
    protected static void setImageBackground(int index){
        PREFS.putInt("ImageBackground", index);
    }
    protected static int getImageBackground(){
        return PREFS.getInt("ImageBackground", 0);
    }
    
    protected static void setQuickDialogShow(boolean show){
        PREFS.putBoolean("QuickDoneDialog", show);                
    }
    protected static boolean getQuickDialogShow(){
        return PREFS.getBoolean("QuickDoneDialog", true);
    }
    
    protected static void setWhenQuickDone(int index){
        PREFS.putInt("WhenQuickDone", index);
    }
    protected static int getWhenQuickDone(){
        return  PREFS.getInt("WhenQuickDone", 0);
    }
    
    protected static void setQuickPathType(int type){
        PREFS.putInt("QuickPath", type);        
    }
    protected static int getQuickPathType(){
        return PREFS.getInt("QuickPath", 0);
    }
    
    //non-settings window settings
    protected static void setTakeWay(int indx){
        PREFS.putInt("TakeScreenshotWay", indx);          
    } 
    protected static int getTakeWay(){
        return PREFS.getInt("TakeScreenshotWay", 0);
    }  
    
    protected static void setPrimaryX(double x){
        PREFS.putDouble("PrimaryX", x);          
    } 
    protected static double getPrimaryX(){
        return PREFS.getDouble("PrimaryX", Screen.getPrimary().getVisualBounds().getMaxX()-412);
    }  
    protected static void setPrimaryY(double y){
        PREFS.putDouble("PrimaryY", y);          
    } 
    protected static double getPrimaryY(){
        return PREFS.getDouble("PrimaryY", 80);
    }  
    
    
    protected static void setQuickScreenshot(boolean enable){
        PREFS.putBoolean("QuickScreenshot", enable);
    }
    protected static boolean getQuickScreenshot(){
        return PREFS.getBoolean("QuickScreenshot", false);
    }
    
    /*
    ** saver settings
    */    
    protected static void setSavePath(String path){
        if(path == null)
            path = System.getProperty("user.home");        
        PREFS.put("SavePath",path);        
    }    
    protected static String getSavePath(){
        return PREFS.get("SavePath",System.getProperty("user.home"));
    }   
    
    protected static void setSpecificQuickPath(String path){ 
        if(path == null)
            path = System.getProperty("user.home");  
        PREFS.put("SpecificQuickPath", path);                
    }
    protected static String getSpecificQuickPath(){
        return PREFS.get("SpecificQuickPath",System.getProperty("user.home"));
    }
    
    protected static int getSaveExtensionIndex(){        
        return PREFS.getInt("SaveExtension",0);
    }    
    protected static void setSaveExtensionIndex(int indx){               
        PREFS.putInt("SaveExtension",indx);
    }  
}
