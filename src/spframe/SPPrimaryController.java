package spframe;

import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class SPPrimaryController implements SPCustomizable{
    private CustomStage windowManager;
    @FXML private ResourceBundle resources;    
    @FXML private AnchorPane anchorpane,foregroundPane;
    @FXML private Pane instrcPane;
    @FXML private Label instrcLabel;
    @FXML private HBox bottomBox;
    @FXML private SplitMenuButton btnNew;
    @FXML private Button btnOption,btnAbout; 
    @FXML private CheckBox quickCheck;
    @FXML private ToggleGroup takeWayGroup;
    private ImageView takeWayIcon;  
    @FXML private ImageView backImageView;    
    
    @FXML public void initialize( ) {         
        this.windowManager = new CustomStage(this.anchorpane,312,478);  
        this.windowManager.getScene().getRoot().getChildrenUnmodifiable().get(0).setStyle("-fx-border-radius: 5 5 15 15;"
                + "                                                                        -fx-background-radius: 5 5 15 15");
        this.windowManager.getIcons().addAll(new Image("/rec/icons/transparentlogo/512.png"),
                                          new Image("/rec/icons/transparentlogo/256.png"),
                                          new Image("/rec/icons/transparentlogo/128.png"),
                                          new Image("/rec/icons/transparentlogo/64.png"),
                                          new Image("/rec/icons/transparentlogo/32.png"),
                                          new Image("/rec/icons/transparentlogo/24.png"),
                                          new Image("/rec/icons/transparentlogo/16.png"));        
        this.windowManager.setCustomResizable(false);
        this.windowManager.setInAnimation(InAnimation.FLYIN);
        this.windowManager.setOutAnimation(OutAnimation.FLYOUT);        
        this.windowManager.setTitle(this.resources.getString("PRIMARY_TITLE"));  
        this.setNightModeEnabled(SPSettings.getNightMode());
        this.applyTheme(SPSettings.getThemePath());        
        String usedOrie = this.resources.getString("NODE_ORIENTATION");        
        this.setUIOrientation(usedOrie.equals("ltr")? 
                NodeOrientation.LEFT_TO_RIGHT:NodeOrientation.RIGHT_TO_LEFT);  
        
        this.quickCheck.setSelected(SPSettings.isQuickScreenshot());        
        this.backImageView.setPreserveRatio(false);
        this.backImageView.setCache(true);this.backImageView.setSmooth(true);
        this.backImageView.setEffect(new GaussianBlur(5.8));        
        this.takeWayIcon = new ImageView(); 
        this.btnNew.setGraphic(takeWayIcon);
        this.btnNew.setGraphicTextGap(40);  
        refreshTakeWayInfo(SPSettings.getTakeWay());
        this.takeWayGroup.selectToggle((Toggle) this.btnNew.getItems().get(SPSettings.getTakeWay()));                
        initListener();
        useWindowLastPosition();
    }   
    
    private void initListener(){
        this.btnNew.setOnAction(value ->{
            takeSPFrame(true);
        });    
        this.quickCheck.selectedProperty().addListener(listener ->{
            SPSettings.setQuickScreenshot(quickCheck.isSelected());
        });
        SPShooter.enabledProperty().addListener(
                (ObservableValue<? extends Boolean> obeservable,Boolean oldValue,Boolean newValue)->{
                    this.btnOption.setDisable(newValue);                    
                    this.btnAbout.setDisable(newValue); 
                    if(SPSettings.getInstructionShow()){
                        this.instrcPane.setVisible(newValue);
                    }                    
                    if(newValue){
                        this.btnNew.setText(resources.getString("PRIMARY_CANCEL_BUTTON"));
                        this.btnNew.setGraphicTextGap(60);
                    }
                    else{
                        this.btnNew.setText(resources.getString("PRIMARY_TAKESECREENSHOT_BUTTON"));
                        this.btnNew.setGraphicTextGap(40);                        
                    }                        
        });
        
        this.takeWayGroup.selectedToggleProperty().addListener(
            (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                if(newValue != null){               
                    int way = this.btnNew.getItems().indexOf(newValue);
                    SPSettings.setTakeWay(way);
                    this.takeSPFrame(false);
                    refreshTakeWayInfo(way);
                }
        });   
        
        this.windowManager.iconifiedProperty().addListener(listener ->{
            if(windowManager.isIconified() && SPShooter.isEnabled())  
                SPShooter.cancelShooter();       
        });   
        
        this.windowManager.addEventHandler(KeyEvent.KEY_PRESSED, event ->{
            if(SPShooter.isEnabled())             
                SPShooter.cancelShooter();                       
        });
        this.windowManager.setOnCloseRequest(event ->{
            if(SPShooter.isEnabled())             
                SPShooter.cancelShooter();                       
        });   
        
        this.windowManager.xProperty().addListener(listener -> SPSettings.setPrimaryX(windowManager.getX()));
        this.windowManager.yProperty().addListener(listener -> SPSettings.setPrimaryY(windowManager.getY()));
        
        this.btnNew.setOnMouseEntered(value ->{
            this.takeWayIcon.setScaleX(1.05);
            this.takeWayIcon.setScaleY(1.05);
        });
        
        this.btnNew.setOnMouseExited(value ->{
            this.takeWayIcon.setScaleX(1.0);
            this.takeWayIcon.setScaleY(1.0);
        });
        this.btnOption.setOnMouseEntered(value ->{
            Node graph = btnOption.getGraphic();
            graph.setScaleX(1.05); graph.setScaleY(1.05);
            RotateTransition transition = new RotateTransition(Duration.millis(400),graph);
            transition.setFromAngle(0);
            transition.setToAngle(-80);
            transition.setCycleCount(0);
            transition.play();
        });
        this.btnOption.setOnMouseExited(value ->{
            Node graph = btnOption.getGraphic();
            graph.setScaleX(1.0); graph.setScaleY(1.0);
            RotateTransition transition = new RotateTransition(Duration.millis(100),graph);
            transition.setFromAngle(-20);
            transition.setToAngle(0);
            transition.setCycleCount(0);
            transition.play();
        });
        
        this.backImageView.setOnMouseEntered(value ->{              
            backImageView.setEffect(new GaussianBlur(0.5));
        });
        this.backImageView.setOnMouseExited(value ->{       
            Timeline anime = new Timeline();     
            anime.setCycleCount(0);
            anime.setAutoReverse(false);
            for(double i = 0; i<=5.8; i+=0.8){
                KeyValue val = new KeyValue(backImageView.effectProperty(),new GaussianBlur(i));
                KeyFrame frame = new KeyFrame(Duration.millis(i*20),val);
                anime.getKeyFrames().add(frame);  
            }
            anime.play();
        });
    }
        
    protected void takeSPFrame(boolean cancel){          
        if(!SPShooter.isEnabled()){            
            int way = SPSettings.getTakeWay();
            this.windowManager.hide();            
            SPShooter.showShooter(way);
            SPShooter.setShooterType(way);
            if(way != 2){                
                this.windowManager.show();                
            }            
        }
        else if(SPShooter.isEnabled() && !cancel)
            SPShooter.setShooterType(SPSettings.getTakeWay());        
        else if(SPShooter.isEnabled() && cancel)
            SPShooter.cancelShooter();
    } 
    
    private void refreshTakeWayInfo(int way){                
        Image icon = null;
        switch(way){
            case 0:                     
                icon = new Image("/rec/icons/rectangluar-icon.png");
                this.instrcLabel.setText(resources.getString("INSTRUCTOR_RECTUNGULAR_MESSAGE"));
                break;
            case 1:                
                icon = new Image("/rec/icons/drawshape-icon.png");
                this.instrcLabel.setText(resources.getString("INSTRUCTOR_DRAWSHAPE_MESSAGE"));
                break;
            case 2:
                icon = new Image("/rec/icons/fullscreen-icon.png");
                break;                                
        }
        this.takeWayIcon.setImage(icon);        
    }
    
    private void refreshBackground(boolean nighMode,int theme){        
        Image image = null;
        Timeline anime = new Timeline();
        anime.setAutoReverse(true);
        anime.setCycleCount(2);
        EventHandler<ActionEvent> halfZoomEvent = (ActionEvent event) -> {
            backImageView.fireEvent(new WindowEvent(null, WindowEvent.WINDOW_HIDING));
        };
        if(!nighMode){
            if(theme == 0){  
                anime.getKeyFrames().addAll(getZoomFrames(400, 250,1050,800,30,15,40,halfZoomEvent));
                anime.play();                               
                image = new Image("rec/background/orange-day.jpg");                              
            }
            else if(theme == 1){                
                anime.getKeyFrames().addAll(getZoomFrames(336, 0,1248,1080,30,13,45,halfZoomEvent));                
                anime.play();                               
                image = new Image("rec/background/green-day.jpg");    
            }                
        }
        if(nighMode){
            if(theme == 0){
                anime.getKeyFrames().addAll(getZoomFrames(0, 0,900,801,30,13,45,halfZoomEvent));
                anime.play(); 
                image = new Image("rec/background/orange-night.jpg");
            }
            else if(theme == 1){  
                anime.getKeyFrames().addAll(getZoomFrames(176, 0,976,845,30,13,45,halfZoomEvent));
                anime.play(); 
                image = new Image("rec/background/green-night.jpg");                
            }
                
        }        
        Image finalImg = image;
        backImageView.addEventHandler(WindowEvent.WINDOW_HIDING, value->{            
            backImageView.setImage(finalImg);
        });
                    
    }     
    
    private ObservableList<KeyFrame> getZoomFrames(double x,double y,double width,double height,
            double sizeStep,double timeStep,int count,EventHandler<ActionEvent> halfAction){
        ObservableList<KeyFrame> frames = FXCollections.observableArrayList();        
        for (int i = 0; i < count;i++){
            double valX = x + (i*sizeStep/2.0);
            double valY = y+(i*sizeStep/2.5);
            double valW = width-(i*sizeStep);
            double valH = height-(i*sizeStep);
            if(valW <= 0) valW = 1;
            if(valH <= 0) valH = 1;
            KeyValue value = new KeyValue(backImageView.viewportProperty(),
                                         new Rectangle2D(valX,valY,valW,valH));
            KeyFrame frame;
            frame = new KeyFrame(Duration.millis(i*timeStep), value);              
            if(i == Math.round(count /2) + 10)
                 frame = new KeyFrame(Duration.millis(i*timeStep),"midFrame",halfAction,value);
            frames.add(frame);
        }                
        return frames;
    }
    
    @FXML private void showSettings() {        
        SPSettings.getController().show();
    }
    
    @FXML private void showAboutDialog(){
        SPAbout.getController().show();
    }
    
    private void useWindowLastPosition(){
        Rectangle2D vBounds = Screen.getPrimary().getVisualBounds();
        double toSetX = SPSettings.getPrimaryX();
        double toSetY = SPSettings.getPrimaryY();
        if(toSetX >= vBounds.getMaxX()-200)
            toSetX = vBounds.getMaxX()-412;
        if(toSetX <= 0)
            toSetX = 60;
        if(toSetY >= vBounds.getMaxY() -420)
            toSetY = 80; 
        if(toSetY <= 2)
            toSetY = 2;
        
        this.windowManager.setX(toSetX);
        this.windowManager.setY(toSetY);        
    }
    
    @Override()
    public Pane getMainPane(){
        refreshBackground(SPSettings.getNightMode(),SPSettings.getTheme());
        return this.anchorpane;
    }      
    @Override
    public Scene getWindowScene(){
        refreshBackground(SPSettings.getNightMode(),SPSettings.getTheme());
        return this.windowManager.getScene();
    } 
    
    @Override
    public void setUIOrientation(NodeOrientation orientation) {
        if(orientation.equals(NodeOrientation.LEFT_TO_RIGHT))
            this.windowManager.setTitlebarorientation(NodeOrientation.RIGHT_TO_LEFT);
        else
            this.windowManager.setTitlebarorientation(NodeOrientation.LEFT_TO_RIGHT);        
        this.instrcPane.setNodeOrientation(orientation);
        this.bottomBox.setNodeOrientation(orientation);
    }
    
    @Override
    public void setDataKeeperDetails() {       
        SPDataKeeper.primaryDetails = FXCollections.observableArrayList(
                this.windowManager.isShowing());   
        this.windowManager.close();
    }

    @Override
    public void useDataKeeperDetails() {
        ObservableList details = SPDataKeeper.primaryDetails;
        boolean show = (boolean)details.get(0);        
        if(show)
            this.windowManager.show();
    }
            
    protected void show(){
        this.windowManager.show();
    }   
    
    protected boolean isShowing(){
        return this.windowManager.isShowing();
    }
    
    protected void hide(){
        this.windowManager.hide();
    }
    
    protected void toFront(){
        this.windowManager.toFront();
    }
}

