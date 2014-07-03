/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.codegen;

import odog.codegen.BoundaryData.BoundaryType;
import odog.codegen.Platform.Host.SR.SRCodeGenerator;
import odog.graph.ComponentGraphEdge;
import odog.graph.ComponentGraphNode;
import odog.graph.ComponentGraphNode.NodeType;
import odog.graph.CompositeComponentGraphNode;
import odog.syntax.Nodes.Acomp;
import odog.syntax.Nodes.CompBase;
import odog.syntax.Nodes.CompInstance;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.AttrClass;
import odog.syntax.Nodes.Connection;
import odog.syntax.Nodes.Dport;
import odog.syntax.Nodes.ExportedPort;
import odog.syntax.Nodes.Hver;
import odog.syntax.Nodes.Method;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.Topology;
import odog.syntax.Nodes.Value;
import odog.syntax.Nodes.Ver;
import odog.syntax.Nodes.VersionBase;
import odog.syntax.Nodes.VirtualPort;
import edu.uci.ics.jung.graph.Graph;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This is a base class for all interaction semantic code generators
 *
 * @author ivan
 */
public class ISemGenerator {

    public ISemGenerator(HashMap <Attr, Value> attrValueMap) {
        /*clibDirList = new LinkedList<String>();
        clibNameList = new LinkedList<String>();
        cincludeDirList = new LinkedList<String>();*/
        
        generatedObjects = new HashMap<String, String>();
        
        inportsQueue = new HashMap<VirtualPort,  LinkedList<String>>();
        outportsQueue = new HashMap<String, LinkedList<String>>();
        inconQueue = new HashMap<String, String>();
        outconQueue = new HashMap<String, LinkedList<String>>();
        componentIdTable = new HashMap<String, Integer>();
        queueDataType = new HashMap<String, String>();
        
        this.attrValueMap = attrValueMap;
        boundarySignals = new LinkedList<BoundaryData>();
    }

    ////////////////////////////// PUBLIC METHODS //////////////////////////////

    public Set<String> getGeneratedObjects() {
        return generatedObjects.keySet();
    }

    public String getObjectBuilder(String object) {
        return generatedObjects.get(object);
    }

    public void addGeneratedObject(String object, String builder) {
        generatedObjects.put(object, builder);
    }
    
    public String getReport() {
        return report;
    }

    /*
    public List<String> getClibDirs() {
        return clibDirList;    
    }
    
    public List<String> getClibNames() {
        return clibNameList;
    }
    
    public List<String> getCincludeDirs() {
        return cincludeDirList;
        
    }*/
    
    ////////////////////////////// PROTECTED METHODS //////////////////////////////
    
    protected String getAttributeDeclarations(CompInstance ains, 
            VersionBase ver) {
        StringBuffer ret = new StringBuffer();
        
        CompBase abase = ains.getComponent();
        Iterator ite = abase.attributesIterator();
        while(ite.hasNext()) {
            Attr at = (Attr) ite.next();
            AttrClass classification = at.getClassification();
            if(classification.hasData() && classification.isVisible()) {                
                Value v = attrValueMap.get(at);
                if(v == null) {
                    v = at.getDefaultValue();
                }
                ret.append("static ");
                if(classification.isStatic()) {
                    ret.append("const ");
                }
                //ret.append(Type.convertToCType(v.getValueType()) + " " + at.getName() +
                            //" = ");
               ret.append(v.getValueType() + " " + at.getName() +
                            " = ");                            
                if(v.getValueType().equals("string")) {
                    ret.append("\"" + v.getValueExpr() + "\";\n");
                }
                else {
                    ret.append(v.getValueExpr() + ";\n");
                }
            }
        }
        
        ite = ver.attributesIterator();
        while(ite.hasNext()) {
            Attr at = (Attr) ite.next();
            AttrClass classification = at.getClassification();
            if(classification.hasData() && classification.isVisible()) {
                Value v = attrValueMap.get(at);
                if(v == null) {
                    v = at.getDefaultValue();
                }
                ret.append("static ");
                if(classification.isStatic()) {
                    ret.append("const ");
                }
                //ret.append(Type.convertToCType(v.getValueType()) + " " + at.getName() +
                  //          " = ");
                ret.append(v.getValueType() + " " + at.getName() + " = ");
                if(v.getValueType().equals("string")) {
                    ret.append("\"" + v.getValueExpr() + "\";\n");
                }
                else {
                    ret.append(v.getValueExpr() + ";\n");
                }
            }
        }
        return ret.toString();
    }
    
