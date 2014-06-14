/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.configuration;

/**
 *
 * @author ivan
 */
public class RuleDescription {
    
    /** Creates a new instance of RuleDescription */
    public RuleDescription(String name, String libURL) {
        this.name = name;
        libraryURL = libURL;
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public String toString() {
        return name;
    }
    
    public String exportXML() {
        return "<ruleDescription name=\"" + name + "\" libraryURL=\"" + 
                libraryURL + "\"/>\n";
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLibraryURL() {
        return libraryURL;
    }

    public void setLibraryURL(String libraryURL) {
        this.libraryURL = libraryURL;
    }
    
    ////////////////////////////// PRIVATE METHODS /////////////////////////////
        

    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    private String name;
    private String libraryURL;
}
