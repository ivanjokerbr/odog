/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.graph;

import odog.syntax.Nodes.CompInstance;
import odog.syntax.Nodes.VersionBase;
import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;
import java.util.LinkedList;
import java.util.List;
import odog.syntax.Nodes.VirtualPort;

/**
 *
 * @author ivan
 */
public class ComponentGraphNode extends DirectedSparseVertex {
    
    /** Creates a new instance of CompNode */
    public ComponentGraphNode(CompInstance comp, NodeType type) {
        super();
        
        this.comp = comp;
        ntype = type;

        isinfo = new BaseComponentISemInfo();
        
        inputPorts = new LinkedList<VirtualPort>();
        inputEdges = new LinkedList<LinkedList<ComponentGraphEdge>>();
        inputComps = new LinkedList<ComponentGraphNode>();
        
        outputPorts = new LinkedList<VirtualPort>();
        outputEdges = new LinkedList<LinkedList<ComponentGraphEdge>>();
        outputComps = new LinkedList<ComponentGraphNode>();
    }
    
    public enum NodeType { 
        COMPOSITE_SEMANTIC_HEAD,      // no hierarquico com modelo ISEM
        COMPOSITE_ASEMANTIC,         // no hierarquico sem modelo ISEM
        ATOMIC 
    };
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////

    public String toString() {
        return comp.toString();
    }
    
    public CompInstance getComponent() {
        return comp;
    }
    
    public NodeType getType() {
        return ntype;
    }
    
    public void setSelectedVer(VersionBase vb) {
        selectedVer = vb;
    }
    
    public VersionBase getSelectedVer() {
        return selectedVer;
    }

    public void setISInfo(BaseComponentISemInfo info) {
        isinfo = info;
    }
    
    public BaseComponentISemInfo getISInfo() {
        return isinfo;
    }

    /// METHODS FOR INPUT CONNECTIONS
    public void addedInputEdge(ComponentGraphEdge edge) {
        VirtualPort vp = edge.getSinkPort();
        int index;
        if(inputPorts.contains(vp)) {
            index = inputPorts.indexOf(vp);
        }
        else {
            index = inputPorts.size();
            inputPorts.add(vp);
        }

        LinkedList<ComponentGraphEdge> edges;
        if(inputEdges.size() < index + 1) {    
            // no edges for this port
            edges = new LinkedList<ComponentGraphEdge>();
            inputEdges.add(edges);
        }
        else {
            edges = inputEdges.get(index);
        }
        // assumes that there will not be duplicated edges
        edges.add(edge);
        
        ComponentGraphNode node = (ComponentGraphNode) edge.getSource();
        if(!inputComps.contains(node)) {
            inputComps.add(node);
        }
    }

    public int numberInputPorts() {
        return inputPorts.size();
    }        
    
    public VirtualPort getInputPort(int index) {
        return inputPorts.get(index);
    }
    
    public int getInputPort(VirtualPort vp) {    
        return inputPorts.indexOf(vp);
    }
    
    public int numberInputConnections(int portIndex) {
        LinkedList<ComponentGraphEdge> l = inputEdges.get(portIndex);
        return l.size();
    }
    
    public List<ComponentGraphEdge> getInputConnections(int portIndex) {
        return inputEdges.get(portIndex);
    }
    
    public ComponentGraphEdge getInputGraphEdge(int portIndex, int conIndex) {
        LinkedList<ComponentGraphEdge> l = inputEdges.get(portIndex);
        return l.get(conIndex);        
    }

    public int getInputConnectionIndex(ComponentGraphEdge edge) {
        int portIndex = getInputPortIndex(edge);
        LinkedList<ComponentGraphEdge> l = inputEdges.get(portIndex);
        return l.indexOf(edge);
    }
    
    public int getInputPortIndex(ComponentGraphEdge edge) {
        VirtualPort vp = edge.getSinkPort();
        return getInputPort(vp);
    }

    public int numberOfInputComponents() {
        return inputComps.size();
    }
    
    public int getInputComponent(ComponentGraphNode comp) {
        return inputComps.indexOf(comp);
    }
    
    public void removeInputComponent(ComponentGraphNode comp) {
        if(inputComps.contains(comp)) {
            inputComps.remove(comp);
        }
    }
    
    public ComponentGraphNode getInputComponent(int index) {
        return inputComps.get(index);
    }
    
    /// METHODS FOR OUTPUT CONNECTIONS
    public void addedOutputEdge(ComponentGraphEdge edge) {
        VirtualPort vp = edge.getSourcePort();
        int index;
        if(outputPorts.contains(vp)) {
            index = outputPorts.indexOf(vp);
        }
        else {
            index = outputPorts.size();
            outputPorts.add(vp);
        }
        
        LinkedList<ComponentGraphEdge> edges;
        if(outputEdges.size() < index + 1) {
            // no edges for this port
            edges = new LinkedList<ComponentGraphEdge>();
            outputEdges.add(edges);
        }
        else {
            edges = outputEdges.get(index);
        }
        // assumes that there will not be duplicated edges
        edges.add(edge);
        
        ComponentGraphNode node = (ComponentGraphNode) edge.getDest();
        if(!outputComps.contains(node)) {
            outputComps.add(node);
        }
    }

    public int numberOutputPorts() {
        return outputPorts.size();
    }        
    
    public VirtualPort getOutputPort(int index) {
        return outputPorts.get(index);
    }
    
    public int getOutputPort(VirtualPort vp) {    
        return outputPorts.indexOf(vp);
    }

    public int numberOutputConnections(int portIndex) {
        LinkedList<ComponentGraphEdge> l = outputEdges.get(portIndex);
        return l.size();
    }
    
    public List<ComponentGraphEdge> getOutputConnections(int portIndex) {
        return outputEdges.get(portIndex);
    }
    
    public ComponentGraphEdge getOutputGraphEdge(int portIndex, int conIndex) {
        LinkedList<ComponentGraphEdge> l = outputEdges.get(portIndex);
        return l.get(conIndex);        
    }

    public int getOutputConnectionIndex(ComponentGraphEdge edge) {
        int portIndex = getOutputPortIndex(edge);
        LinkedList<ComponentGraphEdge> l = outputEdges.get(portIndex);
        return l.indexOf(edge);
    }
    
    public int getOutputPortIndex(ComponentGraphEdge edge) {
        VirtualPort vp = edge.getSourcePort();
        return getOutputPort(vp);
    }

    public int numberOfOutputComponents() {
        return outputComps.size();
    }
    
    public int getOutputComponent(ComponentGraphNode comp) {
        return outputComps.indexOf(comp);
    }
    
    public ComponentGraphNode getOutputComponent(int index) {
        return outputComps.get(index);
    }
    
    public void removeOutputComponent(ComponentGraphNode comp) {
        if(outputComps.contains(comp)) {
            outputComps.remove(comp);
        }
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
 
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////

    private CompInstance comp;
    private NodeType ntype;    
    private VersionBase selectedVer;

    // data required for code generation specific to the IS of this component
    private BaseComponentISemInfo isinfo;

    private LinkedList<VirtualPort> inputPorts;
    // each index represents the virtual port, as in inputPorts.
    private LinkedList<LinkedList<ComponentGraphEdge>> inputEdges;
    private LinkedList<ComponentGraphNode> inputComps;

    private LinkedList<VirtualPort> outputPorts;
    private LinkedList<LinkedList<ComponentGraphEdge>> outputEdges;
    private LinkedList<ComponentGraphNode> outputComps;
}
