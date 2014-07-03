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
public class SRDEBoundary extends BoundaryData {
    
    public SRDEBoundary(String outsideSignal, ExportedPort port, Connection c,
            int prio, Topology containerTopology, HashMap<Attr,Value> attrValueMap) {
        super(outsideSignal, port, c, containerTopology, BoundaryType.SRDE, 
                attrValueMap, BoundaryDirection.OUTPUT);
        this.destNodePriority = prio;
    }

    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public int getDestNodePriority() {
        return destNodePriority;
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    // in case of an output generation
    private int destNodePriority;

}
