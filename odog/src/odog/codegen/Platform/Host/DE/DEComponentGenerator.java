/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.codegen.Platform.Host.DE;

import odog.codegen.AtomicComponentGenerator;

/**
 *
 * @author ivan
 */
public class DEComponentGenerator extends AtomicComponentGenerator {
    
    /** Creates a new instance of DEComponentGenerator */
    public DEComponentGenerator() {
        super("Host/DE/FileGenerators/", "atomicComponent.xml");
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////

    public void setScheduleMe(String text) {
        scheduleMe = text;
    }

    public String getComponentText() {
        fileGenerator.setArgumentValue("scheduleMeBody", scheduleMe);       
        return super.getComponentText();
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    private String scheduleMe = "";
}
