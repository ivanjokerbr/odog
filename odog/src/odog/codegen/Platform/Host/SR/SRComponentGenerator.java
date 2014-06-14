/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.codegen.Platform.Host.SR;

import odog.codegen.AtomicComponentGenerator;

/**
 *
 * @author ivan
 */
public class SRComponentGenerator extends AtomicComponentGenerator {
    
    public SRComponentGenerator() {
        super("Host/SR/FileGenerators/", "atomicComponent.xml");
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////

    public void setIsAbsent(String text) {
        isAbsentText = text;
    }
    
    public void setSetAbsent(String text) {
        setAbsentText = text;
    }
    
    public String getComponentText() {        
        fileGenerator.setArgumentValue("isAbsentBody", isAbsentText);
         fileGenerator.setArgumentValue("setAbsentBody", setAbsentText);
        
        return super.getComponentText();
    }

    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    private String isAbsentText;
    private String setAbsentText;
}