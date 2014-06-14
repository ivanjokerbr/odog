/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors.Composite;

import java.util.*;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.Connection;
import odog.syntax.Nodes.Node;
import xmleditorgenerator.BaseFiles.*;

public class connectionDataWarper extends DataWarper {

    public connectionDataWarper(Object element) {
        dataElement = element;
    }

    /////////////////////////////////////////////////////////////////
        
    public void setElement(String type, Object value)  {
    }

    public Object getElement(String type) {
        return null;
    }

    public Object newElement(String type) {
        if(type.equals("attribute")) {
            return new Attr("");
        }
        return null;
    }

    public Iterator elementIterator(String type) {
        if(type.equals("attribute")) {
            return ((Connection)dataElement).attributesIterator();
        }
        return null;
    }
 
    public String addElement(String type, Object obj) {
        if(type.equals("attribute")) {
            try {
                ((Connection)dataElement).addAttribute((Attr) obj);
            }
            catch(Exception ex) {
                return ex.toString();
            }
        }

        return null;
    }

    public void removeElement(String type, Object obj) {
        if(type.equals("attribute")) {
            ((Connection)dataElement).removeAttribute((Attr)obj);
        }
    }
   
    public String setAttribute(String name, String value) {
        if(name.equals("name")) {
            Connection con = (Connection) dataElement;
            Node container = con.getContainer();
            if(container == null || !container.containsName(value, Node.CONNECTION)) {
                con.setName(value);
            }
            else {
                return container.getName() + " already have connection named " + 
                        value;
            }            
        }
        return null;
    }

    public String getAttribute(String name)  {
        if(name.equals("name")) {
            return ((Connection)dataElement).getName();
        }
        return null;
    }
}