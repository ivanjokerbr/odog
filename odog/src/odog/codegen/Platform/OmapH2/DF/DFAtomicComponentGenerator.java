/*
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2
 * see LICENSE file
 */
package odog.codegen.Platform.OmapH2.DF;

import odog.codegen.Platform.Host.DF.DFComponentGenerator;

/**
 *
 * @author ivan
 */
public class DFAtomicComponentGenerator extends DFComponentGenerator {
    
    /** Creates a new instance of OmapH2DFAtomicComponentGenerator */
    public DFAtomicComponentGenerator(String fileGeneratorDir, String 
            componentTemplate) {
        super(fileGeneratorDir, componentTemplate);
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    @Override
    public String getComponentText() {        
        fileGenerator.setArgumentValue("myId", myIdText);
        fileGenerator.setArgumentValue("boundaryId", boundaryIdText);
        fileGenerator.setArgumentValue("ncomponents", ncomponentsText);
        fileGenerator.setArgumentValue("nboundary", nboundaryText);
        
        return super.getComponentText();
    }
    
    public void setMyId(String text) {
        myIdText = text;
    }
    
    public void setBoundaryId(String text) {
        boundaryIdText = text;
    }
    
    public void setNcomponents(String text) {
        ncomponentsText = text;
    }

    public void setNboundary(String text) {
        nboundaryText = text;
    }
    
    ////////////////////////////// PRIVATE METHODS //////////////////////////////
    
    ////////////////////////////// PRIVATE VARIABLES ///////////////////////////
    
    private String myIdText;
    private String boundaryIdText;
    private String ncomponentsText;
    private String nboundaryText;
}
