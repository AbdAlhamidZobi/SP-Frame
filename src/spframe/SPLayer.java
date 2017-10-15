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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 *
 * @author mcc
 */
public class SPLayer extends Label{
    private final StringProperty name;    
    private final BooleanProperty selected;
    private final StringProperty type;
    private final ObjectProperty<Image> thumbnail;
    
    private final Pane space;
    private final DoubleProperty spaceX;
    private final DoubleProperty spaceY;
    private final DoubleProperty spaceWidth;
    private final DoubleProperty spaceHeight;   
    private final DoubleProperty spaceOpacity;

    public SPLayer(String name,String type,Image thumbnail,Pane space,
            double spaceX,double spaceY,double spaceWidth,double spaceHeight){        
        this.getStyleClass().add("layer");
        this.setPrefWidth(200);
        this.setMaxWidth(200);
        this.name = new SimpleStringProperty(name);           
        this.selected = new SimpleBooleanProperty(false);
        this.type = new SimpleStringProperty(type);
        this.thumbnail = new SimpleObjectProperty<>(thumbnail);
       
        this.space = space;
        this.spaceX =  new SimpleDoubleProperty(spaceX);        
        this.spaceY = new SimpleDoubleProperty(spaceY);        
        this.spaceWidth =  new SimpleDoubleProperty(spaceWidth);        
        this.spaceHeight =  new SimpleDoubleProperty(spaceHeight);  
        this.spaceOpacity =  new SimpleDoubleProperty(1); 
        
        this.textProperty().bind(this.name);
        this.space.prefHeightProperty().bind(this.spaceHeight);
        this.space.prefWidthProperty().bind(this.spaceWidth);
        this.space.layoutXProperty().bind(this.spaceX);
        this.space.layoutYProperty().bind(this.spaceY);
        this.space.opacityProperty().bind(this.spaceOpacity);
        this.space.setMouseTransparent(true);
        this.space.setStyle("-fx-background-color: transparent;");
        ImageView thumbView = new ImageView(thumbnail);        
        thumbView.setFitWidth(25);thumbView.setFitHeight(25);
        this.setGraphic(thumbView);
        this.setOnMouseClicked(value ->{
            SPLayerManager layerManager = SPCapture.getController().getLayerManager();         
            layerManager.setSelectedLayer(this);
        });   
        this.thumbnail.addListener(listener ->{            
            thumbView.setImage(this.thumbnail.getValue());
        });
    }
    
    public Rectangle2D getUsedBounds(){
        ObservableList<Node> children = this.space.getChildren();
        Rectangle2D bounds = null;
        if(children.isEmpty())
            return bounds;
        
        Bounds paneBounds = this.space.getBoundsInParent();        
        double startX = paneBounds.getWidth(); double startY = paneBounds.getHeight();  
        double endX = 0; double endY = 0; 
        
        ObservableList<Node> list = this.space.getChildren();        
        for(Node item : list){     
            Bounds itemBounds = item.getBoundsInParent();
            double nodeStartX = itemBounds.getMinX();
            double nodeStartY = itemBounds.getMinY(); 
            double nodeEndX = itemBounds.getMaxX();
            double nodeEndY = itemBounds.getMaxY();
            
            if ( nodeStartX < startX) startX = nodeStartX;  
            if ( nodeStartY < startY) startY = nodeStartY;                                                     
            if ( nodeEndX >endX) endX = nodeEndX;  
            if ( nodeEndY > endY) endY = nodeEndY;                
        }   
        double pwidth =  endX - startX;
        double pheight = endY - startY;
        bounds = new Rectangle2D(startX, startY, pwidth, pheight); 
        
        return bounds;
    }
    
    public void setName(String name){
        if(!"".equals(name))
            this.name.setValue(name);        
    }
    
    public String getName(){
        return this.name.getValue();
    }
        
    public void setSelected(boolean selected){        
        this.selected.setValue(selected);
        if(selected)
            this.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), true);
        else
            this.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), false);
    }
    
    public boolean isSelected(){
        return this.selected.getValue();
    }
    
    public void setType(String type){
        if(!"".equals(type))
            this.type.setValue(type);        
    }
    
    public String getType(){
        return this.type.getValue();
    }
    
    public void setThumbnail(Image thumbnail){
        if(thumbnail != null)
            this.thumbnail.setValue(thumbnail);
    }
    
    public Image getThumbnail(){
        return this.thumbnail.getValue();
    }
    
    public Pane getSpace(){
        return this.space;
    }                    
    
    public void setSpaceOpacity(double opacity){
        if(opacity > 1 || opacity < 0)
            return;
        this.spaceOpacity.setValue(opacity);
    }
    
    public double getSpaceOpacity(){        
        return this.spaceOpacity.getValue();
    }
    
    public DoubleProperty spaceOpacityProperty(){        
        return this.spaceOpacity;
    }
    
    public void setSpaceX(double x){
        this.spaceX.setValue(x);
    }
    
    public double getSpaceX(){
        return this.spaceX.getValue();        
    }
    
    public void setSpaceY(double y){
        this.spaceY.setValue(y);
    }
    
    public double getSpaceY(){
        return this.spaceY.getValue();        
    }
    
    public void setSpaceWidth(double w){
        this.spaceWidth.setValue(w);
    }
    
    public double getSpaceWidth(){
        return this.spaceWidth.getValue();        
    }
    
    public void setSpaceHieght(double h){
        this.spaceHeight.setValue(h);
    }
    
    public double getSpaceHeight(){
        return this.spaceHeight.getValue();        
    }
    
}
