/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.syntax.Parse;

import odog.configuration.BaseConfiguration;
import odog.configuration.DTDResolver;
import odog.design.Artifact;
import odog.design.Design;
import odog.design.AtomicComponent;
import odog.design.CompositeComponent;
import odog.design.DesignRepository;
import odog.syntax.Nodes.CompInstance;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.AttrClass;
import odog.syntax.Nodes.Attributable;
import odog.syntax.Nodes.Connection;
import odog.syntax.Nodes.Hver;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.NonUniqueNameException;
import odog.syntax.Nodes.Reqserv;
import odog.syntax.Nodes.Topology;
import odog.syntax.Nodes.Value;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import odog.syntax.Nodes.Acomp;
import odog.syntax.Nodes.CompBase;
import odog.syntax.Nodes.CompInterface;
import odog.syntax.Nodes.DefVer;
import odog.syntax.Nodes.ExportedPort;
import odog.syntax.Nodes.Method;
import odog.syntax.Nodes.Ver;
import odog.syntax.Nodes.VersionBase;
import odog.syntax.Nodes.VirtualPort;

/**
 *
 * Responsavel por fazer o parse de um arquivo xml representando uma topologia
 *
 * @author ivan
 */
public class TopologyParser extends DefaultHandler {

    /** Creates a new instance of TopologyParser */
    public TopologyParser(DesignRepository repository) {
        super();
        processingNodes = new LinkedList();

        valueAttmap = new HashMap();
        compLibMap = new HashMap();

        this.repository = repository;
        dependsOn = new LinkedList<Artifact>();
    }

    /////////////////////////// PUBLIC ATTRIBUTES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    //////////////////////////// PUBLIC METHODS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    
    public Topology getTopology() {
        return topology;
    }
    
    public List<Artifact> getDependsOn() {
        return dependsOn;
    }

