/*
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2
 * see LICENSE file
 */
package odog.graph;

import odog.syntax.Nodes.CompBase;
import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;

/**
 *
 * @author ivan
 */
public class ComponentViewVertex extends DirectedSparseVertex {
    
    public ComponentViewVertex(CompBase abase) {
        component = abase;
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public CompBase getComponent() {
        return component;
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////

    private CompBase component;
}
