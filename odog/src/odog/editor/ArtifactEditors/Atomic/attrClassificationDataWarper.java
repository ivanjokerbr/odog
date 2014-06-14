/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors.Atomic;

import java.util.*;
import odog.syntax.Nodes.AttrClass;
import xmleditorgenerator.BaseFiles.*;

public class attrClassificationDataWarper extends DataWarper {

    public attrClassificationDataWarper(Object element) {
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
   
    public String setAttribute(final String name, final String value) {
        if(name.equals("visible")) {
            if(value.equals("true")) {
                ((AttrClass) dataElement).setVisible(true);
            }
            else {
                ((AttrClass) dataElement).setVisible(false);
            }
        }
        else
        if(name.equals("hasData")) {
            if(value.equals("true")) {
                ((AttrClass) dataElement).setWithData(true);
            }
            else {
                ((AttrClass) dataElement).setWithData(false);
            }
        }
        else
        if(name.equals("static")) {
            if(value.equals("true")) {
                ((AttrClass) dataElement).setStatic(true);
            }
            else {
                ((AttrClass) dataElement).setStatic(false);
            }
        }
        return null;
    }

    public String getAttribute(String name)  {
        if(name.equals("visible")) {
            if( ((AttrClass)dataElement).isVisible() ) {
                return "true";
            }
            else {
                return "false";
            }
        }
        else
        if(name.equals("hasData")) {
            if( ((AttrClass)dataElement).hasData() ) {
                return "true";
            }
            else {
                return "false";
            }
        }
        else
        if(name.equals("static")) {
            if( ((AttrClass)dataElement).isStatic() ) {
                return "true";
            }
            else {
                return "false";
            }
        }
        return "";
    }
}