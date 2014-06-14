/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.syntax.Parse;

import odog.configuration.DTDResolver;
import odog.syntax.Nodes.Acomp;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.AttrClass;
import odog.syntax.Nodes.Attributable;
import odog.syntax.Nodes.Dport;
import odog.syntax.Nodes.Method;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.NonUniqueNameException;
import odog.syntax.Nodes.Reqserv;
import odog.syntax.Nodes.Value;
import odog.syntax.Nodes.Ver;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 *
 * @author ivan
 */
public class AtomicComponentParser extends DefaultHandler {
    
    /** Creates a new instance of AtomicComponentParser */
    public AtomicComponentParser() {
        errors = new LinkedList();
        processingNodes = new LinkedList();
        valueAttmap = new HashMap();
    }
    
    ///////////////// PUBLIC METHODS ///////////////////////////////////////////
    
    public Acomp getComponent() {
        return component;
    }
    
    public LinkedList getErrors() {
        return errors;
    }
    
    public void startElement(
            String uri,
            String localName,
            String qName,
            Attributes attributes) throws SAXException {
        Map attmap = getAttributeMap(attributes);
        
        if(qName.equals("atomicComponent")) {
            component = new Acomp((String)attmap.get("name")); 
            processingNodes.addFirst(component);
            
            DefaultMutableTreeNode n = new DefaultMutableTreeNode(component.toString());
            currentRoot = n;
        }
        else
        if(qName.equals("dport")) {
            String name = (String) attmap.get("name");    
            Boolean isInput = new Boolean((String) attmap.get("isInput"));
            Boolean isOutput = new Boolean((String) attmap.get("isOutput"));
            
            Dport dport = new Dport(name, isInput.booleanValue(), 
                    isOutput.booleanValue());
            DefaultMutableTreeNode treenode = new DefaultMutableTreeNode(
                    dport.toString());
            currentRoot.add(treenode);
            
            Node n = (Node) processingNodes.peek();            
            if(n instanceof Acomp) {
                Acomp comp = (Acomp) n;
                try {
                    comp.addPort(dport);
                }
                catch(NonUniqueNameException ex) {
                    throw new SAXException(ex.toString());
                }
            }
            else
            if(n instanceof Ver) {
                Ver v = (Ver) n;
                try {
                    v.addPort(dport);
                }
                catch(NonUniqueNameException ex) {
                    throw new SAXException(ex.toString());
                }
            }
            else {
               throw new SAXException("ERROR - node type does not match!");
            }
            processingNodes.addFirst(dport);
            currentRoot = treenode;
        }
        else
        if(qName.equals("attribute")) {
            Attr at = new Attr((String) attmap.get("name"));
            DefaultMutableTreeNode treenode = new DefaultMutableTreeNode(at.toString());
            currentRoot.add(treenode);

            Node n = (Node) processingNodes.peek();
            if(n instanceof Dport) {
                Dport dp = (Dport) n;
                if(portType) {
                    dp.setDataType(at);
                }
                else {
                   try {
                       dp.addAttribute(at);
                    }
                    catch(NonUniqueNameException ex) {
                        throw new SAXException(ex.toString());
                    }
                }
            }
            else
            if(n instanceof Attributable) {
                Attributable atb = (Attributable) n;
                try {
                   atb.addAttribute(at);
                }
                catch(NonUniqueNameException ex) {
                    throw new SAXException(ex.toString());
                }
            }
            else {
                throw new SAXException("startElement: attribute - node type does not match!");
            }

            processingNodes.addFirst(at);
            currentRoot = treenode;
        }
        else
        if(qName.equals("attrClassification")) {
            Boolean isVisible = new Boolean((String) attmap.get("visible"));
            Boolean hasData = new Boolean((String) attmap.get("hasData"));
            Boolean stat = new Boolean((String) attmap.get("static"));

            AttrClass classification = new AttrClass(isVisible.booleanValue(),
                    hasData.booleanValue(), stat.booleanValue());
            DefaultMutableTreeNode treenode = new DefaultMutableTreeNode(
                    classification.toString());
            currentRoot.add(treenode);

            Node n = (Node) processingNodes.peek();
            if(n instanceof Attr) {
                Attr at = (Attr) n;
                at.setClassification(classification);
            }
            else {
                throw new SAXException("startElement: attrClass - node type does not match!");
            }
            processingNodes.addFirst(classification);
            currentRoot = treenode;
        }
        else
        if(qName.equals("version")) {
            Ver v = new Ver((String) attmap.get("name"));
            
            DefaultMutableTreeNode treenode = new DefaultMutableTreeNode(v.toString());
            currentRoot.add(treenode);

            Node n = (Node) processingNodes.peek();
            if(n instanceof Acomp) {
                Acomp comp = (Acomp) n;
                try {
                    comp.addVersion(v);
                }
                catch(NonUniqueNameException ex) {
                    throw new SAXException(ex.toString());
                }
            }
            else {
                throw new SAXException("startElement: version - node type does not match!");
            }
            processingNodes.addFirst(v);
            currentRoot = treenode;
        }
        else
        if(qName.equals("value")) {
            Value v = new Value(new String((String) attmap.get("type")), 
                    new String((String) attmap.get("valueExpr")));
 
            valueAttmap.put(v, new LinkedList<String>());
            
            DefaultMutableTreeNode treenode = new DefaultMutableTreeNode(v.toString());
            currentRoot.add(treenode);
            
            Node n = (Node) processingNodes.peek();
            if(n instanceof Ver) {
                Ver version = (Ver) n;
                try {
                    version.addValue(v);
                }
                catch(NonUniqueNameException ex) {
                    throw new SAXException(ex.toString());
                }
            }
            else
            if(n instanceof Attr) {
                Attr at = (Attr) n;
                at.setDefaultValue(v);
            }
            else {
                throw new SAXException("startElement: value - node type does not match!");
            }

            processingNodes.addFirst(v);
            currentRoot = treenode;
        }
        else
        if(qName.equals("attrRef")) {
            Value value = (Value) processingNodes.peek();
            if(value.isDefaultValue()) return;

            LinkedList<String> list = (LinkedList<String>) valueAttmap.get(value);

            String name = (String) attmap.get("completeAttrName");
            list.add(name);
        }
        else
        if(qName.equals("reqserv")) {
            Reqserv sp = new Reqserv((String) attmap.get("name"));

            DefaultMutableTreeNode treenode = new DefaultMutableTreeNode(sp.toString());
            currentRoot.add(treenode);
            
            Node n = (Node) processingNodes.peek();
            if(n instanceof Ver) {
                Ver v = (Ver) n;
                try {
                    v.addReqserv(sp);
                }
                catch(NonUniqueNameException ex) {
                    throw new SAXException(ex.toString());
                }
            }
            else {
                throw new SAXException("startElement: reqserv - node type does not match!");
            }
            processingNodes.addFirst(sp);
            currentRoot = treenode;
        }
        else
        if(qName.equals("method")) {
            Method m = new Method((String) attmap.get("name"));
            m.setLanguage((String) attmap.get("language"));   
            m.setCodeURL((String) attmap.get("codeURL"));
            
            DefaultMutableTreeNode treenode = new DefaultMutableTreeNode(m.toString());
            currentRoot.add(treenode);

            Node n = (Node) processingNodes.peek();
            if(n instanceof Ver) {
                Ver v = (Ver) n;
                try {
                    v.addMethod(m);
                }
                catch(NonUniqueNameException ex) {
                    throw new SAXException(ex.toString());
                }
            }
            else {
                throw new SAXException("startElement: method - node type does not match!");
            }
            processingNodes.addFirst(m);
            currentRoot = treenode;
        }
        else
        if(qName.equals("portType")) {
            portType = true;
        }
        else {
            throw new SAXException("Unkown element " + qName);
        }
    }
/*
    public void characters(char [] ch, int start, int length) {
        String s = new String(ch, start, length);
    }
  */  
    public void endElement(
            String uri,
            String localName,
            String qName) throws SAXException {
            
        if(qName.equals("portType")  || qName.equals("attrRef")) {
            portType = false;
            return;
        }
        
        Node n = (Node) processingNodes.removeFirst();
        if(qName.equals("atomicComponent")) {
            if(!(n instanceof Acomp)) {                
                throw new SAXException("endElement error: node type not Acomp");
            }
            component.setTreeNode(currentRoot);
            return;
        }

        if(qName.equals("dport")) {            
            if(!(n instanceof Dport)) {
                throw new SAXException("endElement error: node type not Dport");
            }
        }
        else
        if(qName.equals("attribute")) {
            if(!(n instanceof Attr)) {
                throw new SAXException("endElement error: node type not Attr");                
            }
        }
        else
        if(qName.equals("attrClassification")) {
            if(!(n instanceof AttrClass)) {
                throw new SAXException("endElement error: note type not AttrClass");
            }
        }
        else
        if(qName.equals("version")) {
            if(!(n instanceof Ver)) {
                throw new SAXException("endElement error: note type not Ver");
            }
        }
        else
        if(qName.equals("value")) {
            if(!(n instanceof Value)) {
                throw new SAXException("endElement error: note type not Value");
            }
        }
        else
        if(qName.equals("reqserv")) {
            if(!(n instanceof Reqserv)) {
                throw new SAXException("endElement error: note type not Sport");
            }
        }
        else
        if(qName.equals("method")) {
            if(!(n instanceof Method)) {
                throw new SAXException("endElement error: note type not Method");
            }
        }
        currentRoot = (DefaultMutableTreeNode) currentRoot.getParent();
    }

