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
import odog.syntax.Nodes.Method;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.NonUniqueNameException;
import odog.syntax.Nodes.Reqserv;
import odog.syntax.Nodes.Value;
import odog.syntax.Nodes.Ver;
import xmleditorgenerator.BaseFiles.*;

public class versionDataWarper extends DataWarper {

    public versionDataWarper(Object element) {
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
            return new Dport("", false, false);
        }
        else
        if(type.equals("value")) {
            //return new Value("", "", "");
            return new Value("","");
        }
        else
        if(type.equals("attribute")) {
            return new Attr("");
        }
        else
        if(type.equals("method")) {
            return new Method("");
        }
        else
        if(type.equals("reqserv")) {
            return new Reqserv("");
        }                

        return null;
    }

    public Iterator elementIterator(String type) {
        if(type.equals("dport")) {
           return ((Ver) dataElement).portsIterator();
        }
        else
        if(type.equals("value")) {
            return ((Ver) dataElement).valuesIterator();
        }
        else
        if(type.equals("attribute")) {
            return ((Ver) dataElement).attributesIterator();
        }
        else
        if(type.equals("method")) {
            return ((Ver) dataElement).methodsIterator();
        }
        else
        if(type.equals("reqserv")) {
            return ((Ver) dataElement).reqservIterator();
        }
        return null;
    }
 
    public String addElement(String type, Object obj) {
        if(type.equals("dport")) {
            try {
                ((Ver)dataElement).addPort((Dport) obj);
            }
            catch(NonUniqueNameException ex) {
                return ex.toString();
            }
        }
        else
        if(type.equals("value")) {
            try {
                ((Ver)dataElement).addValue((Value) obj);
            }
            catch(NonUniqueNameException ex) {
                return ex.toString();
            }
        }
        else
        if(type.equals("attribute")) {
            try {
                ((Ver)dataElement).addAttribute((Attr) obj);
            }
            catch(NonUniqueNameException ex) {
                return ex.toString();
            }
        }
        else
        if(type.equals("method")) {
            try {
                ((Ver)dataElement).addMethod((Method) obj);
            }
            catch(NonUniqueNameException ex) {
                return ex.toString();
            }
        }
        else
        if(type.equals("reqserv")) {
            try {
                ((Ver) dataElement).addReqserv((Reqserv) obj);
            }
            catch(NonUniqueNameException ex) {
                return ex.toString();
            }
        }
        
        return null;
    }

    public void removeElement(String type, Object obj) {
        if(type.equals("dport")) {
            ((Ver)dataElement).removePort((Dport)obj);
        }
        else
        if(type.equals("value")) {
             ((Ver)dataElement).removeValue((Value) obj);
        }
        else
        if(type.equals("attribute")) {
             ((Ver)dataElement).removeAttribute((Attr)obj);
        }
        else
        if(type.equals("method")) {
            ((Ver)dataElement).removeMethod((Method)obj);
        }
        else
        if(type.equals("reqserv")) {
            ((Ver)dataElement).removeReqserv((Reqserv)obj);
        }
    }
   
    public String setAttribute(String name, String value) {
        if(name.equals("name")) {
            Ver dp = (Ver) dataElement;
            Node container = dp.getContainer();
            if(container == null || !container.containsName(value, Node.VER)) {
                dp.setName(value);
            }
            else {
                return container.getName() + " already have version named " + value;
            }
        }
        return null;
    }

    public String getAttribute(String name)  {
        if(name.equals("name")) {
            return ((Ver)dataElement).getName();
        }
        return null;
    }
}