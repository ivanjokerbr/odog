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

public class servicesDataWarper extends DataWarper {

    public servicesDataWarper(Object element) {
        dataElement = element;
    }

    /////////////////////////////////////////////////////////////////
        
    public void setElement(String type, Object value)  {
    }

    public Object getElement(String type) {
        return null;
    }

    public Object newElement(String type) {
        if(type.equals("service")) {
            return new Service("", "", "");
        }
        return null;
    }

    public Iterator elementIterator(String type) {
        if(type.equals("service")) {
             return ((Services) dataElement).servicesIterator();
        }
        return null;
    }
 
    public String addElement(String type, Object obj) {
        if(type.equals("service")) {
            ((Services) dataElement).addService((Service) obj);
        }
        return null;
    }

    public void removeElement(String type, Object obj) {
        if(type.equals("service")) {
            ((Services) dataElement).removeService((Service) obj);
        }
    }
   
    public String setAttribute(String name, String value) {
        return null;
    }

    public String getAttribute(String name)  {
       return null;
    }
}