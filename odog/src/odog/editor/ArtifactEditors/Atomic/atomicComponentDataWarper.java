/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors.Atomic;

import java.util.*;
import odog.syntax.Nodes.Acomp;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.Dport;
import odog.syntax.Nodes.NonUniqueNameException;
import odog.syntax.Nodes.Ver;
import xmleditorgenerator.BaseFiles.*;

public class atomicComponentDataWarper extends DataWarper {

    public atomicComponentDataWarper(Object element) {
        dataElement = element;
    }

    /////////////////////////////////////////////////////////////////
        
    public void setElement(String type, Object value)  {
    }

    public Object getElement(String type) {
        return null;
    }

    public Object newElement(String type) {
        if(type.equals("dport")) {
            return new Dport("",false, false);
        }
        else
        if(type.equals("attribute")) {
            return new Attr("");
        }
        else
        if(type.equals("version")) {
            return new Ver("");
        }
        return null;
    }

    public Iterator elementIterator(String type) {
        if(type.equals("dport")) {
            return ((Acomp) dataElement).getPorts();
        }
        else
        if(type.equals("attribute")) {
            return ((Acomp) dataElement).getAttributes();
        }
        else
        if(type.equals("version")) {
            return ((Acomp) dataElement).versionsIterator();
        }
        return null;
    }
  
    public String addElement(String type, Object obj) {
        try {
            if(type.equals("dport")) {
                ((Acomp) dataElement).addPort((Dport) obj);
            }
            else
            if(type.equals("attribute")) {
                ((Acomp) dataElement).addAttribute((Attr) obj);
            }
            else
            if(type.equals("version")) {
                ((Acomp) dataElement).addVersion((Ver) obj);
            }
        } 
        catch(NonUniqueNameException ex) {
            return ex.toString();
        }
        return null;
    }

    public void removeElement(String type, Object obj) {
        if(type.equals("dport")) {
            ((Acomp) dataElement).removePort((Dport) obj);
        }
        else
        if(type.equals("attribute")) {
            ((Acomp) dataElement).removeAttribute((Attr) obj);
        }
        else
        if(type.equals("version")) {
            ((Acomp) dataElement).removeVersion((Ver) obj);
        }
    }
   
    public String setAttribute(String name, String value) {
        return null;
    }

    public String getAttribute(String name)  {
        if(name.equals("name")) {
            return ((Acomp) dataElement).getName();
        }
       return null;
    }
}