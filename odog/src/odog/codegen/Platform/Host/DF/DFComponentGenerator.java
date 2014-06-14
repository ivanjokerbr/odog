/*
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2
 * see LICENSE file
 */
package odog.codegen.Platform.Host.DF;

import odog.codegen.AtomicComponentGenerator;

/**
 *
 * @author ivan
 */
public class DFComponentGenerator extends AtomicComponentGenerator {
    
    public DFComponentGenerator(String fileGenDir, String componentTemplate) {
        super(fileGenDir, componentTemplate);
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
       
    public void setSetSampleRate(String text) {
        setSampleRateText = text;
    }
    
    public void setGetSampleRate(String text) {
        getSampleRateText = text;
    }
    public void setStopExecution(String text) {
        stopExecutionText = text;
    }
    
     public String getComponentText() {        
        fileGenerator.setArgumentValue("setSampleRateBody", setSampleRateText);
        fileGenerator.setArgumentValue("getSampleRateBody", getSampleRateText);
        fileGenerator.setArgumentValue("stopExecutionBody", stopExecutionText);
        
        return super.getComponentText();
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    private String setSampleRateText;
    private String getSampleRateText;
    private String stopExecutionText;
}
