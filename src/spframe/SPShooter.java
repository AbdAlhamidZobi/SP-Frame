package spframe;

import java.awt.Point;
import java.awt.Robot;
import java.awt.image.BufferedImage;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import static spframe.SPSettings.getLanguageString;

/**
 * this class shoots some balls
 * this is the main class which handles the taking and cropping it provides the three taking ways 
 * and also the cropping function as static methods and fields
 * @author mcc
 */
public class SPShooter {
    
    private static Rectangle2D screenDim;
    private static Stage stage;
    private static ImageView imageView;
    private static Rectangle shooterRect;
    private static Path shooterPath;
    private static BooleanProperty enabled;
    private static IntegerProperty shooterType;
            
    protected static void initialize(){
        screenDim = Screen.getPrimary().getBounds();        
        stage =  new Stage(StageStyle.TRANSPARENT);         
        stage.setWidth(screenDim.getWidth()); stage.setHeight(screenDim.getHeight());
        stage.setX(0);stage.setY(0);
        
        imageView = new ImageView();
        imageView.setFitWidth(screenDim.getWidth());
        imageView.setFitHeight(screenDim.getHeight());
                      
        shooterRect = new Rectangle();
        shooterRect.setId("crop-rectangle"); 
        shooterRect.setMouseTransparent(true);
        Rectangle effRect = new Rectangle(screenDim.getWidth(),screenDim.getHeight(),Color.rgb(255, 255, 255, 0.25));
        effRect.setMouseTransparent(true);        
        shooterPath = new Path();
        shooterPath.setMouseTransparent(true);
        shooterPath.setId("crop-path");
        shooterPath.setFillRule(FillRule.NON_ZERO);
                               
        enabled = new SimpleBooleanProperty(false);          
        shooterType = new SimpleIntegerProperty(SPSettings.getTakeWay());       
        
        Group g = new Group();  
        g.getChildren().addAll(imageView,effRect,shooterRect,shooterPath);   
        stage.setScene(new Scene(g, screenDim.getWidth(), screenDim.getHeight()));  
        stage.addEventHandler(KeyEvent.KEY_PRESSED, (final KeyEvent keyEvent) -> {
            if(isEnabled())             
                cancelShooter();
        });         
    }    
            
    protected static void showShooter(int type){          
        stage.getScene().getStylesheets().clear(); stage.getScene().getStylesheets().add(SPSettings.getThemePath());        
        try{
            BufferedImage image = new Robot().createScreenCapture(new java.awt.Rectangle(0, 0, 
                    (int)screenDim.getWidth(), (int)screenDim.getHeight()));
            imageView.setImage(SwingFXUtils.toFXImage(image,null));
        }catch(Exception ex){            
        }        
        setEnabled(true);
        if(type != 2)
            stage.show();  
    }      
    
    protected static void initShooterType(int type){                 
        switch (type) {
            case 0:
                setRectangluarWay();
                break;
            case 1:
                setDrawShapeWay();
                break;
            case 2:       
                setFullscreenWay();
                break;            
        }
        
        imageView.setOnMouseReleased(value ->{
            if (value.getButton() == MouseButton.PRIMARY && isEnabled() && getShooterType()!= 2){
                if(shooterRect.getWidth() >= 40 && shooterRect.getHeight() >= 40){
                    if(getShooterType() == 1){
                        Path p = new Path(shooterPath.getElements());
                        p.setFill(Color.WHITE);
                        imageView.setClip(p);                    
                    }
                    finishShooting();    
                }else{
                    clearShooter();                    
                }                    
            }else
                clearShooter();
        });     
    }
    
