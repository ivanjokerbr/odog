/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.codegen.Platform.OmapH2.DF;

import odog.graph.BaseComponentISemInfo;

/**
 *
 * @author ivan
 */
public class DFComponentISemInfo extends BaseComponentISemInfo {
    
    public DFComponentISemInfo() {
        super();
    }
    
    public DFComponentISemInfo(BaseComponentISemInfo info) {
        this.id = info.getId();
        this.cgname = info.getCGName();
    }

    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public void setToDSP(boolean value) {
        toDSP = value;
    }
    
    public boolean getToDSP() {
        return toDSP;
    }
    
    public void setIsBoundary(boolean value) {
        isBoundary = value;
    }
    
    public boolean getIsBoundary() {
        return isBoundary;
    }
    
    public void setBoundaryId(int value) {
        boundaryId = value;
    }
    
    public int getBoundaryId() {
        return boundaryId;
    }
    
    ////////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    
    ////////////////////////////// PRIVATE VARIABLES ///////////////////////////
    
    private boolean toDSP;
    private boolean isBoundary;
    private int boundaryId;
    
}
