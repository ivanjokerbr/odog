/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.configuration;

import java.io.File;
import java.io.FileInputStream;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 *
 * @author ivan
 */
public class DTDResolver implements EntityResolver  {
    
    /** Creates a new instance of DTDResolver */
    public DTDResolver() {
        super();
        location = System.getenv("ODOG_WORKSPACE");
        if(location == null) {
            System.err.println("ODOG_WORKSPACE variable not set");
            System.exit(1);
        }
        location = BaseConfiguration.appendSlash(location) + "dtds/";
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public InputSource resolveEntity (String publicId, String systemId) {
       if(publicId.contains("ODOGDESIGN")) {
           try {
               File f = new File(location + "design.dtd");
               FileInputStream fin = new FileInputStream(f);
               return new InputSource(fin);
           }
           catch(Exception ex) {
               System.out.println(ex);
           }
        }
        else
        if(publicId.contains("ODOGFILEGENERATOR")) {
            try {
               File f = new File(location + "fileGenerator.dtd");
               FileInputStream fin = new FileInputStream(f);
               return new InputSource(fin);
           }
           catch(Exception ex) {
               System.out.println(ex);
           }
        }
        else
        if(publicId.contains("ODOGSYNTAXATOMIC")) {
            try {
               File f = new File(location + "syntaxAtomic.dtd");
               FileInputStream fin = new FileInputStream(f);
               return new InputSource(fin);
           }
           catch(Exception ex) {
               System.out.println(ex);
           }
        }
        else
        if(publicId.contains("ODOGSYNTAXTOPOLOGY")) {
            try {
               File f = new File(location + "syntaxTopology.dtd");
               FileInputStream fin = new FileInputStream(f);
               return new InputSource(fin);
           }
           catch(Exception ex) {
               System.out.println(ex);
           }
        }
        else
        if(publicId.contains("ODOGSERVICES")) {
            try {
               File f = new File(location + "services.dtd");
               FileInputStream fin = new FileInputStream(f);
               return new InputSource(fin);
           }
           catch(Exception ex) {
               System.out.println(ex);
           }
        }

        return null;
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    private String location;
}