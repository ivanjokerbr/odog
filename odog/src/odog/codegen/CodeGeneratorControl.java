/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.codegen;

import odog.configuration.BaseConfiguration;
import odog.design.Design;
import odog.design.DesignParser;
import odog.design.DesignRepository;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import odog.design.CompositeComponent;
import odog.syntax.Nodes.Topology;

/**
 *
 * @author ivan
 */
public class CodeGeneratorControl {
    
    /** Creates a new instance of CodeGenerator */
    public CodeGeneratorControl(BaseConfiguration conf, DesignRepository repository) {
        String cgfile = conf.getCodeGenerators() + "codegenerators.desc";
        configuration = conf;
        designRepository = repository;
        processPlatforms(cgfile);
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public static void main(String [] args) {
        if(args.length < 2) {
            usage();
            return;
        }
        /*
        BaseConfiguration conf = new BaseConfiguration();
        DesignRepository repository = new DesignRepository(conf);
        CodeGeneratorControl cg = new CodeGeneratorControl(conf, repository);
    
        if(args.length == 3) {
            if(!cg.codeGenerate(args[0], args[1], args[2], null)) {
                System.out.println(cg.getReport());
            }
        }
        else
        if(args.length == 4) {
            if(!cg.codeGenerate(args[0], args[1], args[2], args[3])) {
                System.out.println(cg.getReport());
            }
        }
        else {
            usage();            
            return;
        }*/
    }

    /*
    public boolean codeGenerate(String designFile, String platform, String versionName,
            String topology) {
        Design d = getDesign(designFile);
        if(d == null) {
            report = "CodeGeneratorControl: Error while obtaining design\n" + DesignParser.errorMessage;
            return false;
        }
        return codeGenerate(d, platform, versionName, topology);
    }
*/

    public boolean codeGenerate(Design d, String platform, String versionName, 
            Topology top, CompositeComponent composite) {
        CodeGenerator cg = (CodeGenerator) codeGenerators.get(platform);
        if(cg == null) {
            report = new String("CodeGeneratorControl:  Unknown platform " + platform);
            return false;
        } 

        if(d.getConfiguration().isLibrary()) {
            report = new String("CodeGeneratorControl: Cannot code generate a library");
            return false;
        }
        
        if(top == null || composite == null) {
            report =  "Missing topology for code generation\n";
            return false;
        }
        
        boolean res = cg.codeGenerate(d, top, composite, versionName);
        report = cg.getReport();
        
        return res;
    }
    
    /*
    // This is the last method to be called
    public boolean codeGenerate(Design design, String platform, String versionName,
            String topology) {
        CodeGenerator cg = (CodeGenerator) codeGenerators.get(platform);
        if(cg == null) {
            report = new String("CodeGeneratorControl:  Unknown platform " + platform);
            return false;
        } 

        if(design.getConfiguration().isLibrary()) {
            report = new String("CodeGeneratorControl: Cannot code generate a library");
            return false;
        }

        boolean res;
        if(topology == null) {
            res = cg.codeGenerate(design, versionName);
        }
        else {
            res = cg.codeGenerate(design, topology, versionName);
        }
        report = cg.getReport();
        
        return res;
    }
    */
     
    public String getReport() {
        return report;
    }
    
    public Set<String> getAvailablePlatforms() {
        return codeGenerators.keySet();
    }

    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    private void processPlatforms(String cgfile) {
        codeGenerators = new HashMap<String, CodeGenerator>();
        try {
            File f = new File(cgfile);
            if(!f.exists()) return;
            
            FileReader fr = new FileReader(f);
            char [] buf = new char[(int)f.length()];
            fr.read(buf, 0, (int)f.length());
            
            String [] data = (new String(buf)).split(" |\n");
            
            if(data.length % 2 != 0) {
                System.out.println("Wrong codegenerators.desc format");
                return;
            }
            
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            for(int i = 0;i < data.length;i += 2) {
                try {
                    CodeGenerator instance = (CodeGenerator) (cl.loadClass(data[i+1])).newInstance();
                    codeGenerators.put(data[i], instance);
                }
                catch(Exception ex) {
                    System.out.println(ex);
                }
            }
        } 
        catch(IOException ex) {
            System.out.println(ex);
        }        
    }
    
    private Design getDesign(String file) {
        String designName = null, designLocation = "";

        File f = new File(file);
        if(!f.exists()) {
            System.out.println("Didn't find design " + file);
            usage();
            return null;
        }

        String [] data = file.split("/");
        designName = data[data.length - 1];
        String [] t = designName.split("\\p{Punct}");
        designName = t[0];

        for(int i = 1;i < data.length - 1;i++) {
            designLocation = designLocation + "/" + data[i];
        }
        designLocation = designLocation + "/";
        
        Design d = DesignParser.parseDesign(designName, designLocation, false, true,
            designRepository); 
        
        return d;
    }

    private static void usage() {
        System.err.println("Usage: codegenerator designFile platform version [topologyName]");
    }

    /////////////////////////// PRIVATE VARIABLES //////////////////////////////

    private String report;
    private BaseConfiguration configuration;
    private DesignRepository designRepository;
    private HashMap<String, CodeGenerator> codeGenerators;
}