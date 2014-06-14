/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */

package odog.editor.configuration;

import java.util.*;
import odog.syntax.Nodes.NonUniqueNameException;
import xmleditorgenerator.BaseFiles.*;

public class ruleConfigDataWarper extends DataWarper {

    public ruleConfigDataWarper(Object element) {
        dataElement = element;
    }

    /////////////////////////////////////////////////////////////////
        
    public void setElement(String type, Object value)  {
    }

    public Object getElement(String type) {
        return null;
    }

    public Object newElement(String type) {
        if(type.equals("ruleDescription")) {
            return new RuleDescription("", "");
        }
        return null;
    }

    public Iterator elementIterator(String type) {
        if(type.equals("ruleDescription")) {
            return ((RuleConfiguration)dataElement).ruleDescriptionsIterator();
        }
        return null;
    }
 
    public String addElement(String type, Object obj) {
        if(type.equals("ruleDescription")) {
            try {
                ((RuleConfiguration)dataElement).addRuleDescription(
                        (RuleDescription)obj);
            }
            catch(NonUniqueNameException ex) {
                System.out.println(ex);
            }
        }
        return null;
    }

    public void removeElement(String type, Object obj) {
        if(type.equals("ruleDescription")) {
            ((RuleConfiguration) dataElement).removeRuleDescription(
                    (RuleDescription)obj);
        }
    }
   
    public String setAttribute(String name, String value) {
        if(name.equals("name")) {
            ((RuleConfiguration) dataElement).setName(value);
        }
        return null;
    }

    public String getAttribute(String name)  {
        if(name.equals("name")) {
            return ((RuleConfiguration) dataElement).getName();
        }

       return null;
    }
}