    @Override
    public void startElement(
            String uri,
            String localName,
            String qName,
            Attributes attributes) throws SAXException {
        Map attmap = getAttributeMap(attributes);

        if (qName.equals("topology")) {
            topology = new Topology((String) attmap.get("name"));
            processingNodes.addFirst(topology);

            DefaultMutableTreeNode n = new DefaultMutableTreeNode(topology.toString());
            currentRoot = n;
        } 
        else 
        if (qName.equals("compInstance")) {
            String libURL = (String) attmap.get("libraryURL");
            String compName = (String) attmap.get("compName");

            CompInstance ains = new CompInstance((String) attmap.get("instanceName"),
                    compName, libURL);
            DefaultMutableTreeNode tnode = new DefaultMutableTreeNode(ains.getInstanceName());
            currentRoot.add(tnode);

            Design lib;
            if (compLibMap.containsKey(libURL)) {
                lib = (Design) compLibMap.get(libURL);
            } else {
                lib = repository.findAndParseLib(libURL);
                compLibMap.put(libURL, lib);
            }

            if (lib == null) {
                System.out.println("Could not find library " + libURL);
                return;
            }
            if (lib == null) {
                return;
            }

            Artifact element = lib.getArtifact(compName);
            if (element == null) {  // nao carregou o elemento
                return;
            }

            if (element instanceof AtomicComponent) {
                AtomicComponent comp = (AtomicComponent) element;
                Acomp acomp = comp.getNewInstance();

                tnode.add(acomp.getTreeNode());
                ains.setComponent(acomp);
                
                if(!dependsOn.contains(comp)) {
                    dependsOn.add(comp);
                }
            } 
            else 
            if (element instanceof CompositeComponent) {
                CompositeComponent comp = (CompositeComponent) element;
                Topology top = comp.getNewInstance();

                tnode.add(top.getTreeNode());
                ains.setComponent(top);
                
                if(!dependsOn.contains(comp)) {
                    dependsOn.add(comp);
                }
            }

            Node n = (Node) processingNodes.peek();
            if (n instanceof Topology) {
                Topology t = (Topology) n;
                try {
                    t.addComponentInstance(ains);
                } catch (NonUniqueNameException ex) {
                    throw new SAXException(ex.toString());
                }
            } else if (n instanceof Hver) {
                Hver version = (Hver) n;
                try {
                    version.addComponentInstance(ains);
                } catch (NonUniqueNameException ex) {
                    throw new SAXException(ex.toString());
                }
            } else {
                throw new SAXException("startElement: compInstance, node type does not match!");
            }

            processingNodes.addFirst(ains);
            currentRoot = tnode;
        } 
        else 
        if (qName.equals("value")) {
            Value v = new Value((String) attmap.get("type"),
                    (String) attmap.get("valueExpr"));
            valueAttmap.put(v, new LinkedList<String>());

            DefaultMutableTreeNode tnode = new DefaultMutableTreeNode(v.toString());
            currentRoot.add(tnode);

            Node n = (Node) processingNodes.peek();
            if (n instanceof CompInstance) {
                CompInstance ains = (CompInstance) n;
                try {
                    ains.addValue(v);
                } catch (NonUniqueNameException ex) {
                    throw new SAXException(ex.toString());
                }
            } else if (n instanceof Attr) {
                Attr at = (Attr) n;
                at.setDefaultValue(v);
            } else if (n instanceof Hver) {
                Hver ver = (Hver) n;
                try {
                    ver.addValue(v);
                } catch (NonUniqueNameException ex) {
                    throw new SAXException(ex.toString());
                }
            } else {
                throw new SAXException("startElement: value, node type does not match!");
            }

            processingNodes.addFirst(v);
            currentRoot = tnode;
        } 
        else 
        if (qName.equals("attrRef")) {
            Value value = (Value) processingNodes.peek();
            if (value.isDefaultValue()) {
                return;
            }            
            String name = (String) attmap.get("completeAttrName");
            
            LinkedList<String> list = (LinkedList<String>) valueAttmap.get(value);           
            list.add(name);
        }
        else
        if (qName.equals("attribute")) {
            Attr at = new Attr((String) attmap.get("name"));
            DefaultMutableTreeNode treenode = new DefaultMutableTreeNode(at.toString());
            currentRoot.add(treenode);

            Node n = (Node) processingNodes.peek();

            if (n instanceof Attributable) {
                Attributable atb = (Attributable) n;
                try {
                    atb.addAttribute(at);
                } catch (NonUniqueNameException ex) {
                    throw new SAXException(ex);
                }
            } else {
                throw new SAXException("startElement: attribute - node type does not match!");
            }

            processingNodes.addFirst(at);
            currentRoot = treenode;
        } 
        else 
        if (qName.equals("attrClassification")) {
            Boolean isVisible = new Boolean((String) attmap.get("visible"));
            Boolean hasData = new Boolean((String) attmap.get("hasData"));
            Boolean stat = new Boolean((String) attmap.get("static"));

            AttrClass classification = new AttrClass(isVisible.booleanValue(),
                    hasData.booleanValue(), stat.booleanValue());
            DefaultMutableTreeNode treenode = new DefaultMutableTreeNode(
                    classification.toString());
            currentRoot.add(treenode);

            Node n = (Node) processingNodes.peek();
            if (n instanceof Attr) {
                Attr at = (Attr) n;
                at.setClassification(classification);
            } else {
                throw new SAXException("startElement: attrClass - node type does not match!");
            }

            processingNodes.addFirst(classification);
            currentRoot = treenode;
        } 
        else
        if(qName.equals("connection")) {
            Connection con = new Connection((String) attmap.get("name"));

            DefaultMutableTreeNode treenode = new DefaultMutableTreeNode(con.toString());
            currentRoot.add(treenode);

            Node n = (Node) processingNodes.peek();
            if (n instanceof Topology) {
                Topology t = (Topology) n;
                try {
                    t.addConnection(con);
                } catch (NonUniqueNameException ex) {
                    throw new SAXException(ex);
                }
            } else if (n instanceof Hver) {
                Hver v = (Hver) n;
                try {
                    v.addConnection(con);
                } catch (NonUniqueNameException ex) {
                    throw new SAXException(ex);
                }
            } else {
                throw new SAXException("startElement: connection - node type does not match!");
            }

            processingNodes.addFirst(con);
            currentRoot = treenode;
        }
        else
        if (qName.equals("exportedPort")) {
            Node n = (Node) processingNodes.peek();

            String name = (String) attmap.get("name");
            DefaultMutableTreeNode treenode = new DefaultMutableTreeNode("exportedPort " +
                    name);
            currentRoot.add(treenode);

            ExportedPort ep = new ExportedPort(name);
            if (n instanceof Topology) {
                try {
                    topology.addPort(ep);
                } catch (NonUniqueNameException ex) {
                    throw new SAXException(ex);
                }
            } else if (n instanceof Hver) {
                Hver ver = (Hver) n;
                try {
                    ver.addPort(ep);
                } catch (NonUniqueNameException ex) {
                    throw new SAXException(ex);
                }
            }

            processingNodes.addFirst(ep);
            currentRoot = treenode;
        } 
        else 
        if (qName.equals("method")) {
            Method m = new Method((String) attmap.get("name"));
            m.setLanguage((String) attmap.get("language"));
            m.setCodeURL((String) attmap.get("codeURL"));

            DefaultMutableTreeNode treenode = new DefaultMutableTreeNode(m.toString());
            currentRoot.add(treenode);

            Node n = (Node) processingNodes.peek();
            if (n instanceof Hver) {
                Hver v = (Hver) n;
                try {
                    v.addMethod(m);
                } catch (NonUniqueNameException ex) {
                    throw new SAXException(ex);
                }
            } else {
                throw new SAXException("startElement: method - node type does not match!");
            }
            processingNodes.addFirst(m);
            currentRoot = treenode;
        } 
        else 
        if (qName.equals("portRef")) {
            String pname = (String) attmap.get("completePortName");
            Node n = (Node) processingNodes.peek();

            if(n instanceof Connection) {                
                Connection con = (Connection) n;
                Node conparent = con.getContainer();                                               
            
                VirtualPort vp = topology.findPort(pname);
                if(!(conparent.getType() == Node.HVER)) {
                    if(vp == null) {
                        // inconsistency in the specification
                        return;
                    }
                } 
                else {
                    if (vp == null) {
                        vp = findPortInVersionComponentInstance((Hver) conparent, pname);
                        if (vp == null) {
                            vp = findPortInVersions((Hver) conparent, pname);
                            if (vp == null) {
                                // inconsistency in the specification
                                return;
                            }
                        }
                    }
                }

                try {
                    con.addPort(vp);
                } catch (NonUniqueNameException ex) {
                    throw new SAXException(ex);
                }

                DefaultMutableTreeNode treenode = new DefaultMutableTreeNode("PortRef " +
                        pname);
                currentRoot.add(treenode);
            } 
            else 
            if (n instanceof ExportedPort) {
                ExportedPort ep = (ExportedPort) n;
                Node epparent = ep.getContainer();
               
                VirtualPort vp = topology.findPort(pname);
                if (!(epparent.getType() == Node.HVER)) {
                    if (vp == null) {
                        // inconsistency
                        return;
                    }
                } 
                else {
                    if (vp == null) {
                        vp = findPortInVersionComponentInstance((Hver) epparent, pname);
                        if (vp == null) {
                            vp = findPortInVersions((Hver) epparent, pname);
                            if (vp == null) {
                                // inconsistency
                                return;
                            }
                        }
                    }
                }

                ep.addRefPort(vp);
                DefaultMutableTreeNode treenode = new DefaultMutableTreeNode("PortRef " +
                        pname);
                currentRoot.add(treenode);
            }
        } 
        else
        if (qName.equals("version")) {
            Hver ver = new Hver((String) attmap.get("name"));
            DefaultMutableTreeNode treenode = new DefaultMutableTreeNode(ver.toString());
            currentRoot.add(treenode);

            Node n = (Node) processingNodes.peek();
            if (n instanceof Topology) {
                Topology t = (Topology) n;
                try {
                    t.addVersion(ver);
                } catch (NonUniqueNameException ex) {
                    throw new SAXException(ex);
                }
            } else {
                throw new SAXException("startElement: version - node type does not match!");
            }
            processingNodes.addFirst(ver);
            currentRoot = treenode;
        } 
        else
        if (qName.equals("defVer")) {
            Node n = (Node) processingNodes.peek();
            Hver hv = (Hver) n;

            // Tem que pegar o nome da topologia...
            Topology parent = (Topology) hv.getContainer();

            DefVer dv = new DefVer((String) attmap.get("name"),
                    (String) attmap.get("instanceName"),
                    (String) attmap.get("versionName"));
            processingNodes.addFirst(dv);           

            CompInstance ains = topology.findComponentInstance(dv.getInstanceName());
            if (ains == null) {
                ains = (CompInstance) hv.findComponentInstance(dv.getInstanceName());
                if (ains == null) {
                    // inconsistency
                    return;
                }
            }
            dv.setSelectedInstance(ains);

            CompBase abase = ains.getComponent();
            VersionBase ver = abase.getVersion(dv.getVersionName());
            if (ver == null) {
                // inconsistency
                return;
            }
            dv.setSelectedVersion(ver);

            try {
                hv.addDefVersion(dv);
            } 
            catch (NonUniqueNameException ex) {
                throw new SAXException(ex);
            }
            
            DefaultMutableTreeNode treenode = new DefaultMutableTreeNode(dv.toString());
            currentRoot.add(treenode);

            currentRoot = treenode;
        } 
        else if (qName.equals("reqserv")) {
            Reqserv sp = new Reqserv((String) attmap.get("name"));

            DefaultMutableTreeNode treenode = new DefaultMutableTreeNode(sp.toString());
            currentRoot.add(treenode);

            Node n = (Node) processingNodes.peek();
            if (n instanceof Hver) {
                Hver v = (Hver) n;
                try {
                    v.addReqserv(sp);
                } catch (NonUniqueNameException ex) {
                    throw new SAXException(ex);
                }
            } else {
                throw new SAXException("startElement: reqserv - node type does not match!");
            }

            processingNodes.addFirst(sp);
            currentRoot = treenode;
        } else {
            throw new SAXException("Unkown element " + qName);
        }        
    }