    private static void setRectangluarWay(){
        imageView.setCursor(Cursor.CROSSHAIR);
        Point theGreater = new Point();
        Point theSmaller = new Point();        
        imageView.setOnMousePressed(value ->{
            if (value.getButton() != MouseButton.PRIMARY || isEnabled() == false || getShooterType() != 0)
                return;
            shooterRect.setOpacity(1);
            theGreater.setLocation(value.getX() + imageView.getLayoutX() ,value.getY() + imageView.getLayoutY());
            shooterRect.setX(0);shooterRect.setY(0);shooterRect.setWidth(0);shooterRect.setHeight(0);
        }); 
        imageView.setOnMouseDragged(value ->{
            if (value.getButton() != MouseButton.PRIMARY ||isEnabled() == false || getShooterType() != 0)
                return;
            theSmaller.setLocation(value.getX() + imageView.getLayoutX() ,value.getY() + imageView.getLayoutY());
            double recStartX = Math.min(theGreater.x, theSmaller.x);
            double recStartY = Math.min(theGreater.y, theSmaller.y);
            double recWidth = (Math.max(theGreater.x, theSmaller.x)-Math.min(theGreater.x, theSmaller.x));
            double recHeight = Math.max(theGreater.y, theSmaller.y)-Math.min(theGreater.y, theSmaller.y);

            shooterRect.setX(recStartX);shooterRect.setY(recStartY);
            shooterRect.setWidth(recWidth);shooterRect.setHeight(recHeight);

            if(value.getX() > imageView.getFitWidth() )
                shooterRect.setWidth(imageView.getFitWidth() - shooterRect.getX() + imageView.getLayoutX());
            if(value.getX() < 0){
                shooterRect.setX(imageView.getLayoutX());
                shooterRect.setWidth(value.getX()+shooterRect.getWidth());
            }
            if(value.getY() > imageView.getFitHeight())
                shooterRect.setHeight(imageView.getFitHeight() - shooterRect.getY() + imageView.getLayoutY());
            if(value.getY() < 0){
                shooterRect.setY(imageView.getLayoutY());
                shooterRect.setHeight(value.getY()+shooterRect.getHeight());
            }
        });
    }
    
    private static void setDrawShapeWay(){
        imageView.setCursor(Cursor.HAND);
        imageView.setOnMousePressed((MouseEvent value) ->{
            if(getShooterType() == 1 && isEnabled() &&  value.isPrimaryButtonDown()){
                shooterRect.setOpacity(0);
                shooterPath.getElements().clear();
                shooterPath.getElements().addAll(new MoveTo(value.getX(), value.getY()),
                        new LineTo(value.getX(), value.getY()));
            }
        }); 
        imageView.setOnMouseDragged(value ->{
            if(getShooterType() == 1 && isEnabled() && value.isPrimaryButtonDown()){
                shooterPath.getElements().add(new LineTo(value.getX(), value.getY()));
                Bounds pBounds = shooterPath.getBoundsInParent();
                shooterRect.setX(pBounds.getMinX());shooterRect.setY(pBounds.getMinY());
                shooterRect.setWidth(pBounds.getWidth());shooterRect.setHeight(pBounds.getHeight());
            }
        });
                
    }
    
    private static void setFullscreenWay(){
        shooterRect.setX(0);
        shooterRect.setY(0);
        shooterRect.setWidth(screenDim.getWidth());
        shooterRect.setHeight(screenDim.getHeight());
        finishShooting(); 
    }
        
    protected static void finishShooting(){
        SPPrimary.getController().hide();
        SnapshotParameters param = new SnapshotParameters();
        param.setViewport(new Rectangle2D(shooterRect.getX(), shooterRect.getY(), 
                shooterRect.getWidth(),shooterRect.getHeight()));  
        Color fill = Color.TRANSPARENT;
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
        if(isQuickShot()){
            param.setFill(fill);
            SPSaver.saveQuickFrame(imageView.snapshot(param, null));            
        }else{
            param.setFill(Color.TRANSPARENT);
            SPCapture.showSPCaptureController(imageView.snapshot(param, null));     
        }   
        cancelShooter();
    }
    
    protected static void clearShooter(){     
        SPPrimary.getController().toFront();
        imageView.setClip(null);        
        shooterPath.getElements().clear(); 
        shooterRect.setX(0);shooterRect.setY(0);
        shooterRect.setWidth(0);shooterRect.setHeight(0);
    }
    
    protected static int getShooterType(){
        return shooterType.getValue();
    }
    
    protected static void setShooterType(int type){
        /* 0- shooterRectangluar * 1- draw-shape * 2- full-screen */        
        shooterType.setValue(type);   
        initShooterType(type);
    }
    
    protected static void cancelShooter(){        
        clearShooter();setEnabled(false);        
        imageView.setCursor(Cursor.WAIT);
        stage.close();        
    }

    public static void setEnabled(boolean enable) {        
        enabled.setValue(enable);
    }    
    protected static boolean isEnabled(){
        return enabled.getValue();
    } 
    protected static BooleanProperty enabledProperty(){
        return enabled;
    }
            
    protected static boolean isQuickShot(){
        return SPSettings.getQuickScreenshot();
    } 
        
    protected static boolean isFocused(){
        return stage.isFocused();
    }
}