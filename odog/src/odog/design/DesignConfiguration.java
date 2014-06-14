/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.design;

import odog.editor.configuration.RuleConfiguration;
import odog.syntax.Nodes.NonUniqueNameException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author ivan
 */
public class DesignConfiguration {
    
    /** Creates a new instance of DesignConfiguration */
    public DesignConfiguration(boolean islib) {
        isLibrary = islib;
        ruleConfigurations = new LinkedList<RuleConfiguration>();
    }

    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public String exportXML() {
        StringBuffer buf = new StringBuffer();               
        buf.append("<configuration islibrary=\"" + isLibrary + "\">\n");
        
        for(RuleConfiguration rc : ruleConfigurations) {
            buf.append(rc.exportXML());
        }        

        buf.append("</configuration>\n");

        return buf.toString();
    }

    public boolean isLibrary() {
        return isLibrary;
    }
    
    public void setIsLibrary(boolean value) {
        isLibrary = value;
    }
        
    public void addRuleConfiguration(RuleConfiguration rc) throws NonUniqueNameException {
        for(RuleConfiguration r : ruleConfigurations) {
            if(r.getName().equals(rc.getName())) {
                throw new NonUniqueNameException("already contains rule configuration " +
                        "named " + rc.getName());
            }
        }
        ruleConfigurations.add(rc);
    }
    
    public void removeRuleConfiguration(RuleConfiguration rc) {
        ruleConfigurations.remove(rc);
    }
    
    public RuleConfiguration getRuleConfiguration(String name) {
        for(RuleConfiguration rc : ruleConfigurations) {
            if(rc.getName().equals(name)) return rc;
        }
        return null;
    }
    
    public Iterator ruleConfigurationIterator() {
        return ruleConfigurations.iterator();
    }
    
    public Object[] ruleConfigurationArray() {
        return ruleConfigurations.toArray();
    }

    //////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    // determina se o design eh um projeto ou uma biblioteca de componentes
    private boolean isLibrary;
    
    // armazena todos os cenarios de checagem de regras
    private LinkedList<RuleConfiguration> ruleConfigurations;
}