    @Override
    public void endElement(
            String uri,
            String localName,
            String qName) throws SAXException {

        if (qName.equals("portRef") || qName.equals("attrRef")) {
            return;
        }

        Node n = (Node) processingNodes.removeFirst();
        if (qName.equals("topology")) {
            if (!(n instanceof Topology)) {
                throw new SAXException("endElement error: node typ not Topology");
            }
            topology.setTreeNode(currentRoot);
            return;
        }

        if (qName.equals("compInstance")) {
            if (!(n instanceof CompInstance)) {
                throw new SAXException("endElement error: not type not compInstance");
            }
        } else if (qName.equals("value")) {
            if (!(n instanceof Value)) {
                throw new SAXException("endElement error: not type not Value");
            }
        }
        else if (qName.equals("attribute")) {
            if (!(n instanceof Attr)) {
                throw new SAXException("endElement error: not type not attribute");
            }
        } 
        else if (qName.equals("attrClassification")) {
            if (!(n instanceof AttrClass)) {
                throw new SAXException("endElement error: not type not attrClassification");
            }
        } 
        else 
        if(qName.equals("connection")) {
            if (!(n instanceof Connection)) {
                throw new SAXException("endElement error: not type not connection");
            }

            Connection con = (Connection) n;
            // due to inconsistencies, this connection has no ports
            if(con.getPorts().size() == 0 || con.getOutputPort() == null ||
                    con.inputPortsSize() == 0) {
                Node container = con.getContainer();
                if(container instanceof Hver) {
                    Hver hv = (Hver) container;
                    hv.removeConnection(con);
                }
                else {
                    Topology top = (Topology) container;
                    top.removeConnection(con);
                }
                
                DefaultMutableTreeNode tn = currentRoot;    
                currentRoot = (DefaultMutableTreeNode) currentRoot.getParent();
                currentRoot.remove(tn);
                return;
            }
        } 
        else
        if (qName.equals("version")) {
            if (!(n instanceof Hver)) {
                throw new SAXException("endElement error: note type not version");
            }
        } else if (qName.equals("reqserv")) {
            if (!(n instanceof Reqserv)) {
                throw new SAXException("endElement error: note type not reqserv");
            }
        } 
        else 
        if (qName.equals("exportedPort")) {
            if (!(n instanceof ExportedPort)) {
                throw new SAXException("endElement error: note type not exportedPort");
            }
            // due to inconsistencies, this connection has no ports
            ExportedPort ep = (ExportedPort) n;
            if(ep.getRefPorts().size() == 0) {
                Node container = ep.getContainer();
                if(container instanceof Hver) {
                    Hver hv = (Hver) container;
                    hv.removePort(ep);
                }
                else {
                    Topology top = (Topology) container;
                    top.removePort(ep);
                }

                DefaultMutableTreeNode tn = currentRoot;    
                currentRoot = (DefaultMutableTreeNode) currentRoot.getParent();
                currentRoot.remove(tn);
                return;
            }
        }
        else
        if (qName.equals("defVer")) {
            if (!(n instanceof DefVer)) {
                throw new SAXException("endElement error: note type not defver");
            }
            if(n.getContainer() == null) {
                // due to inconsistencies, it was not inserted in the model
                return;
            }
        }
        else
        if(qName.equals("method")) {
            if (!(n instanceof Method)) {
                throw new SAXException("endElement error: note type not method");
            }
        }
        currentRoot = (DefaultMutableTreeNode) currentRoot.getParent();
    }

