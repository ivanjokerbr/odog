/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.syntax.Nodes;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Value extends Node implements Referencia {

    public Value(String type, String data) {
        super(new String("value_" + Value.nextUniqueValue() + Value.nextUniqueValue()));

        this.type = type;
        this.valueExpr = data;
        isDefault = false;
       
        associatedAttributes = new LinkedList<Attr>();
    }

    public Value(String name, String type, String data) {
        this(type, data);
        this.name = name;
        isDefault = false;
        
        associatedAttributes = new LinkedList<Attr>();
    }
    
    static {
        rgenerator = new Random((new Date().getTime()));
    }
    
    private static Random rgenerator;
    
    public static double nextUniqueValue() {
        return rgenerator.nextDouble(); 
    }
    
    ///////////////////////// public methods ///////////////////////////////////
    
    public int getType() {
        return VALUE;
    }
    
    public String toString() {
        return name + " = " + valueExpr;
    }
    
    public Value clone() {
        Value ret = new Value(name, type, valueExpr);        
        // O campo do atributo associado deve ser preenchido externamente, pois
        // normalmente nao se conhece o attributo no momento do clone do valor
        return ret;
    }

    public String exportXML(int ident) {
        StringBuffer buf = new StringBuffer();        
        String pad = identForXML(ident);

        String v = "";
        for(int i = 0;i < valueExpr.length();i++) {
            if(valueExpr.charAt(i) == '"') {
                v += "&quot;";
            }
            else
            if(valueExpr.charAt(i) == '\'') {
                v += "&apos;";
            }
            else
            if(valueExpr.charAt(i) == '&') {
                v += "&amp;";
            }
            else
            if(valueExpr.charAt(i) == '<') {
                v += "&lt;";
            }
            else
            if(valueExpr.charAt(i) == '>') {
                v += "&gt;";
            }
            else {
                v += valueExpr.charAt(i);
            }
        }

        buf.append(pad + "<value type=\"" + type + "\" valueExpr=\"" +
                    v + "\">\n");
         
        // it's a call to getAssociatedAttribute because of default values
        for(int i = 0;i < getAssociatedAttributes().size();i++) {
            buf.append(pad + "  <attrRef completeAttrName=\"" +
                    getAssociatedAttributes().get(i).getFullName() + "\"/>\n");
        }
        buf.append(pad + "</value>\n");

        return buf.toString();
    }

    public void setDefaultValue(boolean value) {
        isDefault = value;
    }
    
    public boolean isDefaultValue() {
        return isDefault;
    }

    public void setValueType(String value) {
        type = value;
    }
    
    public String getValueType() {
        return type;
    }
    
    public void setValueExpr(String value) {
        valueExpr = value;
    }
    
    public String getValueExpr() {
        return valueExpr;
    }

    public boolean addAssociatedAttribute(Attr at) {
        if(at == null) return false;
        
        if(associatedAttributes.contains(at)) return false;
        
        associatedAttributes.add(at);
        at.addReference(this);

        return true;
    }
    
    public List<Attr> getAssociatedAttributes() {
        if(isDefault) {
            LinkedList<Attr> ret = new LinkedList<Attr>();
            ret.add((Attr)this.getContainer());
            return ret;
        }

        return associatedAttributes;
    }

    public void removeAssociatedAttribute(Attr at) {
        if(associatedAttributes.contains(at)) {
            at.removeReference(this);
            associatedAttributes.remove(at);
        }
    }

    public Object getAttributeValue(String attribute) throws NonExistentAttributeException {
        if(attribute.equals("type")) {
            return new String(type);
        }
        else
        if(attribute.equals("valueExpr")) {
            return new String(valueExpr);
        }
        else
        if(attribute.equals("isDefault")) {
            return new Boolean(isDefault);
        }
        else {
            return super.getAttributeValue(attribute);
        }
    }

     public Iterator getAllConnectedNodes() {
        LinkedList l = new LinkedList();
        l.addAll(associatedAttributes);
        return l.iterator();
    }
    
    public Class getAttributeType(String attribute) throws NonExistentAttributeException {
        if(attribute.equals("type") || attribute.equals("valueExpr")) {
            return String.class;
        }
        else
        if(attribute.equals("isDefault")) {
            return Boolean.class;
        }
        else {
            return super.getAttributeType(attribute);
        }
    }

    //******** METODOS RELATIVOS A REFERENCIA QUE UM OBJETO VALUE PODE TER A UM
    //******** ATRIBUTO

    // Quando um atributo for destruido, nao ha necessidade de guardar o valor
    public void unlink(Referenciado obj) {
        Attr attribute = (Attr) obj;
        associatedAttributes.remove(attribute);
        
        if(associatedAttributes.size() == 0) {
            if(container instanceof VersionBase) {
                ((VersionBase) container).removeValue(this);
            }
            else
            if(container instanceof CompInstance) {
                ((CompInstance) container).removeValue(this);
            }
        }
    }

    public void update(Referenciado obj) {
    }

    public void removeReferences() {
        for(int i = 0;i < associatedAttributes.size();i++) {
            associatedAttributes.get(i).removeReference(this);
        }
    }

    ///////////////////////////// private attributes ///////////////////////////

    private String type;
    private String valueExpr;
    private boolean isDefault;
    
    private LinkedList<Attr> associatedAttributes;
}