    // Efetua o ordenamento topologico do grafo, ja com os cyclos quebrados atraves
    // da marcacao de arestas especificas. Associa um ID unico para cada instancia(no)
    // e fila de entrada (aresta)
    protected boolean topologicalSort(Graph g) {
        int index = 0, edgeIndex = 0;
        topologicalSort = new ComponentGraphNode[g.numVertices()];
        
        LinkedList<ComponentGraphNode> process = new LinkedList<ComponentGraphNode>();
        Iterator ite = g.getVertices().iterator();
        while(ite.hasNext()) {
            ComponentGraphNode n = (ComponentGraphNode) ite.next();
            if(n.getInEdges().size() == 0) {
                process.add(n);
            }
            else {
                boolean allBreak = true;
                Iterator inite = n.getInEdges().iterator();
                while(inite.hasNext()) {
                    ComponentGraphEdge edge = (ComponentGraphEdge) inite.next();
                    if(!edge.getISInfo().canBreakCycle()) {
                        allBreak = false;
                        break;
                    }
                }
                if(allBreak) {
                    process.add(n);
                }               
            }
        }

        if(process.size() == 0) {
            report = "Process = 0. Topology contains cycles that cannot be removed.";
            return false;
        }
        
        HashMap <ComponentGraphNode, Object> nodeTable = new HashMap<ComponentGraphNode, Object>();
        for(ComponentGraphNode node : process) {
            if(!dfsSearch(node, nodeTable)) {
                report = "Topology contains cycles that cannot be removed.";   
                return false;
            }
        }
        
        HashMap<ComponentGraphNode, Object> processed = new HashMap<ComponentGraphNode, Object>();
        while(process.size() > 0) {
            ComponentGraphNode node = process.removeFirst();
            if(processed.containsKey(node)) {
                continue;
            }

            boolean notYet = false;
            Iterator inite = node.getInEdges().iterator();
            while(inite.hasNext()) {
                ComponentGraphEdge edge = (ComponentGraphEdge) inite.next();
                if(edge.getISInfo().canBreakCycle()) continue;

                ComponentGraphNode source = (ComponentGraphNode) edge.getSource();
                if(!processed.containsKey(source)) {
                    notYet = true;
                }
            }
            if(notYet) continue;

            topologicalSort[index] = node;
            node.getISInfo().setId(index);
            processed.put(node, new Object());
            index++;
            
            Iterator outite = node.getOutEdges().iterator();
            while(outite.hasNext()) {
               ComponentGraphEdge edge = (ComponentGraphEdge) outite.next();
               
               edge.getISInfo().setId(edgeIndex++);
               if(edge.getISInfo().canBreakCycle()) {
                   continue;
               }
               process.addLast((ComponentGraphNode)edge.getDest());
            } 
        }
        
        ite = g.getVertices().iterator();
        while(ite.hasNext()) {
            ComponentGraphNode n = (ComponentGraphNode) ite.next();
            if(n.getISInfo().getId() == -1) {                
                report = "Missing node. Topology contains cycles that cannot be removed.";
                return false;
            }
        }

        return true;
    }

    protected void generateAtomicComponents(AtomicComponentGenerator gen, String containerName, 
            ComponentGraphNode node, String outputDir, String designLocation, 
            HashMap <Attr, Value> attrValueMap, LinkedList<BoundaryData> outsideBinfo,
            String objectBuilder) {
        if(node.getType() != NodeType.ATOMIC) return;

        CompInstance ains = node.getComponent();
        VersionBase selectedVer = node.getSelectedVer();

        // 1. instance name
        gen.setComponentInstanceName(node.getISInfo().getCGName());

        // 4. attributes
        gen.addAttributeLine(getAttributeDeclarations(
                ains, selectedVer));
        
        generateCommunicationMethods(node, gen, attrValueMap, containerName, 
                outsideBinfo);
        
        // 5. execution methods
        generateExecutionMethods(node, selectedVer, gen, 
                designLocation);
        
        gen.setContainerName(containerName);
        try {
            File f = new File(outputDir + node.getISInfo().getCGName() + ".c");
            FileOutputStream fos = new FileOutputStream(f);
            PrintStream ps = new PrintStream(fos);
            ps.print(gen.getComponentText());

            ps.close();
            fos.close();

            addGeneratedObject(node.getISInfo().getCGName(), 
                    objectBuilder);
        }
        catch(Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }        
    }

