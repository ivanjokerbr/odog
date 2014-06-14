/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.codegen;

import odog.design.CompositeComponent;
import odog.design.Design;
import odog.syntax.Nodes.Topology;

/**
 *
 * Base class for a codegenerator
 *
 * @author ivan
 */
public abstract class CodeGenerator {
    
    /** Creates a new instance of CodeGenerator */
    public CodeGenerator() {
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public abstract boolean codeGenerate(Design d, String toplevelVersion);
    
    public abstract boolean codeGenerate(Design d, Topology topology, 
            CompositeComponent composite, String toplevelVersion);
    
    public String getReport() {
        return report;
    }
       
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    protected String report;
}
