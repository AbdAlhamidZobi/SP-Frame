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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;

/**
 * this class describes the layout manager and the stuff linked to.
 * @author mcc
 */
public class SPLayerManager{    
    private final ObservableList<SPLayer> layers;
    private final ObjectProperty<SPLayer> selectedLayer;    
    private final VBox layerList;
    private final Pane layersPane;   
    private final String namePrefix;
    private final ScrollBar opactiyBar;
    private final Label opacityLabel;    
    private final Button newButton;
    private final Button delButton;
        
    public SPLayerManager(Pane layersPane, ObservableList<Node> controles,
                                VBox layerList,String namePrefix){        
        this.layers = FXCollections.observableArrayList();
        this.selectedLayer = new SimpleObjectProperty<>();                
        this.layerList = layerList;
        this.layersPane = layersPane;
        this.namePrefix = namePrefix;
        this.opactiyBar = (ScrollBar)controles.get(0);
        this.opacityLabel = (Label)controles.get(1);        
        this.newButton = (Button)controles.get(2);
        this.delButton = (Button)controles.get(3);        
        delButton.setDisable(true);
        
        this.newButton.setOnAction(value ->{
            this.addLayer("draw");            
        });           
        this.delButton.setOnAction(value ->{             
            this.removeLayer(this.getSelectedLayer());            
        });        
        this.opactiyBar.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {                                               
            this.setLayerOpacity((double)newValue/100);
            this.opacityLabel.setText(Math.round((double)newValue)+"%");
        });
    }
    
    public void addLayer(String type){
          addLayer(type, 0, 0,layersPane.getPrefWidth(),layersPane.getPrefHeight());
    }    
    
    public void addLayer(String type,double x,double y,
            double width,double height){
        SPLayer newLayer = new SPLayer(this.namePrefix+" "+this.layers.size(), type,                
                new WritableImage(40, 40), new Pane(), x, y, width, height);
        this.layers.add(newLayer);        
        this.layerList.getChildren().add(newLayer);
        layersPane.getChildren().add(newLayer.getSpace());         
        this.setSelectedLayer(newLayer); 
        if(type.equals("draw")){
        SnapshotParameters params = new SnapshotParameters(); 
        params.setFill(new ImagePattern(new  Image("rec/background/transparency-grid.PNG"),0, 0, 0.3, 0.3, true));                
        newLayer.setThumbnail(newLayer.getSpace().snapshot(params, null));    
        }
        SPCapture.getController().setFrameChanged(true);
        if(layers.size() > 1)            
            delButton.setDisable(false);              
    }
    
    public void removeLayer(SPLayer layer){      
        int nextIndx = layers.indexOf(layer);
        layers.remove(layer);        
        layerList.getChildren().remove(layer); 
        layersPane.getChildren().remove(layer.getSpace());
        int lastIndx = layers.size() - 1;
        if(lastIndx < nextIndx) 
            nextIndx = lastIndx;
        this.setSelectedLayer(layers.get(nextIndx));
        SPCapture.getController().setFrameChanged(true);
        if(lastIndx == 0)        
            delButton.setDisable(true);
    }
    
    public Pane getLayersPane() {
        return layersPane;
    }
    
    public void setSelectedLayer(SPLayer layer){
        if(this.layers.contains(layer) && this.getSelectedLayer()!= layer){
            this.layers.forEach(item ->{
                if(item != layer)                
                    item.setSelected(false);
            });
            layer.setSelected(true);
            this.selectedLayer.setValue(layer);   
            opactiyBar.setValue(layer.getSpaceOpacity()*100);
        }
    }
    
    public SPLayer getSelectedLayer(){               
        return this.selectedLayer.getValue();    
    }
        
    public ObjectProperty<SPLayer> selectedLayerProperty(){
        return null;
    }        
    
    public void setLayers(ObservableList<SPLayer> layers){
        this.layers.setAll(layers);
        this.layerList.getChildren().setAll(layers);
        this.layersPane.getChildren().remove(1, layersPane.getChildren().size());
        
        layers.forEach(item ->{
            this.layersPane.getChildren().add(item.getSpace());
            if(item.isSelected())
                this.setSelectedLayer(item);
        });
        
        if(layers.size() > 1) 
            delButton.setDisable(false);
        else 
            delButton.setDisable(true);        
    }
    
    public void restLayers(){
        layers.clear();
        layerList.getChildren().clear();  
        layersPane.getChildren().remove(1, layersPane.getChildren().size());
    }
    
    public ObservableList<SPLayer> getLayers(){
        return this.layers;
    }
        
    private void setLayerOpacity(double opacity){ 
        getSelectedLayer().setSpaceOpacity(opacity);
    }          
}