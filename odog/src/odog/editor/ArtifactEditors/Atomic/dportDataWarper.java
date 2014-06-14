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
import odog.syntax.Nodes.Dport;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.NonUniqueNameException;
import xmleditorgenerator.BaseFiles.*;

public class dportDataWarper extends DataWarper {

    public dportDataWarper(Object element) {
        dataElement = element;
    }

    /////////////////////////////////////////////////////////////////
        
    public void setElement(String type, Object value)  {
        if(type.equals("portType")) {
            ((Dport) dataElement).setDataType((Attr) value);
        }
    }

    public Object getElement(String type) {
        if(type.equals("portType")) {
            return ((Dport) dataElement).getDataType();
        }
        return null;
    }

    public Object newElement(String type) {
        if(type.equals("portType")) {
            return Attr.getPortTypeAttribute();
        }
        if(type.equals("attribute")) {
            return new Attr("");
        }
        return null;
    }

    public Iterator elementIterator(String type) {
        if(type.equals("attribute")) {
            return ((Dport) dataElement).attributesIterator();
        }
        return null;
    }
 
    public String addElement(String type, Object obj) {
        if(type.equals("attribute")) {
            try {
                ((Dport) dataElement).addAttribute((Attr) obj);
            }
            catch(NonUniqueNameException ex) {
                return ex.toString();
            }
        }
        return null;
    }

    public void removeElement(String type, Object obj) {
        if(type.equals("attribute")) {
            ((Dport) dataElement).removeAttribute((Attr) obj);
        }
    }
   
    public String setAttribute(String name, String value) {
        if(name.equals("name")) {
            Dport dp = (Dport) dataElement;
            Node container = dp.getContainer();
            if(container == null || !container.containsName(value, Node.DPORT)) {
                dp.setName(value);
            }
            else {
                return container.getName() + " already have data port named " + value;
            }
        }
        else
        if(name.equals("isInput")) {
            if(value.equals("true")) {
                ((Dport) dataElement).setInput(true);
            }
            else
            if(value.equals("false")) {
                ((Dport) dataElement).setInput(false);
            }
            else {
                return "Error: data port must be input or output.";
            }
        }
        else
        if(name.equals("isOutput")) {
            if(value.equals("true")) {
                ((Dport) dataElement).setOutput(true);
            }
            else
            if(value.equals("false")) {
                ((Dport) dataElement).setOutput(false);
            }
            else {
                return "Error: data port must be input or output.";
            }
        }
        return null;
    }

    public String getAttribute(String name)  {
        if(name.equals("name")) {
            return ((Dport) dataElement).getName();
        }
        else
        if(name.equals("isInput")) {
            if(((Dport) dataElement).isInput()) {
                return "true";
            }
            else {
                return "false";
            }
        }
        else
        if(name.equals("isOutput")) {
            if(((Dport) dataElement).isOutput()) {
                return "true";
            }
            else {
                return "false";
            }
        }
        return null;
    }
}