    public void endDocument() {        
        component.buildElementTable();
        
        // uma vez terminado todo o parse, falta setar as referencias para os
        // atributos dentro de um no de valor
        Iterator ite = valueAttmap.keySet().iterator();
        while(ite.hasNext()) {
            Value v = (Value) ite.next();
            LinkedList<String> list = (LinkedList<String>) valueAttmap.get(v);

            for(int i = 0;i < list.size();i++) {
                Attr attribute = (Attr) component.getNode(list.get(i));                
                if(attribute == null) {
                    System.err.println("AtomicComponent Parse error: didn't find attribute " +
                            list.get(i));                    
                }
                v.addAssociatedAttribute(attribute);
            }
        }
    }
    
    // Para propositos de teste
    public static void main(String [] args) {
        if(args.length == 0) return;
        
        Acomp comp =  AtomicComponentParser.parseAtomicComponent(args[0]);

        javax.swing.JFrame frame = new javax.swing.JFrame("Node tree");
        javax.swing.tree.DefaultTreeModel model = 
                new javax.swing.tree.DefaultTreeModel(comp.getTreeNode());
        javax.swing.JTree tree = new javax.swing.JTree(model);
        frame.add(tree);

        frame.setSize(200,300);

        frame.setVisible(true);
    }
    
    public static String errorMessage;
    
