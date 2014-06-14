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
import odog.syntax.Nodes.ExportedPort;
import odog.syntax.Nodes.Hver;
import odog.syntax.Nodes.NonUniqueNameException;
import odog.syntax.Nodes.Topology;
import xmleditorgenerator.BaseFiles.*;

public class topologyDataWarper extends DataWarper {

    public topologyDataWarper(Object element) {
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
        if(type.equals("exportedPort")) {
            return new ExportedPort("");
        }
        else
        if(type.equals("connection")) {
            return new Connection("");
        }
        else
        if(type.equals("version")) {
            return new Hver("");
        }
        return null;
    }

    public Iterator elementIterator(String type) {
        if(type.equals("compInstance")) {
            return ((Topology)dataElement).componentInstancesIterator();
        }
        else
        if(type.equals("attribute")) {
            return ((Topology)dataElement).attributesIterator();
        }
        else
        if(type.equals("exportedPort")) {
            return ((Topology)dataElement).portsIterator();
        }
        else
        if(type.equals("connection")) {
            return ((Topology)dataElement).connectionsIterator();
        }
        else
        if(type.equals("version")) {
            return ((Topology)dataElement).versionsIterator();
        }
        return null;
    }
 
    public String addElement(String type, Object obj) {
        try {
            if(type.equals("compInstance")) {
                ((Topology)dataElement).addComponentInstance((CompInstance)obj);
            }
            else
            if(type.equals("attribute")) {
                ((Topology)dataElement).addAttribute((Attr)obj);
            }
            else
            if(type.equals("exportedPort")) {
                ((Topology)dataElement).addPort((ExportedPort) obj);
            }
            else
            if(type.equals("connection")) {
                ((Topology)dataElement).addConnection((Connection) obj);
            }
            else
            if(type.equals("version")) {
                ((Topology)dataElement).addVersion((Hver) obj);
            }
        } 
        catch(NonUniqueNameException ex) {
            return ex.toString();
        }
        return null;
    }

    @Override
    public void removeElement(String type, Object obj) {
        if(type.equals("compInstance")) {
            ((Topology)dataElement).removeComponentInstance((CompInstance)obj);
        }
        else
        if(type.equals("attribute")) {
            ((Topology)dataElement).removeAttribute((Attr) obj);
        }
        else
        if(type.equals("exportedPort")) {
            ((Topology)dataElement).removePort((ExportedPort) obj);
        }
        else
        if(type.equals("connection")) {
            ((Topology)dataElement).removeConnection((Connection) obj);
        }
        else
        if(type.equals("version")) {
            ((Topology)dataElement).removeVersion((Hver) obj);
        }
    }
   
    public String setAttribute(String name, String value) {        
        return null;
    }

    public String getAttribute(String name)  {
        if(name.equals("name")) {
            return ((Topology)dataElement).getName();
        }
        return null;
    }
}