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
import odog.syntax.Nodes.Method;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.NonUniqueNameException;
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
        if(type.equals("attribute")) {
             return new Attr("");
        }
        return null;
    }

    public Iterator elementIterator(String type) {
        if(type.equals("attribute")) {
            return ((Method) dataElement).attributesIterator();
        }
        return null;
    }
 
    public String addElement(String type, Object obj) {
        if(type.equals("attribute")) {
            try {
                ((Method) dataElement).addAttribute((Attr) obj);
            }
            catch(NonUniqueNameException ex) {
                return ex.toString();
            }                        
        }
        return null;
    }

    public void removeElement(String type, Object obj) {
        if(type.equals("attribute")) {
            ((Method) dataElement).removeAttribute((Attr) obj);
        }
    }
   
    public String setAttribute(String name, String value) {
        if(name.equals("name")) {
            Method dp = (Method) dataElement;
            Node container = dp.getContainer();
            if(container == null || !container.containsName(value, Node.METHOD)) {
                dp.setName(value);
            }
            else {
                return container.getName() + " already have method named " + value;
            }
        }
        else
        if(name.equals("language")) {
            ((Method) dataElement).setLanguage(value);
        }
        else
        if(name.equals("codeURL")) {
            ((Method) dataElement).setCodeURL(value);
        }
        return null;
    }

    public String getAttribute(String name)  {
        if(name.equals("name")) {
            return ((Method) dataElement).getName();
        }
        else
        if(name.equals("language")) {
            return ((Method) dataElement).getLanguage();
        }
        else
       if(name.equals("codeURL")) {
            return ((Method) dataElement).getCodeURL();
        }

        return "";
    }
}