    protected void generateCommunicationMethods(ComponentGraphNode node, 
            AtomicComponentGenerator gen, HashMap <Attr, Value> attrValueMap, 
            String containerName, LinkedList<BoundaryData> outsideBinfo) {
        return;
    }        

    protected String generateTopologyExecMethods(String url) {
        String text = null;
        try {
            File f = new File(url);
            FileReader fr = new FileReader(f);
            char [] buf = new char[(int)f.length()];
            
            fr.read(buf, 0, buf.length);
            
            text = new String(buf);
        }
        catch(Exception ex) {
            System.out.println(ex);
        }
        
        return text;
    }
 
    // Find out all the signal declarations that an atomic component may need.
    // isem is the interaction semantic of the topology containing the atomic component
    
    // TODO...remove the code related to boundaries to another method, in order to
    // make it clearer.
    protected void buildSignalVarLists(String isem, ComponentGraphNode node, 
            HashMap <Attr, Value> attrValueMap, 
            LinkedList<BoundaryData> outsideBinfo) {
        // given the name of a port, or connection, will list all the *variable names*
        // associated with them
        inportsQueue.clear();
        outportsQueue.clear();
        inconQueue.clear();
        outconQueue.clear();
        componentIdTable.clear();
        queueDataType.clear();

        // Process incoming signals to the atomic component
        Iterator ite = node.getInEdges().iterator();
        while(ite.hasNext()) {
            ComponentGraphEdge edge = (ComponentGraphEdge) ite.next();
            Connection c = edge.getConnection();
            VirtualPort vp = edge.getSinkPort();
            
            //String qname = Node.replaceDotForUnd(vp.getFullName() + "_" +  c.getName());
            String qname = edge.getISInfo().getCGName();
            addInputPortInformation(vp, qname, c);
        
            // check for output boundaries
            vp = edge.getSourcePort();
            if(vp instanceof ExportedPort) {  // it is a boundary connection
                CompositeComponentGraphNode sourceNode = (CompositeComponentGraphNode) edge.getSource();
                if(sourceNode.ISem().equals("SR")) {
                    if(isem.equals("DE")) {
                        // SR composite to DE atomic
                        SRDEBoundary binfo = new SRDEBoundary(qname, (ExportedPort)vp, c, 
                            node.getISInfo().getId(), (Topology) sourceNode.getComponent().getComponent(),
                            attrValueMap);
                        outsideBinfo.add(binfo);
                    }
                }
                else
                if(sourceNode.ISem().equals("DF")) {
                    
                    
                }
            }
        }
        
        // Process outgoing signals from the atomic component
        ite = node.getOutEdges().iterator();
        while(ite.hasNext()) {
            ComponentGraphEdge edge = (ComponentGraphEdge) ite.next();
            ComponentGraphNode destNode = (ComponentGraphNode) edge.getDest();
            Connection c = edge.getConnection();
            
            VirtualPort vp = edge.getSinkPort();
            //String qname = Node.replaceDotForUnd(vp.getFullName() + "_" +  c.getName());
            String qname = edge.getISInfo().getCGName();
            String pname = edge.getSourcePort().getName();

            addOutputPortInformation(vp, qname, pname, c, destNode.getISInfo().getId());
            
            // check for input boundaries
            if(vp instanceof ExportedPort) {
                CompositeComponentGraphNode sourceNode = (CompositeComponentGraphNode) destNode;
                if(sourceNode.ISem().equals("SR")) {
                    if(isem.equals("DE")) {
                        // DE atomic to SR composite
                        DESRBoundary binfo = new DESRBoundary(qname, (ExportedPort) vp, c,
                           (Topology) destNode.getComponent().getComponent(), BoundaryType.DESR,
                            attrValueMap);
                        binfo.setOutsideCurrentTime("currentTime");
                        outsideBinfo.add(binfo);
                    }
                }
                else
                if(sourceNode.ISem().equals("DF")) {
                    
                    
                }
            }
        }

        // After processing the connections related to this atomic component, it must
        // be checked if there are ports with no connections, but related to exported
        // ports. This is donde by looking at the current boundary data
        Acomp comp = (Acomp) node.getComponent().getComponent();
        Ver selver = (Ver) node.getSelectedVer();
        Node container = comp.getComponentInstance().getContainer();
        if(container instanceof Hver) {
            container = container.getContainer();
        }

        Iterator pite = comp.portsIterator();
        while(pite.hasNext()) {
            Dport dp = (Dport) pite.next();
            List<BoundaryData> bl = searchBoundaryData(outsideBinfo, dp);
            for(BoundaryData binfo : bl) {
                String qname = binfo.getOutsideSignal() + "_" + 
                        Node.replaceDotForUnd(dp.getLocalName());
                binfo.addInternalSignal((Topology) container, dp);
                if(isem.equals("SR")) {
                    if(dp.isInput()) {
                        addInputPortInformation(dp, qname, binfo.getConnection());
                    }
                    else {
                        if(binfo instanceof SRDEBoundary) {
                            SRDEBoundary srdeb = (SRDEBoundary) binfo;
                            addOutputPortInformation(dp, qname, dp.getName(), srdeb.getConnection(),
                                    srdeb.getDestNodePriority());
                        }
                        else {
                            addOutputPortInformation(dp, qname, dp.getName(), binfo.getConnection(),
                                    0);
                        }
                    }    
                }
                else 
                if(isem.equals("DF")) {

                }                
            }
        }
    }    

