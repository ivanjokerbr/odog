/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.codegen.Platform.OmapH2.DF;

import odog.graph.BaseEdgeISemInfo;

/**
 *
 * @author ivan
 */
public class DFEdgeISemInfo extends BaseEdgeISemInfo {
    
    public DFEdgeISemInfo() {
        super();
    }
    
    public DFEdgeISemInfo(BaseEdgeISemInfo info) {
        this.id = info.getId();
        this.cgname = info.getCGName();
        this.breakCycle = info.canBreakCycle();
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public void setSharedMemAddr(int addr) {
        sharedMemAddr = addr;
    }
    
    public int getSharedMemAddr() {
        return sharedMemAddr;
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
  
    private int sharedMemAddr;
    
}
