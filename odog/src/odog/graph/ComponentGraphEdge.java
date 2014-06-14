/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.graph;

import odog.syntax.Nodes.Connection;
import odog.syntax.Nodes.VirtualPort;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import java.util.Hashtable;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.Value;

/**
 *
 * @author ivan
 */
public class ComponentGraphEdge extends DirectedSparseEdge {
    
    /** Creates a new instance of CompGraphEdge */
    public ComponentGraphEdge(Vertex sourceNode, Vertex destNode, VirtualPort source, 
            VirtualPort sink, Connection connection, Hashtable<Attr,Value> attrValueMap) {
        super(sourceNode, destNode);
     
        isinfo = new BaseEdgeISemInfo();
        
        this.source = source;
        this.sink = sink;
        this.connection = connection;            
        
        //this.signalName = Node.replaceDotForUnd(sink.getFullName() + "_" + connection.getName());
        
        Attr datatype = source.getDataType();
        Value type = attrValueMap.get(datatype);
        if(type == null) {
            type = datatype.getDefaultValue();
        }
        //this.signalTypeName = Type.convertToCType(type.getValueExpr());
        this.signalTypeName = type.getValueExpr();
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public VirtualPort getSourcePort() {
        return source;
    }
    
    public VirtualPort getSinkPort() {
        return sink;
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public void setISInfo(BaseEdgeISemInfo info) {
        isinfo = info;
    }
    
    public BaseEdgeISemInfo getISInfo() {
        return isinfo;
    }
    
 //   public String getSignalName() {
//        return signalName;
//    }
    
    public String getSignalTypeName() {
        return signalTypeName;
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
 
    private String signalTypeName;
 //   private String signalName;
    
    private Connection connection;
    private VirtualPort source;
    private VirtualPort sink;    
    
    private BaseEdgeISemInfo isinfo;
}