    @Override
    public void endDocument() throws SAXException {
        topology.buildElementTable();

        Iterator ite = valueAttmap.keySet().iterator();
        while (ite.hasNext()) {
            Value v = (Value) ite.next();
            LinkedList<String> atlist = (LinkedList<String>) valueAttmap.get(v);

            for(int i = 0; i < atlist.size(); i++) {
                Attr attribute = topology.getAttributeFromTable(atlist.get(i));
                if (attribute == null) {
                    continue;

                // Instead of stopping the parse, continue. This situation can
                // happen when an instanced component was changed, i.e, an attribute
                // was removed.
                } 
                else {
                    AttrClass classification = attribute.getClassification();
                    if(!classification.hasData()) {
                        // inconsistency
                        continue;
                    }

                    boolean res = v.addAssociatedAttribute(attribute);
                    if (!res) {
                        throw new SAXException("TopologyParser Error: value " + v + 
                                " does not belongs to the same version or component of " +
                                atlist.get(i));
                    }
                }
            }

            // this can happen when the associated attributes were removed
            if(v.getAssociatedAttributes().size() == 0) {
                Node container = v.getContainer();
                if (container instanceof VersionBase) {
                    ((VersionBase) container).removeValue(v);
                } 
                else 
                if (container instanceof CompInstance) {
                    ((CompInstance) container).removeValue(v);
                }
            }
        }
    }

