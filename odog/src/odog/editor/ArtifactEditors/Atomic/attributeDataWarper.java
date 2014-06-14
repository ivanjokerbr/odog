/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors.Atomic;

import java.util.*;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.AttrClass;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.Value;
import xmleditorgenerator.BaseFiles.*;

public class attributeDataWarper extends DataWarper {

    public attributeDataWarper(Object element) {
        dataElement = element;
    }

    /////////////////////////////////////////////////////////////////
        
    public void setElement(String type, Object value)  {
        if(type.equals("value")) {
            ((Attr) dataElement).setDefaultValue((Value) value);
        }
        else
        if(type.equals("attrClassification")) {
            ((Attr) dataElement).setClassification((AttrClass) value);
        }
    }

    public Object getElement(String type) {
        if(type.equals("value")) {
            return ((Attr) dataElement).getDefaultValue();
        }
        else
        if(type.equals("attrClassification")) {
            return ((Attr) dataElement).getClassification();
        }
        return null;
    }

    public Object newElement(String type) {
        if(type.equals("attrClassification")) {
            return new AttrClass(false, false, false);
        }
        else
        if(type.equals("value")) {
            //return new Value("", "", "");
            return new Value("","");
        }
        return null;
    }

    public Iterator elementIterator(String type) {
        return null;
    }
 
    public String addElement(String type, Object obj) {
        return null;
    }

    public void removeElement(String type, Object obj) {
    }
   
    public String setAttribute(String name, String value) {
        if(name.equals("name")) { 
            Attr dp = (Attr) dataElement;
            Node container = dp.getContainer();
            if(container == null || !container.containsName(value, Node.ATTR)) {
                dp.setName(value);
            }
            else {
                return container.getName() + " already have attribute named " + value;
            }
        }
        return null;
    }

    public String getAttribute(String name)  {
        if(name.equals("name")) {
            return ((Attr)dataElement).getName();
        }
        return null;
    }
}