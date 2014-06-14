/*
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2
 * see LICENSE file
 */
package odog.design;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Stores permanent information regarding the checking of rules of an component.
 * 
 * @author ivan
 */
public class RuleCheckingStatus implements MetaArtifactStatusListener {
    
    public RuleCheckingStatus() {
        rulesApplied = new LinkedList<String>();
        checked = false;
    }

    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public String exportXML() {
        String ret;
        if(!checked) {
            ret = "<ruleCheckingStatus checked=\"false\" passed=\"false\"" +
                    " versionChecked=\"0\" ruleConfiguration=\"\"/>\n";
        }
        else {
            ret = "<ruleCheckingStatus checked=\"" + checked + "\" passed=\"" +
                passed + "\" versionChecked=\"" + versionChecked.getTime() + 
                "\" ruleConfiguration=\"" + ruleConfiguration + "\">\n";    
            if(!passed) {
                ret = ret + "<report><![CDATA[" + report + "]]></report>\n";
            }
            ret = ret +  "</ruleCheckingStatus>\n";
        }
       
        return ret;
    }
    
    public void statusChanged(MetaArtifactStatus obj) {
        checked = false;
        rulesApplied.clear();
        versionChecked = null;
        passed = false;
        ruleConfiguration = null;
        report = null;
    }

    public boolean wasChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public Date getVersionChecked() {
        return versionChecked;
    }

    public void setVersionChecked(Date versionChecked) {
        this.versionChecked = versionChecked;
    }

    public boolean hasPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }    
    
    public void addRule(String rule) {
        rulesApplied.add(rule);
    }

    public boolean containsRule(String rule) {
        return rulesApplied.contains(rule);
    }
    
    public List<String> getRules() {
        return rulesApplied;
    }
    
    public void setRuleConfiguration(String ruleConfiguration) {
        this.ruleConfiguration = ruleConfiguration;
    }
    
    public String getRuleConfiguration() {
        return ruleConfiguration;
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
            
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    private boolean checked;
    private boolean passed;
    
    // the date of the artifact when it was checked
    private Date versionChecked;
    private String ruleConfiguration;

    private LinkedList<String> rulesApplied;
    
    private String report;
}
