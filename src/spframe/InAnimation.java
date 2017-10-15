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

/**
 *
 * @author mcc
 */
public enum InAnimation {
    NONE(1){
        
    },FADE(280){
        
    },FLYIN(1000){
        
    };
    
    private final int duration;
    
    private InAnimation(int duration){
        this.duration = duration;              
    }
    
    public int getDuration(){
        return this.duration;
    }                    
}
