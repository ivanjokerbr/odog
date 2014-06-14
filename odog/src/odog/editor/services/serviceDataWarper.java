/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.services;

import java.util.*;
import xmleditorgenerator.BaseFiles.*;

public class serviceDataWarper extends DataWarper {

    public serviceDataWarper(Object element) {
        dataElement = element;
    }

    /////////////////////////////////////////////////////////////////
        
    public void setElement(String type, Object value)  {
    }

    public Object getElement(String type) {
        return null;
    }

    public Object newElement(String type) {
        if(type.equals("method")) {
            return new Method("");
        }
        return null;
    }

    public Iterator elementIterator(String type) {
        if(type.equals("method")) {
            return ((Service) dataElement).methodsIterator();
        }
        return null;
    }
 
    public String addElement(String type, Object obj) {
        if(type.equals("method")) {
            ((Service) dataElement).addMethod((Method)obj);
        }
        return null;
    }

    public void removeElement(String type, Object obj) {
        if(type.equals("method")) {
            ((Service) dataElement).removeMethod((Method)obj);
        }
    }
   
    public String setAttribute(String name, String value) {
        if(name.equals("name")) {
            ((Service) dataElement).setName(value);
        }
        else
        if(name.equals("associatedAttribute")) {
            ((Service) dataElement).setAssociatedAttribute(value);
        }
        else
        if(name.equals("value")) {
            ((Service) dataElement).setValue(value);
        }

        return null;
    }

    public String getAttribute(String name)  {
        if(name.equals("name")) {
            return ((Service) dataElement).getName();
        }
        else
        if(name.equals("associatedAttribute")) {
            return ((Service) dataElement).getAssociatedAttribute();
        }
        else
        if(name.equals("value")) {
            return ((Service) dataElement).getValue();
        }

        return null;
    }
}