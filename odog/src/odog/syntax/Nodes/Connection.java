/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.syntax.Nodes;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Connection extends Node implements Attributable, Referencia {

    public Connection(String name) {
        super(name);

        inputPorts = new LinkedList();
        outputPort = null;
        attributes = new Edges(this);
    }
    
    ///////////////////////// PUBLIC METHODS ///////////////////////////////////

    public int getType() {
        return CONNECTION;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(name + "( ");
        
        if(outputPort != null) {
            buf.append(outputPort.getFullName() + " => ");
        }

        for(VirtualPort vp : inputPorts) {
            buf.append(vp.getFullName() + " ");
        }

        buf.append(")");
        
        return buf.toString();
    }

    public Connection clone() {
        Connection ret = new Connection(name);

        Iterator ite = attributesIterator();
        while(ite.hasNext()) {
            Attr att = (Attr) ite.next();
            try {
                ret.addAttribute(att.clone());
            }
            catch(NonUniqueNameException ex) {
                System.out.println(ex); 
            }
        }
        // nao mexe nas conexoes pois sao referencias....
        // ... serao atribuidas no metodo clone da classe Topology e HVer
        
        return ret;
    }

    public String exportXML(int ident) {
        StringBuffer buf = new StringBuffer();

        String pad = identForXML(ident);
        
        buf.append(pad + "<connection name=\"" + name + "\">\n");

        attributes.exportXML(buf, ident + 2);

        if(outputPort != null) {
            buf.append(pad + "  <portRef completePortName=\"" + 
                        outputPort.getFullName()  + "\"/>\n");
        }
        Iterator ite = inputPorts.iterator();
        while(ite.hasNext()) {
            VirtualPort dp = (VirtualPort) ite.next();
            buf.append(pad + "  <portRef completePortName=\"" + 
                    dp.getFullName()  + "\"/>\n");
        }        
        buf.append(pad + "</connection>\n");

        return buf.toString();
    }

    public void addPort(VirtualPort port) throws NonUniqueNameException {        
        if(port.isInput()) {        
            inputPorts.add(port);
        }
        else {
            if(outputPort != null) {
                outputPort.removeReference(this);
            }
            outputPort = port;
        }
        port.addReference(this);
    }
    
    public void removePort(VirtualPort port) {
        if(port.isInput()) {
            inputPorts.remove(port);
            port.removeReference(this);
        }
        else {
            outputPort.removeReference(this);
            outputPort = null;
        }
    }
    
    public Iterator portsIterator() {
        LinkedList<VirtualPort> ret = new LinkedList();

        ret.addAll(inputPorts);
        if(outputPort != null) {
            ret.add(outputPort);
        }
        
        return ret.iterator();
    }
    
    public List<VirtualPort> getPorts() {
        LinkedList<VirtualPort> ret = new LinkedList();

        ret.addAll(inputPorts);
        if(outputPort != null) {
            ret.add(outputPort);
        }
        
        return ret;        
    }
    
    public VirtualPort getOutputPort() {
        return outputPort;
    }
    
    public Iterator inputPortsIterator() {
        return inputPorts.iterator();
    }

    public int inputPortsSize() {
        return inputPorts.size();
    }
    
    public Attr getAttribute(String name) {
        Iterator ite = attributes.iterator();
        while(ite.hasNext()) {
            Attr at = (Attr) ite.next();
            if(at.getName().equals(name)) return at;
        }
        return null;
    }
    
    public void addAttribute(Attr attribute) throws NonUniqueNameException {
        attributes.add(attribute);
    }
    
    public void removeAttribute(Attr attribute) {
        attributes.remove(attribute);
        attribute.removeReferences();
    }
    
    public Iterator attributesIterator() {
        return attributes.iterator();
    }

    public Iterator getAllConnectedNodes() {
        List<Node> ret = new LinkedList<Node>();
        
        ret.addAll(attributes.getList());
        if(outputPort != null) {            
            ret.add(outputPort);            
        }
        ret.addAll(inputPorts);

        return ret.iterator();
    }

    public boolean containsName(String name, int nodetype) {
        switch(nodetype) {            
            case ATTR: {
                return attributes.containsName(name);
            }
        }
        return false; 
    }
    
    //////////////////////////// metodos de referencia ////////////////////////

    public void unlink(Referenciado obj) {
        VirtualPort vp = (VirtualPort) obj;
        removePortAndUpdate(vp);
    }

    // if a version definition is removed, the ports of the associated instance
    // will call this method. Check to see if they are stil acessible
    public void update(Referenciado obj) {
        if(container instanceof Topology) return;
        
        if(container == null) return;  // this happens when a connection is being inserted in a version
        
        VirtualPort vp = (VirtualPort) obj;

        boolean res = VirtualPort.checkPortLocation(vp, 
                (Hver) container);
        if(!res) {            
            removePortAndUpdate(vp);                        
        }
    }

    public void removeReferences() {
        if(outputPort != null) {
            outputPort.removeReference(this);
        }
        for(VirtualPort v : inputPorts) {
            v.removeReference(this);
        }
    }
    
    private void removePortAndUpdate(VirtualPort vp) {
        if(vp.equals(outputPort)) {
            vp.removeReference(this);
            outputPort = null;
        }
        else {
            VirtualPort v = null;
            for(int i = 0;i < inputPorts.size();i++) {
                VirtualPort vv = (VirtualPort) inputPorts.get(i);
                if(vv.equals(vp)) {
                    v = vv;
                    vp.removeReference(this);
                    break;
                }
            }
            if(v != null) {
                inputPorts.remove(v);
            }
        }
        
        if(outputPort == null && inputPorts.size() == 0) {
            if(container instanceof Topology) {
                ((Topology)container).removeConnection(this);
            }   
            else {
                ((Hver)container).removeConnection(this);
            }
        }
    }

    /////////////////////////////// PRIVATE VARIABLES //////////////////////////
    
    // todos esses portos sao referencias
    private VirtualPort outputPort;
    private LinkedList<VirtualPort> inputPorts;
    
    private Edges attributes;
}
