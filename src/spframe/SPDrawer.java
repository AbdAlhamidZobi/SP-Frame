/*
 * Copyright (C) 2017 mcc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package spframe;

import java.util.ArrayList;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import spframe.SPCaptureController.ToolManager;

/**
 * this class describes the layout manager and the stuff linked to. * 
 * @author mcc
 */
public class SPDrawer {  
    private boolean enabled = false;          
    private final SPLayerManager layerManager;
    private final ToolManager toolManager;
    private final Pane drawPane;  
    private Path currentDraw;
    private Rectangle eraserRect;
    
    public SPDrawer(){
        this(SPCapture.getController().getLayerManager(),SPCapture.getController().getToolManager());
    }        
    public SPDrawer(SPLayerManager layerManager,ToolManager toolManager){                       
        this.layerManager = layerManager;       
        this.drawPane = layerManager.getLayersPane();     
        this.toolManager = toolManager;         
        this.eraserRect = new Rectangle();    
        this.eraserRect.setOpacity(0);
        this.eraserRect.setMouseTransparent(true);
        initDrawer();
    }
         
    public final void initDrawer(){          
        this.drawPane.setOnMousePressed((MouseEvent value) ->{    
            String tool = toolManager.getSelectedTool();
            SPLayer layer = layerManager.getSelectedLayer();
            if(!isEnabled() || !value.isPrimaryButtonDown() || tool.equals("none") || !"draw".equals(layer.getType()) )
                return;
            if(tool.equals("pen") || tool.equals("highlighter")){
                currentDraw = new Path(new MoveTo(value.getX(), value.getY()),
                                       new LineTo(value.getX(), value.getY()));
                currentDraw.setMouseTransparent(true);
                currentDraw.setStrokeLineCap(StrokeLineCap.ROUND);
                currentDraw.setStrokeLineJoin(StrokeLineJoin.ROUND);
                currentDraw.setStroke((tool.equals("pen")? toolManager.getPenColor():toolManager.getHighlighterColor()));
                currentDraw.setStrokeWidth((tool.equals("pen")? toolManager.getPenSize():toolManager.getHighlighterSize()));
                if(tool.equals("highlighter"))
                    currentDraw.setBlendMode(BlendMode.MULTIPLY);
                layerManager.getSelectedLayer().getSpace().getChildren().add(currentDraw);
                SPCapture.getController().setFrameChanged(true);      
                                
            }else if(tool.equals("eraser")){
                Pane parent;
                if(eraserRect.getParent() != null){
                    parent = (Pane)eraserRect.getParent();
                    parent.getChildren().remove(eraserRect);
                }
                eraserRect.setWidth(toolManager.getEraserSize()*2);
                eraserRect.setHeight(toolManager.getEraserSize()*2);   
                layerManager.getSelectedLayer().getSpace().getChildren().add(eraserRect);
                eraserRect.relocate(value.getX(), value.getY());   
                checkToEraser();                
            }
        });
                
        this.drawPane.setOnMouseDragged(value ->{
            String tool = toolManager.getSelectedTool();
            SPLayer layer = layerManager.getSelectedLayer();
            if(!isEnabled() || !value.isPrimaryButtonDown() || tool.equals("none") || !"draw".equals(layer.getType())  )
                return;
            if(tool.equals("pen") || tool.equals("highlighter")){
                currentDraw.getElements().add(new LineTo(value.getX(), value.getY()));                                               
            }else if(tool.equals("eraser")){                
                eraserRect.relocate(value.getX(), value.getY());   
                checkToEraser();               
            }
        });      
        
        this.drawPane.setOnMouseReleased(value ->{
            String tool = toolManager.getSelectedTool();
            SPLayer layer = layerManager.getSelectedLayer();
            if(!isEnabled() || !value.getButton().equals(MouseButton.PRIMARY) || tool.equals("none") || !"draw".equals(layer.getType()) )
                return;
            
            if(tool.equals("eraser")){  
                Pane parent;
                if(eraserRect.getParent() != null){
                    parent = (Pane)eraserRect.getParent();
                    parent.getChildren().remove(eraserRect);                    
                }               
            }
            updateLayerThumbnail();
        });  
        
        this.drawPane.setOnMouseClicked(value ->{            
            String tool = toolManager.getSelectedTool();
            SPLayer layer = layerManager.getSelectedLayer();
            if(!isEnabled() || !value.getButton().equals(MouseButton.PRIMARY) || tool.equals("none") || !"draw".equals(layer.getType()) )
                return;              
            if(tool.equals("eraser") && value.getClickCount() == 2){    
                layer.getSpace().getChildren().clear();
                updateLayerThumbnail();
            }            
        });
    }

    private void checkToEraser(){  
        ObservableList<Node> list = layerManager.getSelectedLayer().getSpace().getChildren();
        if(list == null || list.isEmpty())
            return;
        ArrayList<Node> removeList = new ArrayList<>();
        Bounds eraserBounds = this.eraserRect.getBoundsInParent();   
        list.forEach(item ->{  
            if(item != this.eraserRect &&item.getBoundsInParent().intersects(eraserBounds)){ 
                removeList.add(item);
                SPCapture.getController().setFrameChanged(true);
            }
        });            
        list.removeAll(removeList);        
    }  
    
    private void updateLayerThumbnail(){
        SPLayer layer = this.layerManager.getSelectedLayer();
        SnapshotParameters params = new SnapshotParameters(); 
        params.setFill(new ImagePattern(new  Image("rec/background/transparency-grid.PNG"),0, 0, 0.3, 0.3, true));
        if(layer.getUsedBounds() != null)
            params.setViewport(layer.getUsedBounds());
        layer.setThumbnail(layer.getSpace().snapshot(params, null));        
    }
       
    public void setEnabled(boolean enable){        
        this.enabled = enable;
    }

    public boolean isEnabled(){
        return enabled;
    }        
}  