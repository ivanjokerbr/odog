/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.services;

import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author ivan
 */
public class Service {
    
    /** Creates a new instance of Service */
    public Service(String name, String associatedAttribute, String value) {
        this.name = name;
        this.associatedAttribute = associatedAttribute;
        this.value = value;
        
        methods = new LinkedList<Method>();
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public String toString() {
        return name + " for attribute " + associatedAttribute +  " with value " +
            value;
    }
    
    
    public String exportXML() {
        StringBuffer buf = new StringBuffer();
        
        buf.append("<service name=\"" + name + "\" associatedAttribute=\"" +
                associatedAttribute + "\" value=\"" + value + "\">\n");
        
        for(Method m : methods) {
            buf.append(m.exportXML());
        }

        buf.append("</service>\n");
        
        return buf.toString();        
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAssociatedAttribute() {
        return associatedAttribute;
    }

    public void setAssociatedAttribute(String associatedAttribute) {
        this.associatedAttribute = associatedAttribute;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }    
    
    public void addMethod(Method m) {
        for(Method x : methods) {
            if(x.getInterface().equals(m.getInterface())) return;
        }    
        
        methods.add(m);
    }
    
    public Iterator methodsIterator() {
        return methods.iterator();
    }
    
    public void removeMethod(Method m) {
        methods.remove(m);
    }
    
    ////////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    ////////////////////////////// PRIVATE VARIABLES  //////////////////////////
    
    private String name;
    private String associatedAttribute;
    private String value;
    
    private LinkedList<Method> methods;
}
