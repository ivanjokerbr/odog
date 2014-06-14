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
import java.net.*;
import java.util.Date;
import odog.editor.ArtifactEditors.ArtifactEditor.EditorType;
import odog.syntax.Nodes.Acomp;
import odog.syntax.Parse.AtomicComponentParser;

public class AtomicComponent extends Artifact implements ComponentArtifact {

    public AtomicComponent(String name, URL url, Date version, String baseDir) {
        super(name, url, version, baseDir, EditorType.ATOMIC, ElementType.ATOMICCOMP);
        ruleCheckingStatus = new RuleCheckingStatus();
        status.addStatusListener(ruleCheckingStatus);
    }

    ///////////////// PUBLIC METHODS ///////////////////////////////////////////

    public static AtomicComponent newEmptyComponent(String compName, String designLocation) {
        try {
            File f = new File(designLocation + compName + ".xml");
            FileOutputStream fos = new FileOutputStream(f);
            PrintStream ps = new PrintStream(fos);

            ps.print(EMPTY_ATOMIC_COMPONENT_HEAD);
            ps.print(compName);
            ps.print(EMPTY_ATOMIC_COMPONENT_TAIL);

            ps.close();
            fos.close();
        }
        catch(IOException ex) {
            System.out.println(ex);
        }

        URL url = null;
        try {
            url = new URL("file:" + compName + ".xml");
        }
        catch(Exception ex) {
            System.out.println(ex);
        }
        AtomicComponent comp = new AtomicComponent(compName, url, new Date(), designLocation);
        comp.getStatus().signalChanged();
        
        return comp;
    }

    public String exportXML() {
        StringBuffer buf = new StringBuffer();        
        buf.append("<artifact name=\"" + name + "\" type=\"atomic\"" + 
                " url=\"file:" + url.getFile() + "\" version=\"" +
                 status.getVersion().getTime() + "\">\n");

        if(summary != null) {
            buf.append("  <summary><![CDATA[" + summary + "]]></summary>\n");
        }
        buf.append(ruleCheckingStatus.exportXML());

        buf.append("</artifact>\n");
        
        return buf.toString();
    }

    // whenever a component instance must be referenced within an 
    // componentinstance object, clone and add the reference
    public Acomp getNewInstance() {
        if(rootNode == null) {
            parse();
        }
        if(AtomicComponentParser.errorMessage != null) {
            System.out.println(AtomicComponentParser.errorMessage);
            return null;
        }

        if(rootNode == null) {
            return null;
        }

        return rootNode.clone();
    }

    // not to be used for new componentinstances reference
    public Acomp getRootNode() {
        if(rootNode == null) {
            parse();
        }
        if(AtomicComponentParser.errorMessage != null) {
            System.out.println(AtomicComponentParser.errorMessage);
            return null;
        }
        return rootNode;
    }

    public void setRootNode(Acomp comp) {
        rootNode = comp;
    }
    
    public RuleCheckingStatus getRuleCheckingStatus() {
       return ruleCheckingStatus;    
    }
    
    public void setRuleCheckingStatus(RuleCheckingStatus rs) {
        if(ruleCheckingStatus != null) {
            status.removeStatusListener(ruleCheckingStatus);
        }
        ruleCheckingStatus = rs;
        status.addStatusListener(ruleCheckingStatus);
    }

    ////////////////// private methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    
    private void parse() {
        rootNode = AtomicComponentParser.parseAtomicComponent(baseDir + url.getFile());
    }
    
    //////////////////// private attributes ////////////////////////////////////
            
    private Acomp rootNode;
    private RuleCheckingStatus ruleCheckingStatus;
    
    private static final String EMPTY_ATOMIC_COMPONENT_HEAD = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE atomicComponent PUBLIC \"-//ODOGSYNTAXATOMIC//DTD//EN\" "+
                        "\"\">\n" +
            "<atomicComponent name=\"";

    private static final String EMPTY_ATOMIC_COMPONENT_TAIL = 
            "\">\n" +
            "</atomicComponent>\n";
}
