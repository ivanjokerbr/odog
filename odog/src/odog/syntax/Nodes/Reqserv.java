/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.syntax.Nodes;

public class Reqserv extends Node {

    public Reqserv(String name) {
        super(name);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    public int getType() {
        return REQSERV;
    }
    
    public String toString() {
        return name;
    }
        
    public Reqserv clone() {
        Reqserv ret = new Reqserv(name);
        return ret;
    }
    
    public String exportXML(int ident) {
        StringBuffer buf = new StringBuffer();

        String pad = identForXML(ident);
        buf.append(pad + "<reqserv name=\"" + name + "\"/>\n");
        return buf.toString();
    }

    ///////////////////////////////////////////////////////////////////////////

}