    // Para propositos de teste
    public static void main(String[] args) {
        if (args.length == 0) {
            return;
        }
        System.out.println("Parsing " + args[0] + " ...");

        Topology top = TopologyParser.parseTopology(args[0], new DesignRepository(
                new BaseConfiguration()));
        if (top != null) {
            javax.swing.JFrame frame = new javax.swing.JFrame("Node tree");
            javax.swing.tree.DefaultTreeModel model =
                    new javax.swing.tree.DefaultTreeModel(top.getTreeNode());
            javax.swing.JTree tree = new javax.swing.JTree(model);
            frame.add(tree);

            frame.setSize(200, 300);
            frame.setVisible(true);
        }
    }
    
    public static String errorMessage;
    public static List<Artifact> deps;
    
    public static Topology parseTopology(String fname, DesignRepository repository) {
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

        TopologyParser parser = new TopologyParser(repository);
        xmlReader.setContentHandler(parser);

        xmlReader.setErrorHandler(new MyErrorHandler(System.err));

        try {
            xmlReader.parse(convertToFileURL(fname));
        } catch (SAXException se) {
            errorMessage = se.toString();
            return null;
        } catch (IOException ioe) {
            errorMessage = ioe.toString();
            return null;
        }
        deps = parser.getDependsOn();
        
        return parser.getTopology();
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

    private VirtualPort findPortInVersionComponentInstance(final Hver version,
            final String portName) {
        VirtualPort dc = null;

        Iterator aite = version.componentInstancesIterator();
        while (aite.hasNext()) {
            CompInstance ains = (CompInstance) aite.next();
            CompInterface ai = (CompInterface) ains.getComponent();

            Iterator portsite = ai.getPorts();
            while (portsite.hasNext()) {
                dc = (VirtualPort) portsite.next();
                if (portName.equals(dc.getFullName())) {
                    return dc;
                }
            }
        }

        return null;
    }

    private VirtualPort findPortInVersions(final Hver version, final String portName) {
        VirtualPort dc = null;

        Iterator dfite = version.defVersionsIterator();
        while (dfite.hasNext()) {
            DefVer df = (DefVer) dfite.next();
            Node insv = df.getSelectedVersion();
            if (insv.getType() == Node.HVER) {
                Hver inshv = (Hver) insv;
                dc = inshv.findPort(portName);
            } else {
                Ver insver = (Ver) insv;
                dc = insver.findPort(portName);
            }
            if (dc != null) {
                break;
            }
        }

        return dc;
    }
    /////////////////////////// PRIVATE ATTRIBUTES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    private LinkedList processingNodes;
    private HashMap valueAttmap;
    private Topology topology;

    private DefaultMutableTreeNode currentRoot;
    private DesignRepository repository;
    private HashMap compLibMap;
    
    private LinkedList<Artifact> dependsOn;
}
