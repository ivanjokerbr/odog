/*
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2
 * see LICENSE file
 */
package odog.editor;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.Coordinates;
import java.util.Iterator;

/**
 *
 * @author ivan
 */
public class CompositeComponentLayout extends AbstractLayout {
    
    public CompositeComponentLayout(Graph g)  {
        super(g);
    }

    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public void advancePositions() {
    }
    
    public boolean incrementsAreDone() {
        return true;        
    }
    
    public boolean isIncremental() {
        return false;
    }
    
    ////////////////////////////// PROTECTED METHODS //////////////////////////////
    
    protected void initialize_local_vertex(Vertex v) {
    }

   protected void initializeLocations() {
        super.initializeLocations();
        Graph g = getGraph();
        Iterator ite = g.getVertices().iterator();
        int x = 0;
        int y = 0;
        while(ite.hasNext()) {
            Vertex v = (Vertex) ite.next();
            Coordinates cord = getCoordinates(v);
            cord.setX(x);
            cord.setY(y);
            x += 50;
            if(x == 300) {
                y += 20;
                x = 0;
            }
        }
               
   }
        
}
