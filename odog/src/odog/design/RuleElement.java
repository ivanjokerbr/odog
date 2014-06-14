/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.design;

import odog.design.Artifact.ElementType;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Date;
import odog.editor.ArtifactEditors.ArtifactEditor.EditorType;
import odog.ruleChecker.Rule;

/**
 *F
 * @author ivan
 */
public class RuleElement extends Artifact {

    public RuleElement(String name, URL url, Date version, String baseDir) {
        super(name, url, version, baseDir, EditorType.RULE, ElementType.RULE);
    }

    //////////////////// PUBLIC METHODS ////////////////////////////////////////
    
    public static RuleElement newEmptyRule(String artifactName, String designLocation) {
        try {
            File f = new File(designLocation + artifactName + ".rule");
            FileOutputStream fos = new FileOutputStream(f);
            PrintStream ps = new PrintStream(fos);
            
            ps.print("");

            ps.close();
            fos.close();
        }
        catch(IOException ex) {
            System.out.println(ex);
        }
        
        URL url = null;
        try {
            url = new URL("file:" + artifactName + ".rule");
        }
        catch(Exception ex) {
            System.out.println(ex);
        }
        RuleElement r = new RuleElement(artifactName, url, new Date(), designLocation);
        r.getStatus().signalChanged();
        
        return r;
    }
    
    public String exportXML() {
        StringBuffer buf = new StringBuffer();

       buf.append("<artifact name=\"" + name + "\" type=\"rule\"" + 
           " url=\"file:" + url.getFile() + "\" version=\"" +
           status.getVersion().getTime() + "\">\n");

        if(summary != null) {
            buf.append("  <summary><![CDATA[" + summary + "]]></summary>\n");
        }
        buf.append("</artifact>\n");
        
        return buf.toString();
    }
    
    public void setRule(Rule r) {
        rule = r;    
    }
    
    public static boolean testRuleFile(String fname) {
        return Rule.testParse(fname);
    }
    
    public void parseRule() {
        rule = new Rule(name, baseDir + url.getFile());
    }
    
    public Rule getRule() {
        if(rule == null) {
            parseRule();
        }        
        return rule;
    }
    
    //////////////////// private attributes ////////////////////////////////////
    
    private Rule rule;
}