/*
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2
 * see LICENSE file
 */
package odog.codegen.Platform.OmapH2;

import odog.codegen.util.FileGenerator;
import odog.codegen.util.FileGeneratorParser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import odog.codegen.BoundaryData;
import odog.codegen.CodeGenerator;
import odog.codegen.ISemGenerator;
import odog.codegen.Platform.OmapH2.DF.DFCodeGenerator;
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
public class OmapH2Generator extends CodeGenerator {
    
    public OmapH2Generator() {
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
    
    public boolean codeGenerate(Design d, Topology toplevel, CompositeComponent composite, 
            String toplevelVersion) {
        // 2. get the version object
        Hver ver = (Hver) toplevel.getVersion(toplevelVersion);
        if(ver == null) {
            report = "Version " + toplevelVersion + " no found in " +
                    toplevel.getName();
            return false;
        }

        // 3. Get attribute value map
        Hashtable<Attr,Value> attrValueMap = toplevel.buildValuesTable(ver);

        // 4. Builds the topology graph for all the specification
        TopologyGraph tgraph = new TopologyGraph();
        CompositeComponentGraphNode topNode = tgraph.generateISemGraph(composite, ver, 
                attrValueMap);

        ISemGenerator isemcg = null;
        if(topNode.ISem().equals("DF")) {
            DFCodeGenerator dfcg = new DFCodeGenerator(attrValueMap);
            LinkedList <BoundaryData> outsideBinfo = new LinkedList<BoundaryData>();
            if(!dfcg.codeGenerate(topNode, null, d.getDesignLocation(), outsideBinfo)) {
                report = dfcg.getReport();
                return false;
            }
            isemcg = dfcg;
        }
        else {
            report = topNode.ISem() + " :Invalid Interaction Semantic";
            return false;
        }        
        
        String outputDir = d.getDesignLocation() + ver.getFullName() + "/";
        File f = new File(outputDir);
        if(!f.exists()) {
            f.mkdir();
        }
        
        String loc = System.getenv("ODOG_CODEGENERATORS");
        loc = BaseConfiguration.appendSlash(loc) + "OmapH2/FileGenerators/";
        // 5. generate the makefile for all the files
        generateARMMakefile(loc, isemcg, outputDir);
        generateDSPMakefile(loc, isemcg, outputDir);
        
        return true;
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////

    private void generateARMMakefile(String loc, ISemGenerator isemcg,  String outputDir) {
        FileGenerator armmakefile = FileGeneratorParser.parse(loc + "ARMMakefile.xml");
        String codegenDir = BaseConfiguration.appendSlash(System.getenv("ODOG_CODEGENERATORS"));

        /*
        List<String> clibDirs = isemcg.getClibDirs();
        List<String> clibNames = isemcg.getClibNames();
        List<String> cincludeDirs = isemcg.getCincludeDirs();
*/
        
        // 1. the include directories
        /*String includ = "";
        for(String s : cincludeDirs) {
            includ = s + " " + includ;
        }*/
        String includ = " -I" + codegenDir + "OmapH2/DF/library ";
        includ += " -I" + codegenDir + "library/\n";
        armmakefile.setArgumentValue("includeDirs", includ);

        // 2. library directories
        includ = "";
        /*for(String s : clibDirs) {
            includ = s + " " + includ;
        }*/
        includ += " -L" + codegenDir + "OmapH2/DF/library ";
        includ += " -L" + codegenDir + "library/\n";
        armmakefile.setArgumentValue("libraryDirs", includ);
        
        // 3. the library switch for linkediting
        /*includ = "";
        for(String s : clibNames) {
            includ = s + " " + includ;
        }
        armmakefile.setArgumentValue("libraries", includ);*/

        String armobjs = "";
        String dspobjs = "";
        Iterator site = isemcg.getGeneratedObjects().iterator();
        while(site.hasNext()) {
            String object = (String) site.next();
            if(isemcg.getObjectBuilder(object).equals("ARM")) {
                armobjs += " " + object + ".o";
            }
        }

        armmakefile.setArgumentValue("objects", armobjs);        
        try {
            File f = new File(outputDir + "ARMMakefile");
            FileOutputStream fos = new FileOutputStream(f);
            PrintStream ps = new PrintStream(fos);
            ps.print(armmakefile.toString());

            ps.close();
            fos.close();
        }
        catch(Exception ex) {
            System.out.println(ex);
        }
    }
    
    private void generateDSPMakefile(String loc, ISemGenerator isemcg,  String outputDir) {
        FileGenerator dspmakefile = FileGeneratorParser.parse(loc + "DSPMakefile.xml");
        String codegenDir = BaseConfiguration.appendSlash(System.getenv("ODOG_CODEGENERATORS"));

        /*
        List<String> clibDirs = isemcg.getClibDirs();
        List<String> clibNames = isemcg.getClibNames();
        List<String> cincludeDirs = isemcg.getCincludeDirs();
*/
        
        // 1. the include directories
        /*String includ = "";
        for(String s : cincludeDirs) {
            includ = s + " " + includ;
        }*/
        String includ = "\n\t-i" + codegenDir + "OmapH2/DF/library \\";
        includ += "\n\t-i" + codegenDir + "library/\n";
        dspmakefile.setArgumentValue("includeDirs", includ);

        // 2. library directories
        includ = "";
        /*for(String s : clibDirs) {
            includ = s + " " + includ;
        }*/
        includ += "\n\t-i" + codegenDir + "OmapH2/DF/library \\";
        includ += "\n\t-i" + codegenDir + "library/\n";
        dspmakefile.setArgumentValue("libraryDirs", includ);
        
        // 3. the library switch for linkediting
        /*includ = "";
        for(String s : clibNames) {
            includ = s + " " + includ;
        }
        dspmakefile.setArgumentValue("libraries", includ); */

        String dspobjs = "";
        Iterator site = isemcg.getGeneratedObjects().iterator();
        while(site.hasNext()) {
            String object = (String) site.next();
            if(isemcg.getObjectBuilder(object).equals("DSP")) {
                dspobjs +=  " " + object + ".obj";
            }
        }

        dspmakefile.setArgumentValue("objects", dspobjs);
        try {
            File f = new File(outputDir + "DSPMakefile");
            FileOutputStream fos = new FileOutputStream(f);
            PrintStream ps = new PrintStream(fos);
            ps.print(dspmakefile.toString());

            ps.close();
            fos.close();
        }
        catch(Exception ex) {
            System.out.println(ex);
        }
    }
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
}
