/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.syntax.Nodes;

public class AttrClass extends Node {

    public AttrClass(boolean visible, boolean hasData,
            boolean Static) {
        super("classificaion");

        this.visible = visible;
        this.hasData = hasData;
        this.Static = Static;
    }

    ///////////////////////////////// public methods ///////////////////////

    public int getType() {
        return ATTRCLASS;
    }
    
    public String toString() {
        return "<Classification>";
    }

    public AttrClass clone() {
        AttrClass ret = new AttrClass(visible, hasData, Static);
        return ret;
    }
    
    public String exportXML(int ident) {
        StringBuffer buf = new StringBuffer();
        
        String pad = identForXML(ident);
        
        buf.append(pad + "<attrClassification visible=\"" + visible +
            "\" hasData=\"" + hasData + "\" static=\"" + Static + "\"/>\n");
        
        return buf.toString();
    }

    public boolean isVisible() {
        return visible;
    }
    
    public boolean hasData() {
        return hasData;
    }
    
    public boolean isStatic() {
        return Static;
    }
    
    public boolean validCombination() {
        if(visible == false && Static == true) return true;
        if(visible == true && hasData == true) return true;

        return false;
    }

    public void setVisible(boolean value) {
        visible = value;
    }
    
    public void setWithData(boolean value) {
        hasData = value;
    }
    
    public void setStatic(boolean value) {
        Static = value;
    }
    
    public Object getAttributeValue(String attribute) throws NonExistentAttributeException {
        if(attribute.equals("visible")) {
            return new Boolean(visible);
        }
        else
        if(attribute.equals("hasData")) {
            return new Boolean(hasData);
        }
        else
        if(attribute.equals("static")) {
            return new Boolean(Static);
        }
        else {
            return super.getAttributeValue(attribute);
        }
    }

    public Class getAttributeType(String attribute) throws NonExistentAttributeException {
        if(attribute.equals("visible") || attribute.equals("hasData") ||
                attribute.equals("static")) {
            return Boolean.class;
        }
        else {
            return super.getAttributeType(attribute);
        }
    }

    ///////////////////////////////// private attributes ///////////////////////
    
    private boolean visible;
    private boolean hasData;
    private boolean Static;
}
