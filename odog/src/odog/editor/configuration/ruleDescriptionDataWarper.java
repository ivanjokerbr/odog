/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.configuration;

import java.util.*;
import xmleditorgenerator.BaseFiles.*;

public class ruleDescriptionDataWarper extends DataWarper {

    public ruleDescriptionDataWarper(Object element) {
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
            ((RuleDescription)dataElement).setName(value);
        }
        else
        if(name.equals("libraryURL")) {
           ((RuleDescription)dataElement).setLibraryURL(value);
        }

        return null;
    }

    public String getAttribute(String name)  {
        if(name.equals("name")) {
            return ((RuleDescription)dataElement).getName();
        }
        else
        if(name.equals("libraryURL")) {
            return ((RuleDescription)dataElement).getLibraryURL();
        }

       return null;
    }
}