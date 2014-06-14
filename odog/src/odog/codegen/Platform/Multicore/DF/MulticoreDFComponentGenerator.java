/* 
 * Copyright (c) 2006-2008, Ivan Jeukens
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */

package odog.codegen.Platform.Multicore.DF;

import odog.codegen.Platform.Host.DF.DFComponentGenerator;

/**
 *
 * @author ivan
 */
public class MulticoreDFComponentGenerator extends DFComponentGenerator {
    
    public MulticoreDFComponentGenerator(String fileGenDir, String componentTemplate) {
        super(fileGenDir, componentTemplate);
    }
    
   ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public void setYieldTest(String text) {
        yieldTest = text;
    }
    
    public void setInputTest(String text) {
        inputTest = text;        
    }
    
    public void setNumberOfComponents(String text) {
        numberOfComponents = text;
    }
    
    public void setComponentId(String text) {
        componentId = text;
    }
    
    public String getComponentText() {        
        fileGenerator.setArgumentValue("inputTest", inputTest);
        fileGenerator.setArgumentValue("yieldTest", yieldTest);
        fileGenerator.setArgumentValue("componentId", componentId);
        fileGenerator.setArgumentValue("numberOfComponents", numberOfComponents);

        return super.getComponentText();
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    private String yieldTest = "";
    private String inputTest = "";
    private String numberOfComponents = "";
    private String componentId = "";

}
