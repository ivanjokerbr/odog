/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.services;

import odog.configuration.DTDResolver;
import odog.syntax.Parse.MyErrorHandler;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author ivan
 */
public class ServicesParser extends DefaultHandler {

    public ServicesParser() {
        services = new Services();
    }
        
    ///////////////////////  PUBLIC METHODS ///////////////////////////////////
    
    public Services getServices() {
        return services;
    }
    
    public void startElement(
            String uri, 
            String localName, 
            String qName,
            Attributes attributes) throws SAXException {
        Map attmap = getAttributeMap(attributes);
    
        if(qName.equals("services")) {
            
            
        }
        else
        if(qName.equals("service")) {
            String name = (String) attmap.get("name");
            String associatedAttribute = (String) attmap.get("associatedAttribute");
            String value = (String) attmap.get("value");
            
            Service s = new Service(name, associatedAttribute, value);
            services.addService(s);
            
            currentService = s;
        }
        else 
        if(qName.equals("method")) {
            String interf = (String) attmap.get("interface");
            Method m = new Method(interf);
            currentService.addMethod(m);
        }
        else {
            throw new SAXException("Invalid XML element.");
        }
    }
    
    public void characters(char [] ch, int start, int length) {
    }

    public void endElement(
            String uri,
            String localName,
            String qName) {
    }

    public static String errorMessage;

    public static Services parseServices(String location) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);

        XMLReader xmlReader = null;
        try {
            SAXParser saxParser = factory.newSAXParser();
            xmlReader = saxParser.getXMLReader();
            xmlReader.setEntityResolver(new DTDResolver());
        } catch (Exception ex) {
            errorMessage = ex.getMessage();
            return null;
        }
        
        ServicesParser parser = new ServicesParser();
        xmlReader.setContentHandler(parser);
        xmlReader.setErrorHandler(new MyErrorHandler(System.err));
        
        try {
            xmlReader.parse(convertToFileURL(location));
        } 
        catch (SAXException se) {
            errorMessage = se.getMessage();
            return null;
        } 
        catch (IOException ioe) {
            errorMessage = ioe.getMessage();
            return null;
        }        

        return parser.getServices();
    }
    
    ///////////////////////  PRIVATE METHODS ///////////////////////////////////
    
     private static String convertToFileURL(String filename) {
        String path = new File(filename).getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file:" + path;
    }
    
    private Map getAttributeMap(Attributes attrs) {
        Map map = new HashMap();
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                map.put(attrs.getQName(i), attrs.getValue(i));
            }
        }
        return map;
    }
    
    ///////////////////////  PRIVATE VARIABLES ////////////////////////////////

    private Services services;
    private Service currentService;

}