    protected void generateCompositeComponents(Graph g, String outputDir, 
            String designLocation, HashMap <Attr,Value> attrValueMap, 
            LinkedList<BoundaryData> outsideBinfo) {

        Iterator ite = g.getVertices().iterator();
        while(ite.hasNext()) {
            ComponentGraphNode node = (ComponentGraphNode) ite.next();
            if(node.getType() != ComponentGraphNode.NodeType.COMPOSITE_SEMANTIC_HEAD) continue;

            CompositeComponentGraphNode cnode = (CompositeComponentGraphNode) node;

            if(cnode.ISem().equals("SR")) {
                SRCodeGenerator srgen = new SRCodeGenerator(attrValueMap);
                srgen.codeGenerate(cnode, outputDir, designLocation, outsideBinfo);

                Set s = srgen.getGeneratedObjects();
                Iterator site = s.iterator();
                while(site.hasNext()) {
                    String object = (String) site.next();
                    addGeneratedObject(object, srgen.getObjectBuilder(object));
                }
            }
            else 
            if(cnode.ISem().equals("DF")) {
                
                
                
            }
        }
    }

    protected String performSubstitutions(ComponentGraphNode node, String url) {        
        return "";
    }
    
    protected void generateConnectionMethods(AtomicComponentGenerator gen, 
            ComponentGraphNode node, LinkedList<BoundaryData> outsideBinfo) {
        HashMap<String, LinkedList> tab = new HashMap<String, LinkedList>();

        Iterator ite = node.getInEdges().iterator();
        while(ite.hasNext()) {
            ComponentGraphEdge edge = (ComponentGraphEdge) ite.next();

            String port = edge.getSinkPort().getName();
            if(tab.containsKey(port)) {
                LinkedList l = tab.get(port);
                if(!l.contains(edge.getConnection().getName())) {
                    l.add(edge.getConnection().getName());
                }
            }
            else {
                LinkedList l = new LinkedList();
                tab.put(port, l);
                if(!l.contains(edge.getConnection().getName())) {
                    l.add(edge.getConnection().getName());
                }
            }            
        }
        
        ite = node.getOutEdges().iterator();
        while(ite.hasNext()) {
            ComponentGraphEdge edge = (ComponentGraphEdge) ite.next();
            String port = edge.getSourcePort().getName();
            if(tab.containsKey(port)) {
                LinkedList l = tab.get(port);
                if(!l.contains(edge.getConnection().getName())) {
                    l.add(edge.getConnection().getName());
                }
            }
            else {
                LinkedList l = new LinkedList();
                tab.put(port, l);
                if(!l.contains(edge.getConnection().getName())) {
                    l.add(edge.getConnection().getName());
                }
            }
        }

        Acomp comp = (Acomp) node.getComponent().getComponent();
        Ver selver = (Ver) node.getSelectedVer();
        Node container = comp.getComponentInstance().getContainer();
        if(container instanceof Hver) {
            container = container.getContainer();
        }

        // check for ports without connections, but containded in a boundary
        Iterator pite = comp.portsIterator();
        while(pite.hasNext()) {
            Dport dp = (Dport) pite.next();
            List<BoundaryData> bl = searchBoundaryData(outsideBinfo, dp);
            for(BoundaryData binfo : bl) {
                if(binfo != null) {  // this port is connected to the outside
                    String port = dp.getName();
                    if(tab.containsKey(port)) {
                        LinkedList l = tab.get(port);
                        if(!l.contains(binfo.getConnection().getName())) {
                            l.add(binfo.getConnection().getName());
                        }
                    }
                    else {
                        LinkedList l = new LinkedList();
                        tab.put(port, l);
                        if(!l.contains(binfo.getConnection().getName())) {
                            l.add(binfo.getConnection().getName());
                        }
                    }                 
                }
            }
        }

        String numberof = "";
        String nameof = "";
        ite = tab.keySet().iterator();
        while(ite.hasNext()) {
            String port = (String) ite.next();
            LinkedList l = tab.get(port);
            numberof += "\tif(strcmp(port, \"" + port + "\") == 0) return " +
                    l.size() + ";\n";
            nameof += "\tif(strcmp(port, \"" + port + "\") == 0) {\n";
            nameof += "\t  switch(number) {\n";
            for(int i = 0;i < l.size();i++) {
                nameof += "\t      case " + i + ": return \"" + (String)l.get(i) +
                        "\";\n";
            }
            nameof += "\t  }\n\t}\n";
        }
        gen.setNumberOfConnections(numberof);
        gen.setNameOfConnection(nameof);
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////

    private boolean dfsSearch(ComponentGraphNode node, 
            HashMap<ComponentGraphNode, Object> nodeTable) {
        if(nodeTable.containsKey(node)) return false;
        
        nodeTable.put(node, new Object());
        
        Iterator ite = node.getOutEdges().iterator();
        while(ite.hasNext()) {
            ComponentGraphEdge edge = (ComponentGraphEdge) ite.next();
            if(edge.getISInfo().canBreakCycle()) continue;

            if(!dfsSearch((ComponentGraphNode)edge.getDest(), nodeTable)) return false;
        }
        nodeTable.remove(node);
        
        return true;
    }
    
    private void addInputPortInformation(VirtualPort vp, String qname, Connection c) {        
        String pname = vp.getName();
        LinkedList<String> l = null;
        if(inportsQueue.containsKey(vp)) {
            //l = inportsQueue.get(pname);
            l = inportsQueue.get(vp); 
        }
        else {
            l = new LinkedList<String>();
            inportsQueue.put(vp, l);
        }
        l.add(qname);

        if(inconQueue.containsKey(c.getName())) {
            System.out.println("ISemGenerator - multiple connections to " +
                    "input queue");
            /*System.out.println("vp = " + vp);
            System.out.println("qname = " + qname);
            System.out.println("c name = " + c.getName());
            
            String s = inconQueue.get(c.getName());
            System.out.println("in queue = " + s);*/
        }
        inconQueue.put(c.getName(), qname);

        Attr datatype = vp.getDataType();
        Value type = attrValueMap.get(datatype);
        if(type == null) {
            type = datatype.getDefaultValue();
        }
        //queueDataType.put(qname, Type.convertToCType(type.getValueExpr()));
        queueDataType.put(qname, type.getValueExpr());
    }
    
    public void addOutputPortInformation(VirtualPort vp, String qname, String pname,
            Connection c, int prio) {
        LinkedList<String> l;
        if(outportsQueue.containsKey(pname)) {
            l = outportsQueue.get(pname);
        }
        else {
            l = new LinkedList<String>();
            outportsQueue.put(pname, l);
        }
        l.add(qname);

        if(outconQueue.containsKey(c.getName())) {
            l = outconQueue.get(c.getName());
        }
        else {
            l = new LinkedList<String>();
            outconQueue.put(c.getName(), l);
        }
        l.add(qname);

        componentIdTable.put(qname, new Integer(prio));

        Attr datatype = vp.getDataType();
        Value type = attrValueMap.get(datatype);
        if(type == null) {
            type = datatype.getDefaultValue();
        }
        //queueDataType.put(qname, Type.convertToCType(type.getValueExpr()));
        queueDataType.put(qname, type.getValueExpr());
    }

    private List<BoundaryData> searchBoundaryData(LinkedList<BoundaryData> outsideBinfo, Dport dp) {
        LinkedList<BoundaryData> ret = new LinkedList<BoundaryData>();
        
        for(BoundaryData bd : outsideBinfo) {
            if(bd.isLinkedTo(dp)) {
                ret.add(bd);
            }
        }
        
        return ret;
    }
    
    private void generateExecutionMethods(ComponentGraphNode node, VersionBase ver, 
            AtomicComponentGenerator gen, String designLocation) {        
        String initURL = null, computeURL = null, finishURL = null, fixpointURL = null;

        // The LIBC_DIRS, INCLUDE_DIRS E LIBCNAMES attributes are also processed
        Method method = ver.getMethod("init");        
        if(method == null) {
            gen.setInitModule("void odog_init() {\n}\n");
        }
        else {
            initURL = method.getCodeURL();
      //      processCmakefileAttributes(method, ver);
        }

        method = ver.getMethod("compute");
        if(method == null) {
            gen.setComputeModule("void odog_compute() {\n}\n");
        }
        else {
            computeURL = method.getCodeURL();
            if(computeURL.equals(initURL)) { 
                computeURL = null;
                gen.setComputeModule("");
            }
    //        processCmakefileAttributes(method, ver);
        }
        
        method = ver.getMethod("finish");
        if(method == null) {
            gen.setFinishModule("void odog_finish() {\n}\n");
        }
        else {
            finishURL = method.getCodeURL();
            if(finishURL.equals(initURL) || finishURL.equals(computeURL)) {
                finishURL = null;
                gen.setFinishModule("");
            }
  //          processCmakefileAttributes(method, ver);
        }        
        
        method = ver.getMethod("fixpoint");
        if(method == null) {
            gen.setFixpointModule("void odog_fixpoint() {\n}\n");
        }
        else {
            fixpointURL = method.getCodeURL();
            if(fixpointURL.equals(initURL) || fixpointURL.equals(computeURL) ||
                    fixpointURL.equals(finishURL)) {
                fixpointURL = null;
                gen.setFixpointModule("");
            }
//            processCmakefileAttributes(method, ver);
        }        
        
        if(initURL != null) {
            gen.setInitModule(performSubstitutions(node, designLocation + initURL));
        }
        if(computeURL != null) {
            gen.setComputeModule(performSubstitutions(node, designLocation + computeURL));
        }
        if(fixpointURL != null) {
            gen.setComputeModule(performSubstitutions(node, designLocation + fixpointURL));
        }
        if(finishURL != null) {
            gen.setFinishModule(performSubstitutions(node, designLocation + finishURL));
        }                    
    }
    
    /*
    private void processCmakefileAttributes(Method method, VersionBase ver) {
        Iterator ite = method.attributesIterator();        
        while(ite.hasNext()) {
            Attr attr = (Attr) ite.next();
            AttrClass classification = attr.getClassification();
            if(!classification.isVisible() && classification.hasData()) {
                Value v = attrValueMap.get(attr);
                if(v == null) {
                    v = attr.getDefaultValue();
                }
                if(attr.getName().equalsIgnoreCase("libc_dir")) {
                    if(!clibDirList.contains(v.getValueExpr())) {
                        clibDirList.add(v.getValueExpr());
                    }
                }
                else 
                if(attr.getName().equalsIgnoreCase("includec_dir")) {
                    if(!cincludeDirList.contains(v.getValueExpr())) {
                        cincludeDirList.add(v.getValueExpr());
                    }
                }
                else
                if(attr.getName().equalsIgnoreCase("libc_name")) {
                    if(!clibNameList.contains(v.getValueExpr())) {
                        clibNameList.add(v.getValueExpr());
                    }
                }
            }
        }
    }
*/
    
    ///////////////////////// PROTECTED VARIABLES //////////////////////////////    

    protected String report;            
    protected HashMap<String, String> generatedObjects;

    // for the respective attributes of an atomic component written in C
 /*
    protected LinkedList<String> clibDirList;
    protected LinkedList<String> clibNameList;
    protected LinkedList<String> cincludeDirList;
*/
    
    protected ComponentGraphNode [] topologicalSort;

    // These tables are with respect to an atomic component, and valid only when
    // processing one.
    protected HashMap<VirtualPort, LinkedList<String>> inportsQueue;
    protected HashMap<String, LinkedList<String>> outportsQueue;
    protected HashMap<String, String> inconQueue;
    protected HashMap<String, LinkedList<String>> outconQueue;
    protected HashMap<String, Integer> componentIdTable;
    protected HashMap<String, String> queueDataType;
 
    protected HashMap <Attr, Value> attrValueMap;
    protected LinkedList<BoundaryData> boundarySignals;
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////

}