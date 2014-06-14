/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.syntax.Nodes;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;

public class Dport extends VirtualPort implements Attributable, Comparable {

    public Dport(String name, boolean isInput, boolean isOutput) {
        super(name);
     
        this.isInput = isInput;
        this.isOutput= isOutput;

        attributes = new Edges(this);
    }

    ///////////////////////////////////////////////////////////////////////////

    public int compareTo(Object obj) {
        VirtualPort vp = (VirtualPort) obj;

        return getFullName().compareTo(vp.getFullName());
    }

    public int getType() {
        return DPORT;
    }
    
    public String toString() {
        return getFullName();
    }
    
    public Dport clone() {
        Dport ret = new Dport(name, isInput, isOutput);

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

        if(dataType != null) {
            ret.setDataType(dataType.clone());
        }

        return ret;
    }
    
    public String exportXML(int ident) {
        StringBuffer buf = new StringBuffer();

        String pad = identForXML(ident);
        
        buf.append(pad + "<dport name=\"" + name + "\" isInput=\"" +
            isInput + "\" isOutput=\"" + isOutput + "\">\n");
        
        attributes.exportXML(buf, ident + 2);
        
        buf.append(pad + "<portType>\n");
        if(dataType != null) {
            buf.append(dataType.exportXML(ident + 2));
        }
        buf.append(pad + "</portType>\n");
        buf.append(pad + "</dport>\n");

        return buf.toString();
    }
    
    public boolean isInput() {
        return isInput;
    }
    
    public boolean isOutput() {
        return isOutput;
    }
    
    public void setInput(boolean value) {
        isInput = value;
    }
    
    public void setOutput(boolean value) {
        isOutput = value;
    }
    
    public Attr getDataType() {
        return dataType;
    }
    
    public void setDataType(Attr type) {
        dataType = type;
        dataType.setContainer(this);
    }
    
    public Attr getAttribute(String name) {
        return (Attr) attributes.find(name);
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
    
    public Object getAttributeValue(String attribute) throws NonExistentAttributeException {
        if(attribute.equals("isInput")) {
            return new Boolean(isInput);
        }
        else
        if(attribute.equals("isOutput")) {
            return new Boolean(isOutput);
        }
        else {
            return super.getAttributeValue(attribute);
        }
    }
    
    public Class getAttributeType(String attribute) throws NonExistentAttributeException {
        if(attribute.equals("isInput") || attribute.equals("isOutput")) {
            return Boolean.class;
        }
        else {
            return super.getAttributeType(attribute);
        }
    }
        
    public Iterator getAllConnectedNodes() {
        List<Node> ret = new LinkedList<Node>();
        
        ret.addAll(attributes.getList());
        if(dataType != null) {
            ret.add(dataType);
        }
        
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
    
    ////////////////////////////////// private attributes //////////////////////
    
    private Edges attributes;
    
    private boolean isInput;
    private boolean isOutput;

    private Attr dataType;
}