/*
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2
 * see LICENSE file
 */
package odog.graph;

/**
 *
 * @author ivan
 */
public class BaseEdgeISemInfo extends BaseISemInfo {
    
    public BaseEdgeISemInfo() {
        breakCycle = false;
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    
    public void breakCycle(boolean v) {
        breakCycle = v;
    }
    
    public boolean canBreakCycle() {
        return breakCycle;
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    /////////////////////////// PROTECTED VARIABLES //////////////////////////////
    
    protected boolean breakCycle;       
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
}
