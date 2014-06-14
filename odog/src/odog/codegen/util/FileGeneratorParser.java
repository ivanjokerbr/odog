/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.codegen.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 *
 * @author ivan
 */
public class FileGeneratorParser extends DefaultHandler {
    
    /** Creates a new instance of FileGeneratorParser */
    public FileGeneratorParser() {
        generator = new FileGenerator();
    }
    
    ///////////////// PUBLIC METHODS ///////////////////////////////////////////

    public FileGenerator getGenerator() {
        return generator;
    }
    
    public void startElement(
            String uri,
            String localName,
            String qName,
            Attributes attributes) {
        Map attmap = getAttributeMap(attributes);
     
        if(qName.equals("fileGenerator")) {

        }
        else
        if(qName.equals("text")) {

        }
        else
        if(qName.equals("argument")) {
            generator.addElement((String)attmap.get("name"), true);
        }
    }

    public void characters(char [] ch, int start, int length) {
        generator.addElement(new String(ch, start, length), false);
    }
    
    public void endElement(
            String uri,
            String localName,
            String qName) {
    }
    
    public void endDocument() {        
    }

    public static FileGenerator parse(String fname) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);

        XMLReader xmlReader = null;
        try {
            SAXParser saxParser = factory.newSAXParser();
            xmlReader = saxParser.getXMLReader();
            xmlReader.setEntityResolver(new DTDResolver());
        } catch (Exception ex) {
            System.err.println(ex);
            System.exit(1);
        }

        FileGeneratorParser parser = new FileGeneratorParser();
        xmlReader.setContentHandler(parser);        
        xmlReader.setErrorHandler(new MyErrorHandler(System.err));

        try {
            xmlReader.parse(convertToFileURL(fname));
        } catch (SAXException se) {
            System.err.println(se.getMessage());
            System.exit(1);
        } catch (IOException ioe) {
            System.err.println(ioe);
            System.exit(1);
        }

        return parser.getGenerator();
    }
    
    //////////////////////////// PRIVATE METHODS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    
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
    
    ///////////////// PRIVATE VARIABLES/////////////////////////////////////////

    private FileGenerator generator;
}
