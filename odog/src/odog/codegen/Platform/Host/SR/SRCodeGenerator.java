/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.codegen.Platform.Host.SR;

import odog.codegen.AtomicComponentGenerator;
import odog.codegen.BoundaryData;
import odog.codegen.DESRBoundary;
import odog.codegen.ISemGenerator;
import odog.configuration.BaseConfiguration;
import odog.graph.ComponentGraphEdge;
import odog.graph.ComponentGraphNode;
import odog.graph.CompositeComponentGraphNode;
import odog.syntax.Nodes.Acomp;
import odog.syntax.Nodes.CompBase;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.Connection;
import odog.syntax.Nodes.Dport;
import odog.syntax.Nodes.Hver;
import odog.syntax.Nodes.Method;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.Topology;
import odog.syntax.Nodes.Value;
import odog.syntax.Nodes.Ver;
import odog.syntax.Nodes.VirtualPort;
import odog.codegen.util.FileGenerator;
import odog.codegen.util.FileGeneratorParser;
import edu.uci.ics.jung.graph.Graph;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author ivan
 */
public class SRCodeGenerator extends ISemGenerator {

    /** Creates a new instance of SRCodeGenerator */
    public SRCodeGenerator(HashMap <Attr, Value> attrValueMap) {
        super(attrValueMap);
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////

    public boolean codeGenerate(CompositeComponentGraphNode topnode, String outputDir,
            String designLocation, LinkedList<BoundaryData> outsideBinfo) {
        Graph graph = topnode.getInsideGraph();        
        Hver selectedVersion = (Hver) topnode.getSelectedVer();
        boolean isToplevel = false;

        Topology toplevel;
        if(topnode.getComposite() == null) {
            toplevel = (Topology) topnode.getComponent().getComponent();    
        }
        else {
            isToplevel = true;
            toplevel = topnode.getComposite().getRootNode();
        }

        if(!topologicalSort(graph)) {
            return false;
        }

        // 3. Produz o modelo a ser gerado para cada ator
        String containerName;
        if(isToplevel) {
           // containerName = Node.replaceDotForUnd(toplevel.getFullName());
            outputDir = designLocation + selectedVersion.getFullName() + "/";
            File f = new File(outputDir);
            if(!f.exists()) {
                f.mkdir();
            }
        }
        //containerName = Node.replaceDotForUnd(toplevel.getComponentInstance().getFullInstanceName());
        containerName = topnode.getISInfo().getCGName();

        generateAtomicComponents(toplevel, containerName, graph, selectedVersion, outputDir, 
                designLocation, attrValueMap, outsideBinfo);
        
        generateCompositeComponents(graph, outputDir, designLocation, attrValueMap, 
                outsideBinfo);

        // 4. Creates the module for the scheduler
        generateTopology(containerName, toplevel, selectedVersion, graph, 
                designLocation, outputDir, attrValueMap, outsideBinfo);

        // outputs the main.c file if it is the toplevel
        if(isToplevel) {
            String loc = System.getenv("ODOG_CODEGENERATORS");
            loc = BaseConfiguration.appendSlash(loc) + "Host/SR/FileGenerators/";
            FileGenerator maing = FileGeneratorParser.parse(loc + "main.xml");        
            maing.setArgumentValue("toplevelName", containerName);
            try {
                File f = new File(outputDir + "main.c");
                FileOutputStream fos = new FileOutputStream(f);
                PrintStream ps = new PrintStream(fos);
                ps.print(maing.toString());

                ps.close();
                fos.close();
            }
            catch(Exception ex) {
                System.out.println(ex);
            }
        }
        return true;
    }
    
    ///////////////////////////// PROTECTED METHODS //////////////////////////////
    
    protected String performSubstitutions(ComponentGraphNode node, String url) {                
        String text = null;
        try {
            File f = new File(url);
            FileReader fr = new FileReader(f);
            char [] buf = new char[(int)f.length()];
            
            fr.read(buf, 0, buf.length);
            
            text = new String(buf);
        }
        catch(Exception ex) {
            System.out.println("--> " + ex);
        }  
        
        return text;
    }
    
    protected void generateCommunicationMethods(ComponentGraphNode node, 
            AtomicComponentGenerator gen, HashMap <Attr, Value> attrValueMap,
            String containerName, LinkedList<BoundaryData> outsideBinfo) {
        String loc = System.getenv("ODOG_CODEGENERATORS");
        loc = BaseConfiguration.appendSlash(loc) + "Host/SR/FileGenerators/";

        // Specific methods -- buildSignalVarList must already have been called
        generateSpecificMethods((SRComponentGenerator)gen, containerName);

        // Communication methods on input ports
        FileGenerator canReceiveGenerator = FileGeneratorParser.parse(loc + "canReceive.xml");      
      
        // Communication methods on input channels
        FileGenerator receiveScalarGenerator = FileGeneratorParser.parse(loc + "receiveScalar.xml");
        FileGenerator receiveGenerator = FileGeneratorParser.parse(loc + "receive.xml");
        String receiveText = "";
        String canReceiveText = "";
        Iterator ite = inconQueue.keySet().iterator();
        while(ite.hasNext()) {
            String pname = (String) ite.next();

            receiveText = receiveText + "if(strcmp(p1,\"" + pname + "\") == 0) {\n";
            canReceiveText = canReceiveText + "if(strcmp(p1,\"" + pname + "\") == 0) {\n" + "return ";
            
            String qname = inconQueue.get(pname);            
            // RECEIVE
            String dtype = queueDataType.get(qname);            
            if(!scalarCType(dtype)) {                                                
                receiveGenerator.setArgumentValue("valueVar", qname + "_value");
                receiveText = receiveText + "\t" + receiveGenerator.toString() + "\n";    
            }
            else {                    
                receiveScalarGenerator.setArgumentValue("valueVar", qname + "_value");
                receiveScalarGenerator.setArgumentValue("dataType", dtype); 
                receiveText = receiveText + "\t" + receiveScalarGenerator.toString() + "\n";
            }
            
            // CANRECEIVE
            canReceiveGenerator.setArgumentValue("statusVar", qname + "_status");
            canReceiveText = canReceiveText + canReceiveGenerator.toString() + ";\n";
            
            receiveText += "}\n";
            canReceiveText += "}\n";
            if(ite.hasNext()) {
                receiveText += "else\n";           
                canReceiveText += "else\n";
            }
        }
        gen.setReceive(receiveText);
        gen.setCanReceive(canReceiveText);
                
        FileGenerator sendGenerator = FileGeneratorParser.parse(loc + "send.xml");
        FileGenerator sendCopyGenerator = FileGeneratorParser.parse(loc + "sendCopy.xml");
        FileGenerator canSendGenerator = FileGeneratorParser.parse(loc + "canSend.xml");

        /////////////////////////////// Communication methods on output channels        
        
        String sendText = "";
        String canSendText = "";
        ite = outconQueue.keySet().iterator();
        while(ite.hasNext()) {
            String pname = (String) ite.next();

            sendText = sendText + "if(strcmp(p1,\"" + pname + "\") == 0) {\n";            
            canSendText = canSendText + "if(strcmp(p1,\"" + pname + "\") == 0) {\n" + "return ";
            
            LinkedList<String> l = outconQueue.get(pname);
            for(int i = 0;i < l.size();i++) {      
                // SEND
                String qname = l.get(i);
                String dtype = queueDataType.get(qname);
                // since for a port, all queue data types are the same, this if has
                // only one branch enabled per loop iteration
                if(!scalarCType(dtype)) {
                    sendCopyGenerator.setArgumentValue("inputName", qname);
                    sendText = sendText + "\t"+ sendCopyGenerator.toString() + "\n";       
                }
                else {
                    sendGenerator.setArgumentValue("inputName", qname);
                    sendGenerator.setArgumentValue("dataType", "*((" + dtype + " *)");
                    sendText = sendText + "\t"+ sendGenerator.toString() + "\n";
                }
                canSendGenerator.setArgumentValue("statusVar", qname + "_status");
                canSendText = canSendText + " " + canSendGenerator.toString();
                if(i < l.size() - 1) {
                    canSendText = canSendText + " && ";
                }
                else {
                    canSendText = canSendText + ";\n";
                }
            }
            sendText += containerName + "_zeroTimeEvents = 1;\n}\n";
            canSendText += "}\n";
            if(ite.hasNext()) {
                sendText += "else\n";
                canSendText += "else\n";
            }
        }
        gen.setSend(sendText);        
        gen.setCanSend(canSendText);
    }

    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    private void generateTopology(String instanceName, Topology toplevel, 
            Hver topVersion, Graph g, String designLocation, String outputDir,
            HashMap <Attr, Value> attrValueMap, 
            LinkedList<BoundaryData> outsideBinfo) {
        String cgloc = System.getenv("ODOG_CODEGENERATORS");
        String loc = BaseConfiguration.appendSlash(cgloc) + "Host/SR/FileGenerators/";
        FileGenerator topgen = FileGeneratorParser.parse(loc + "scheduler.xml");
        
        topgen.setArgumentValue("ncomponents", String.valueOf(g.getVertices().size()));
        topgen.setArgumentValue("topologyName", instanceName);
        
        processSignals(g, topgen, loc, toplevel, cgloc, instanceName,
                outsideBinfo);

        String strictComponent = "";
        int id = topologicalSort[0].getISInfo().getId();
        String topOrder = String.valueOf(id);

        if(isStrict(topologicalSort[0])) {
            strictComponent = strictComponent + "1";
        }
        else {
            strictComponent = strictComponent + "0";
        }

        //String name = Node.replaceDotForUnd(topologicalSort[0].getComponent().getFullInstanceName());
        String name = topologicalSort[0].getISInfo().getCGName();
        String componentExecMethod = "extern void " + name + "_odog_init();\n";
        componentExecMethod = componentExecMethod + "extern void " + name + "_odog_compute();\n";
        componentExecMethod = componentExecMethod + "extern void " + name + "_odog_finish();\n";
        componentExecMethod = componentExecMethod + "extern void " + name + "_odog_fixpoint();\n";
        
        String fptr = instanceName + "_fptr[" + String.valueOf(id) + "][0] = " + name + "_odog_init;\n";
        fptr += instanceName + "_fptr[" + String.valueOf(id) + "][1] = " + name + "_odog_compute;\n";
        fptr += instanceName + "_fptr[" + String.valueOf(id) + "][2] = " + name + "_odog_finish;\n"; 
        fptr += instanceName + "_fptr[" + String.valueOf(id) + "][3] = " + name + "_odog_fixpoint;\n"; 

        for(int i = 1;i < topologicalSort.length;i++) {
            id = topologicalSort[i].getISInfo().getId();
            topOrder = topOrder + "," + String.valueOf(id);
            
            //name = Node.replaceDotForUnd(topologicalSort[i].getComponent().getFullInstanceName());
            name = topologicalSort[i].getISInfo().getCGName();
            componentExecMethod = componentExecMethod + "extern void " + name + "_odog_init();\n";
            componentExecMethod = componentExecMethod + "extern void " + name + "_odog_compute();\n";
            componentExecMethod = componentExecMethod + "extern void " + name + "_odog_finish();\n";
            componentExecMethod = componentExecMethod + "extern void " + name + "_odog_fixpoint();\n";
            
            fptr += instanceName + "_fptr[" + String.valueOf(id) + "][0] = " + name + "_odog_init;\n";
            fptr += instanceName + "_fptr[" + String.valueOf(id) + "][1] = " + name + "_odog_compute;\n";
            fptr += instanceName + "_fptr[" + String.valueOf(id) + "][2] = " + name + "_odog_finish;\n";
            fptr += instanceName + "_fptr[" + String.valueOf(id) + "][3] = " + name + "_odog_fixpoint;\n"; 

            if(isStrict(topologicalSort[id])) {
                strictComponent = strictComponent + ", 1";
            }
            else {
                strictComponent = strictComponent + ", 0";
            }
        }
        topgen.setArgumentValue("topologicalOrder", topOrder);
        topgen.setArgumentValue("componentsExecMethods", componentExecMethod);
        topgen.setArgumentValue("initializeFptr", fptr);
        topgen.setArgumentValue("strictComponents", strictComponent);
        
        String initURL = null, finishURL = null;
        Method method = topVersion.getMethod("init");        
        if(method == null) {
            topgen.setArgumentValue("userInitMethod", "void " + instanceName + 
                    "_user_init() {\n}\n");
        }
        else {
            initURL = method.getCodeURL();
        }
        
        method = topVersion.getMethod("finish");
        if(method == null) {
            topgen.setArgumentValue("userFinishMethod", "void " + instanceName + 
                    "_user_finish() {\n}\n");
        }
        else {
            finishURL = method.getCodeURL();
            if(finishURL.equals(initURL)) {
                finishURL = null;
                topgen.setArgumentValue("userFinishMethod", "");
            }
        }
        
        if(initURL != null) {
            topgen.setArgumentValue("userInitMethod", 
                    generateTopologyExecMethods(designLocation + initURL));
        }
        if(finishURL != null) {
            topgen.setArgumentValue("userFinishMethod", 
                    generateTopologyExecMethods(designLocation + finishURL));
        }
      
        try {
            File f = new File(outputDir + Node.replaceDotForUnd(instanceName) + ".c");
            FileOutputStream fos = new FileOutputStream(f);
            PrintStream ps = new PrintStream(fos);
            ps.print(topgen.toString());
            
            ps.close();
            fos.close();
            
            addGeneratedObject(Node.replaceDotForUnd(instanceName), "default");
        }
        catch(Exception ex) {
            System.out.println(ex);
        }
    }
    
    // process the signal declarations and initializations. Include boundary signals.
    // generate the numberofconnections method
    private void processSignals(Graph g, FileGenerator topgen, String loc, 
            Topology toplevel, String cgloc, String instanceName, 
            LinkedList<BoundaryData> outsideBinfo) {
        int maxqueues = 0;
        String setQueues = "";
        HashMap<ComponentGraphNode, Integer> compconIndex = 
                new HashMap<ComponentGraphNode, Integer>();

        // 1. Declare all signals (value/status pair)        
        String signalDecl = "";
        String setSignalsStatus = "";
        Iterator ite = g.getVertices().iterator();
        while(ite.hasNext()) {
            ComponentGraphNode node = (ComponentGraphNode) ite.next();

            int index = 0;
            Iterator inite = node.getInEdges().iterator();
            while(inite.hasNext()) {
                ComponentGraphEdge edge = (ComponentGraphEdge) inite.next();
                
                VirtualPort sport = edge.getSinkPort();
                Connection c = edge.getConnection();
                String signalName = edge.getISInfo().getCGName();

                Attr type = sport.getDataType();
                Value v = attrValueMap.get(type);
                if(v == null) {
                    v = type.getDefaultValue();
                }

                String tname = v.getValueExpr();
                if(!scalarCType(tname)) {
                    tname = "odog_data_pkg *";
                }
                signalDecl += tname + " " + signalName + "_value;\n";
                signalDecl += "char " + signalName + "_status;\n";

                setSignalsStatus += instanceName + "_signalsStatus[" + String.valueOf(node.getISInfo().getId()) +
                        "][" + String.valueOf(index) + "] = &" + signalName + "_status;\n";
                index++;  
            }
            compconIndex.put(node, new Integer(index));
            
            if(node.getInEdges().size() > maxqueues) {
                maxqueues = node.getInEdges().size();
            }
        }

        // PROCESS THE BOUNDARY SIGNALS
        HashMap <String, LinkedList<String>> srdeSignals = 
                new HashMap<String, LinkedList<String>>();
        HashMap <String, String> srdeSignalType = new HashMap<String, String>();
        
        // necessary for establishing the number of connections of each inside port
        HashMap <Dport, Integer> desrSignals = new HashMap<Dport, Integer>();
        String fromOutsideData = "";
        String toOutsideData = "";
        String testOutputBoundarySignals = "";
        boolean isEmbedded = false;
        boolean hasDESRb = false;
        boolean hasSRDEb = false;
        FileGenerator sendgen = FileGeneratorParser.parse(loc + "sendInternal.xml");
        FileGenerator sendcopygen = FileGeneratorParser.parse(loc + "sendCopyInternal.xml");
        FileGenerator desrgen = FileGeneratorParser.parse(loc + "DESRboundary.xml");
        for(BoundaryData binfo : outsideBinfo) {
            if(binfo.getContainerTopology() == toplevel) {
                isEmbedded = true;
                switch(binfo.getBoundaryType()) {
                    
                    case SRDE: {  // this is an output boundary
                        hasSRDEb = true;
                        signalDecl += "extern calendarQueue_t *" + binfo.getOutsideSignal() + ";\n";
                        List<Dport> isigs = binfo.getInternalSignals(toplevel);
                        for(Dport dp : isigs) {
                            String s = binfo.getOutsideSignal() + "_" + 
                                Node.replaceDotForUnd(dp.getLocalName());

                            if(!srdeSignals.containsKey(s)) {
                                String dtype = binfo.getDataType();
                                if(!scalarCType(dtype)) {
                                    signalDecl += "odog_data_pkg * " + s + "_value;\n";
                                }
                                else {
                                    signalDecl += binfo.getDataType() + " " + s + "_value;\n";
                                }                                
                                signalDecl += "char " + s + "_status = UNKNOWN;\n";
                                LinkedList<String> l = new LinkedList<String>();
                                l.add(binfo.getOutsideSignal());
                                srdeSignals.put(s, l);
                                srdeSignalType.put(s, dtype);
                            }
                            else {
                                LinkedList<String> l = srdeSignals.get(s);
                                l.add(binfo.getOutsideSignal());
                            }
                            testOutputBoundarySignals += "if(" + s + "_status == UNKNOWN) {\n" +
                                    "  notReady = 1;\n  goto _outsideTest;\n}\n";
                        }
                    } break;

                    case DESR: { // input boundary
                        hasDESRb = true;
                        DESRBoundary desrBinfo = (DESRBoundary) binfo;

                        signalDecl += "extern calendarQueue_t *" + desrBinfo.getOutsideSignal() + ";\n";
                        String sendToInputSignals = "";
                        String absentInputSignals = "";
                        Integer i = null;
                        List<Dport> isigs = desrBinfo.getInternalSignals(toplevel);
                        for(Dport dp : isigs) {
                            ComponentGraphNode compnode = getComponentGraphNode(g, dp);
                            
                            String s = binfo.getOutsideSignal() + "_" + 
                                Node.replaceDotForUnd(dp.getLocalName());
                            
                            String tname = desrBinfo.getDataType(); 
                            if(!scalarCType(tname)) {
                                tname = "odog_data_pkg * ";
                            }
                            signalDecl += tname + " " + s + "_value;\n";
                            signalDecl += "char " + s + "_status;\n";
                        
                            // the actual data transmission                            
                            if(scalarCType(tname)) {
                                sendgen.setArgumentValue("inputName", s);
                                sendgen.setArgumentValue("dataType", "*((" + 
                                        tname + " *)");
                                sendToInputSignals += sendgen.toString();
                            }
                            else {
                                sendcopygen.setArgumentValue("inputName", s);
                                sendToInputSignals += sendcopygen.toString();
                            }                            
                            absentInputSignals += s + "_status = ABSENT;\n";

                            /*
                            // find the connection number of the inside port
                            if(desrSignals.containsKey(dp)) {
                                i = desrSignals.get(dp);
                                i = new Integer(i.intValue() + 1);
                            }
                            else {
                                // must take all input connections from internal connections
                                System.out.println("CompNode = " + compnode);
                                System.out.println("port = " + dp);
                                
                                int ni = compnode.numberInputPorts();
                                System.out.println("n input = " + ni);                                
                                for(int kk = 0;kk < ni;kk++) {
                                    System.out.println("n input con = " + 
                                            compnode.numberInputConnections(kk));
                                    System.out.println("port = " + compnode.getInputPort(kk));
                                }
                                
                                int p = compnode.getInputPort(dp);                                
                                i = new Integer(compnode.numberInputConnections(p));
                            }
                            desrSignals.put(dp, i);*/
                            
                            if(compconIndex.containsKey(compnode)) {
                                i = compconIndex.get(compnode);
                            }
                            else {
                                i = new Integer(0);
                            }

                            setSignalsStatus += instanceName + "_signalsStatus[" + compnode.getISInfo().getId() +
                                "][" + i.intValue() + "] = &" + s + "_status;\n";
                            
                            compconIndex.put(compnode, new Integer(i.intValue() + 1));
                        }
                        desrgen.setArgumentValue("outsideSignal", desrBinfo.getOutsideSignal());
                        desrgen.setArgumentValue("outsideCurrentTime", desrBinfo.getOutsideCurrentTime());
                        desrgen.setArgumentValue("sendToInputSignals", sendToInputSignals);
                        desrgen.setArgumentValue("absentInputSignals", absentInputSignals);
                        
                        if(i != null && (i.intValue() + 1) > maxqueues) {
                            maxqueues = (i.intValue() + 1);
                        }

                        fromOutsideData += desrgen.toString();
                    } break;
                }
            }
        }

        FileGenerator srdegen = FileGeneratorParser.parse(loc + "SRDEboundary.xml");
        FileGenerator desend = FileGeneratorParser.parse(cgloc + "/Host/DE/FileGenerators/send.xml");
        ite = srdeSignals.keySet().iterator();
        while(ite.hasNext()) {
            String insideSignal = (String) ite.next();
            LinkedList<String> l = srdeSignals.get(insideSignal);

            srdegen.setArgumentValue("inputSignal", insideSignal);
            String sendOutsideSignals = "";
            for(String outside : l) {
                desend.setArgumentValue("arg0", outside);
                String dtype = srdeSignalType.get(insideSignal);

                if(!scalarCType(dtype)) {
                    desend.setArgumentValue("arg1", insideSignal + "_value->data");
                    desend.setArgumentValue("arg2", insideSignal + "_value->length");
                }
                else {
                    desend.setArgumentValue("arg1", "&" + insideSignal + "_value");
                    desend.setArgumentValue("arg2", "sizeof(" + dtype + ")");
                }                                
                sendOutsideSignals += desend.toString()  + "\n";
            }
            srdegen.setArgumentValue("sendOutsideSignals", sendOutsideSignals);    
            toOutsideData += srdegen.toString() + "\n";
        }
        
        if(isEmbedded) {
            fromOutsideData = "odog_data_pkg *p2;\n" + fromOutsideData;
            fromOutsideData += instanceName + "_nIterations = 1;\n";
        }
        if(hasDESRb) {
            signalDecl += "extern double currentTime;\n";
        }

        if(hasSRDEb || hasDESRb) {
            topgen.setArgumentValue("extraIncludes", "#include \"calendarQueue.h\"");
        }
        else {
            topgen.setArgumentValue("extraIncludes", "");
        }

        topgen.setArgumentValue("testOutputBoundarySignals", testOutputBoundarySignals);
        topgen.setArgumentValue("toOutsideData", toOutsideData);
        topgen.setArgumentValue("fromOutsideData", fromOutsideData);
        topgen.setArgumentValue("maxInputs", String.valueOf(maxqueues));
        topgen.setArgumentValue("signals", signalDecl);
        topgen.setArgumentValue("setSignalsStatus", setSignalsStatus);
    }
    
    private ComponentGraphNode getComponentGraphNode(Graph g, Dport dp) {
        Node c = dp.getContainer();
        if(c instanceof Ver) {
            c = c.getContainer();
        }
        Acomp comp = (Acomp) c;
        
        Iterator ite = g.getVertices().iterator();
        while(ite.hasNext()) {
            ComponentGraphNode node = (ComponentGraphNode) ite.next();
            if(node.getComponent().getComponent() == comp) {
                return node;   
            }
        }    
        return null;
    }
    
    private int findIdOfPort(Graph g, Dport dp) {
        Node c = dp.getContainer();
        if(c instanceof Ver) {
            c = c.getContainer();
        }
        Acomp comp = (Acomp) c;
        
        Iterator ite = g.getVertices().iterator();
        while(ite.hasNext()) {
            ComponentGraphNode node = (ComponentGraphNode) ite.next();
            if(node.getComponent().getComponent() == comp) {
                return node.getISInfo().getId();    
            }
        }
        return -1;
    }
    
    private void generateAtomicComponents(Topology container, String containerName, 
            Graph g, Hver topVersion, String outputDir, String designLocation, 
            HashMap <Attr,Value> attrValueMap, 
            LinkedList<BoundaryData> outsideBinfo) {
        
        Iterator ite = g.getVertices().iterator();
        while(ite.hasNext()) {
            ComponentGraphNode node = (ComponentGraphNode) ite.next();
            if(node.getType() != ComponentGraphNode.NodeType.ATOMIC) continue;
            SRComponentGenerator gen = new SRComponentGenerator();

            // 1. generate auxiliary tables mapping connection names to the
            // actual signal declarations
            buildSignalVarLists("SR", node, attrValueMap, outsideBinfo);

            // 2. signals
            String signals = "";
            for(String sname : inconQueue.values()) {
                String tname = queueDataType.get(sname);
                if(!scalarCType(tname)) {
                    tname = "odog_data_pkg *";
                }
                signals = signals + "extern  " + tname + " " + sname + "_value;\n";
                signals = signals + "extern char " + sname + "_status;\n";
            }
           
            for(LinkedList<String> l : outconQueue.values()) {
                for(int i = 0;i < l.size();i++) {
                    String sname = l.get(i);
                    String tname = queueDataType.get(sname);
                    if(!scalarCType(tname)) {
                        tname = "odog_data_pkg *";
                    }
                    signals = signals + "extern  " + tname + " " + sname + "_value;\n";
                    signals = signals + "extern char " + sname + "_status;\n";
                }
            }
            gen.setSignals(signals);
            
            generateConnectionMethods(gen, node, outsideBinfo);
            
            super.generateAtomicComponents(gen, containerName, node, outputDir, 
                    designLocation, attrValueMap, outsideBinfo, "default");
        }
    }

    private boolean isStrict(ComponentGraphNode node) {
        CompBase abase = node.getComponent().getComponent();
        Attr strict = abase.getAttribute("nonstrict");
        if(strict == null) {
            return true;
        }
        return false;
    }

    private void generateSpecificMethods(SRComponentGenerator gen, 
            String containerName) {
        /*
        String isAbsentAllText = "";
        Iterator ite = inportsQueue.keySet().iterator();
        while(ite.hasNext()) {
            VirtualPort vp = (VirtualPort) ite.next();
            String pname = vp.getName();
            isAbsentAllText += "if(strcmp(p1,\"" + pname + "\") == 0) {\n\treturn";

            LinkedList<String> l = inportsQueue.get(vp);
            for(int i = 0;i < l.size();i++) {                            
                isAbsentAllText += "(" + l.get(i) + "_status == ABSENT)";
                if(i < l.size() - 1) {
                    isAbsentAllText += " && ";
                }
                else {
                    isAbsentAllText += ";\n";
                }
            }
            isAbsentAllText += "}\n";
        }*/
        
        String isAbsentText = "";
        Iterator ite = inconQueue.keySet().iterator();
        while(ite.hasNext()) {
            String pname = (String) ite.next();
            String qname = inconQueue.get(pname);
            isAbsentText += "if(strcmp(p1,\"" + pname + "\") == 0) {\n" + 
                    "return " + qname + "_status == ABSENT;\n";
            isAbsentText += "}\n";
        }
        
   
   //     String setAbsentAllText = "";
        String setAbentText = "";        
        
        /*
        ite = outportsQueue.keySet().iterator();
        while(ite.hasNext()) {
            String pname = (String) ite.next();

            isAbsentAllText += "if(strcmp(p1,\"" + pname + "\") == 0) {\n" + 
                    "\treturn ";
            setAbsentAllText += "if(strcmp(p1,\"" + pname + "\") == 0) {\n";

            LinkedList<String> l = outportsQueue.get(pname);
            for(int i = 0;i < l.size();i++) {            
                isAbsentAllText += "(" + l.get(i) + "_status == ABSENT)";
                if(i < l.size() - 1) {
                    isAbsentAllText += " && ";
                }
                else {
                    isAbsentAllText += ";\n";
                }
                setAbsentAllText += l.get(i) + "_status = ABSENT;\n";
            }            
            isAbsentAllText += "}\n";
            setAbsentAllText +=  "}\n";
        }
        gen.setIsAbsentAll(isAbsentAllText);
        gen.setSetAbsentAll(setAbsentAllText);*/
        
        String setAbsentText = "";
        ite = outconQueue.keySet().iterator();
        while(ite.hasNext()) {
            String pname = (String) ite.next();

            setAbsentText += "if(strcmp(p1,\"" + pname + "\") == 0) {\n";            
            isAbsentText += "if(strcmp(p1,\"" + pname + "\") == 0) {\n" + "return ";
            
            LinkedList<String> l = outconQueue.get(pname);
            for(int i = 0;i < l.size();i++) {                      
                isAbsentText += "(" + l.get(i) + 
                        "_status == ABSENT)";
                if(i < l.size() - 1) {
                    isAbsentText += " && ";
                }
                else {
                    isAbsentText += ";\n";
                }
                setAbsentText += l.get(i) + "_status = ABSENT;\n";
            }
            setAbsentText += containerName + "_zeroTimeEvents = 1;\n}\n";
            isAbsentText +=  "}\n";
        }        
        gen.setIsAbsent(isAbsentText);        
        gen.setSetAbsent(setAbsentText);    
    }
    
    private boolean scalarCType(String tname) {
        if(tname.equals("char") || tname.equals("float") || tname.equals("double") ||
                tname.equals("int") || tname.equals("long")) {
            return true;
        }
        return false;
    }
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
}
