/*
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2
 * see LICENSE file
 */
package odog.graph;

import odog.syntax.Nodes.VirtualPort;
import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;

/**
 *
 * @author ivan
 */
public class VirtualPortViewVertex extends DirectedSparseVertex {
    
    public VirtualPortViewVertex(VirtualPort vp) {
        virtualPort = vp;
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public VirtualPort getVirtualPort() {
        return virtualPort;
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////

    private VirtualPort virtualPort;
}
