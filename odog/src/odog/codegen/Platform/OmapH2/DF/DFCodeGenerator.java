/*
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2
 * see LICENSE file
 */
package odog.codegen.Platform.OmapH2.DF;

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
import odog.codegen.AtomicComponentGenerator;
import odog.codegen.BoundaryData;
import odog.codegen.ISemGenerator;
import odog.configuration.BaseConfiguration;
import odog.graph.ComponentGraphEdge;
import odog.graph.ComponentGraphNode;
import odog.graph.ComponentGraphNode.NodeType;
import odog.graph.CompositeComponentGraphNode;
import odog.syntax.Nodes.CompBase;
import odog.syntax.Nodes.CompInstance;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.Connection;
import odog.syntax.Nodes.Hver;
import odog.syntax.Nodes.Method;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.Topology;
import odog.syntax.Nodes.Value;
import odog.syntax.Nodes.VirtualPort;

/**
 *
 * @author ivan
 */
public class DFCodeGenerator extends ISemGenerator {

    public DFCodeGenerator(HashMap <Attr, Value> attrValueMap) {
        super(attrValueMap);
    }

    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public boolean codeGenerate(CompositeComponentGraphNode topnode, String outputDir,
            String designLocation, LinkedList<BoundaryData> outsideBinfo) {
        
        // 1 Get the graph
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

        // 2. check if it is acyclic and order it
        if(!topologicalSort(graph)) {
            return false;
        }

        String containerName;
        if(isToplevel) {
            containerName = Node.replaceDotForUnd(toplevel.getFullName());
            outputDir = designLocation + selectedVersion.getFullName() + "/";
            File f = new File(outputDir);
            if(!f.exists()) {
                f.mkdir();
            }
        }
        else {
            containerName = Node.replaceDotForUnd(toplevel.getComponentInstance().getFullInstanceName());
        }        
        
        toDSP = new boolean[topologicalSort.length];
        isBoundary = new boolean[topologicalSort.length];
        boundaryId = new int[topologicalSort.length];
        armNodeId = new int[topologicalSort.length];
        dspNodeId = new int[topologicalSort.length];

        classifyComponents();
        
        generateARMAtomicComponents(toplevel, containerName, graph, selectedVersion, outputDir,
                designLocation, attrValueMap, outsideBinfo);

        // 4. Creates the module for the scheduler
        generateARMTopology(containerName, toplevel, selectedVersion, graph, 
                designLocation, outputDir, attrValueMap, outsideBinfo);

        generateDSPAtomicComponents(toplevel, containerName, graph, selectedVersion, outputDir,
                designLocation, attrValueMap, outsideBinfo);
        
        // 4. Creates the module for the scheduler
        generateDSPTopology(containerName, toplevel, selectedVersion, graph, 
                designLocation, outputDir, attrValueMap, outsideBinfo);

        // outputs the main.c file if it is the toplevel
        if(isToplevel) {
            String loc = System.getenv("ODOG_CODEGENERATORS");
            loc = BaseConfiguration.appendSlash(loc) + "OmapH2/DF/FileGenerators/";
            
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
            
            // the DSP cmd file
            FileGenerator maincmd = FileGeneratorParser.parse(loc + "mainCmd.xml");        
            maincmd.setArgumentValue("len", "0x" + Integer.toHexString(numberOfPages * 4096));
            try {
                File f = new File(outputDir + "mainCmd.cmd");
                FileOutputStream fos = new FileOutputStream(f);
                PrintStream ps = new PrintStream(fos);
                ps.print(maincmd.toString());

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
        loc = BaseConfiguration.appendSlash(loc) + "OmapH2/DF/FileGenerators/";
        int nodeId = node.getISInfo().getId();

        // Communication methods on input channels
        FileGenerator receiveDynamicFifoGenerator = FileGeneratorParser.parse(loc + 
                "receiveDynamicFifo.xml");
        FileGenerator receiveLimitedFifoGenerator = FileGeneratorParser.parse(loc + 
                "receiveLimitedCapacityFifo.xml");
        String receiveText = "";        
        Iterator ite = node.getInEdges().iterator();
        while(ite.hasNext()) {
            ComponentGraphEdge edge = (ComponentGraphEdge) ite.next();
            String pname = edge.getConnection().getName();    
            receiveText = receiveText + "if(strcmp(p1,\"" + pname + "\") == 0) {\n";
            String qname = inconQueue.get(pname);
            if(!toDSP[nodeId]) {
                if(crossProcessor(edge)) {
                    receiveLimitedFifoGenerator.setArgumentValue("arg0", qname);
                    receiveLimitedFifoGenerator.setArgumentValue("arg1", qname + "_dataS");
                    receiveText = receiveText + "\t" + receiveLimitedFifoGenerator.toString() + "\n";
                }   
                else {                                
                    receiveDynamicFifoGenerator.setArgumentValue("arg0", qname);
                    receiveText = receiveText + "\t" + receiveDynamicFifoGenerator.toString() + "\n";
                }
            }
            else {
                receiveLimitedFifoGenerator.setArgumentValue("arg0", qname);
                receiveLimitedFifoGenerator.setArgumentValue("arg1", qname + "_dataS");
                receiveText = receiveText + "\t" + receiveLimitedFifoGenerator.toString() + "\n";
            }
            receiveText += "}\n";
            if(ite.hasNext()) {
                receiveText += "else\n";
            }
        }
        gen.setReceive(receiveText);

        // Communication methods on output channels        
        String sendText = "";
        FileGenerator sendDynamicFifoGenerator = FileGeneratorParser.parse(loc + 
                "sendDynamicFifo.xml");
        FileGenerator sendLimitedFifoGenerator = FileGeneratorParser.parse(loc + 
                "sendLimitedCapacityFifo.xml");
        HashMap<String, String> outConText = new HashMap<String, String>();
        ite = node.getOutEdges().iterator();
        while(ite.hasNext()) {
            ComponentGraphEdge edge = (ComponentGraphEdge) ite.next();
            Connection c = edge.getConnection();            
            VirtualPort vp = edge.getSinkPort();
            
            String pname = c.getName();
            String ctext = outConText.get(pname);

            String qname = Node.replaceDotForUnd(vp.getFullName() + "_" +  
                    c.getName());
            if(!toDSP[nodeId]) {
                if(crossProcessor(edge)) {
                    sendLimitedFifoGenerator.setArgumentValue("arg0", qname);
                    sendLimitedFifoGenerator.setArgumentValue("arg1", qname + "_dataS");
                    if(ctext == null) {
                        ctext = "";
                    }
                    ctext = ctext + "\t"+ sendLimitedFifoGenerator.toString() + "\n";
                }
                else {                
                    sendDynamicFifoGenerator.setArgumentValue("arg0", qname);
                    sendDynamicFifoGenerator.setArgumentValue("arg1", "p2");
                    sendDynamicFifoGenerator.setArgumentValue("arg2", "length");
                    if(ctext == null) {
                        ctext = "";
                    }
                    ctext = ctext + "\t"+ sendDynamicFifoGenerator.toString() + "\n";
                }
            }
            else {
                sendLimitedFifoGenerator.setArgumentValue("arg0", qname);
                sendLimitedFifoGenerator.setArgumentValue("arg1", qname + "_dataS");
                if(ctext == null) {
                    ctext = "";
                }
                ctext = ctext + "\t"+ sendLimitedFifoGenerator.toString() + "\n";
            }
            outConText.put(pname, ctext);
        }

        ite = outConText.keySet().iterator();
        while(ite.hasNext())  {
            String pname = (String) ite.next();
            String ptext = outConText.get(pname);
            sendText += "if(strcmp(p1,\"" + pname + "\") == 0) {\n";
            sendText += ptext;
            sendText += "}\n";
            if(ite.hasNext()) {
                sendText += "else\n";
            }
        }
        gen.setSend(sendText);

        FileGenerator canSendGenerator = FileGeneratorParser.parse(loc + 
                "canSendLimitedFifo.xml");
        String canSendText = "";
        outConText = new HashMap<String, String>();
        ite = node.getOutEdges().iterator();
        while(ite.hasNext()) {
            ComponentGraphEdge edge = (ComponentGraphEdge) ite.next();
            Connection c = edge.getConnection();            
            VirtualPort vp = edge.getSinkPort();

            String pname = c.getName();
            
            String qname = Node.replaceDotForUnd(vp.getFullName() + "_" +  
                    c.getName());
            String ctext = outConText.get(pname);
            if((!toDSP[nodeId] && crossProcessor(edge)) || toDSP[nodeId]) {
                canSendGenerator.setArgumentValue("arg0", qname);
                if(ctext == null) {
                    ctext = "";
                }
                ctext = ctext + "\tif("+ canSendGenerator.toString() + " == 0) return 0;\n";
                outConText.put(pname, ctext);
            }            
        }
        
        ite = outConText.keySet().iterator();
        while(ite.hasNext())  {
            String pname = (String) ite.next();
            String ptext = outConText.get(pname);
            canSendText += "if(strcmp(p1,\"" + pname + "\") == 0) {\n";
            canSendText += ptext;
            canSendText += "}\n";
            if(ite.hasNext()) {
                canSendText += "else\n";
            }
        }
        gen.setCanSend(canSendText);
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    private void generateARMTopology(String instanceName, Topology toplevel, 
            Hver topVersion, Graph g, String designLocation, String outputDir,
            HashMap <Attr, Value> attrValueMap, 
            LinkedList<BoundaryData> outsideBinfo) {        
        String cgloc = System.getenv("ODOG_CODEGENERATORS");
        String loc = BaseConfiguration.appendSlash(cgloc) + "OmapH2/DF/FileGenerators/";
        FileGenerator topgen = FileGeneratorParser.parse(loc + "ARMscheduler.xml");

        topgen.setArgumentValue("ncomponents", Integer.toString(topologicalSort.length - ntoDSP));
        topgen.setArgumentValue("nboundary", Integer.toString(armboundaryCounter));
        topgen.setArgumentValue("topologyName", instanceName);

        processSignals(g, topgen, loc, toplevel, cgloc, instanceName,
                outsideBinfo);
        
        String compExecMethod = "";
        String fptr = "";
        int id = topologicalSort[0].getISInfo().getId();
        if(!toDSP[id]) {
            String name = Node.replaceDotForUnd(topologicalSort[0].getComponent().getFullInstanceName());
            compExecMethod = "extern void " + name + "_odog_init();\n";
            compExecMethod = compExecMethod + "extern void " + name + "_odog_compute();\n";
            compExecMethod = compExecMethod + "extern void " + name + "_odog_finish();\n";

            fptr = instanceName + "_fptr[" + String.valueOf(armNodeId[id]) + "][0] = " + name + "_odog_init;\n";
            fptr += instanceName + "_fptr[" + String.valueOf(armNodeId[id]) + "][1] = " + name + "_odog_compute;\n";
            fptr += instanceName + "_fptr[" + String.valueOf(armNodeId[id]) + "][2] = " + name + "_odog_finish;\n"; 
        }

        for(int i = 1;i < topologicalSort.length;i++) {
            id = topologicalSort[i].getISInfo().getId();
            if(toDSP[id]) continue;
            
            String name = Node.replaceDotForUnd(topologicalSort[i].getComponent().getFullInstanceName());
            compExecMethod = compExecMethod + "extern void " + name + "_odog_init();\n";
            compExecMethod = compExecMethod + "extern void " + name + "_odog_compute();\n";
            compExecMethod = compExecMethod + "extern void " + name + "_odog_finish();\n";
            
            fptr += instanceName + "_fptr[" + String.valueOf(armNodeId[id]) + "][0] = " + name + "_odog_init;\n";
            fptr += instanceName + "_fptr[" + String.valueOf(armNodeId[id]) + "][1] = " + name + "_odog_compute;\n";
            fptr += instanceName + "_fptr[" + String.valueOf(armNodeId[id]) + "][2] = " + name + "_odog_finish;\n";
        }
        topgen.setArgumentValue("componentsExecMethods", compExecMethod);
        topgen.setArgumentValue("initializeFptr", fptr);
                
        String initURL = null, finishURL = null;
        Method method = topVersion.getMethod("init");        
        if(method == null) {
            topgen.setArgumentValue("userInitMethod", "void " + instanceName + "_user_init() {\n}\n");
        }
        else {
            initURL = method.getCodeURL();
        }
        
        method = topVersion.getMethod("finish");
        if(method == null) {
            topgen.setArgumentValue("userFinishMethod", "void " + instanceName + "_user_finish() {\n}\n");
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
            File f = new File(outputDir + Node.replaceDotForUnd(instanceName) + "_ARM.c");
            FileOutputStream fos = new FileOutputStream(f);
            PrintStream ps = new PrintStream(fos);
            ps.print(topgen.toString());
            
            ps.close();
            fos.close();
            
            addGeneratedObject(Node.replaceDotForUnd(instanceName) + "_ARM", "ARM");
        }
        catch(Exception ex) {
            System.out.println(ex);
        }
    }

     private void generateDSPTopology(String instanceName, Topology toplevel, 
            Hver topVersion, Graph g, String designLocation, String outputDir,
            HashMap <Attr, Value> attrValueMap, 
            LinkedList<BoundaryData> outsideBinfo) {        
        String cgloc = System.getenv("ODOG_CODEGENERATORS");
        String loc = BaseConfiguration.appendSlash(cgloc) + "OmapH2/DF/FileGenerators/";
        FileGenerator topgen = FileGeneratorParser.parse(loc + "DSPscheduler.xml");

        topgen.setArgumentValue("ncomponents", Integer.toString(ntoDSP));        
        topgen.setArgumentValue("nboundary", Integer.toString(dspboundaryCounter));
        topgen.setArgumentValue("topologyName", instanceName);
        topgen.setArgumentValue("bufsiz", (new Integer(numberOfPages * 2048).toString()));
        
        processSignalsDSP(g, topgen, loc, toplevel, cgloc, instanceName,
                outsideBinfo);
        
        String componentExecMethod = "";
        String fptr = "";
        int id = topologicalSort[0].getISInfo().getId();
        if(toDSP[id]) {
            String name = Node.replaceDotForUnd(topologicalSort[0].getComponent().getFullInstanceName());
            componentExecMethod = "extern void " + name + "_init();\n";
            componentExecMethod = componentExecMethod + "extern void " + name + "_odog_compute();\n";
            componentExecMethod = componentExecMethod + "extern void " + name + "_odog_finish();\n";

            fptr = instanceName + "_fptr[" + String.valueOf(dspNodeId[id]) + "][0] = " + name + "_odog_init;\n";
            fptr += instanceName + "_fptr[" + String.valueOf(dspNodeId[id]) + "][1] = " + name + "_odog_compute;\n";
            fptr += instanceName + "_fptr[" + String.valueOf(dspNodeId[id]) + "][2] = " + name + "_odog_finish;\n"; 
        }

        for(int i = 1;i < topologicalSort.length;i++) {
            id = topologicalSort[i].getISInfo().getId();
            if(!toDSP[id]) continue;
            
            String name = Node.replaceDotForUnd(topologicalSort[i].getComponent().getFullInstanceName());
            componentExecMethod = componentExecMethod + "extern void " + name + "_odog_init();\n";
            componentExecMethod = componentExecMethod + "extern void " + name + "_odog_compute();\n";
            componentExecMethod = componentExecMethod + "extern void " + name + "_odog_finish();\n";
            
            fptr += instanceName + "_fptr[" + String.valueOf(dspNodeId[id]) + "][0] = " + name + "_odog_init;\n";
            fptr += instanceName + "_fptr[" + String.valueOf(dspNodeId[id]) + "][1] = " + name + "_odog_compute;\n";
            fptr += instanceName + "_fptr[" + String.valueOf(dspNodeId[id]) + "][2] = " + name + "_odog_finish;\n";
        }
        topgen.setArgumentValue("componentsExecMethods", componentExecMethod);
        topgen.setArgumentValue("initializeFptr", fptr);
                
        String initURL = null, finishURL = null;
        Method method = topVersion.getMethod("init");        
        if(method == null) {
            topgen.setArgumentValue("userInitMethod", "void " + instanceName + "_user_init() {\n}\n");
        }
        else {
            initURL = method.getCodeURL();
        }
        
        method = topVersion.getMethod("finish");
        if(method == null) {
            topgen.setArgumentValue("userFinishMethod", "void " + instanceName + "_user_finish() {\n}\n");
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
            File f = new File(outputDir + Node.replaceDotForUnd(instanceName) + "_DSP.c");
            FileOutputStream fos = new FileOutputStream(f);
            PrintStream ps = new PrintStream(fos);
            ps.print(topgen.toString());

            ps.close();
            fos.close();

            addGeneratedObject(Node.replaceDotForUnd(instanceName) + "_DSP", "DSP");
        }
        catch(Exception ex) {
            System.out.println(ex);
        }
    }

     private void processSignalsDSP(Graph g, FileGenerator topgen, String loc, 
            Topology toplevel, String cgloc, String instanceName, 
            LinkedList<BoundaryData> outsideBinfo) {

        // 1. Declare all signals that have connections. This includes the 
        // dynamic fifo and a sample rate variable for the associated ports.
        String signalDecl = "";
        String initSignals = "";
        String destroySignals = "";
        String isBoundaryText =  "";
        String boundaryText = "";
        String initCanSendText = "";
        String initDSPMemText = "";
        String canSendMethodsText = "";

        // generate the input initializations        
        String ninputsText = "";
        String ninputConnectionsText = "";
        String inputFifoText = "";
        String inputSampleRatesText = "";
        String ninputComponentsText = "";
        String inputComponentsText = "";
        
        // generate the output initializations        
        String noutputsText = "";
        String noutputConnectionsText = "";        
        String outputFifoText = "";
        String outputSampleRatesText = "";        
        String noutputComponentsText = "";
        String outputComponentsText = "";
        
        // must be 1 and not 0 for the dsp compiler not complain. Doesn't matter,
        // since ninputs, noutputs, ninputComponents = zero
        int maxInputConnections = 1;
        int maxInputPorts = 1;
        int maxInputComponents = 1;
        int maxOutputConnections = 1;
        int maxOutputPorts = 1;
        int maxOutputComponents = 1;

        for(int comp = 0;comp < topologicalSort.length;comp++) {
            ComponentGraphNode node = topologicalSort[comp];
            int nodeId = node.getISInfo().getId();
            if(!toDSP[nodeId]) continue;

            if(node.numberInputPorts() > maxInputPorts) {
                maxInputPorts = node.numberInputPorts();
            }
            for(int i = 0;i < node.numberInputPorts();i++) {
                if(node.numberInputConnections(i) > maxInputConnections) {
                    maxInputConnections = node.numberInputConnections(i);
                }
            }
            if(node.numberOfInputComponents() > maxInputComponents) {
                maxInputComponents = node.numberOfInputComponents();
            }
            
            if(node.numberOutputPorts() > maxOutputPorts) {
                maxOutputPorts = node.numberOutputPorts();
            }
            for(int i = 0;i < node.numberOutputPorts();i++) {
                if(node.numberOutputConnections(i) > maxOutputConnections) {
                    maxOutputConnections = node.numberOutputConnections(i);
                }
            }
            if(node.numberOfOutputComponents() > maxOutputComponents) {
                maxOutputComponents = node.numberOfOutputComponents();
            }
        }
        
        topgen.setArgumentValue("maxInputPorts", Integer.toString(maxInputPorts));
        topgen.setArgumentValue("maxInputConnections", Integer.toString(maxInputConnections));
        topgen.setArgumentValue("maxInputComponents", Integer.toString(maxInputComponents));
        topgen.setArgumentValue("maxOutputPorts", Integer.toString(maxOutputPorts));
        topgen.setArgumentValue("maxOutputConnections", Integer.toString(maxOutputConnections));
        topgen.setArgumentValue("maxOutputComponents", Integer.toString(maxOutputComponents));

        for(int comp = 0;comp < topologicalSort.length;comp++) {
            ComponentGraphNode node = topologicalSort[comp];
            int nodeId = node.getISInfo().getId();

            if(!toDSP[nodeId]) continue;

            initCanSendText += "canSend[" + boundaryId[nodeId] + "] = " +
                "canSend_c" + dspNodeId[nodeId] + ";\n";
            canSendMethodsText += "extern char canSend_c" + dspNodeId[nodeId] + "(" +
                "char *,int);\n";

            if(isBoundary[nodeId]) {
                isBoundaryText += "isBoundary[" + dspNodeId[nodeId] + "] = 1;\n";
                boundaryText += "boundary[" + dspNodeId[nodeId] + "] = " + 
                    boundaryId[nodeId] + ";\n";
            }
            else {
                isBoundaryText += "isBoundary[" + dspNodeId[nodeId] + "] = 0;\n";
            }

            int ninputs = node.numberInputPorts();
            ninputsText += "ninputs[" + dspNodeId[nodeId] + "] = " + ninputs + ";\n";

            // PROCESS IN CONNECTIONS - also include the declaration of the dynamic fifos
            for(int i = 0;i < ninputs;i++) {
                VirtualPort vp = node.getInputPort(i);
                signalDecl += "short " + Node.replaceDotForUnd(vp.getFullName()) + "_rate;\n";

                int ninputCon = node.numberInputConnections(i);

                ninputConnectionsText += "ninputConnections[" + dspNodeId[nodeId] + "][" +
                        i + "] = " + ninputCon + ";\n";
                inputSampleRatesText += "inputSampleRates[" + dspNodeId[nodeId] + "][" +
                        i + "] = &" + Node.replaceDotForUnd(vp.getFullName()) + "_rate;\n";
                for(int j = 0;j < ninputCon;j++) {
                    ComponentGraphEdge edge = node.getInputGraphEdge(i, j);

                    VirtualPort sport = edge.getSinkPort();
                    Connection c = edge.getConnection();
                    String signalName = Node.replaceDotForUnd(sport.getFullName() + "_" +
                       c.getName());

                    signalDecl += "DynamicDspFifo *" + signalName + ";\n";
                    

                    inputFifoText += "inputFifo[" + dspNodeId[nodeId] + "][" + i + "][" + j + "] = " +
                            signalName + ";\n";

                    Attr attr = c.getAttribute("capacity");
                    Value v = attrValueMap.get(attr);
                    int capacity = (new Integer(v.getValueExpr())).intValue();

                    attr = c.getAttribute("datasize");
                    v = attrValueMap.get(attr);
                    int datasize = (new Integer(v.getValueExpr())).intValue();

                    if(crossProcessor(edge)) {
                        signalDecl += "UNSIG16 *" + signalName + "_dataS;\n";
                        
                        DFEdgeISemInfo einfo = (DFEdgeISemInfo) edge.getISInfo();
                        int sharedPos = einfo.getSharedMemAddr()/2;
                        initSignals += signalName + " = (DynamicDspFifo *) " +
                                "(data + " + sharedPos + ");\n";
                        initSignals += signalName + "_dataS = (UNSIG16 *) " +
                                "(data + 4 + " + sharedPos + ");\n";
                    }
                    else {
                        signalDecl += "UNSIG16 *" + signalName + "_dataS[" +
                                datasize/2 + "];\n";
                        initSignals += "initDynamicDspFifo(" + signalName + ", " + 
                            datasize + "," +  capacity * datasize /2 + ");\n";
                    }
                }
            }

            int nincomponents = node.numberOfInputComponents();
            ninputComponentsText += "ninputComponentss[" + dspNodeId[nodeId] + "] = " + nincomponents + ";\n";            
            for(int i = 0;i < nincomponents;i++) {
                ComponentGraphNode source = node.getInputComponent(i);
                inputComponentsText +=  "inputComponents[" + dspNodeId[nodeId] + "][" + i +
                        "] = " + source.getISInfo().getId() + ";\n";
            }

            int noutputs = node.numberOutputPorts();

            noutputsText += "noutputs[" + dspNodeId[nodeId] + "] = " + noutputs + ";\n";            
            for(int i = 0;i < noutputs;i++) {
                int ncon = node.numberOutputConnections(i);

                noutputConnectionsText += "noutputConnections[" + dspNodeId[nodeId] + "][" +
                        i + "] = " + ncon + ";\n";
                for(int j = 0;j < ncon;j++) {
                    ComponentGraphEdge edge = node.getOutputGraphEdge(i, j);
                    VirtualPort sport = edge.getSinkPort();
                    Connection c = edge.getConnection();
                    String signalName = Node.replaceDotForUnd(sport.getFullName() + "_" +
                       c.getName());

                    outputFifoText += "outputFifo[" + dspNodeId[nodeId] + "][" + i + "][" + j + "] = " +
                        signalName + ";\n";                                       

                    if(crossProcessor(edge)) {
                        signalDecl += "DynamicDspFifo *" + signalName + ";\n";
                        signalDecl += "UNSIG16 *" + signalName + "_dataS;\n";
                    
                        DFEdgeISemInfo einfo = (DFEdgeISemInfo) edge.getISInfo();
                        int sharedPos = einfo.getSharedMemAddr()/2;
                        
                        initSignals += signalName + " = (DynamicDspFifo *) " +
                                "(data + " + sharedPos + ");\n";
                        initSignals += signalName + "_dataS = (UNSIG16 *) " +
                                "(data + 4 + " + sharedPos + ");\n";
                    }
                 }
            }
            int noutcomponents = node.numberOfOutputComponents();

            noutputComponentsText += "noutputComponents[" + dspNodeId[nodeId] + "] = " + noutcomponents + ";\n";
            for(int i = 0;i < noutcomponents;i++) {
                ComponentGraphNode source = node.getOutputComponent(i);
                outputComponentsText += "outputComponents[" + dspNodeId[nodeId] + "][" + i +
                        "] = " + source.getISInfo().getId() + ";\n";
            }
        }
        
        topgen.setArgumentValue("ninputs", ninputsText);
        topgen.setArgumentValue("ninputConnections", ninputConnectionsText);
        topgen.setArgumentValue("ninputs", ninputsText);
        topgen.setArgumentValue("inputFifo", inputFifoText);
        topgen.setArgumentValue("inputSampleRates", inputSampleRatesText);
        topgen.setArgumentValue("ninputComponents", ninputComponentsText);
        topgen.setArgumentValue("inputComponents", inputComponentsText);

        topgen.setArgumentValue("noutputs", noutputsText);
        topgen.setArgumentValue("noutputConnections", noutputConnectionsText);
        topgen.setArgumentValue("noutputs", noutputsText);
        topgen.setArgumentValue("outputFifo", outputFifoText);
        topgen.setArgumentValue("outputSampleRates", outputSampleRatesText);
        topgen.setArgumentValue("noutputComponents", noutputComponentsText);
        topgen.setArgumentValue("outputComponents", outputComponentsText);

        topgen.setArgumentValue("initDSPMem", initDSPMemText);
        topgen.setArgumentValue("initCanSend", initCanSendText);
        topgen.setArgumentValue("canSendMethods", canSendMethodsText);
        topgen.setArgumentValue("isBoundary", isBoundaryText);
        topgen.setArgumentValue("boundary", boundaryText);
        topgen.setArgumentValue("signals", signalDecl);
        topgen.setArgumentValue("initSignals", initSignals);
        topgen.setArgumentValue("destroySignals", destroySignals);
    }
     
    private void processSignals(Graph g, FileGenerator topgen, String loc, 
            Topology toplevel, String cgloc, String instanceName, 
            LinkedList<BoundaryData> outsideBinfo) {

        // 1. Declare all signals that have connections. This includes the 
        // dynamic fifo and a sample rate variable for the associated ports.
        String signalDecl = "";
        String initSignals = "";
        String destroySignals = "";
        String isBoundaryText =  "";
        String boundaryText = "";
        String initCanSendText = "";
        String initDSPMemText = "";
        String canSendMethodsText = "";
        
        // generate the input initializations        
        String ninputsText = "";
        String ninputConnectionsText = "";        
        String inputFifoText = "";
        String typeInputFifoText = "";
        String inputSampleRatesText = "";
        String ninputComponentsText = "";
        String inputComponentsText = "";
        
        // generate the output initializations        
        String noutputsText = "";
        String noutputConnectionsText = "";        
        String outputFifoText = "";
        String typeOutputFifoText = "";
        String outputSampleRatesText = "";        
        String noutputComponentsText = "";
        String outputComponentsText = "";        

        int sharedMemory = 0;
        for(int comp = 0;comp < topologicalSort.length;comp++) {
            ComponentGraphNode node = topologicalSort[comp];
            int nodeId = node.getISInfo().getId();

            if(toDSP[nodeId]) continue;

            if(isBoundary[nodeId]) {
                isBoundaryText += "isBoundary[" + armNodeId[nodeId] + "] = 1;\n";
                boundaryText += "boundary[" + armNodeId[nodeId] + "] = " + 
                    boundaryId[nodeId] + ";\n";
                initCanSendText += "canSend[" + boundaryId[nodeId] + "] = " +
                        "canSend_c" + armNodeId[nodeId] + ";\n";
                canSendMethodsText += "extern char canSend_c" + armNodeId[nodeId] + "(" +
                        "char *,int);";
            }
            else {
                isBoundaryText += "isBoundary[" + armNodeId[nodeId] + "] = 0;\n";
            }
            
            int ninputs = node.numberInputPorts();
            
            ninputsText += "ninputs[" + armNodeId[nodeId] + "] = " + ninputs + ";\n";
            ninputConnectionsText += "ninputConnections[" + armNodeId[nodeId] + "] =" +
                     "(int *) malloc(sizeof(int) * " + ninputs + ");\n";
            inputFifoText += "inputFifo[" + armNodeId[nodeId] + "] = " + 
                    "(void ***) malloc(sizeof(void **) * " + ninputs + ");\n";
            inputSampleRatesText += "inputSampleRates[" + armNodeId[nodeId] + "] =" +
                     "(short **) malloc(sizeof(short *) * " + ninputs + ");\n";
            typeInputFifoText += "typeInputFifo[" + armNodeId[nodeId] + "] =" +
                     "(char **) malloc(sizeof(char *) * " + ninputs + ");\n";

            // PROCESS IN CONNECTIONS - also include the declaration of the dynamic fifos
            for(int i = 0;i < ninputs;i++) {
                VirtualPort vp = node.getInputPort(i);
                signalDecl += "short " + Node.replaceDotForUnd(vp.getFullName()) + "_rate;\n";
                
                int ninputCon = node.numberInputConnections(i);
                
                ninputConnectionsText += "ninputConnections[" + armNodeId[nodeId] + "][" +
                        i + "] = " + ninputCon + ";\n";
                inputSampleRatesText += "inputSampleRates[" + armNodeId[nodeId] + "][" +
                        i + "] = &" + Node.replaceDotForUnd(vp.getFullName()) + "_rate;\n";
                inputFifoText += "inputFifo[" + armNodeId[nodeId] + "][" + i + "] = " + 
                    "(void **) malloc(sizeof(void *) * " + ninputCon + ");\n";
                typeInputFifoText += "typeInputFifo[" + armNodeId[nodeId] + "][" + i + "] = " + 
                    "(char *) malloc(sizeof(char) * " + ninputCon + ");\n";
                
                for(int j = 0;j < ninputCon;j++) {
                    ComponentGraphEdge edge = node.getInputGraphEdge(i, j);

                    VirtualPort sport = edge.getSinkPort();
                    Connection c = edge.getConnection();
                    String signalName = Node.replaceDotForUnd(sport.getFullName() + "_" +
                       c.getName());
                    if(crossProcessor(edge)) {
                        signalDecl += "DynamicDspFifo *" + signalName + ";\n";
                        signalDecl += "UNSIG16 *" + signalName + "_dataS;\n";
                        typeInputFifoText += "typeInputFifo[" + armNodeId[nodeId] + 
                            "][" + i + "][" + j + "] = " + "FINITE;\n";
                        
                        Attr attr = c.getAttribute("capacity");
                        Value v = attrValueMap.get(attr);
                        int capacity = (new Integer(v.getValueExpr())).intValue();

                        attr = c.getAttribute("datasize");
                        v = attrValueMap.get(attr);                        
                        int datasize = (new Integer(v.getValueExpr())).intValue();

                        DFEdgeISemInfo einfo = new DFEdgeISemInfo(edge.getISInfo());
                        einfo.setSharedMemAddr(sharedMemory);
                        edge.setISInfo(einfo);
                        
                        initDSPMemText += signalName + " = (DynamicDspFifo *) " +
                                " (sharedMem + " + sharedMemory + ");\n";
                        initDSPMemText += signalName + "_dataS = sharedMem + 8 +" +
                                sharedMemory + ";\n";
                        initDSPMemText += "initDynamicDspFifo(" + signalName + ", " +
                                datasize +  ", " + (capacity * datasize) + ");\n";
                        
                        sharedMemory += 8 + capacity * datasize;
                    }
                    else {
                        signalDecl += "dynamic_fifo *" + signalName + ";\n";
                        initSignals += signalName + " = init_dynamic_fifo(10);\n";
                        destroySignals += "destroy_dynamic_fifo(" + signalName + ");\n"; 
                        typeInputFifoText += "typeInputFifo[" + armNodeId[nodeId] + 
                            "][" + i + "][" + j + "] = " + "INFINITE;\n";
                    }                
                    inputFifoText += "inputFifo[" + armNodeId[nodeId] + "][" + i + "][" + j + "] = " +
                            signalName + ";\n";
                }
            }

            int nincomponents = node.numberOfInputComponents();
            ninputComponentsText += "ninputComponents[" + armNodeId[nodeId] + "] = " + nincomponents + ";\n";
            inputComponentsText += "inputComponents[" + armNodeId[nodeId] + "] = " + 
                    "(int *) malloc(sizeof(int) * " + nincomponents + ");\n";
            for(int i = 0;i < nincomponents;i++) {
                ComponentGraphNode source = node.getInputComponent(i);
                inputComponentsText +=  "inputComponents[" + armNodeId[nodeId] + "][" + i +
                        "] = " + source.getISInfo().getId() + ";\n";
            }

            int noutputs = node.numberOutputPorts();

            noutputsText += "noutputs[" + armNodeId[nodeId] + "] = " + noutputs + ";\n";
            noutputConnectionsText += "noutputConnections[" + armNodeId[nodeId] + "] =" +
                     "(int *) malloc(sizeof(int) * " + noutputs + ");\n";
            outputFifoText += "outputFifo[" + armNodeId[nodeId] + "] = " + 
                    "(void ***) malloc(sizeof(void **) * " + noutputs + ");\n";
            typeOutputFifoText += "typeOutputFifo[" + armNodeId[nodeId] + "] = " + 
                    "(char **) malloc(sizeof(char *) * " + noutputs + ");\n";
            outputSampleRatesText += "outputSampleRates[" + armNodeId[nodeId] + "] =" +
                     "(short ***) malloc(sizeof(short **) * " + noutputs + ");\n";

            for(int i = 0;i < noutputs;i++) {
                int ncon = node.numberOutputConnections(i);

                noutputConnectionsText += "noutputConnections[" + armNodeId[nodeId] + 
                        "][" + i + "] = " + ncon + ";\n";
                outputFifoText += "outputFifo[" + armNodeId[nodeId] + "][" + i + 
                        "] = " + "(void **) malloc(sizeof(void *) * " + ncon + ");\n";
                typeOutputFifoText += "typeOutputFifo[" + armNodeId[nodeId] + "][" + 
                        i + "] = " +  "(char **) malloc(sizeof(char *) * " + ncon + ");\n";
                outputSampleRatesText += "outputSampleRates[" + armNodeId[nodeId] + 
                        "][" + i + "] = " + "(short **) malloc(sizeof(short *) * " + ncon + ");\n";

                for(int j = 0;j < ncon;j++) {
                    ComponentGraphEdge edge = node.getOutputGraphEdge(i, j);
                    VirtualPort sport = edge.getSinkPort();
                    Connection c = edge.getConnection();
                    String signalName = Node.replaceDotForUnd(sport.getFullName() + "_" +
                       c.getName());

                    outputFifoText += "outputFifo[" + armNodeId[nodeId] + "][" + i + 
                            "][" + j + "] = " + signalName + ";\n";                    

                    if(crossProcessor(edge))  {
                        // this signal declarations are necessary, sine thos queues
                        // must be tested. But, the will riside in the DSP
                        signalDecl += "DynamicDspFifo *" + signalName + ";\n";
                        signalDecl += "UNSIG16 *" + signalName + "_dataS;\n";
                        typeOutputFifoText += "typeOutputFifo[" + armNodeId[nodeId] + 
                                "][" + i + "][" + j + "] = " + "FINITE;\n";
                        
                        Attr attr = c.getAttribute("capacity");
                        Value v = attrValueMap.get(attr);
                        int capacity = (new Integer(v.getValueExpr())).intValue();

                        attr = c.getAttribute("datasize");
                        v = attrValueMap.get(attr);                        
                        int datasize = (new Integer(v.getValueExpr())).intValue();
                                              
                        DFEdgeISemInfo einfo = new DFEdgeISemInfo(edge.getISInfo());
                        einfo.setSharedMemAddr(sharedMemory);
                        edge.setISInfo(einfo);

                        initDSPMemText += signalName + " = (DynamicDspFifo *) " +
                                " (sharedMem + " + sharedMemory + ");\n";
                        initDSPMemText += signalName + "_dataS = sharedMem + 8 +" +
                                sharedMemory + ";\n";
                        initDSPMemText += "initDynamicDspFifo(" + signalName + "," +
                                datasize +  "," + (capacity * datasize) + ");\n";

                        sharedMemory += 8 + capacity * datasize;
                    }
                    else {
                        typeOutputFifoText += "typeOutputFifo[" + armNodeId[nodeId] + 
                                "][" + i + "][" + j + "] = " + "INFINITE;\n";
                        outputSampleRatesText += "outputSampleRates[" + armNodeId[nodeId] +
                            "][" + i + "][" + j + "] = &" + Node.replaceDotForUnd(sport.getFullName()) + "_rate;\n";
                    }
                }
            }
            int noutcomponents = node.numberOfOutputComponents();

            noutputComponentsText += "noutputComponents[" + armNodeId[nodeId] + "] = " + noutcomponents + ";\n";
            outputComponentsText += "outputComponents[" + armNodeId[nodeId] + "] = " + 
                    "(int *) malloc(sizeof(int) * " + noutcomponents + ");\n";
            for(int i = 0;i < noutcomponents;i++) {
                ComponentGraphNode source = node.getOutputComponent(i);
                outputComponentsText += "outputComponents[" + armNodeId[nodeId] + "][" + i +
                        "] = " + source.getISInfo().getId() + ";\n";
            }
        }
        numberOfPages = sharedMemory/4096 + 1;
        
        topgen.setArgumentValue("numberOfPages", new String(numberOfPages + ""));
        
        topgen.setArgumentValue("ninputs", ninputsText);
        topgen.setArgumentValue("ninputConnections", ninputConnectionsText);
        topgen.setArgumentValue("ninputs", ninputsText);
        topgen.setArgumentValue("inputFifo", inputFifoText);
        topgen.setArgumentValue("inputSampleRates", inputSampleRatesText);
        topgen.setArgumentValue("ninputComponents", ninputComponentsText);
        topgen.setArgumentValue("inputComponents", inputComponentsText);
        topgen.setArgumentValue("typeInputFifo", typeInputFifoText);
        
        topgen.setArgumentValue("noutputs", noutputsText);
        topgen.setArgumentValue("noutputConnections", noutputConnectionsText);
        topgen.setArgumentValue("noutputs", noutputsText);
        topgen.setArgumentValue("outputFifo", outputFifoText);
        topgen.setArgumentValue("outputSampleRates", outputSampleRatesText);
        topgen.setArgumentValue("noutputComponents", noutputComponentsText);
        topgen.setArgumentValue("outputComponents", outputComponentsText);
        topgen.setArgumentValue("typeOutputFifo", typeOutputFifoText);

        topgen.setArgumentValue("initDSPMem", initDSPMemText);
        topgen.setArgumentValue("initCanSend", initCanSendText);
        topgen.setArgumentValue("canSendMethods", canSendMethodsText);
        topgen.setArgumentValue("isBoundary", isBoundaryText);
        topgen.setArgumentValue("boundary", boundaryText);
        topgen.setArgumentValue("signals", signalDecl);
        topgen.setArgumentValue("initSignals", initSignals);
        topgen.setArgumentValue("destroySignals", destroySignals);
    }
    
    private void generateARMAtomicComponents(Topology container, String containerName, 
            Graph g, Hver topVersion, String outputDir, String designLocation, 
            HashMap <Attr,Value> attrValueMap, 
            LinkedList<BoundaryData> outsideBinfo) {                
        
        for(int comp = 0;comp < topologicalSort.length;comp++) {
            ComponentGraphNode node = topologicalSort[comp];
            if(node.getType() != ComponentGraphNode.NodeType.ATOMIC) continue;
            int nodeId = node.getISInfo().getId();

            if(toDSP[nodeId]) continue;
            
            if(node.getType() != ComponentGraphNode.NodeType.ATOMIC) continue;
            DFAtomicComponentGenerator gen = new DFAtomicComponentGenerator(
                    "OmapH2/DF/FileGenerators/", "atomicARMComponent.xml");
            
            gen.setNcomponents(Integer.toString(topologicalSort.length - ntoDSP));
            gen.setNboundary(Integer.toString(armboundaryCounter));
            gen.setCname("c" + armNodeId[nodeId]);
            buildSignalVarLists("DF", node, attrValueMap, outsideBinfo);

            // 2. signals
            String signals = "";
            Iterator ite = node.getInEdges().iterator();
            while(ite.hasNext()) {
                ComponentGraphEdge edge = (ComponentGraphEdge) ite.next();
                String sname = inconQueue.get(edge.getConnection().getName());
                String ftype = "";
                if(crossProcessor(edge)) {
                    ftype = "DynamicDspFifo *";
                    //for the fifo data section
                    signals += "extern UNSIG16 *" + sname + "_dataS;\n";
                }
                else {
                    ftype = "dynamic_fifo *";
                }
                signals += "extern " + ftype + " " + sname + ";\n";
            }
            
            ite = node.getOutEdges().iterator();
            while(ite.hasNext()) {
                ComponentGraphEdge edge = (ComponentGraphEdge) ite.next();
                Connection c = edge.getConnection();            
                VirtualPort vp = edge.getSinkPort();                
                String sname = Node.replaceDotForUnd(vp.getFullName() + "_" +  
                    c.getName());
                String ftype = "";
                if(crossProcessor(edge)) {
                    ftype = "DynamicDspFifo *";
                    signals += "extern UNSIG16 *" + sname + "_dataS;\n";
                }
                else {
                    ftype = "dynamic_fifo *";
                }
                signals += "extern " + ftype + " " + sname + ";\n";
            }           

            for(VirtualPort vp : inportsQueue.keySet()) {
                signals += "extern short " + 
                        Node.replaceDotForUnd(vp.getFullName()) + "_rate;\n";
            }
            gen.setSignals(signals);

            gen.setMyId(Integer.toString(nodeId));
            gen.setBoundaryId(Integer.toString(boundaryId[nodeId]));

            generateDFSpecificMethods(node, gen);
            generateConnectionMethods(gen, node, outsideBinfo);

            super.generateAtomicComponents(gen, containerName, node, outputDir, 
                    designLocation, attrValueMap, outsideBinfo, "ARM");
        }
    }

    private void generateDSPAtomicComponents(Topology container, String containerName, 
            Graph g, Hver topVersion, String outputDir, String designLocation, 
            HashMap <Attr,Value> attrValueMap, 
            LinkedList<BoundaryData> outsideBinfo) {

        for(int comp = 0;comp < topologicalSort.length;comp++) {
            ComponentGraphNode node = topologicalSort[comp];
            if(node.getType() != ComponentGraphNode.NodeType.ATOMIC) continue;

            int nodeId = node.getISInfo().getId();
            
            if(!toDSP[nodeId]) continue;

            DFAtomicComponentGenerator gen = new DFAtomicComponentGenerator(
                    "OmapH2/DF/FileGenerators/", "atomicDSPComponent.xml");

            gen.setNcomponents(Integer.toString(ntoDSP));
            gen.setNboundary(Integer.toString(dspboundaryCounter));
            
            gen.setCname("c" + dspNodeId[nodeId]);
            buildSignalVarLists("DF", node, attrValueMap, outsideBinfo);

            // 2. signals
            String signals = "";
            Iterator ite = node.getInEdges().iterator();
            while(ite.hasNext()) {
                ComponentGraphEdge edge = (ComponentGraphEdge) ite.next();
                String sname = inconQueue.get(edge.getConnection().getName());
                signals += "extern UNSIG16 *" + sname + "_dataS;\n";
                signals += "extern DynamicDspFifo *" + " " + sname + ";\n";
            }
            
            ite = node.getOutEdges().iterator();
            while(ite.hasNext()) {
                ComponentGraphEdge edge = (ComponentGraphEdge) ite.next();
                Connection c = edge.getConnection();            
                VirtualPort vp = edge.getSinkPort();                
                String sname = Node.replaceDotForUnd(vp.getFullName() + "_" +  
                    c.getName());
                signals += "extern UNSIG16 *" + sname + "_dataS;\n";
                signals += "extern DynamicDspFifo *" + " " + sname + ";\n";
            }           

            for(VirtualPort vp : inportsQueue.keySet()) {
                signals += "extern int " + 
                        Node.replaceDotForUnd(vp.getFullName()) + "_rate;\n";
            }
            gen.setSignals(signals);

            gen.setMyId(Integer.toString(nodeId));
            gen.setBoundaryId(Integer.toString(boundaryId[nodeId]));

            generateDFSpecificMethods(node, gen);
            generateConnectionMethods(gen, node, outsideBinfo);

            super.generateAtomicComponents(gen, containerName, node, outputDir, 
                    designLocation, attrValueMap, outsideBinfo, "DSP");
        }
    }
    
    private void generateDFSpecificMethods(ComponentGraphNode node, 
            DFAtomicComponentGenerator gen) {
        String resetSampleRatesText = "";
        String setSampleRateText = "";
        String getSampleRateText = "";
        Iterator ite = inportsQueue.keySet().iterator();
        while(ite.hasNext()) {
            VirtualPort vp = (VirtualPort) ite.next();
            resetSampleRatesText += Node.replaceDotForUnd(vp.getFullName()) 
                + "_rate = 0;\n";

            setSampleRateText += "if(strcmp(p1,\"" + vp.getName() + "\") == 0)\n" +
                    Node.replaceDotForUnd(vp.getFullName()) + "_rate = p2;\n";
            if(ite.hasNext()) {
                setSampleRateText += "else\n";
            }

            getSampleRateText += "if(strcmp(p1,\"" + vp.getName() + "\") == 0)\n" +
                    "return " + Node.replaceDotForUnd(vp.getFullName())
                    + "_rate ;\n";
        }
        gen.setGetSampleRate(getSampleRateText);
        gen.setSetSampleRate(setSampleRateText);
        gen.setStopExecution("");
    }
    
    private void classifyComponents() {        
        ntoDSP = 0;
        int armcounter = 0;
        int dspcounter = 0;

        armboundaryCounter = 0;
        dspboundaryCounter = 0;

        for(int i = 0;i < topologicalSort.length;i++) {
            CompInstance ains = topologicalSort[i].getComponent();
            int nodeId = topologicalSort[i].getISInfo().getId();
            if(topologicalSort[i].getType() == NodeType.COMPOSITE_ASEMANTIC) {
                continue;
            }

            CompBase abase = ains.getComponent();
            Attr atr = abase.getAttribute("toDSP");
            if(atr == null) {
                toDSP[nodeId] = false;
                armNodeId[nodeId] = armcounter++;
            }
            else {
                Value v = attrValueMap.get(atr);
                if(v.getValueExpr().equals("true")) {
                    toDSP[nodeId] = true;
                    dspNodeId[nodeId] = dspcounter++;
                    ntoDSP++;
                }
                else {
                    toDSP[nodeId] = false;
                    armNodeId[nodeId] = armcounter++;
                }
            }           
        }
        
        for(int i = 0;i < topologicalSort.length;i++) {
            CompInstance ains = topologicalSort[i].getComponent();
            int nodeId = topologicalSort[i].getISInfo().getId();
            if(topologicalSort[i].getType() == NodeType.COMPOSITE_ASEMANTIC) {
                continue;
            }
            
            Iterator ite = topologicalSort[i].getInEdges().iterator();
            while(ite.hasNext()) {
                ComponentGraphEdge edge = (ComponentGraphEdge) ite.next();
                if(crossProcessor(edge)) {
                    topologicalSort[i].removeInputComponent((ComponentGraphNode)edge.getSource());

                    if(!isBoundary[nodeId]) {
                        isBoundary[nodeId] = true;
                        if(toDSP[nodeId]) {
                            boundaryId[nodeId] = dspboundaryCounter++;
                        }
                        else {
                            boundaryId[nodeId] = armboundaryCounter++;
                        }                        
                    }                    
                }
            }

            ite = topologicalSort[i].getOutEdges().iterator();
            while(ite.hasNext()) {                
                ComponentGraphEdge edge = (ComponentGraphEdge) ite.next();               
                if(crossProcessor(edge)) {
                    topologicalSort[i].removeOutputComponent((ComponentGraphNode)edge.getDest());
                    if(!isBoundary[nodeId]) {
                        isBoundary[nodeId] = true;
                        if(toDSP[nodeId]) {
                            boundaryId[nodeId] = dspboundaryCounter++;
                        }
                        else {
                            boundaryId[nodeId] = armboundaryCounter++;
                        }
                    }
                }
            }
        } 
    }
    
    private boolean crossProcessor(ComponentGraphEdge edge) {
        ComponentGraphNode source = (ComponentGraphNode) edge.getSource();
        ComponentGraphNode sink = (ComponentGraphNode) edge.getDest();

        if((toDSP[source.getISInfo().getId()] && !toDSP[sink.getISInfo().getId()]) ||
           (!toDSP[source.getISInfo().getId()] && toDSP[sink.getISInfo().getId()])) {
            return true;
        }    
        return false;
    }
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////

    private int armboundaryCounter;
    private int dspboundaryCounter;
    private int ntoDSP;
    private int numberOfPages;   // determined by the processSignals arm method

    // the actual node id, since it must start from zero
    private int [] armNodeId;
    private int [] dspNodeId;
    
    private boolean[] toDSP;
    private boolean[] isBoundary;
    private int[] boundaryId;

}
