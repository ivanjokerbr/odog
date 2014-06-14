/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.codegen.Platform.Host;

import odog.codegen.BoundaryData;
import odog.codegen.CodeGenerator;
import odog.codegen.Platform.Host.DE.DECodeGenerator;
import odog.codegen.Platform.Host.DF.DFCodeGenerator;
import odog.codegen.Platform.Host.SR.SRCodeGenerator;
import odog.codegen.ISemGenerator;
import odog.configuration.BaseConfiguration;
import odog.design.Artifact;
import odog.editor.ArtifactEditors.ArtifactEditor.EditorType;
import odog.design.Design;
import odog.design.CompositeComponent;
import odog.graph.CompositeComponentGraphNode;
import odog.graph.TopologyGraph;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.Hver;
import odog.syntax.Nodes.Topology;
import odog.syntax.Nodes.Value;
import odog.codegen.util.FileGenerator;
import odog.codegen.util.FileGeneratorParser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author ivan
 */
public class HostGenerator extends CodeGenerator {
    
    /** Creates a new instance of Main */
    public HostGenerator() {
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
        Hashtable<Attr,Value> attrValueMap = toplevel.buildValuesTable(ver);

        // 4. Builds the topology graph for all the specification
        TopologyGraph tgraph = new TopologyGraph();
        CompositeComponentGraphNode topNode = tgraph.generateISemGraph(composite, ver, 
                attrValueMap);

        ISemGenerator isemcg = null;
        if(topNode.ISem().equals("DE")) {
            DECodeGenerator decg = new DECodeGenerator(attrValueMap);
            if(!decg.codeGenerate(topNode, d.getDesignLocation())) {
                report = decg.getReport();
                return false;
            }
            isemcg = decg;
        }
        else
        if(topNode.ISem().equals("DF")) {
            DFCodeGenerator dfcg = new DFCodeGenerator(attrValueMap);
            LinkedList <BoundaryData> outsideBinfo = new LinkedList<BoundaryData>();
            if(!dfcg.codeGenerate(topNode, null, d.getDesignLocation(), outsideBinfo)) {
                report = dfcg.getReport();
                return false;
            }
            isemcg = dfcg;
        }
        else 
        if(topNode.ISem().equals("SR")) {
            SRCodeGenerator srcg = new SRCodeGenerator(attrValueMap);
            LinkedList <BoundaryData> outsideBinfo = new LinkedList<BoundaryData>();
            if(!srcg.codeGenerate(topNode, null, d.getDesignLocation(), outsideBinfo)) {
                report = srcg.getReport();
                return false;
            }
            isemcg = srcg;
        }
        else {
            report = "Invalid Interaction Semantic value;";
            return false;
        }        
        
        String loc = System.getenv("ODOG_CODEGENERATORS");
        loc = BaseConfiguration.appendSlash(loc) + "FileGenerators/";
        
        String outputDir = d.getDesignLocation() + ver.getFullName() + "/";
        File f = new File(outputDir);
        if(!f.exists()) {
            f.mkdir();            
        }
        
        // 5. generate the makefile for all the files        
        generateMakefile(d.getDesignLocation(), loc, isemcg, outputDir);

        return true; 
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    private void generateMakefile(String designLoc, String loc, 
            ISemGenerator isemcg, String outputDir) {
        FileGenerator makefileg = FileGeneratorParser.parse(loc + "Makefile.xml");
        String codegenDir = BaseConfiguration.appendSlash(System.getenv("ODOG_CODEGENERATORS"));

        /*
        List<String> clibDirs = isemcg.getClibDirs();
        List<String> clibNames = isemcg.getClibNames();
        List<String> cincludeDirs = isemcg.getCincludeDirs();   
        
        // 1. the include directories
        String includ = "";
        for(String s : cincludeDirs) {
            includ = s + " " + includ;
        }*/
        
        String includ = "-I" + codegenDir + "Host/DE/library ";
        includ += " -I" + codegenDir + "Host/SR/library ";
        includ += " -I" + codegenDir + "Host/DF/library ";
        includ += " -I" + codegenDir + "library/\n";
        makefileg.setArgumentValue("includeDirs", includ);
        
        // 2. library directories
        includ = "";
        /*for(String s : clibDirs) {
            includ = s + " " + includ;
        }*/
        includ += "-L" + codegenDir + "Host/DE/library ";
        includ += " -L" + codegenDir + "Host/SR/library ";
        includ += " -L" + codegenDir + "Host/DF/library ";
        includ += " -L" + codegenDir + "library/\n";
        makefileg.setArgumentValue("libraryDirs", includ);
        
        // 3. the library switch for linkediting
        /*
        includ = "";
        for(String s : clibNames) {
            includ = s + " " + includ;
        }
        makefileg.setArgumentValue("libraries", includ); */

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
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
}
