/* 
 * Copyright (c) 2006-2008, Ivan Jeukens
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.codegen.Platform.Multicore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import odog.codegen.CodeGenerator;
import odog.codegen.ISemGenerator;
import odog.codegen.Platform.Multicore.DF.MulticoreDFCodeGenerator;
import odog.codegen.util.FileGenerator;
import odog.codegen.util.FileGeneratorParser;
import odog.configuration.BaseConfiguration;
import odog.design.Artifact;
import odog.design.CompositeComponent;
import odog.design.Design;
import odog.editor.ArtifactEditors.ArtifactEditor.EditorType;
import odog.graph.CompositeComponentGraphNode;
import odog.graph.TopologyGraph;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.Hver;
import odog.syntax.Nodes.Topology;
import odog.syntax.Nodes.Value;

/**
 *
 * @author ivan
 */
public class MulticoreGenerator extends CodeGenerator {

    public MulticoreGenerator() {
        super();
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public boolean codeGenerate(Design d, String toplevelVersion) {
        // 1. find the toplevel topology
        Topology toplevel = null;
        CompositeComponent composite = null;
        for(Artifact art : d.artifacts()) {
            if(art.getEditorType() == EditorType.COMPOSITE) {
                composite = (CompositeComponent) art;
                Topology top = composite.getRootNode();
                if(top == null) continue;
               
                Attr at = top.getAttributeFromTable(top.getName() + ".Toplevel");
                if(at != null) {
                    toplevel = top;
                    break;
                }
            }
        }
        
        if(toplevel == null) {
            report = "Must have one topology with toplevel attribute.";
            return false;
        }
        
        return this.codeGenerate(d, toplevel, composite, toplevelVersion);
    }
    
    public boolean codeGenerate(Design d, Topology toplevel, 
            CompositeComponent composite, String toplevelVersion) {
        // 2. get the version object
        Hver ver = (Hver) toplevel.getVersion(toplevelVersion);
        if(ver == null) {
            report = "Version " + toplevelVersion + " no found in " +
                    toplevel.getName();
            return false;
        }

        // 3. Get attribute value map
        HashMap<Attr,Value> attrValueMap = toplevel.buildValuesTable(ver);

        // 4. Builds the topology graph for all the specification
        TopologyGraph tgraph = new TopologyGraph();
        CompositeComponentGraphNode topNode = tgraph.generateISemGraph(composite, ver, 
                attrValueMap);

        ISemGenerator isemcg = null;        
        if(topNode.ISem().equals("DF")) {
            MulticoreDFCodeGenerator dfcg = new MulticoreDFCodeGenerator(attrValueMap);
            if(!dfcg.codeGenerate(topNode, null, d.getDesignLocation())) {
                report = dfcg.getReport();
                return false;
            }
            isemcg = dfcg;
        }
        else {
            report = "Invalid Interaction Semantic value for Multicore platform;";
            return false;
        }        

        String outputDir = d.getDesignLocation() + ver.getFullName() + "/";
        File f = new File(outputDir);
        if(!f.exists()) {
            f.mkdir();            
        }
        
        // 5. generate the makefile for all the files        
        generateMakefile(d.getDesignLocation(), isemcg, outputDir);

        return true; 
    }
    
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    private void generateMakefile(String designLoc, ISemGenerator isemcg, 
            String outputDir) {
        String codegenDir = BaseConfiguration.appendSlash(System.getenv("ODOG_CODEGENERATORS"));
        FileGenerator makefileg = FileGeneratorParser.parse(codegenDir + 
                "Multicore/FileGenerators/Makefile.xml");        
                
        String includ = " -I" + codegenDir + "Multicore/DF/library ";
        includ += " -I" + codegenDir + "library/\n";
        makefileg.setArgumentValue("includeDirs", includ);
        
        // 2. library directories
        includ = "";        
        includ += " -L" + codegenDir + "Multicore/DF/library ";
        includ += " -L" + codegenDir + "library/\n";
        makefileg.setArgumentValue("libraryDirs", includ);
        
        String objs = "";
        Iterator site = isemcg.getGeneratedObjects().iterator();
        while(site.hasNext()) {
            String object = (String) site.next();
            objs = objs + " " + object + ".o";
        }

        makefileg.setArgumentValue("designLocation", designLoc);
        
        makefileg.setArgumentValue("objects", objs);
        try {
            File f = new File(outputDir + "Makefile");
            FileOutputStream fos = new FileOutputStream(f);
            PrintStream ps = new PrintStream(fos);
            ps.print(makefileg.toString());

            ps.close();
            fos.close();
        }
        catch(Exception ex) {
            System.out.println(ex);
        }
    }
    
}
