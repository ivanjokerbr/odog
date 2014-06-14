/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors.Atomic;

import java.util.*;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.Reqserv;
import xmleditorgenerator.BaseFiles.*;

public class reqservDataWarper extends DataWarper {

    public reqservDataWarper(Object element) {
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
        if(name.equals("name")) {
            Reqserv rs = (Reqserv) dataElement;
            Node container = rs.getContainer();
            if(container == null || !container.containsName(value, Node.REQSERV)) {
                rs.setName(value);
            }
            else {
                return container.getName() + " already have service named " + value;
            }
        }        
        return null;
    }

    public String getAttribute(String name)  {
        if(name.equals("name")) {
            return ((Reqserv) dataElement).getName();
        }
        return null;
    }
}