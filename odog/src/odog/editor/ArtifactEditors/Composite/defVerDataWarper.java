/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors.Composite;

import java.util.*;
import odog.syntax.Nodes.DefVer;
import odog.syntax.Nodes.Node;
import xmleditorgenerator.BaseFiles.*;

public class defVerDataWarper extends DataWarper {

    public defVerDataWarper(Object element) {
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
            DefVer df = (DefVer) dataElement;
            Node container = df.getContainer();
            if(container == null || !container.containsName(value, Node.DEFVER)) {
                df.setName(value);
            }
            else {
                return container.getName() + " already have version definition  " + 
                        value;
            }            
        }
        else
        if(name.equals("instanceName")) {
            ((DefVer)dataElement).setInstanceName(value);
        }
        else
        if(name.equals("versionName")) {
            ((DefVer)dataElement).setVersionName(value);
        }

        return null;
    }

    public String getAttribute(String name)  {
        if(name.equals("name")) {
             return ((DefVer)dataElement).getName();
        }
        else
        if(name.equals("instanceName")) {
             return ((DefVer)dataElement).getInstanceName();
        }
        else
        if(name.equals("versionName")) {
            return ((DefVer)dataElement).getVersionName();
        }

       return null;
    }
}