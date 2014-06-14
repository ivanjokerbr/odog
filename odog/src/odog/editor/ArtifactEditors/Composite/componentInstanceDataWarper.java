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
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.NonUniqueNameException;
import odog.syntax.Nodes.Value;
import xmleditorgenerator.BaseFiles.*;

public class componentInstanceDataWarper extends DataWarper {

    public componentInstanceDataWarper(Object element) {
        dataElement = element;
    }

    /////////////////////////////////////////////////////////////////
        
    public void setElement(String type, Object value)  {
    }

    public Object getElement(String type) {
        return null;
    }

    public Object newElement(String type) {
        if(type.equals("value")) {
            //return new Value("", "", "");
            return new Value("","");
        }
        return null;
    }

    public Iterator elementIterator(String type) {
        if(type.equals("value")) {
            return ((CompInstance)dataElement).valuesIterator();
        }
        return null;
    }
 
    public String addElement(String type, Object obj) {
        if(type.equals("value")) {
            try {
                ((CompInstance) dataElement).addValue((Value)obj);    
            }
            catch(NonUniqueNameException ex) {
                return ex.toString();
            }
        }
        return null;
    }

    public void removeElement(String type, Object obj) {
        if(type.equals("value")) {
            ((CompInstance) dataElement).removeValue((Value) obj);
        }
    }
   
    public String setAttribute(String name, String value) {
        if(name.equals("instanceName")) {
            CompInstance ains = (CompInstance) dataElement;
            Node container = ains.getContainer();
            if(container == null || !container.containsName(value, Node.COMPONENTINSTANCE)) {
                ains.setName("ComponentInstance_" + value);
                ains.setInstanceName(value);
            }
            else {
                return container.getName() + " already have component instance named " + 
                        value;
            }            
        }
        else
        if(name.equals("compName")) {
            ((CompInstance) dataElement).setComponentName(value);
        }
        else
        if(name.equals("libraryURL")) {
            ((CompInstance) dataElement).setLibraryURL(value);
        }

        return null;
    }

    public String getAttribute(String name)  {
        if(name.equals("instanceName")) {
            return ((CompInstance)dataElement).getInstanceName();
        }
        else
        if(name.equals("compName")) {
            return ((CompInstance)dataElement).getComponentName();
        }
        else
        if(name.equals("libraryURL")) {
            return ((CompInstance)dataElement).getLibraryURL();
        }

       return null;
    }
}