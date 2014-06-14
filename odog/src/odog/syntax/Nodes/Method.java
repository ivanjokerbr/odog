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

public class Method extends Node implements Attributable {

    public Method(String name) {
        super(name);
        
        attributes = new Edges(this);
        language = "";
        codeURL = "";       
    }
    
    /////////////////////////////// PUBLIC METHODS /////////////////////////////
    
    public int getType() {
        return METHOD;
    }
    
    public String toString() {
        return name + " (" + language + "," + codeURL + ")";
    }
  
    public Method clone() {
        Method ret = new Method(name);

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

        ret.setLanguage(language);
        ret.setCodeURL(codeURL);
        
        return ret;
    }

    public String exportXML(int ident) {
        StringBuffer buf = new StringBuffer();
        
        String pad = identForXML(ident);
        buf.append(pad + "<method name=\"" + name + "\" language=\"" + language
                + "\" codeURL=\"" + codeURL + "\">\n");
        attributes.exportXML(buf, ident + 2);
        buf.append(pad + "</method>\n");

        return buf.toString();
    }
    
    public void setLanguage(String lang) {
        language = lang;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setCodeURL(String text) {
        codeURL = text;
    }
    
    public String getCodeURL() {
        return codeURL;
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
    
    public void getAllAttributes(LinkedList toappend) {
        Iterator ite = attributesIterator();
        while(ite.hasNext()) {
            toappend.add(ite.next());
        }
    }
    
    public Object getAttributeValue(String attribute) throws NonExistentAttributeException {
        if(attribute.equals("language")) {
            return new String(language);
        }
        else
        if(attribute.equals("codeURL")) {
            return new String(codeURL);
        }
        else {
            return super.getAttributeValue(attribute);
        }
    }

    public Class getAttributeType(String attribute) throws NonExistentAttributeException {
        if(attribute.equals("language") || attribute.equals("codeURL")) {
            return String.class;
        }
        else {
            return super.getAttributeType(attribute);
        }
    }

    public Iterator getAllConnectedNodes() {
        List<Node> ret = new LinkedList<Node>();
        ret.addAll(attributes.getList());
        
        return ret.iterator();
    }

    ////////////////////////////// attributes //////////////////////////////////

    private String language;
    // this URL is relative to the place that the container resides
    private String codeURL;
    
    private Edges attributes;
}