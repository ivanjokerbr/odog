/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */

package odog.graph;

import odog.design.CompositeComponent;
import odog.graph.ComponentGraphNode.NodeType;
import odog.syntax.Nodes.CompInstance;
import edu.uci.ics.jung.graph.Graph;

/**
 *
 * This class extends the basic ComponentGraphNode, for storing extra information of
 * a node graph associated with a composite Component. The component has to be tagged
 * with a ISem, therefore, having an associated graph for its topology.
 *
 * @author ivan
 */
public class CompositeComponentGraphNode extends ComponentGraphNode {
    
    /** For the toplevel, component = null
     */
    public CompositeComponentGraphNode(CompositeComponent composite, CompInstance comp, 
            String isem) {
        super(comp, NodeType.COMPOSITE_SEMANTIC_HEAD);
        
        this.composite = composite;
        this.isem = isem;
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public String ISem() {
        return isem;
    }
    
    public void setInsideGraph(Graph g) {
        insideGraph = g;
    }
    
    public Graph getInsideGraph() {
        return insideGraph;
    }
    
    public CompositeComponent getComposite() {
        return composite;
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    private String isem;
    private Graph insideGraph;
    
    // if it is the toplevel, will have composite != null
    private CompositeComponent composite;
}
