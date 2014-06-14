/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.design;

import odog.design.Artifact.ElementType;
import odog.editor.ArtifactEditors.ArtifactEditor.EditorType;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Date;
import odog.syntax.Nodes.Topology;
import odog.syntax.Parse.TopologyParser;

public class CompositeComponent extends Artifact implements ComponentArtifact {

    public CompositeComponent(String name, URL url, Date version, String baseDir,
            DesignRepository repository) {
        super(name, url, version, baseDir, EditorType.COMPOSITE, 
                ElementType.COMPOSITECOMP);
        designRepository = repository;

        ruleCheckingStatus = new RuleCheckingStatus();
        status.addStatusListener(ruleCheckingStatus);
    }
    
    ////////////////////////////// methods /////////////////////////////////////
    
    public static CompositeComponent newEmptyComponent(String compName, String designLocation,
            DesignRepository designRepository) {
        try {
            File f = new File(designLocation + compName + ".xml");
            FileOutputStream fos = new FileOutputStream(f);
            PrintStream ps = new PrintStream(fos);
            
            ps.print(EMPTY_COMPOSITE_COMPONENT_HEAD);
            ps.print(compName);
            ps.print(EMPTY_COMPOSITE_COMPONENT_TAIL);

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
        CompositeComponent comp = new CompositeComponent(compName, url, new Date(), designLocation,
                designRepository);
        comp.getStatus().signalChanged();
        
        return comp;
    }
    
    public String exportXML() {
        StringBuffer buf = new StringBuffer();

        buf.append("<artifact name=\"" + name + "\" type=\"composite\"" + 
                " url=\"file:" + url.getFile() + "\" version=\"" +
                status.getVersion().getTime() + "\">\n");

        if(summary != null) {
            buf.append("  <summary><![CDATA[" + summary + "]]></summary>\n");
        }
        buf.append(ruleCheckingStatus.exportXML());

        buf.append("</artifact>\n");
        
        return buf.toString();
    }
    
    public Topology getNewInstance() {
        if(rootNode == null || anyArtifactHasChanged()) {
            parse();
        }
        if(TopologyParser.errorMessage != null) {
            System.out.println(TopologyParser.errorMessage);
            return null;
        }
        if(rootNode == null) return null;

        Topology ret = rootNode.clone();
        ret.buildElementTable();
        ret.cloneAssociatedAttributes(rootNode, ret.getAttributeTable());

        return ret;
    }
    
    public Topology getRootNode() {
        if(rootNode == null || anyArtifactHasChanged()) {
            parse();
        }
        return rootNode;
    }
    
    public void setRootNode(Topology root) {
        rootNode = root;
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

    /////////////////////////////// private methods ////////////////////////////

    protected void parse() {        
        Topology node = TopologyParser.parseTopology(baseDir + url.getFile(),
                designRepository);
        if(node == null) {
            System.out.println("CompositeComponent parse: error while parsing.");
            System.out.println(TopologyParser.errorMessage);
            return;
        }

        if(rootNode != null) {  
            // check to see if they are the same

            status.signalChanged();        
        }

        rootNode = node;
        dependsOn.clear();
        dependsVersions.clear();
        for(Artifact art : TopologyParser.deps) {
            addArtifactDependency(art);
        }
    }
    
    /////////////////////////////// attributes /////////////////////////////////
    
    private DesignRepository designRepository;
    private Topology rootNode;
    private RuleCheckingStatus ruleCheckingStatus;
    
    private static final String EMPTY_COMPOSITE_COMPONENT_HEAD = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE topology PUBLIC \"-//ODOGSYNTAXTOPOLOGY//DTD//EN\" "+
            "\"\">\n" +
            "<topology name=\"";

    private static final String EMPTY_COMPOSITE_COMPONENT_TAIL = 
            "\">\n" +
            "</topology>\n";
}
