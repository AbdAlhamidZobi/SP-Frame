package spframe;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.WindowEvent;

/**
 * this class describes after-capture scene controller
 * @author mcc
 */
public class SPCaptureController implements SPCustomizable{        
    private SPDrawer drawer;
    private SPLayerManager layerManager;
    private ToolManager toolManager;
    private CustomStage windowManager;                 
    @FXML private ResourceBundle resources;
    @FXML private VBox mainPane;
    @FXML private ScrollPane imgScrollPane; 
    @FXML private StackPane layersPane;     
    @FXML private Pane imgViewPane; 
    @FXML private ImageView imageView;
    @FXML private HBox workshopHbox;
    
    @FXML private MenuItem cropTool,penTool,highTool,eraserTool;         
    @FXML private ToggleButton btnCrop,btnPen,btnHighlight,btnEraser;    
    @FXML private ToggleGroup btnBarGroup;    
    
    @FXML private TitledPane prosBox;
    @FXML private ScrollPane prosScroll;
    @FXML private GridPane prosGrid;
    @FXML private Label prosLabel;
    @FXML private ColorPicker colorBox; 
    @FXML private Label colorLabel;
    @FXML private ComboBox<Double> sizeBox;    
    @FXML private Pane previewPane;
        
//    @FXML private TitledPane layerBox;
    @FXML private VBox layerList;
    @FXML private HBox layerControl;
                  
    private final BooleanProperty frameChanged;
    private Alert saveDialog;
      
    public SPCaptureController(){      
        this.frameChanged = new SimpleBooleanProperty(true);
    }
            
    @FXML public void initialize() {  
        this.windowManager = new CustomStage(mainPane);
        windowManager.setTitle(this.resources.getString("CAPTURE_TITLE"));
        windowManager.setMinWidth(524.0); 
        windowManager.setMinHeight(240.0);//w: 524.0 h: 456.0
        windowManager.setOutAnimation(OutAnimation.NONE);   
        this.windowManager.getIcons().addAll(new Image("/rec/icons/transparentlogo/512.png"),
                                          new Image("/rec/icons/transparentlogo/256.png"),
                                          new Image("/rec/icons/transparentlogo/128.png"),
                                          new Image("/rec/icons/transparentlogo/64.png"),
                                          new Image("/rec/icons/transparentlogo/32.png"),
                                          new Image("/rec/icons/transparentlogo/24.png"),
                                          new Image("/rec/icons/transparentlogo/16.png")); 
        this.applyTheme(SPSettings.getThemePath());
        this.setNightModeEnabled(SPSettings.getNightMode());
        this.initDialog();
        this.initListeners();
        
        this.toolManager = new ToolManager();
        this.layerManager = new SPLayerManager(imgViewPane,layerControl.getChildren(),
                layerList,this.resources.getString("CAPTURE_LAYER_PREFIX"));
        this.drawer = new SPDrawer(layerManager,toolManager);      
        
        String usedOrie = this.resources.getString("NODE_ORIENTATION");
        this.setUIOrientation(usedOrie.equals("ltr")? 
                NodeOrientation.LEFT_TO_RIGHT:NodeOrientation.RIGHT_TO_LEFT);  
                                         
        this.toolManager.setSelectedTool("pen");            
    }
    
