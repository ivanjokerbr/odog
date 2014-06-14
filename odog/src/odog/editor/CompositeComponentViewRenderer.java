/*
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2
 * see LICENSE file
 */
package odog.editor;

import odog.graph.ComponentViewVertex;
import odog.graph.VirtualPortViewVertex;
import odog.syntax.Nodes.CompBase;
import odog.syntax.Nodes.CompInstance;
import odog.syntax.Nodes.Topology;
import odog.syntax.Nodes.VirtualPort;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/**
 *
 * @author ivan
 */
public class CompositeComponentViewRenderer extends PluggableRenderer {
    
    public CompositeComponentViewRenderer() {
        mNodeSizeScale = 1;
	mDefaultNodeSize = 8;   
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public void paintVertex(Graphics g, Vertex v, int x, int y) {
        String label = null;
        int nodeSize = 0;

        if(v instanceof ComponentViewVertex) {
            ComponentViewVertex vertex = (ComponentViewVertex) v;
            CompBase comp = vertex.getComponent();
            CompInstance ains = comp.getComponentInstance();
            label = ains.getInstanceName();
           
            nodeSize = 60;
            
            g.setColor(Color.RED);
            if(comp instanceof Topology) {
                g.drawOval(x - nodeSize / 2, y - nodeSize / 2, nodeSize + 5, nodeSize + 5);
                g.setColor(Color.CYAN);
            }
            else {
                g.setColor(Color.BLUE);
            }
        }
        else
        if(v instanceof VirtualPortViewVertex) {
            VirtualPortViewVertex vertex = (VirtualPortViewVertex) v;
            VirtualPort vp = vertex.getVirtualPort();
            
            label = vp.getName();
            
            if(vp.isInput()) {
                g.setColor(Color.GREEN);
            }
            else {
                g.setColor(Color.MAGENTA);   
            }
            nodeSize = 20;            
        }

        if(isPicked(v)) {
	    g.setColor(Color.ORANGE);
        }
        
        int labelSize = g.getFontMetrics().stringWidth(label);
        nodeSize = Math.max(nodeSize, 10);
        nodeSize = Math.min(nodeSize, 150);
        
        g.fillOval(x - nodeSize / 2, y - nodeSize / 2, nodeSize, nodeSize);
        g.setColor(Color.GRAY);
        g.drawOval(x - nodeSize / 2, y - nodeSize / 2, nodeSize, nodeSize);
        g.setColor(Color.BLACK);
        
        Font font = new Font("Arial", Font.PLAIN, 12);
        Font f = g.getFont();
        g.setFont(font);
        if (nodeSize > labelSize) {
                g.drawString(label, x - labelSize / 2, y + 4);
        } else {
                g.drawString(label, x - labelSize / 2 + 20, y + 15);

        }
        g.setFont(f);
    }

    public double getNodeSizeScale() {
            return mNodeSizeScale;
    }

    public void setNodeSizeScale(double nodeSizeScale) {
            this.mNodeSizeScale = nodeSizeScale;
    }
        
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    private double mNodeSizeScale;
    private int mDefaultNodeSize;
}