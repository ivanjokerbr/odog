/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */

package odog.editor.ArtifactEditors.Composite;

import java.util.*;
import odog.syntax.Nodes.CompInstance;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.Connection;
import odog.syntax.Nodes.DefVer;
import odog.syntax.Nodes.ExportedPort;
import odog.syntax.Nodes.Hver;
import odog.syntax.Nodes.Method;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.NonUniqueNameException;
import odog.syntax.Nodes.Reqserv;
import odog.syntax.Nodes.Value;
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
        if(type.equals("compInstance")) {
            return new CompInstance("","","");
        }
        else
        if(type.equals("attribute")) {
            return new Attr("");
        }
        else
        if(type.equals("defVer")) {
            return new DefVer("","","");
        }
        else
        if(type.equals("value")) {
            //return new Value("", "", "");
            return new Value("","");
        }
        else
        if(type.equals("exportedPort")) {
            return new ExportedPort("");
        }
        else
        if(type.equals("connection")) {
            return new Connection("");
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
        if(type.equals("compInstance")) {
            return ((Hver)dataElement).componentInstancesIterator();
        }
        else
        if(type.equals("attribute")) {
            return ((Hver)dataElement).attributesIterator();
        }
        else
        if(type.equals("defVer")) {
            return ((Hver)dataElement).defVersionsIterator();
        }
        else
        if(type.equals("value")) {
            return ((Hver)dataElement).valuesIterator();
        }
        else
        if(type.equals("exportedPort")) {
            return ((Hver)dataElement).portsIterator();
        }
        else
        if(type.equals("connection")) {
            return ((Hver)dataElement).connectionsIterator();
        }
        else
        if(type.equals("method")) {
            return ((Hver)dataElement).methodsIterator();
        }
        else
        if(type.equals("reqserv")) {
            return ((Hver)dataElement).reqservIterator();
        }
        return null;
    }
 
    public String addElement(String type, Object obj) {
        try {
            if(type.equals("compInstance")) {
                ((Hver)dataElement).addComponentInstance((CompInstance) obj);
            }
            else
            if(type.equals("attribute")) {
                ((Hver)dataElement).addAttribute((Attr) obj);
            }
            else
            if(type.equals("defVer")) {
                ((Hver)dataElement).addDefVersion((DefVer) obj);
            }
            else
            if(type.equals("value")) {
                ((Hver)dataElement).addValue((Value) obj);
            }
            else
            if(type.equals("exportedPort")) {
                ((Hver)dataElement).addPort((ExportedPort) obj);
            }
            else
            if(type.equals("connection")) {
                ((Hver)dataElement).addConnection((Connection) obj);
            }
            else
            if(type.equals("method")) {
                ((Hver)dataElement).addMethod((Method) obj);
            }
            else
            if(type.equals("reqserv")) {
                ((Hver)dataElement).addReqserv((Reqserv) obj);
            }
        } 
        catch(NonUniqueNameException ex) {
            return ex.toString();
        }

        return null;
    }

    public void removeElement(String type, Object obj) {
        if(type.equals("compInstance")) {
            ((Hver)dataElement).removeComponentInstance((CompInstance) obj);
        }
        else
        if(type.equals("attribute")) {
            ((Hver)dataElement).removeAttribute((Attr) obj);
        }
        else
        if(type.equals("defVer")) {
            ((Hver)dataElement).removeDefVersion((DefVer) obj);
        }
        else
        if(type.equals("value")) {
            ((Hver)dataElement).removeValue((Value) obj);
        }
        else
        if(type.equals("exportedPort")) {
            ((Hver)dataElement).removePort((ExportedPort) obj);
        }
        else
        if(type.equals("connection")) {
            ((Hver)dataElement).removeConnection((Connection) obj);
        }
        else
        if(type.equals("method")) {
             ((Hver)dataElement).removeMethod((Method) obj);
        }
        else
        if(type.equals("reqserv")) {
            ((Hver)dataElement).removeReqserv((Reqserv) obj);
        }
    }
   
    public String setAttribute(String name, String value) {
        if(name.equals("name")) {
            Hver v = (Hver) dataElement;
            Node container = v.getContainer();
            if(container == null || !container.containsName(value, Node.HVER)) {
                v.setName(value);
            }
            else {
                return container.getName() + " already have version named " + value;
            }
        }
        return null;        
    }

    public String getAttribute(String name)  {
        if(name.equals("name")) {
            return ((Hver) dataElement).getName();
        }

       return null;
    }
}