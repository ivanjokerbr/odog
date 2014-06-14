/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.services;

import java.util.Hashtable;
import java.util.Iterator;

/**
 *
 * @author ivan
 */
public class Services {
    
    /** Creates a new instance of Services */
    public Services() {
        services = new Hashtable<String, Service>();
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public void addService(Service s) {
       if(!services.containsKey(s.getName())) {
           services.put(s.getName(), s);
       } 
    }
    
    public Iterator servicesIterator() {
        return services.values().iterator();
    }
    
    public void removeService(Service s) {
        services.remove(s.getName());
    }
     
    public String exportXML() {
        StringBuffer buf = new StringBuffer();
        
        buf.append("<!DOCTYPE services PUBLIC \"-//ODOGSERVICES//DTD//EN\" "+
                   "\"\">");
        buf.append("<services>\n");
        
        Iterator ite = services.values().iterator();
        while(ite.hasNext()) {
            Service s = (Service) ite.next();
            buf.append(s.exportXML());
        }
        
        buf.append("</services>\n");
        
        return buf.toString();        
    }
    
    ////////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    ////////////////////////////// PRIVATE VARIABLES  //////////////////////////
    
    public Hashtable<String, Service> services;    
}
