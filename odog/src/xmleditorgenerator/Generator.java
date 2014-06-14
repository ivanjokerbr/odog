/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package xmleditorgenerator;

import odog.codegen.util.FileGenerator;
import odog.codegen.util.FileGeneratorParser;

import com.conradroche.matra.decl.AttList;
import com.conradroche.matra.decl.Attribute;
import com.conradroche.matra.decl.DocType;
import com.conradroche.matra.decl.ElementType;
import com.conradroche.matra.dtdparser.DTDParser;
import com.conradroche.matra.exception.DTDException;
import com.conradroche.matra.io.DTDFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author ivan
 */
public class Generator {
    
    /** Creates a new instance of Generator */
    public Generator(String fname, String pkgname, boolean onlygui) {        
        this.onlygui = onlygui;
        this.pkgname = pkgname;
        fileName = fname;                
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public void generateXMLEditor() {
        DTDFile dtdFile = new DTDFile(fileName);
        DTDParser parser = new DTDParser();
        
        try {
            // 1. Faz o parse do arquivo dtd, e constroi as tabelas com os elementos 
            // contidos
            parser.parse(dtdFile);                    
            DocType doctype = parser.getDocType();            
            AttributeList = doctype.getAllAttributes();
	    ElementList = doctype.getElementList();
            
            // 2. Cria os arquivos modelo para o gerador do painel e do data warper
            panelGenerator = FileGeneratorParser.parse(
                    "/home/ivan/odog/XMLEditorGenerator/src/xmleditorgenerator/BaseFiles/" +
                    "panelGenerator.xml");
            dataWarperGenerator = FileGeneratorParser.parse(
                    "/home/ivan/odog/XMLEditorGenerator/src/xmleditorgenerator/BaseFiles/" +
                    "dataWarperGenerator.xml");

            // 3. Gera os arquivos recursivamente            
            processed = new Hashtable<String, Object>();
            
            Enumeration roots = parser.getDocType().getRootElements();            
	    if(roots != null) {
		while( roots.hasMoreElements() ) {
                    String root = (String) roots.nextElement();                    
                    processTree(root);
		}
	    } 
            else {
		System.out.println("No root Element found.");
	    }
        } 
        catch(DTDException dtdEx) {
            System.out.println("Got DTDException while parsing the file - ");
            dtdEx.printStackTrace();
        }        
    }
    
    ////////////////////////////// PRIVATE METHODS //////////////////////////////
    
    private void processTree(String elementName) {
        processed.put(elementName, new Object());
        
        panelGenerator.setArgumentValue("elementName", elementName);
        panelGenerator.setArgumentValue("packageName", pkgname);
        ElementType node = (ElementType) ElementList.get(elementName);
        
        StringBuffer buf = new StringBuffer();
        AttList attlist = (AttList) AttributeList.get(elementName);
	if(attlist != null)  {
	    Enumeration attribs = attlist.getAttribs().elements();
            while(attribs.hasMoreElements()) {
                Attribute attr = (Attribute) attribs.nextElement();
                buf.append("\"" + attr.getAttributeName() + "\"");
                if(attribs.hasMoreElements()) {
                    buf.append(",");
                }
            }
        }
        panelGenerator.setArgumentValue("attributeElements", buf.toString());
        
        StringBuffer listElementsBuf = new StringBuffer();
        StringBuffer listElementsClassBuf = new StringBuffer();
        StringBuffer necessaryElementsBuf = new StringBuffer();
        StringBuffer necessaryElementsClassBuf = new StringBuffer();
        StringBuffer optionalElementsBuf = new StringBuffer();
        StringBuffer optionalElementsClassBuf = new StringBuffer();

        String [] children = node.getChildrenNames();
        if(children != null) {
            for(int i = 0;i < children.length;i++) {
                if(children[i].equals("#PCDATA")) continue;

                if(node.getChildOptionality(children[i]).equals("")) {
                    necessaryElementsBuf.append("\"" + children[i] + "\",");
                    necessaryElementsClassBuf.append(children[i] + "Panel.class,");
                }
                else
                if(node.getChildOptionality(children[i]).equals("?")) {
                    optionalElementsBuf.append("\"" + children[i] + "\",");
                    optionalElementsClassBuf.append(children[i] + "Panel.class,");
                }   
                else
                if(node.getChildOptionality(children[i]).equals("*")) {
                    listElementsBuf.append("\"" + children[i] + "\",");
                    listElementsClassBuf.append(children[i] + "Panel.class,");
                }
            }
        }

        if(listElementsBuf.length() > 0) {
            listElementsBuf.deleteCharAt(listElementsBuf.length() - 1);
        }
        if(listElementsClassBuf.length() > 0) {
            listElementsClassBuf.deleteCharAt(listElementsClassBuf.length() - 1);
        }
        if(necessaryElementsBuf.length() > 0) {
            necessaryElementsBuf.deleteCharAt(necessaryElementsBuf.length() - 1);
        }
        if(necessaryElementsClassBuf.length() > 0) {
            necessaryElementsClassBuf.deleteCharAt(necessaryElementsClassBuf.length() - 1);
        }
        if(optionalElementsBuf.length() > 0) {
            optionalElementsBuf.deleteCharAt(optionalElementsBuf.length() - 1);
        }
        if(optionalElementsClassBuf.length() > 0) {
            optionalElementsClassBuf.deleteCharAt(optionalElementsClassBuf.length() - 1);
        }

        panelGenerator.setArgumentValue("listElements", 
                listElementsBuf.toString());
        panelGenerator.setArgumentValue("listElementsClass", 
                listElementsClassBuf.toString());
        panelGenerator.setArgumentValue("listElementsLabels", 
                listElementsBuf.toString());

        panelGenerator.setArgumentValue("necessaryElements", 
                necessaryElementsBuf.toString());
        panelGenerator.setArgumentValue("necessaryElementsClass", 
                necessaryElementsClassBuf.toString());
        panelGenerator.setArgumentValue("necessaryElementsLabel", 
               necessaryElementsBuf.toString());

        panelGenerator.setArgumentValue("optionalElements", 
                optionalElementsBuf.toString());
        panelGenerator.setArgumentValue("optionalElementsClass", 
                optionalElementsClassBuf.toString());
        panelGenerator.setArgumentValue("optionalElementsLabel",
                optionalElementsBuf.toString());

        try {
            File f = new File(elementName + "Panel.java");
            FileOutputStream fos = new FileOutputStream(f);
            PrintStream ps = new PrintStream(fos);

            ps.print(panelGenerator.toString());

            ps.close();
            fos.close();
        }
        catch(IOException ex) {
            System.out.println(ex); 
        }

        if(!onlygui) {
            dataWarperGenerator.setArgumentValue("elementName", elementName);
            dataWarperGenerator.setArgumentValue("packageName", pkgname);
            
            // 1. Corpo das funcoes de atributo
            buf = new StringBuffer();            
            attlist = (AttList) AttributeList.get(elementName);
	    if(attlist != null)  {
	        Enumeration attribs = attlist.getAttribs().elements();
                while(attribs.hasMoreElements()) {
                    Attribute attr = (Attribute) attribs.nextElement();
                    buf.append("\n        if(name.equals(\"" + attr.getAttributeName() +
                            ("\")) {\n\n        }\n"));
                    if(attribs.hasMoreElements()) {
                        buf.append("        else");
                    }
                }
            }
            dataWarperGenerator.setArgumentValue("setAttributeBody", buf.toString());
            dataWarperGenerator.setArgumentValue("getAttributeBody", buf.toString());
            
            StringBuffer opbuf = new StringBuffer();
            StringBuffer necbuf = new StringBuffer();
            StringBuffer listbuf = new StringBuffer();
            
            if(children != null) {
                for(int i = 0;i < children.length;i++) {
                    if(children[i].equals("#PCDATA")) continue;

                    if(node.getChildOptionality(children[i]).equals("")) {
                        necbuf.append("\n        if(type.equals(\"" + children[i] +
                            ("\")) {\n\n        }\n"));
                        necbuf.append("        else");
                    }
                    else
                    if(node.getChildOptionality(children[i]).equals("?")) {
                        opbuf.append("\n        if(type.equals(\"" + children[i] +
                            ("\")) {\n\n        }\n"));
                        opbuf.append("        else");
                    }   
                    else
                    if(node.getChildOptionality(children[i]).equals("*")) {
                        listbuf.append("\n        if(type.equals(\"" + children[i] +
                            ("\")) {\n\n        }\n"));
                        listbuf.append("        else");
                    }
                }                    
            }
            
            if(necbuf.length() > 0) {
                necbuf.delete(necbuf.length() - 13, necbuf.length());
            }
            if(opbuf.length() > 0) {
                opbuf.delete(opbuf.length() - 13, opbuf.length());
            }
            if(listbuf.length() > 0) {
                listbuf.delete(listbuf.length() - 13, listbuf.length());
            }

            dataWarperGenerator.setArgumentValue("setElementBody", opbuf.toString() + 
                    necbuf.toString());
            dataWarperGenerator.setArgumentValue("getElementBody", opbuf.toString() + 
                    necbuf.toString());
            dataWarperGenerator.setArgumentValue("newElementBody", necbuf.toString() + 
                    opbuf.toString() + listbuf.toString());
            dataWarperGenerator.setArgumentValue("elementIteratorBody", listbuf.toString());
            dataWarperGenerator.setArgumentValue("addElementBody", listbuf.toString());
            dataWarperGenerator.setArgumentValue("removeElementBody", listbuf.toString());

            try {
                File f = new File(elementName + "DataWarper.java");
                FileOutputStream fos = new FileOutputStream(f);
                PrintStream ps = new PrintStream(fos);

                ps.print(dataWarperGenerator.toString());

                ps.close();
                fos.close();
            }
            catch(IOException ex) {
                System.out.println(ex); 
            }               
        }
        
        if(children != null) {
            for(int i = 0;i < children.length;i++) { 
                if(children[i].equals("#PCDATA")) continue;

                if(!processed.containsKey(children[i])) {
                    processTree(children[i]);
                }
            }
        }
        
    }

    /////////////////////////// PRIVATE VARIABLES //////////////////////////////    
    
    private boolean onlygui;
    private String fileName;
    private String pkgname;
    
    private Hashtable ElementList;
    private Hashtable AttributeList;

    private Hashtable<String, Object> processed;
    
    private FileGenerator panelGenerator;
    private FileGenerator dataWarperGenerator;    
}
