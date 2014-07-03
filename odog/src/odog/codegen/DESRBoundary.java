/*
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2
 * see LICENSE file
 */
package odog.codegen;

import odog.codegen.BoundaryData.BoundaryType;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.Connection;
import odog.syntax.Nodes.ExportedPort;
import odog.syntax.Nodes.Topology;
import odog.syntax.Nodes.Value;
import java.util.HashMap;

/**
 *
 * @author ivan
 */
public class DESRBoundary extends BoundaryData {
    
    public DESRBoundary(String outsideSignal, ExportedPort port, Connection c,
            Topology containerTopology, BoundaryType boundaryType,
            HashMap<Attr, Value> attrValueMap) {
        super(outsideSignal, port, c, containerTopology, BoundaryType.DESR, attrValueMap,
                BoundaryDirection.INPUT);
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public void setOutsideCurrentTime(String outsideCurrentTime) {
        this.outsideCurrentTime = outsideCurrentTime;
    }
    
    public String getOutsideCurrentTime() {
        return outsideCurrentTime;
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    private String outsideCurrentTime;
}
