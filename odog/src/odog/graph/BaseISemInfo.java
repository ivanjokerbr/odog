/*
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2
 * see LICENSE file
 */
package odog.graph;

/**
 *
 * @author ivan
 */
public class BaseISemInfo {
    
    public BaseISemInfo() {        
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }

    public void setCGName(String name) {
        cgname = name;
    }
    
    public String getCGName() {
        return cgname;
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    /////////////////////////// PROTECTED VARIABLES //////////////////////////////
    
    protected int id;
    protected String cgname;
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
}
