/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.services;

/**
 *
 * @author ivan
 */
public class Method {
    
    /** Creates a new instance of Method */
    public Method(String interf) {
        this.setInterf(interf);
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////

    public String exportXML() {
        StringBuffer buf = new StringBuffer();
        buf.append("<method interface=\"" + interf + "\"/>\n");

        return buf.toString();        
    }
    
    public String toString() {
        return interf;
    }
    
    public String getInterface() {
        return interf;
    }
    
    public void setInterf(String interf) {
        this.interf = interf;
    }

    ////////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
   ////////////////////////////// PRIVATE VARIABLES  //////////////////////////
 
    private String interf;
}
