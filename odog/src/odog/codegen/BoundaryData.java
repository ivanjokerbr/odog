/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.codegen;

import odog.codegen.languages.C.Type;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.Connection;
import odog.syntax.Nodes.Dport;
import odog.syntax.Nodes.ExportedPort;
import odog.syntax.Nodes.Topology;
import odog.syntax.Nodes.Value;
import odog.syntax.Nodes.VirtualPort;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author ivan
 */
public class BoundaryData {
    
    public BoundaryData(String outsideSignal, ExportedPort port, Connection c,
            Topology containerTopology, BoundaryType boundaryType,
            HashMap<Attr, Value> attrValueMap, BoundaryDirection direction) {
        
        this.outsideSignal = outsideSignal;
        this.port = port;
        this.con = c;
        this.containerTopology = containerTopology;
        this.boundaryType = boundaryType;
        this.direction = direction;

        internalSignals = new HashMap<Topology, LinkedList<Dport>>();
        
        VirtualPort vp = con.getOutputPort();
        Attr dtype = vp.getDataType();
        Value v = attrValueMap.get(dtype);
        if(v == null) {
            v = dtype.getDefaultValue();
        }
        //dataType = Type.convertToCType(v.getValueExpr());
        dataType = v.getValueExpr();
    }
    
    public enum BoundaryType { SRDE, DESR, IGNORE };
    public enum BoundaryDirection { INPUT, OUTPUT };
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public boolean isLinkedTo(Dport dp) {
        for(VirtualPort vp : port.getRefPorts()) {
            if(vp instanceof Dport) {
                if((Dport)vp == dp) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public String getOutsideSignal() {
        return outsideSignal;
    }

    public Connection getConnection() {
        return con;
    }
    
    public ExportedPort getBoundaryPort() {
        return port;
    }
    
    public Topology getContainerTopology() {
        return containerTopology;
    }
    
    public BoundaryType getBoundaryType() {
        return boundaryType;
    }
    
    public void addInternalSignal(Topology container, Dport port) {
        LinkedList<Dport> l;
        if(internalSignals.containsKey(container)) {
            l = internalSignals.get(container);
        }
        else {
            l = new LinkedList<Dport>();
            internalSignals.put(container, l);
        }
        l.add(port);
    }
    
    public List<Dport> getInternalSignals(Topology container) {
        return internalSignals.get(container);
    }
    
    public String getDataType() {
        return dataType;
    }
    
    public boolean isInput() {
        return direction == BoundaryDirection.INPUT;
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    // the declaration of the outside data container
    private String outsideSignal;
    
    // the source or sink port of the composite component
    private ExportedPort port;

    // the outside connection
    private Connection con;

    // A link to the topology that contains the exported port
    private Topology containerTopology;
    
    private BoundaryType boundaryType;
    
    private HashMap <Topology, LinkedList<Dport>> internalSignals;
    
    private String dataType;

    private BoundaryDirection direction;
}
