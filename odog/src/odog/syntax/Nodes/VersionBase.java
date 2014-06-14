/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.syntax.Nodes;

import java.util.Iterator;

/**
 *
 * @author ivan
 */
public class VersionBase extends Node implements Attributable {

    /** Creates a new instance of VersionBse */
    public VersionBase(String name) {
        super(name);
        
        ports = new Edges(this);
        values = new Edges(this);
        attributes = new Edges(this);
        methods = new Edges(this);
        reqserv = new Edges(this);
    }
    
    //////////////////////////////// public methods ////////////////////////////
    
    public void addPort(VirtualPort port) throws NonUniqueNameException {
        ports.add(port);
    }
    
    public void removePort(VirtualPort port) {
        ports.remove(port);
        port.removeReferences();
    }

    public Iterator portsIterator() {
        return ports.iterator();
    }

    public VirtualPort findPort(String pname) {
        Iterator ite = ports.iterator();
        while(ite.hasNext()) {
            VirtualPort dp = (VirtualPort) ite.next();
            if(dp.getFullName().equals(pname)) return dp;
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

    public Attr getAttribute(String name) {
        Iterator ite = attributes.iterator();
        while(ite.hasNext()) {
            Attr at = (Attr) ite.next();
            if(at.getName().equals(name)) return at;
        }
        return null;
    }
    
    public void addValue(Value v) throws NonUniqueNameException {
        values.add(v);
    }

    public void removeValue(Value v) {
        values.remove(v);
        v.removeReferences();
    }

    public Iterator valuesIterator() {
        return values.iterator();
    }
    
    public Value getValue(String name) {
        return (Value) values.find(name);
    }

    public void addMethod(Method m) throws NonUniqueNameException {
        methods.add(m);
    }

    public void removeMethod(Method m) {
        methods.remove(m);
        m.removeReferences();
    }

    public Iterator methodsIterator() {
        return methods.iterator();
    }

    public Method getMethod(String name) {
        return (Method) methods.find(name);
    }    
    
    public void addReqserv(Reqserv req) throws NonUniqueNameException {
        reqserv.add(req);
    }

    public void removeReqserv(Reqserv req) {
        reqserv.remove(req);
        req.removeReferences();
    }

    public Iterator reqservIterator() {
        return reqserv.iterator();
    }
    
    /////////////////////// private methods ////////////////////////////////////
    
            
    ///////////////////////////// private attributes ///////////////////////////

    protected Edges ports;
    protected Edges values;
    protected Edges attributes;
    protected Edges methods;
    protected Edges reqserv;    
}