    private void initListeners(){   
        GridPane grid = new GridPane();
        this.layersPane.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        layersPane.minWidthProperty().bind(Bindings.createDoubleBinding(() -> 
            imgScrollPane.getViewportBounds().getWidth(), imgScrollPane.viewportBoundsProperty()));
        layersPane.minHeightProperty().bind(Bindings.createDoubleBinding(() -> 
            imgScrollPane.getViewportBounds().getHeight(), imgScrollPane.viewportBoundsProperty()));            
        grid.getChildren().add(layersPane);      
                        
        this.btnBarGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable,
                Toggle oldValue, Toggle newValue) -> {               
            if(newValue == btnCrop)
                this.toolManager.setSelectedTool("crop");
            else if(newValue == btnPen)
                this.toolManager.setSelectedTool("pen");
            else if (newValue == btnHighlight)
                this.toolManager.setSelectedTool("highlighter");            
            else if (newValue == btnEraser)
                this.toolManager.setSelectedTool("eraser");
            else if (newValue == null){              
                this.toolManager.setSelectedTool("none");                  
            }
        });     
                
        this.cropTool.setOnAction(value -> this.btnBarGroup.selectToggle(this.btnCrop));
        this.penTool.setOnAction(value -> this.btnBarGroup.selectToggle(this.btnPen));
        this.highTool.setOnAction(value -> this.btnBarGroup.selectToggle(this.btnHighlight));
        this.eraserTool.setOnAction(value -> this.btnBarGroup.selectToggle(this.btnEraser));          
        
        this.workshopHbox.widthProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    this.imgScrollPane.setPrefWidth((double)newValue-200);
        });
    }
     
    private void initDialog(){
        ButtonType buttonYes = new ButtonType(resources.getString("YES_BUTTON"),ButtonBar.ButtonData.YES);
        ButtonType buttonNo = new ButtonType(resources.getString("NO_BUTTON"),ButtonBar.ButtonData.NO);
        ButtonType buttonCancel = new ButtonType(resources.getString("CANCEL_BUTTON"),ButtonBar.ButtonData.CANCEL_CLOSE);               
         
        this.saveDialog = createAlertWithOptOut(AlertType.CONFIRMATION, resources.getString("CAPTURE_DIALOG_TITLE"),
                resources.getString("CAPTURE_DIALOG_HEADER"),resources.getString("CAPTURE_DIALOG_CONTEXT"),
                        resources.getString("CAPTURE_DIALOG_CHECK"), 
                        param -> SPSettings.setSaveDialogShow(!param),
                        buttonYes,buttonNo,buttonCancel);     
        GridPane pane = (GridPane)this.saveDialog.getDialogPane().getChildren().get(0);        
        pane.setPrefHeight(50);    
                        
        this.windowManager.setOnCloseRequest(value ->{  
            
            String data = (String) windowManager.getUserData();
            if(data != null && data.equals("language")){                
                windowManager.setUserData(null);
                return;
            }
            
            if(!this.windowManager.isShowing() ||
               SPSettings.getSaveDialogShow() == false || 
               this.getFrameChanged() == false)
                return;         
            
            Optional<ButtonType> response = this.saveDialog.showAndWait();            
            if(response.get() == buttonYes)
                this.showSaveChooser();     
            else if(response.get() == buttonCancel)
                value.consume();
        });
    }
    
    public void setCapturedImage(Image img,boolean layout){
        this.layerManager.restLayers();
        this.imageView.setImage(img);
        if(layout)
            this.layoutNodes(img.getWidth(), img.getHeight());
        this.centerImage();
        this.setFrameChanged(true);  
        windowManager.centerOnScreen();
    }
          
    private void layoutNodes(double width, double height){
        Rectangle2D vBounds = Screen.getPrimary().getVisualBounds();
        double wMaxWidth;double wWidth;
        double wMaxHeight = vBounds.getHeight();
        double wHeight; 
        // if the image is normal 
        if(width >= windowManager.getMinWidth()){
            wMaxWidth = vBounds.getWidth()+200;
            wWidth = width+250;
            if(wWidth > vBounds.getWidth())    
                wWidth = vBounds.getWidth();
            
        }else{//if the image is too small 
             wMaxWidth = windowManager.getMinWidth()+350;
             wWidth = windowManager.getMinWidth()+250;
        }
        
        if(height >= windowManager.getMinHeight()){
            //wMaxHeight = vBounds.getHeight()+200;
            wHeight = height+150;
            if(wHeight > vBounds.getHeight())
                wHeight = vBounds.getHeight();
        }else{    
            //wMaxHeight = windowManager.getMinHeight()+350;
            wHeight = windowManager.getMinHeight()+300;      
        }
        
        windowManager.setCustomWidth(wWidth);windowManager.setCustomHeight(wHeight);    
        windowManager.setMaxWidth(wMaxWidth);windowManager.setMaxHeight(wMaxHeight);
                          
        this.imgViewPane.setPrefSize(wMaxWidth,wMaxHeight); 
        this.imageView.setFitWidth(width); 
        this.imageView.setFitHeight(height); 
                
        this.imgScrollPane.setHvalue(0.5); this.imgScrollPane.setVvalue(0.5);                      
        this.layerManager.addLayer("draw");        
    }    
    
    @Override()
    public Pane getMainPane(){
        return this.mainPane;
    }   
    @Override
    public Scene getWindowScene(){
        return this.windowManager.getScene();
    }     
    @Override
    public void setUIOrientation(NodeOrientation orientation) {
        if(orientation.equals(NodeOrientation.LEFT_TO_RIGHT))
            this.windowManager.setTitlebarorientation(NodeOrientation.RIGHT_TO_LEFT);
        else
            this.windowManager.setTitlebarorientation(NodeOrientation.LEFT_TO_RIGHT);                
        
        this.windowManager.getScene().getRoot().setNodeOrientation(orientation);
        this.saveDialog.getDialogPane().setNodeOrientation(orientation);
    }
    
    @Override
    public void setDataKeeperDetails() {
        ArrayList<Double> windowDetails = new ArrayList<>(); 
        windowDetails.add(this.windowManager.getCustomX());          // 0
        windowDetails.add(this.windowManager.getCustomY());          // 1
        windowDetails.add(this.windowManager.getCustomWidth());      // 2
        windowDetails.add(this.windowManager.getCustomHeight());     // 3
        windowDetails.add(this.windowManager.getMaxWidth());   // 4
        windowDetails.add(this.windowManager.getMaxHeight());  // 5
                
        SPDataKeeper.captureDetails = FXCollections.observableArrayList(     
                
                this.windowManager.isShowing(), // 0
                this.windowManager.isMaximized(),     // 1
                windowDetails,                        // 2
                this.imageView.getFitWidth(),         // 3
                this.imageView.getFitHeight(),        // 4
                this.imageView.getImage(),            // 5               
                
                this.layerManager.getLayers(),        // 6
                this.toolManager.getSelectedTool());  // 7
        windowManager.setUserData("language");
        this.windowManager.close();
    }
    @Override
    public void useDataKeeperDetails() {  
        ObservableList details = SPDataKeeper.captureDetails;    
        
        boolean show = (boolean)details.get(0);  
        boolean maximized = (boolean)details.get(1);   
        
        ArrayList<Double> winDetails = (ArrayList<Double>)details.get(2);   
        double winX = winDetails.get(0);
        double winY = winDetails.get(1);
        double winWidth = winDetails.get(2);
        double winHeight = winDetails.get(3);  
        double winMaxWidth = winDetails.get(4);
        double winMaxHeight = winDetails.get(5);
        
        double imgWidth =(double)details.get(3); 
        double imgHeight = (double)details.get(4);     
        Image img = (Image)details.get(5);       
        
        ObservableList<SPLayer> layers = (ObservableList<SPLayer>) details.get(6); 
        this.toolManager.setSelectedTool((String)details.get(7));
        if(img != null ){                                   
            // using the following lines to not affect the window and imgViewPane size
            // if the image has been cut before the re-initializing.
            this.setCapturedImage(img,false);
            this.windowManager.setCustomX(winX);this.windowManager.setCustomY(winY);
            this.windowManager.setCustomWidth(winWidth);this.windowManager.setCustomHeight(winHeight); 
            this.windowManager.setMaxWidth(winMaxWidth);
            this.windowManager.setMaxHeight(winMaxHeight);
            this.imageView.setFitWidth(imgWidth);
            this.imageView.setFitHeight(imgHeight);
            this.imgViewPane.setPrefSize(winMaxWidth, winMaxHeight); 
            centerImage();
        }               
        if(show)
            this.windowManager.show();    
        if(maximized)
                this.windowManager.maximizeWindow();
        if(layers != null)
            this.layerManager.setLayers(layers);
    }    
    
    private void centerImage(){
            Platform.runLater(() ->{ 
                double rectCentX = this.imgViewPane.getBoundsInParent().getWidth()/2;
                double rectCentY = this.imgViewPane.getBoundsInParent().getHeight()/2;           

                double imgX = rectCentX - this.imageView.getFitWidth()/2;
                double imgY = rectCentY - this.imageView.getFitHeight()/2;             
                this.imgScrollPane.setHvalue(0.5);
                this.imgScrollPane.setVvalue(0.5);             
                this.imageView.relocate(Math.round(imgX),Math.round(imgY));                
            });           
    }
                
    protected void setFrameChanged(boolean changed){
        frameChanged.setValue(changed);
    }
    protected boolean getFrameChanged() {
        return frameChanged.getValue();
    }  
    
    protected ToolManager getToolManager() {
        return this.toolManager;
    }
    private SPDrawer getDrawer() {
        return this.drawer;
    }
    public SPLayerManager getLayerManager() {
        return this.layerManager;
    } 
    
    @FXML private void takeNewSPFrame(){   
        if(windowManager.isMaximized())
            windowManager.restoreWindow();
        this.layerManager.restLayers();
        this.windowManager.hide();        
        if(SPSettings.getTakeWay() == 2)
            SPPrimary.getController().show();        
        else
            SPPrimary.getController().takeSPFrame(false);
    }
    
    @FXML public void copySPFrame(){        
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();                
        content.putImage(getCurrentFrame());
        clipboard.setContent(content);        
    } 
    
    @FXML private void showSaveChooser(){
        SPSaver.initChooser(getCurrentFrame(), this.windowManager,resources.getString("CAPTURE_SAVER_TITLE"));
    }
    
    @FXML private void showAboutDialog(){        
        SPAbout.getController().show();
    }
    
    @FXML private void showSettings() throws IOException{
        SPSettings.getController().show();
    }
    
    public void show(){
        this.windowManager.show();
    }
    
    @FXML private void closeCaptrue(){this.windowManager.close();}
    
    public WritableImage getCurrentFrame(){         
        ObservableList<SPLayer> layers = this.layerManager.getLayers();
        if(layers == null || layers.isEmpty())
            return null;
        double startX = imageView.getBoundsInParent().getMinX(); 
        double startY = imageView.getBoundsInParent().getMinY(); 
        double endX = imageView.getBoundsInParent().getMaxX(); 
        double endY = imageView.getBoundsInParent().getMaxY(); 
                
        for(SPLayer layer : layers){     
            Rectangle2D layerBounds = layer.getUsedBounds();
            if(layerBounds != null && layer.getSpaceOpacity() != 0.0) {        
            double layerStartX = layerBounds.getMinX();
            double layerStartY = layerBounds.getMinY(); 
            double layerEndX = layerBounds.getMaxX();
            double layerEndY = layerBounds.getMaxY();
            
            if ( layerStartX < startX) startX = layerStartX;  
            if ( layerStartY < startY) startY = layerStartY;                                                     
            if ( layerEndX >endX) endX = layerEndX;  
            if ( layerEndY > endY) endY = layerEndY; 
            }
        }    
        
        double pwidth =  endX - startX;
        double pheight = endY - startY;
        final SnapshotParameters fParameters = new SnapshotParameters();           
        Rectangle2D dimRect = new Rectangle2D(startX, startY, pwidth, pheight);        
        fParameters.setViewport(dimRect); 
        Color fill = null;
        switch(SPSettings.getImageBackground()){
            case 0:
                fill = Color.TRANSPARENT;
                break;
            case 1:
                fill = Color.WHITE;
                break;
            case 2:
                fill = Color.BLACK;
                break;
        }
        fParameters.setFill(fill);
        WritableImage returnedImg = this.imgViewPane.snapshot(fParameters, null);   
//        Popup pop = new Popup();
//        ImageView imageView1 = new ImageView(returnedImg);
//        imageView1.setFitHeight(400); imageView1.setFitWidth(600);
//        pop.getContent().add(imageView1); pop.show(this.imgViewPane,50,50);
        return returnedImg;        
    }
    
    protected class ToolManager{     
        private final Preferences toolPreferences = Preferences.userNodeForPackage(SPSettings.class);  
        private final StringProperty selectedTool; 
        // pen properties
        private final ObjectProperty<Color> penColor;
        private final DoubleProperty penSize;  
        // highlighter properties
        private final ObjectProperty<Color> highColor;
        private final DoubleProperty highSize;  
        // eraser properties        
        private final DoubleProperty eraserSize;  
        private final Double[] penSizeList = {1.0,1.5,2.0,2.5,3.0,4.0,5.0,6.0,7.0,
                                              8.0,9.0,10.0,11.0,12.0,13.0,14.0};
        private final Double[] highSizeList = {7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,
                                               15.0,16.0,17.0,18.0,19.0,20.0,21.0,22.0};
        private final Double[] eraserSizeList = {4.0,5.0,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0};
                                        
        public ToolManager(){  
            this.selectedTool = new SimpleStringProperty("none");                    
            this.penColor = new SimpleObjectProperty<>();
            this.penSize = new SimpleDoubleProperty();              
            this.highColor = new SimpleObjectProperty<>();
            this.highSize = new SimpleDoubleProperty();
            this.eraserSize = new SimpleDoubleProperty(); 
            sizeBox.getItems().addAll(this.penSizeList);    
            initBoxListener();
            initToolListener();
        }    
                
        private void initToolListener(){
            this.selectedToolProperty().addListener((ObservableValue<? extends String> observable,
                                String oldValue, String newValue) -> {   
                colorBox.setValue(null);
                boolean noProperties = false;   
                boolean isEraser = false;
                switch(newValue){
                    case "crop":        
                        btnBarGroup.selectToggle(btnCrop);
                        ImageCroper.setImagePane(imgViewPane);
                        ImageCroper.isCroperEnabled = true;   
                        prosBox.setText(resources.getString("CAPTURE_PROPERTIES_CROP"));
                        prosLabel.setText(resources.getString("CAPTURE_PROPERTIES_NO_PROPERTIES"));
                        imgViewPane.setCursor(Cursor.DEFAULT);
                        imageView.setCursor(Cursor.CROSSHAIR); 
                        drawer.setEnabled(false);     
                        noProperties = true;
                        break;
                    case "pen":         
                        btnBarGroup.selectToggle(btnPen);
                        sizeBox.getItems().setAll(penSizeList);
                        sizeBox.setValue(this.toolPreferences.getDouble("PenSize", 2.5));                          
                        colorBox.setValue(Color.valueOf(toolPreferences.get("PenColor", "Red")));                                              
                        penSize.setValue(sizeBox.getValue());
                        penColor.setValue(colorBox.getValue());
                        prosBox.setText(resources.getString("CAPTURE_PROPERTIES_PEN"));  
                        getDrawer().setEnabled(true);
                        ImageCroper.isCroperEnabled = false;                          
                        break;
                    case "highlighter":
                        btnBarGroup.selectToggle(btnHighlight);       
                        sizeBox.getItems().setAll(highSizeList);
                        sizeBox.setValue(this.toolPreferences.getDouble("HighlighterSize", 10.0));
                        colorBox.setValue(Color.valueOf(toolPreferences.get("HighlighterColor", "Yellow")));                                                   
                        highSize.setValue(sizeBox.getValue());
                        highColor.setValue(colorBox.getValue());
                        prosBox.setText(resources.getString("CAPTURE_PROPERTIES_HIGHLIGHTER"));
                        getDrawer().setEnabled(true);
                        ImageCroper.isCroperEnabled = false;                          
                        break;
                    case "eraser":                        
                        btnBarGroup.selectToggle(btnEraser);                          
                        prosBox.setText(resources.getString("CAPTURE_PROPERTIES_ERASER")); 
                        sizeBox.getItems().setAll(eraserSizeList);
                        sizeBox.setValue(this.toolPreferences.getDouble("EraserSize", 8.0));
                        eraserSize.setValue(sizeBox.getValue());                        
                        getDrawer().setEnabled(true);
                        ImageCroper.isCroperEnabled = false;  
                        isEraser = true;
                        break;                                                             
                    case "none":
                        btnBarGroup.selectToggle(null);   
                        noProperties = true;                        
                        getDrawer().setEnabled(false);
                        ImageCroper.isCroperEnabled = false;  
                        imgViewPane.setCursor(Cursor.DEFAULT);
                        imageView.setCursor(Cursor.DEFAULT);           
                        prosBox.setText("...");  
                        prosLabel.setText(resources.getString("CAPTURE_PROPERTIES_NONE_SELECTED"));
                        break;                    
                }   
                colorBox.setDisable(isEraser);
                colorLabel.setDisable(isEraser);          
                if(noProperties){
                    prosGrid.toBack();
                    prosLabel.setVisible(true);
                    prosGrid.setDisable(true);
                    prosGrid.setOpacity(0.4);                    
                }else{              
                    prosLabel.setVisible(false);                    
                    prosLabel.toBack();    
                    prosGrid.setDisable(false);
                    prosGrid.setOpacity(1.0);
                }
                refreshPreviewObject();
            });           
        }
        
        private void initBoxListener(){
            sizeBox.valueProperty().addListener( 
                (ObservableValue<? extends Number> observable, Number oldValue , Number newValue)->{
                    if(newValue != null)
                        if(getSelectedTool().equals("pen"))                                                   
                            this.setPenSize((double)newValue);
                        else if (getSelectedTool().equals("highlighter"))
                            this.setHighlighterSize((double)newValue);
                        else if(getSelectedTool().equals("eraser"))
                            this.setEraserSize((double)newValue);
                    refreshPreviewObject();                                
            });          
            colorBox.valueProperty().addListener(
                (ObservableValue<? extends Color> observable, Color oldValue, Color newValue) -> {
                    if(newValue != null)                
                        if(getSelectedTool().equals("pen"))
                            this.setPenColor(newValue);
                        else if (getSelectedTool().equals("highlighter"))
                            this.setHighlighterColor(newValue);
                    refreshPreviewObject();
            });                         
        }
                
        private void refreshPreviewObject(){
            ObservableList<Node> prevChildren = previewPane.getChildren();
            prevChildren.clear();
            boolean isCustomCursor = true;
            Shape preview = new Rectangle();
            Shape cursor = new Circle();
            cursor.setStroke(Color.BLACK);
            cursor.setStrokeWidth(1.0);
            double hotspotX = 0;double hotspotY = 0;           
            switch(getSelectedTool()){    
                case"crop":
                    isCustomCursor = false;                
                    break;
                case "pen":   
                    preview = new Path(new MoveTo(15, 25),
                                       new LineTo(155, 25));
                    preview.setStroke(getPenColor());
                    preview.setStrokeWidth(getPenSize());  

                    cursor = new Circle((getPenSize()+2)/2);                        
                    cursor.setFill(getPenColor());                                
                    hotspotX = ((Circle) cursor).getRadius();
                    hotspotY = hotspotX;
                    break;                        
                case "highlighter":
                    preview = new Path(new MoveTo(15, 25),
                                       new LineTo(155, 25));
                    preview.setStroke(getHighlighterColor());
                    preview.setStrokeWidth(getHighlighterSize());  
                    preview.setBlendMode(BlendMode.MULTIPLY);

                    cursor = new Circle((getHighlighterSize()+2)/2);
                    cursor.setFill(getHighlighterColor());                    
                    cursor.setOpacity(0.852);
                    hotspotX = ((Circle) cursor).getRadius();
                    hotspotY = hotspotX;                    
                    break;                        
                case "eraser":   
                    double eraserHW = getEraserSize()*2;
                    preview = new Rectangle(85 -eraserHW/2, 25 -eraserHW/2,eraserHW, eraserHW);                      
                    preview.setFill(Color.rgb(255, 255, 255,1.0));
                    preview.setStroke(Color.rgb(0, 0, 0,0.75)); 
                    preview.setStrokeWidth(1);

                    cursor = new Rectangle(eraserHW, eraserHW);
                    cursor.setFill(Color.rgb(255, 255, 255,0.75));
                    cursor.setStroke(Color.rgb(0, 0, 0,0.75));
                    cursor.setStrokeWidth(1);                                           
                    break;   
                case "none":
                    isCustomCursor = false;                           
                    break;
            }                        
            prevChildren.add(preview);
            if(isCustomCursor){
                SnapshotParameters cursorParams = new SnapshotParameters();
                cursorParams.setFill(Color.TRANSPARENT);
                imgViewPane.setCursor(new ImageCursor(cursor.snapshot(cursorParams, null),
                            hotspotX,hotspotY));
                imageView.setCursor(imgViewPane.getCursor());
            }
        }
        
        private void setPenSize(double size){            
            this.penSize.setValue(size);
            toolPreferences.putDouble("PenSize", size);            
        }
        
        public double getPenSize(){
            return this.penSize.getValue();
        }
        
        public DoubleProperty penSizeProperty(){
            return this.penSize;
        }
        
        private void setPenColor(Color color){                
            this.penColor.setValue(color);            
            this.toolPreferences.put("PenColor", color.toString());
        }
        
        public Color getPenColor(){
            return this.penColor.getValue();
        }
        
        public ObjectProperty penColorProperty(){
            return this.penColor;
        }
        
        private void setHighlighterSize(double size){            
            this.highSize.setValue(size);
            toolPreferences.putDouble("HighlighterSize", size);            
        }
        
        public double getHighlighterSize(){
            return this.highSize.getValue();
        }
        
        public DoubleProperty highlighterSizeProperty(){
            return this.highSize;
        }
        
        private void setHighlighterColor(Color color){            
            this.highColor.setValue(color);            
            this.toolPreferences.put("HighlighterColor", color.toString());
        }
        
        public Color getHighlighterColor(){
            return this.highColor.getValue();
        }
        
        public ObjectProperty highlighterColorProperty(){
            return this.highColor;
        }
        
        private void setEraserSize(double size){            
            this.eraserSize.setValue(size);
            toolPreferences.putDouble("EraserSize", size);            
        }
        
        public double getEraserSize(){
            return this.eraserSize.getValue();
        }
        
        public DoubleProperty eraserSizeProperty(){
            return this.eraserSize;
        }
                               
        public void setSelectedTool(String tool){
            this.selectedTool.setValue(tool);
        }
        
        public String getSelectedTool(){
            return this.selectedTool.getValue();
        }
        
        public StringProperty selectedToolProperty(){
            return this.selectedTool;
        }
    }
            
    private static final class ImageCroper{        
        static ImageView imgView;         
        static Pane imgPane;
        static Rectangle rect = new Rectangle();
        static boolean isCroperEnabled;
                
        public static void setImagePane(Pane pane){
            imgPane = pane; 
            imgView = (ImageView)imgPane.getChildren().get(0);   
            rect.setId("crop-rectangle"); 
            InitializeCroper();
        }
                
        private static void InitializeCroper() {              
            Point theGreater = new Point();
            Point theSmaller = new Point();                
                                                
            imgView.setOnMousePressed(value ->{ 
                if (value.getButton() != MouseButton.PRIMARY || isCroperEnabled == false)                                                 
                    return;                
                theGreater.setLocation(value.getX() + imgView.getLayoutX() ,value.getY() + imgView.getLayoutY());
                rect.setX(0);rect.setY(0);rect.setWidth(0);rect.setHeight(0);                    
                imgPane.getChildren().remove(rect);
                imgPane.getChildren().add(rect);
            });
                
            imgView.setOnMouseDragged(value ->{
               if (value.getButton() != MouseButton.PRIMARY ||isCroperEnabled == false)                                                 
                    return;
                theSmaller.setLocation(value.getX() + imgView.getLayoutX() ,value.getY() + imgView.getLayoutY());   
                double recStartX = Math.min(theGreater.x, theSmaller.x);
                double recStartY = Math.min(theGreater.y, theSmaller.y);
                double recWidth = (Math.max(theGreater.x, theSmaller.x)-Math.min(theGreater.x, theSmaller.x));
                double recHeight = Math.max(theGreater.y, theSmaller.y)-Math.min(theGreater.y, theSmaller.y);

                rect.setX(recStartX);rect.setY(recStartY);
                rect.setWidth(recWidth);rect.setHeight(recHeight);  
                                                  
                if(value.getX() > imgView.getFitWidth() )
                    rect.setWidth(imgView.getFitWidth() - rect.getX() + imgView.getLayoutX());
                if(value.getX() < 0){
                    rect.setX(imgView.getLayoutX());
                    rect.setWidth(value.getX()+rect.getWidth());
                }    
                if(value.getY() > imgView.getFitHeight())
                    rect.setHeight(imgView.getFitHeight() - rect.getY() + imgView.getLayoutY());
                if(value.getY() < 0){
                    rect.setY(imgView.getLayoutY());
                    rect.setHeight(value.getY()+rect.getHeight());
                }                
            });                
            imgView.setOnMouseReleased(value ->{
                if (value.getButton() == MouseButton.PRIMARY && isCroperEnabled)                 
                    cropFrame();
            });                                  
        }
        
        private static void cropFrame(){
                int recStartX = (int)Math.round(rect.getX());
                int recStartY = (int)Math.round(rect.getY());
                int recWidth = (int)Math.round(rect.getWidth());
                int recHeight = (int)Math.round(rect.getHeight());
                int recArea = (int)(recWidth * recHeight);
                
                if (recArea > 1500 ){                    
                    imgPane.getChildren().remove(rect);  
                    WritableImage image = new WritableImage(recWidth, recHeight); 
                    final SnapshotParameters fParameters = new SnapshotParameters();        
                    Rectangle2D dimRect = new Rectangle2D(recStartX, recStartY, recWidth, recHeight);        
                    fParameters.setViewport(dimRect);  
                    fParameters.setFill(Color.TRANSPARENT);
                    imgView.snapshot(fParameters, image);  
                    imgView.setFitWidth(image.getRequestedWidth());
                    imgView.setFitHeight(image.getRequestedHeight());                                                                                                   
                    SPCapture.getController().centerImage();
                    imgView.setImage(image);        
                    SPCapture.getController().setFrameChanged(true);                                                                                             
                }   
                rect.setX(0); rect.setY(0);
                rect.setWidth(0); rect.setHeight(0);  
                imgPane.getChildren().remove(rect);    
        }                    
    }   
    
    public static Alert createAlertWithOptOut(AlertType type, String title, String headerText, 
               String message, String optOutMessage, Consumer<Boolean> optOutAction, 
               ButtonType... buttonTypes) {
   Alert alert = new Alert(type);
   // Need to force the alert to layout in order to grab the graphic,
    // as we are replacing the dialog pane with a custom pane
    alert.getDialogPane().applyCss();
    Node graphic = alert.getDialogPane().getGraphic();
    // Create a new dialog pane that has a checkbox instead of the hide/show details button
    // Use the supplied callback for the action of the checkbox
    alert.setDialogPane(new DialogPane() {
      @Override
      protected Node createDetailsButton() {
        CheckBox optOut = new CheckBox();
        optOut.setText(optOutMessage);
        optOut.setOnAction(e -> optOutAction.accept(optOut.isSelected()));
        return optOut;
      }
    });
    alert.getDialogPane().getButtonTypes().addAll(buttonTypes);
    alert.getDialogPane().setContentText(message);
    // Fool the dialog into thinking there is some expandable content
    // a Group won't take up any space if it has no children
    alert.getDialogPane().setExpandableContent(new Group());
    alert.getDialogPane().setExpanded(true);
    // Reset the dialog graphic using the default style
    alert.getDialogPane().setGraphic(graphic);
    alert.setTitle(title);
    alert.setHeaderText(headerText);
    return alert;
}
}
