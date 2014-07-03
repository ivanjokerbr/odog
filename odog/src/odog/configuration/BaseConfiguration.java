/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.configuration;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Centralisa elementos da configurcao do ambiente
 *
 * @author ivan
 */
public class BaseConfiguration {

    public BaseConfiguration() {
        odogWorkspace = BaseConfiguration.appendSlash(System.getenv("ODOG_WORKSPACE"));
        if(odogWorkspace == null || odogWorkspace.equals("")) {
            System.err.println("ODOG_WORKSPACE not set. Using home directory/odogWorkspace");
            System.exit(1);
        }
        odogLibraries = new LinkedList<String>();
        
        String libs = System.getenv("ODOG_LIBRARIES");
        if(libs != null) {        
            StringTokenizer tk = new StringTokenizer(libs, ":");
            while(tk.hasMoreTokens()) {
                String s = tk.nextToken();
                s = BaseConfiguration.appendSlash(s);     
                addLibraryDir(s);
            }
        }
        
        odogServices = BaseConfiguration.appendSlash(System.getenv("ODOG_SERVICES"));
        if(odogServices == null || odogServices.equals("")) {
            System.err.println("ODOG_SERVICES not set");
            System.exit(1);
        }
        
        codeGenerators = BaseConfiguration.appendSlash(System.getenv("ODOG_CODEGENERATORS"));
        if(codeGenerators == null || codeGenerators.equals("")) {
            System.err.println("ODOG_CODEGENERATORS not set.");
            System.exit(1);
        }
    }

    public static final String [] componentLanguages = { "C" };
    
    public static final String [] dataTypes = { "integer" , "boolean", "float",
        "double", "object", "byte", "string"};

    public static final String [] executionMethods = { "odog_init", "odog_fixpoint", "odog_compute",
        "odog_finish" };
    
    ///////////////////////////////// PUBLIC METHODS ///////////////////////////

    public static String appendSlash(String s) {
        if(s == null) return null;
        
        char c = s.charAt(s.length() - 1);
        if(c != '/') {
            return s + "/";
        }
        return s;
    }

    public String getOdogWorkspace() {
        return odogWorkspace;
    }

    public void setOdogWorkspace(String odogWorkspace) {
        this.odogWorkspace = odogWorkspace;
    }

    public void addLibraryDir(String dir) {
        if(!odogLibraries.contains(dir)) {
            odogLibraries.add(dir);
        }
    }

    public void removeLibraryDir(String dir) {
        odogLibraries.remove(dir);        
    }

    public List<String> getOdogLibraries() {
        return odogLibraries;
    }

    public String getOdogServices() {
        return odogServices;
    }
    
    public String getCodeGenerators() {
        return codeGenerators;
    }
    

    ///////////////////////////////// PRIVATE METHODS ///////////////////////////

    ///////////////////////////////// PRIVATE VARIABLES /////////////////////////

    // Diretorios para buscar as bibliotecas
    private LinkedList<String> odogLibraries;

    // Diretorios para buscar a descricao dos serviceos
    // private LinkedList<String> odogServices;
    private String odogServices;

    // Diretorio com os arquivos de configuracao da ferramenta
    private String odogWorkspace;

    // Diretorio que indica onde esta a lista dos geradores de codigo disponiveis
    private String codeGenerators;
}