    public static Acomp parseAtomicComponent(String fname) {
        errorMessage = null;
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);

        XMLReader xmlReader = null;
        try {
            SAXParser saxParser = factory.newSAXParser();
            xmlReader = saxParser.getXMLReader();
            xmlReader.setEntityResolver(new DTDResolver());
        } catch (Exception ex) {
            errorMessage = ex.toString();
            return null;
        }

        AtomicComponentParser parser = new AtomicComponentParser();
        xmlReader.setContentHandler(parser);
        xmlReader.setErrorHandler(new MyErrorHandler(System.err));

        try {
            xmlReader.parse(convertToFileURL(fname));
        } 
        catch (SAXException se) {
            errorMessage = se.toString();
            return null;
        } 
        catch (IOException ioe) {
            errorMessage = ioe.toString();
            return null;
        }

        LinkedList errors = parser.getErrors();
        if(errors.size() != 0) {
            for(int i = 0;i < errors.size();i++) {
                System.out.println(errors.get(i));
            }
            return null;
        } 
        
        return parser.getComponent();
    }

    ///////////////// PRIVATE METHODS //////////////////////////////////////////
    
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
    
    private LinkedList errors;
    private LinkedList processingNodes;
    
    private HashMap valueAttmap;
    private Acomp component;

    private DefaultMutableTreeNode currentRoot;
    
    private boolean portType;
}
