package spframe;

import java.util.Timer;
import java.util.TimerTask;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class CustomStage extends Stage{               
    private final Rectangle2D screenDim;
    private final Pane backPane,frontPane;
    private final DoubleProperty titleBarHeight;
    private final HBox titleBar;   
    private final Button closeButton;
    private Button minButton,maxButton;
    private final Label titleLabel;   
    private double lastStageX,lastStageY,lastStageWidth,lastStageHeight;
    private double minimizeX,minimizeY,minimizeWidth,minimizeHeight;
    private final Image maxNormal,maxHover,restNormal,restHover;         
    private double startMoveX = -1, startMoveY = -1;
    private Boolean dragging = false;   
    
    private final Popup alignPopup;
    private final Rectangle alignRect;
    private final StringProperty alignTo;    
    private final BooleanProperty aligned;
    
    private InAnimation inAnime = InAnimation.NONE;
    private OutAnimation outAnime = OutAnimation.FADE;
    private final EventHandler<InputEvent> minimizeFilter;
    
    /**
         * constructs the custom window with a scene contains the passed pane
         * which height and width properties are the prefered size of the passed pane
     * @param _p
         */
    public CustomStage(Pane _p){   
       this(_p,_p.getPrefWidth(),_p.getPrefHeight());       
    } 
    
    public CustomStage(Pane _p,double width,double height){            
        this.closeButton = new Button("X");        
        this.maxButton = new Button();        
        this.minButton = new Button("_");
        this.titleLabel = new Label();
        this.titleBar = new HBox();              
        this.titleBarHeight = new SimpleDoubleProperty(34);
        this.aligned = new SimpleBooleanProperty(false);
        this.alignPopup = new Popup();
        this.alignRect = new Rectangle();   
        this.screenDim = Screen.getPrimary().getVisualBounds();
        this.restHover = new Image("/rec/icons/restore-button-hover.png");
        this.restNormal = new Image("/rec/icons/restore-button-normal.png");
        this.maxHover = new Image("/rec/icons/maximize-button-hover.png");
        this.maxNormal = new Image("/rec/icons/maximize-button-normal.png");
        this.alignTo = new SimpleStringProperty("toNone");
        this.setScene(generateCustomScene(_p,width,height));
        this.backPane = (Pane)this.getScene().getRoot().getChildrenUnmodifiable().get(0);
        this.frontPane = (Pane)this.backPane.getChildren().get(0);         
        initializeUtilitys();  
        
        this.minimizeFilter = (InputEvent event) -> {
            event.consume();
        };       
    }            
     
    private Scene generateCustomScene(Pane p ,double width,double height) {              
        boolean isShadowed = true;
        Group rg = new Group();
        Scene scene = new Scene(rg, width, height, Color.TRANSPARENT);                                  
        
        String osName = System.getProperty("os.name");           
        if( osName != null && osName.startsWith("Windows"))  {            
            isShadowed = false;            
            initStyle(StageStyle.TRANSPARENT);          
        }else
            initStyle(StageStyle.UNDECORATED);                           
              
        this.onTitlebarHeightChanged(34, 34);
        double tHeight = this.getTitlebarHeight();
        Pane pane = new Pane();   
        pane.setPrefSize(width, height +tHeight);          
        p.setTranslateY(tHeight);
        pane.getChildren().add(0,p);         
        pane.setId("background"); 
        this.setCustomWidth(width);
        this.setCustomHeight(height + tHeight); 
        this.titleBar.setPrefWidth(getCustomWidth());
        this.titleBar.setPrefHeight(tHeight);
        if(isShadowed == false){
            pane.setEffect(new DropShadow());             
            pane.setTranslateX(5);pane.setTranslateY(5);
            this.titleBar.setTranslateX(5);this.titleBar.setTranslateY(5);                      
        }       
        rg.getChildren().add(pane);  
        rg.getChildren().add(this.titleBar);  
        return scene;
    }   
    
    private class WindowRelocater {   
        /**
         * A Class describing some functions for relocating this custom stage that have been passed to the 
         * class "WindowUtility"
         */       
        public WindowRelocater(){    
            titleBar.setOnDragDetected(value ->{ 
                if(value.isPrimaryButtonDown()){                   
                    this.startMoveWindow(value);
                }
            });        
            titleBar.setOnMouseDragged(value ->{
                if(value.isPrimaryButtonDown()){    
                        this.moveWindow(value);
                    }
            });   
            titleBar.setOnMouseReleased(value ->{
                if(value.getButton() == MouseButton.PRIMARY){
                    this.endMoveWindow(value);
                }            
            });                
        }

        private void startMoveWindow(MouseEvent event){    
            if(!isMaximized() && !aligned.getValue()){
                lastStageX = getCustomX();
                lastStageY = getCustomY();
                lastStageWidth = getCustomWidth();
                lastStageHeight = getCustomHeight();
            }
            startMoveX = getX() - event.getScreenX();
            startMoveY = getY() - event.getScreenY();
            dragging = true;       
        }

        private void moveWindow(MouseEvent evt) {
            if (dragging) {           
              double mouseX = evt.getScreenX();
              double mouseY = evt.getScreenY();   
              double newX = mouseX + startMoveX;
              double newY = mouseY + startMoveY; 
              double screenMaxX = screenDim.getMaxX();
              double screenMaxY = screenDim.getMaxY();              
              if(newY >= screenMaxY - 40)
                  newY = screenMaxY - 40;
              
              if(isMaximized()){        
                  double restX = mouseX - (lastStageWidth/2);   
                  if(restX <= 1){
                      double toAdd = (lastStageWidth/2) - mouseX ;
                      restX += toAdd;                      
                  }else if(mouseX + (lastStageWidth/2)>= screenMaxX){
                      double toRemove = (mouseX + (lastStageWidth/2))- screenMaxX;
                      restX = (mouseX - (lastStageWidth/2))- toRemove;
                  }   
                  restoreWindow(restX,newY,lastStageWidth,lastStageHeight);                                                                                           
                  startMoveX =  getCustomX() - evt.getScreenX();                   
                }else if(!isMaximized() && aligned.getValue() && mouseY >= 50) { 
                  double restX = mouseX - (lastStageWidth/2);   
                  if(restX <= 1){
                      double toAdd = (lastStageWidth/2) - mouseX ;
                      restX += toAdd;                      
                  }else if(mouseX + (lastStageWidth/2)>= screenMaxX){
                      double toRemove = (mouseX + (lastStageWidth/2))- screenMaxX;
                      restX = (mouseX - (lastStageWidth/2))- toRemove;
                  }   
                  setCustomAlignedTo("toNone");
                  aligned.setValue(false);
                  restoreWindow(restX,newY,lastStageWidth,lastStageHeight);                                                                                           
                  startMoveX =  getCustomX() - evt.getScreenX(); 
                }else if(!isMaximized() && aligned.getValue() && mouseY < 50) { 
                    setCustomX(newX);                   
                }else if(!isMaximized() && !aligned.getValue() ){                               
                    if(mouseX >= screenMaxX-1){   
                        alignTo.set("toRight");                                    
                    }else if(mouseX <= 1){                    
                        alignTo.set("toLeft");                           
                    }else if(mouseY <= 1){
                        alignTo.set("toMaximize");                                                      
                    }
                    //use more space to cancel 
                    else if( mouseX >= 25 && "toLeft".equals(getCustomAlignedTo())) { 
                        alignTo.set("toNone");                  
                    }else if(mouseX <= screenMaxX-26 && "toRight".equals(getCustomAlignedTo())){ 
                        alignTo.set("toNone");                  
                    }else if( mouseY >= 25 && ("toMaximize".equals(getCustomAlignedTo())
                            ||"toNorth".equals(getCustomAlignedTo())
                            ||"toSouth".equals(getCustomAlignedTo()))){ 
                        alignTo.set("toNone");                  
                    } 
                    setCustomX(newX);setCustomY(newY);  
              }
            }
        }

        private void endMoveWindow(MouseEvent evt) {
            if (dragging){    
                if(!"toNone".equals(alignTo.get()) && !aligned.getValue())               
                    alignWindow();
                resetMoveOperation();          
            }
        }
        
        private void resetMoveOperation() {
            startMoveX = 0;
            startMoveY = 0;
            dragging = false;    
            if(alignPopup.isShowing())
                alignPopup.hide();            
        }
}
    
    private class WindowResizer {                
        private double startMoveX;
        private double startMoveY;  
        private final StringProperty resizeType;             
        
        public WindowResizer(){                   
            this.resizeType = new SimpleStringProperty();              
            syncBoxParentSize();
            this.initMaximize();
            this.initResize();        
        }

        private void initMaximize(){
            maxButton.setOnAction(value ->{
                if(!isMaximized()){
                    maximizeWindow();                
                }else if(isMaximized()){
                    restoreWindow(lastStageX,lastStageY,lastStageWidth,lastStageHeight);
                }          
            });
            ImageView btnIcon = (ImageView)maxButton.getGraphic();
            if(!isMaximized()) 
                btnIcon.setImage(maxNormal);      
            else
                btnIcon.setImage(restNormal);    

            maxButton.setOnMouseEntered(value ->{
                if(!isMaximized())
                    btnIcon.setImage(maxHover);      
                else
                    btnIcon.setImage(restHover);                  
            });
            maxButton.setOnMouseExited(value ->{
                if(!isMaximized())
                    btnIcon.setImage(maxNormal);      
                else
                    btnIcon.setImage(restNormal);                  
            });   
        }

        private void initResize(){              
            backPane.setOnMouseMoved(value ->{
                if(isResizable() && !isMaximized()){                     
                    double stageX = getX(); double stageY = getY();
                    double stageMaxX = stageX + getWidth();        
                    double stageMaxY = stageY + getHeight();
                    double mouseX = value.getScreenX();
                    double mouseY = value.getScreenY();

                    if(mouseX <= (stageX+6) && mouseX > (stageX-6)){   
                        this.startMoveX = getX() - value.getScreenX(); 
                        backPane.setCursor(Cursor.E_RESIZE);
                        setResizeType("east");                         
                    }else if(mouseX <= (stageMaxX+6) && mouseX >= (stageMaxX-6)){
                        backPane.setCursor(Cursor.W_RESIZE);
                        setResizeType("west");                        
                    }else if(mouseY <= (stageY+6) && mouseY > (stageY-6)){
                        this.startMoveY = getY() - value.getScreenY();
                        backPane.setCursor(Cursor.N_RESIZE);  
                        setResizeType("north");                        
                    }else if(mouseY <= (stageMaxY+6) && mouseY >= (stageMaxY-6)){
                        backPane.setCursor(Cursor.S_RESIZE);
                        setResizeType("south");    
                    }
                    else{                        
                        backPane.setCursor(Cursor.DEFAULT);
                        setResizeType("none");
                    }    
                }else if(isMaximized()){                    
                    backPane.setCursor(Cursor.DEFAULT);
                    setResizeType("none");          
                }
            });
            
            this.resizeType.addListener((ObservableValue<? extends String> observable,
                                        String oldValue, String newValue) -> {                   
                switch (newValue) {
                    case "east":
                    case "west":    
                        frontPane.setMouseTransparent(true);
                        resizeEastWest();
                        break;
                    case "north":  
                    case "south":         
                        frontPane.setMouseTransparent(true);
                        resizeNorthSouth();
                    case "none" : default:                    
                        frontPane.setMouseTransparent(false); 
                        break;                    
                }
            }); 
            backPane.setOnMouseReleased(value->{
                if(value.getButton() != MouseButton.PRIMARY || "none".equals(getResizeType())
                        || "toNone".equals(getCustomAlignedTo()) || aligned.getValue())
                    return;
                alignWindow();
            });
            
            backPane.setOnDragDetected(value ->{
                if(!aligned.getValue() &&value.getButton() == MouseButton.PRIMARY && !"none".equals(getResizeType())){                    
                    lastStageX = getCustomX();
                    lastStageY = getCustomY();
                    lastStageWidth = getCustomWidth();
                    lastStageHeight = getCustomHeight();
                }
            });
        }

        private void resizeEastWest(){        
            final double stageStartX = getX();
            final double stageEndX = getX()+getWidth();            
            backPane.setOnMouseDragged(value ->{
                if(value.getButton() == MouseButton.PRIMARY && 
                        (getResizeType().equals("west") || getResizeType().equals("east"))){                      
                    
                    double mouseX = value.getScreenX();
                    double newX = 0,newWidth =0;
                    if("east".equals(getResizeType())){                        
                        newX = (mouseX + this.startMoveX);                
                        newWidth = stageEndX - newX;  
                        
                        if(newWidth>= screenDim.getWidth())                         
                            newX = stageEndX-getWidth();                          
                        if(newWidth<= getMinWidth())                            
                            newX = stageEndX-getWidth();                         
                        if(newWidth>=getMaxWidth())                          
                            newX = stageEndX-getWidth();                          
                    }                    
                    if("west".equals(getResizeType())){                       
                        newX = getCustomX();
                        newWidth = mouseX - stageStartX; 
                    }
                                        
                    if(newWidth>= screenDim.getWidth())
                        newWidth = screenDim.getWidth();  
                    if(newWidth<=getMinWidth())
                        newWidth = getMinWidth();   
                    if(newWidth>=getMaxWidth())
                        newWidth = getMaxWidth();                    
                          
                    setCustomX(newX);
                    setCustomWidth(newWidth); 
                }   
            }); 
        }
        
        private void resizeNorthSouth(){            
            double stageEndY = getY()+getHeight();
            double stageStartY = getCustomY();
            final double screenHeight = screenDim.getHeight(); 
            backPane.setOnMouseDragged(value ->{
                if(value.getButton() == MouseButton.PRIMARY && 
                        (getResizeType().equals("north") || getResizeType().equals("south"))){                      
                    double mouseY = value.getScreenY(); 
                    double newY = 0,newHeight = 0;                   
                    double windowX = getCustomX(),windowY = 0; 
                    double maxHeight= getMaxHeight();                     
                    double windowWidth = getCustomWidth(),windowHeight = 0; 
                    
                    if(getResizeType().equals("north")){
                        newY = (mouseY + this.startMoveY);                
                        newHeight = stageEndY - newY;  
                        if(aligned.getValue()){       
                            double lastBottomY = lastStageY + lastStageHeight;                            
                            windowY = mouseY;                            
                            windowHeight = (lastBottomY-mouseY > maxHeight)? 100: lastBottomY-mouseY;                                  
                        }  

                        if(mouseY <= 0){
                            alignTo.set("toNorth");
                        }else if(mouseY >= 15 && "toNorth".equals(getCustomAlignedTo()))
                            setCustomAlignedTo("toNone");

                        if(newHeight>= screenHeight)                            
                            newY = stageEndY-getHeight(); 
                        if(newHeight<=getMinHeight())                            
                            newY = stageEndY-getHeight();
                        if(newHeight>=getMaxHeight())                            
                            newY = stageEndY-getHeight();             
                    }                                        
                    if(getResizeType().equals("south")){
                        newY = getCustomY();
                        newHeight = mouseY - stageStartY; 
                        if(aligned.getValue()){   
                            windowHeight = (maxHeight < mouseY - lastStageY)? maxHeight :mouseY - lastStageY;                                                                            
                            windowY = (windowHeight < mouseY - lastStageY)? (mouseY - maxHeight): lastStageY;                            
                        }  
                        if(mouseY >= screenDim.getMaxY()){
                            setCustomAlignedTo("toSouth");
                        }else if(mouseY <= screenDim.getMaxY()-15 && "toSouth".equals(getCustomAlignedTo()))
                            setCustomAlignedTo("toNone");                                                             
                    }
                    
                    if(aligned.getValue()){   
                        setCustomAlignedTo("toNone");
                        aligned.setValue(false);
                        newHeight = windowHeight;
                        restoreWindow(windowX , windowY, windowWidth, windowHeight);
                    }                    
                    if(newHeight >= screenHeight)
                        newHeight = screenHeight;                                             
                    if(newHeight<=getMinHeight())
                        newHeight = getMinHeight();                                         
                    if(newHeight>=getMaxHeight())
                        newHeight = getMaxHeight();                                
                    
                    setCustomY(newY);
                    setCustomHeight(newHeight); 
                }
            }); 
        }
           
        private void setResizeType(String type){
            this.resizeType.setValue(type);
        }        
        private String getResizeType(){
            return this.resizeType.getValue();
        }
        private StringProperty resizeTypeProperty(){
            return this.resizeType;
        }  
    }     
    
    private void maximizeWindow(double x,double y,double width,double height){   
        if(this.isMaximized() || this.isResizable() == false)
            return;        
            lastStageHeight = height;lastStageWidth = width;
            lastStageX = x;lastStageY = y;       
            this.setMaximized(true);
            double stageX = 0; double stageY = 0;
            double stageWidth = screenDim.getWidth();
            double stageHeight = screenDim.getHeight();   
            
            if(stageWidth > getMaxWidth())
                stageWidth = getMaxWidth();             
            if(stageHeight > getMaxHeight())
                stageHeight = getMaxHeight();
            
            this.setCustomWidth(stageWidth);
            this.setCustomHeight(stageHeight);            
            this.setCustomX(stageX);
            this.setCustomY(stageY);                

            ImageView btnIcon = (ImageView)this.maxButton.getGraphic();                
            if(this.maxButton.isHover())
                btnIcon.setImage(this.restHover);
            else
                btnIcon.setImage(this.restNormal);        
        }
    
    public void maximizeWindow(){         
        this.maximizeWindow(this.getCustomX(), 
                this.getCustomY(),
                this.getCustomWidth(),  
                this.getCustomHeight());
    }
        
    public void restoreWindow(double x,double y,double width,double height){                                          
            this.setCustomWidth(width);this.setCustomHeight(height);            
            this.setCustomX(x);this.setCustomY(y);
            this.setMaximized(false);     
            ImageView btnIcon = (ImageView)this.maxButton.getGraphic();                
            this.syncBoxParentSize();
            if(this.maxButton.isHover())
                btnIcon.setImage(this.maxHover);
            else
                btnIcon.setImage(this.maxNormal);            
        }    
    
    public void restoreWindow(){
        this.restoreWindow(lastStageX,lastStageY,
                        lastStageWidth,lastStageHeight);
    }
    
    public void minimizeWindow(){  
        double screenMaxY = this.screenDim.getMaxY();        
        this.minimizeX = getCustomX();
        this.minimizeY = getCustomY();  
        if(isMaximized()){
            minimizeWidth = getCustomWidth();
            minimizeHeight = getCustomHeight();
        }
        Parent node = getScene().getRoot();
        Animation anime = new FadeTransition();
        
        switch(outAnime){
            case NONE:
                setIconified(true);                
                break;
            case FADE:                
                anime = new FadeTransition(Duration.millis(outAnime.getDuration()),node);
                ((FadeTransition)anime).setFromValue(1.0);
                ((FadeTransition)anime).setToValue(0.0);
                anime.setCycleCount(0);
                anime.play();                               
                break;    
            case FLYOUT:
                Timer timer = new java.util.Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                         Platform.runLater(() -> {
                             if(getCustomY() >= screenMaxY || isIconified()){                         
                                 this.cancel();
                                 timer.cancel();   
                                 setIconified(true);                          
                             }
                             setCustomY(getCustomY()+10);                     
                         });
                    }
                }, 0, 10); 
        }      
        anime.setOnFinished(value -> {
            node.setOpacity(1);
            setIconified(true);  
       });
        
    }           
            
    public void deminimizeWindowWindow(){     
        this.setIconified(false);        
        Parent node = getScene().getRoot();
        Animation anime = new FadeTransition();        
        switch(getInAnimation()){
            case NONE:              
                node.setOpacity(1);
                setIconified(false);
                setCustomX(minimizeX);
                setCustomY(minimizeY);   
                if (isMaximized()){
                    setCustomWidth(minimizeWidth);
                    setCustomHeight(minimizeHeight);
                }
                break;
            case FADE:    
                setCustomX(minimizeX);
                setCustomY(minimizeY);   
                if (isMaximized()){
                    setCustomWidth(minimizeWidth);
                    setCustomHeight(minimizeHeight);
                }
                anime = new FadeTransition(Duration.millis(inAnime.getDuration()),node);
                ((FadeTransition)anime).setFromValue(0.5);
                ((FadeTransition)anime).setToValue(1.0);
                anime.setCycleCount(0);
                anime.play();
                break;   
            case FLYIN:
                setCustomY(screenDim.getMaxY());
                Timer timer = new java.util.Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                         Platform.runLater(() -> {                     
                             if(getCustomY() <= minimizeY+10){                         
                                 this.cancel();
                                 timer.cancel();                         
                             }
                             setCustomY(getCustomY()-10);
                         });
                    }
                }, 0, 10);
        }   
    }
            
    private void showWidnowAligner(boolean show){
        if(!isResizable())
                return;
        if(show && !this.alignPopup.isShowing()) 
            this.alignPopup.show(this);                
        else if(!show)                    
           this.alignPopup.hide();
    }               
    
    private void alignWindow(){        
        String type = getCustomAlignedTo();
        boolean isAligned = true;
        if(type.equals("toMaximize")){
            this.maximizeWindow();
            isAligned = false;            
        }else if(!type.equals("toNone")){            
            double windowX = this.alignPopup.getX();
            double windowY = this.alignPopup.getY()+1;
            double windowWidth = this.alignPopup.getWidth();
            double windowHeight = this.alignPopup.getHeight();      
            this.setCustomHeight(windowHeight);this.setCustomWidth(windowWidth);
            this.setCustomX(windowX);this.setCustomY(windowY);  
            isAligned = true;        
        }   
        if (alignPopup.isShowing())
            this.alignPopup.hide();
        this.aligned.setValue(isAligned);
    }        
    public void setCustomAlignedTo(String _alignTo){
         if(!this.isResizable() || this.isMaximized())
             return;
        this.alignTo.setValue(_alignTo);            
    }    
    public String getCustomAlignedTo(){
        if(!isResizable())
            return "toNone";
        return this.alignTo.getValue();
    }          
       
    @Override
    public void close(){  
        WindowEvent windowEvent = new WindowEvent(this,WindowEvent.WINDOW_CLOSE_REQUEST);
        this.fireEvent(windowEvent);  
        if(!windowEvent.isConsumed())
            this.hide();        
    }
    
    private void initializeUtilitys(){        
        initializeMinimizeButton();
        initializeTitlebar();
        initializeMaximizeButton();
        initializeCloseButton();
        initializeTitleLabel();
        initializeWidnowAligner();            
        this.setMinHeight(this.titleBarHeight.getValue());
        this.setMinWidth(80);
        this.getScene().getStylesheets().add("/rec/styles/CustomWindow.css");        
        
        this.addEventHandler(WindowEvent.WINDOW_SHOWING, handler ->{            
            this.syncBoxParentSize();
        });
    }         
    
    private void initializeTitlebar(){
        if(this.titleBar != null){
            // set style id for ths titlebar            
            this.titleBar.setId("title-bar");            
            this.titleBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            this.titleBar.getChildren().addAll(this.closeButton,this.maxButton,this.minButton,this.titleLabel);       
            
            titleBar.setOnMouseClicked(value ->{
                if(this.maxButton != null && value.getButton() == MouseButton.PRIMARY && value.getClickCount() == 2){  
                    if(!isMaximized() && !this.aligned.getValue())
                        maximizeWindow();                    
                    else if((isMaximized()) || (!isMaximized() && this.aligned.getValue())){
                        this.setCustomAlignedTo("toNone");
                        this.aligned.setValue(false);
                        restoreWindow(this.lastStageX,this.lastStageY,
                                this.lastStageWidth,
                                this.lastStageHeight);
                    }   
                } 
            });
            
            this.titleBarHeight.addListener((ObservableValue<? extends Number> observable,
                                                   Number oldValue, Number newValue) -> {
                this.onTitlebarHeightChanged((double)oldValue,(double)newValue);
            });            
            WindowRelocater windowRelocater = new WindowRelocater();
        }          
    } 
    
    private void initializeWidnowAligner(){
        if(this.alignPopup != null && this.alignRect != null){
            this.alignPopup.getContent().add(alignRect);                    
            this.alignRect.relocate(13,13);
            this.alignRect.setId("algin-rectangle");       
            this.alignRect.setEffect(new DropShadow());
            
            this.alignPopup.setOnShowing(value->{
                
            });            
            alignTo.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->{
                    double toReachX,toReachY,toReachWidth,toReachHeight;
                    double screenWidth = screenDim.getWidth();
                    double screenHeight = screenDim.getHeight();
                    switch (newValue) {
                        case "toRight":
                            toReachX = screenWidth/2;
                            toReachY = 0;
                            toReachWidth = screenWidth/2;
                            if (this.getMaxWidth() < toReachWidth) {
                                toReachWidth = this.getMaxWidth();
                                toReachX = screenWidth - this.getMaxWidth();
                            }
                            toReachHeight = screenHeight;
                            if (this.getMaxHeight() < toReachHeight) {
                                toReachHeight = this.getMaxHeight();
                                toReachY = 0;
                            }
                            break;
                        case "toLeft":
                            toReachX = 0;
                            toReachY = 0;
                            toReachWidth = screenWidth/2;
                            if (toReachWidth > this.getMaxWidth()) {
                                toReachWidth = this.getMaxWidth();
                            }
                            toReachHeight = screenHeight;
                            if (toReachHeight > this.getMaxHeight()) {
                                toReachHeight = this.getMaxHeight();
                            }
                            break;
                        case "toMaximize":
                            toReachX = 0;
                            toReachY = 0;
                            toReachWidth = screenWidth;
                            if (toReachWidth > this.getMaxWidth()) {
                                toReachWidth = this.getMaxWidth();
                            }
                            toReachHeight = screenHeight;
                            if (toReachHeight > this.getMaxHeight()) {
                                toReachHeight = this.getMaxHeight();
                            }
                            break;
                        case "toNorth":
                            toReachX = this.getX();
                            toReachY = 0;
                            toReachWidth = this.getWidth();
                            toReachHeight = screenHeight;
                            if (toReachHeight > this.getMaxHeight()) {
                                toReachHeight = this.getMaxHeight();
                            }
                            break;
                        case "toSouth":
                            toReachX = this.getX();
                            toReachY = (this.getMaxHeight() < screenHeight) ? screenHeight - this.getMaxHeight() : 0;
                            toReachWidth = this.getWidth();
                            toReachHeight = (this.getMaxHeight() < screenHeight) ? this.getMaxHeight() : screenHeight;
                            break;
                        case "toNone":
                            toReachX = 0;
                            toReachY = 0;
                            toReachWidth = 0;
                            toReachHeight = 0;
                            break;
                        default:
                            toReachX = 0;
                            toReachY = 0;
                            toReachWidth = 0;
                            toReachHeight = 0;
                            break;
                    }
                    this.showWidnowAligner("toNone".equals(newValue)? false : true);
                    this.alignPopup.setX(toReachX);
                    this.alignPopup.setY(toReachY);
                    this.alignRect.setWidth(toReachWidth-26);
                    this.alignRect.setHeight(toReachHeight-26);                
            });
        }                          
    }
    
    private void initializeMinimizeButton(){        
        if(this.minButton != null){             
            this.minButton.setId("min-button");
            this.minButton.setOnAction(value ->{                             
                minimizeWindow();
            }); 
            this.iconifiedProperty().addListener((ObservableValue<? extends Boolean> observable,
                                                   Boolean oldValue, Boolean newValue) -> {                
            if(!newValue){ 
                this.deminimizeWindowWindow();                 
            }
        });    
        }        
    }
    
    private void initializeCloseButton(){        
        if(this.closeButton != null){
            this.closeButton.setId("close-button");
            this.closeButton.setOnAction(value ->{
                this.close();            
            });
        }                    
    }    

    private void initializeMaximizeButton(){        
        if(this.maxButton != null){
            ImageView maxIcon = new ImageView();
            maxIcon.setFitHeight(18);maxIcon.setFitWidth(20);        
            this.maxButton.setGraphic(maxIcon);   
            this.maxButton.setId("max-button");
            new WindowResizer();
        }                    
    }      
    
    private void initializeTitleLabel(){
        if(this.titleLabel != null){           
            this.titleLabel.setMouseTransparent(true);
            this.titleLabel.setId("title-label"); 
            this.titleLabel.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            this.titleLabel.textProperty().bind(this.titleProperty());
            this.titleLabel.textProperty().addListener(listener ->{
                syncBoxParentSize();
            });
            this.getIcons().addListener((ListChangeListener.Change<? extends Image> c) -> {                
                double minWidth = 1000000;
                Image toSetImage = null;
                for(Image item : this.getIcons()){                          
                    if(item.getWidth() < minWidth && item.getWidth() > 16){                                              
                        minWidth = item.getWidth();
                        toSetImage = item;
                    }
                }      
                ImageView icon = new ImageView(toSetImage);
                icon.setFitHeight(20);icon.setFitWidth(20);
                this.titleLabel.setGraphic(icon);
            });
        }
    }   
    
    private void onTitlebarHeightChanged(double oldValue,double newValue){        
                this.titleBar.setPrefHeight(newValue);
                if(this.closeButton != null){
                    this.closeButton.setStyle("-fx-padding: "+(0.1470 * newValue)+" "+
                                            (0.2941 * newValue)+" "+
                                            (0.1470 * newValue)+" "+
                                            (0.2941 * newValue)+";");
                }
                if(this.maxButton != null){
                    this.maxButton.setStyle("-fx-padding: "+(0.1470 * newValue)+" "+
                                           (0.2058 * newValue)+" "+
                                           (0.0588 * newValue)+" "+
                                           (0.1470 * newValue)+";");                 
                }                
                if(this.minButton != null){
                    this.minButton.setStyle("-fx-padding: "+(0.1470 * newValue)+" "+
                                           (0.2941 * newValue)+" "+
                                           (0.1470 * newValue)+" "+
                                           (0.2941 * newValue)+";");
                }                
                syncBoxParentSize();
    }
    
    private void syncBoxParentSize(){
            if(this.backPane == null || this.frontPane == null)
                return;      
            double newWidth = this.getCustomWidth();
            double newHeight = this.getCustomHeight();               
            this.titleBar.setPrefWidth(newWidth+1);
            
            double tHeight = getTitlebarHeight();            
            this.frontPane.setPrefSize(newWidth, newHeight-tHeight);
            this.backPane.setPrefSize(newWidth, newHeight);      
            
            Platform.runLater(() -> {
                int lastIndx = (this.titleBar.getChildren().size() >1)? this.titleBar.getChildren().size()-1:-1;
                double lblNewX;
                if(lastIndx>=1){
                    Node lastTileBarNode = this.titleBar.getChildren().get(lastIndx-1);                    
                    double nodeMaxX = lastTileBarNode.getBoundsInParent().getMaxX();                                        
                    lblNewX = (this.titleBar.getPrefWidth() - nodeMaxX)-this.titleLabel.getWidth();                     
                }else{                       
                    lblNewX = (this.titleBar.getPrefWidth())-this.titleLabel.getWidth();                                                            
                }                    
                this.titleLabel.setTranslateX(lblNewX);
            });  
        }      
      
    public void setTitlebarorientation(NodeOrientation orie){
        this.titleBar.setNodeOrientation(orie);
        this.titleLabel.setNodeOrientation((orie.equals(NodeOrientation.LEFT_TO_RIGHT))? 
                    NodeOrientation.RIGHT_TO_LEFT: NodeOrientation.LEFT_TO_RIGHT);
    }               
               
    public void setCustomResizable(boolean resize){
        this.setResizable(resize);
        if(!resize){
            this.titleBar.getChildren().remove(this.maxButton);            
        }
    }    
    
    public void setTitlebarHeight(double h){
        if(h < 30 || h >50)
            return;
        this.titleBarHeight.setValue(h);
    }        
    public double getTitlebarHeight(){
        return this.titleBarHeight.getValue();
    }    
    
    public void setInAnimation(InAnimation anime){
        if(anime == null )
            return;
        this.inAnime = anime;
    }    
    public InAnimation getInAnimation(){
        return this.inAnime;
    }
    public void setOutAnimation(OutAnimation anime){
        if(anime == null )
            return;
        this.outAnime = anime;
    }    
    public OutAnimation getOutAnimation(){
        return this.outAnime;
    }
    
    public void setCustomX(double x){  
        String osName = System.getProperty("os.name"); 
        if( osName != null && osName.startsWith("Windows")){ 
            this.setX(x-5);
        }else
            this.setX(x);
    }
    public void setCustomY(double y){
        String osName = System.getProperty("os.name"); 
        if( osName != null && osName.startsWith("Windows")){ 
            this.setY(y-5);
        }else
            this.setY(y);
    }     
    public void setCustomWidth(double w){
        String osName = System.getProperty("os.name"); 
        double wWidth = w;
        if( osName != null && osName.startsWith("Windows"))                 
            wWidth = w+10;      
        
        this.setWidth(wWidth);                         
        syncBoxParentSize();
    }     
    public void setCustomHeight(double h){
        String osName = System.getProperty("os.name"); 
        double wHeight = h; 
        if( osName != null && osName.startsWith("Windows"))
            wHeight += 10;
        this.setHeight(wHeight);
        syncBoxParentSize();            
    }
           
    public double getCustomX(){          
        double x;
        String osName = System.getProperty("os.name"); 
        if( osName != null && osName.startsWith("Windows")){ 
            x = this.getX()+5;
        }else
            x = this.getX();
        return x;
    }
    public double getCustomY(){
            double y;
            String osName = System.getProperty("os.name"); 
            if( osName != null && osName.startsWith("Windows")){ 
                y = this.getY()+5;
            }else
                y = this.getY();
            return y;        
    }  
    public double getCustomWidth(){
            double w;
            String osName = System.getProperty("os.name"); 
            if( osName != null && osName.startsWith("Windows")){ 
                w = this.getWidth()-10;
            }else
                w = this.getWidth();
            return w;
    }     
    public double getCustomHeight(){        
            double h;
            String osName = System.getProperty("os.name"); 
            if( osName != null && osName.startsWith("Windows")){ 
                h = this.getHeight()-10;
            }else
                h = this.getHeight();
            return h;
    }  
          
    public void initWindiwOwner(Window owner){
        if(owner == null) return;        
        this.initOwner(owner);
        Modality mod = this.getModality();
        if(mod.equals(Modality.APPLICATION_MODAL))
            this.titleBar.getChildren().remove(this.minButton);           
        else if(mod.equals(Modality.WINDOW_MODAL) && this.getOwner() != null)
            this.titleBar.getChildren().remove(this.minButton);      
    }
    
    public void initWindowModality(Modality mod){
        if(mod == null) return;
        this.initModality(mod);
        if(mod.equals(Modality.APPLICATION_MODAL))
            this.titleBar.getChildren().remove(this.minButton);           
        else if(mod.equals(Modality.WINDOW_MODAL) && this.getOwner() != null)
            this.titleBar.getChildren().remove(this.minButton);  
    }
}
