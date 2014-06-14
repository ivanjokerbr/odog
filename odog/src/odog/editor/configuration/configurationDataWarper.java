/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.configuration;

import odog.design.DesignConfiguration;
import java.util.*;
import odog.syntax.Nodes.NonUniqueNameException;
import xmleditorgenerator.BaseFiles.*;

public class configurationDataWarper extends DataWarper {

    public configurationDataWarper(Object element) {
        dataElement = element;
    }

    /////////////////////////////////////////////////////////////////
        
    public void setElement(String type, Object value)  {
    }

    public Object getElement(String type) {
        return null;
    }

    public Object newElement(String type) {
        if(type.equals("ruleConfig")) {
            return new RuleConfiguration("");
        }
        return null;
    }

    public Iterator elementIterator(String type) {
        if(type.equals("ruleConfig")) {
            return ((DesignConfiguration)dataElement).ruleConfigurationIterator();
        }
        return null;
    }
 
    public String addElement(String type, Object obj) {
        if(type.equals("ruleConfig")) {
            try {
                ((DesignConfiguration)dataElement).addRuleConfiguration(
                        (RuleConfiguration) obj);
            }   
            catch(NonUniqueNameException ex) {
                return ex.toString();
            }
        }
        return null;
    }

    public void removeElement(String type, Object obj) {
        if(type.equals("ruleConfig")) {
            ((DesignConfiguration) dataElement).removeRuleConfiguration(
                    (RuleConfiguration)obj);
        }
    }

    public String setAttribute(String name, String value) {
        if(name.equals("islibrary")) {
            if(value.equals("true")) {
                ((DesignConfiguration)dataElement).setIsLibrary(true);      
            }
            else {
                ((DesignConfiguration)dataElement).setIsLibrary(false);
            }
        }
        return null;
    }

    public String getAttribute(String name)  {
        if(name.equals("islibrary")) {
            return (new Boolean(
                    ((DesignConfiguration) dataElement).isLibrary())).toString();
        }
       return null;
    }
}