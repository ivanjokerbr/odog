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

public class methodDataWarper extends DataWarper {

    public methodDataWarper(Object element) {
        dataElement = element;
    }

    /////////////////////////////////////////////////////////////////
        
    public void setElement(String type, Object value)  {
    }

    public Object getElement(String type) {
        return null;
    }

    public Object newElement(String type) {
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
        if(name.equals("interface")) {
            ((Method) dataElement).setInterf(value);
        }

        return null;
    }

    public String getAttribute(String name)  {
        if(name.equals("interface")) {
            return ((Method) dataElement).getInterface();
        }
       return null;
    }
}