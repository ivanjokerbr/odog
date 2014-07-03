/*
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2
 * see LICENSE file
 */
package odog.codegen.Platform.Host.DF;

import odog.codegen.AtomicComponentGenerator;
import odog.codegen.BoundaryData;
import odog.codegen.ISemGenerator;
import odog.configuration.BaseConfiguration;
import odog.graph.ComponentGraphEdge;
import odog.graph.ComponentGraphNode;
import odog.graph.CompositeComponentGraphNode;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.Connection;
import odog.syntax.Nodes.Hver;
import odog.syntax.Nodes.Method;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.Topology;
import odog.syntax.Nodes.Value;
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

/**
 *
 * The dataflow code generator.
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
            //containerName = Node.replaceDotForUnd(toplevel.getFullName());
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
            loc = BaseConfiguration.appendSlash(loc) + "Host/DF/FileGenerators/";
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
        loc = BaseConfiguration.appendSlash(loc) + "Host/DF/FileGenerators/";
        
        // verificar a implementacao dessse port queue 
        HashMap <String, String> portQueueTable = new HashMap<String, String>();
      
        // Communication methods on input ports
        Iterator ite = inportsQueue.keySet().iterator();
        while(ite.hasNext()) {
            VirtualPort vp = (VirtualPort) ite.next();
            LinkedList<String> l = inportsQueue.get(vp);
            for(int i = 0;i < l.size();i++) {
               String qname = l.get(i);
               portQueueTable.put(qname, vp.getFullName());
            }
        }

        // Communication methods on input channels
        FileGenerator receiveGenerator = FileGeneratorParser.parse(loc + "receive.xml");
        String receiveText = "";        
        ite = inconQueue.keySet().iterator();
        while(ite.hasNext()) {
            String pname = (String) ite.next();
            receiveText = receiveText + "if(strcmp(p1,\"" + pname + "\") == 0) {\n";
            String qname = inconQueue.get(pname);            
            // RECEIVE
            receiveGenerator.setArgumentValue("arg0", qname);            
            receiveText = receiveText + "\t" + receiveGenerator.toString() + "\n"; 

            receiveText += "}\n";
            if(ite.hasNext()) {
                receiveText += "else\n";
            }
        }
        gen.setReceive(receiveText);        

        FileGenerator sendGenerator = FileGeneratorParser.parse(loc + "send.xml");
        String sendText = "";
        ite = outconQueue.keySet().iterator();
        while(ite.hasNext()) {
            String pname = (String) ite.next();

            sendText = sendText + "if(strcmp(p1,\"" + pname + "\") == 0) {\n";
            LinkedList<String> l = outconQueue.get(pname);
            for(int i = 0;i < l.size();i++) {      
                // SEND
                String qname = l.get(i);
                sendGenerator.setArgumentValue("arg0", qname);
                sendGenerator.setArgumentValue("arg1", "p2");
                sendGenerator.setArgumentValue("arg2", "length");
                sendText = sendText + "\t"+ sendGenerator.toString() + "\n";            
            }            
            sendText += "}\n";
            if(ite.hasNext()) {
                sendText += "else\n";
            }
        }
        gen.setSend(sendText);
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    private void generateTopology(String instanceName, Topology toplevel, 
            Hver topVersion, Graph g, String designLocation, String outputDir,
            HashMap <Attr, Value> attrValueMap, 
            LinkedList<BoundaryData> outsideBinfo) {
        String cgloc = System.getenv("ODOG_CODEGENERATORS");
        String loc = BaseConfiguration.appendSlash(cgloc) + "Host/DF/FileGenerators/";
        FileGenerator topgen = FileGeneratorParser.parse(loc + "scheduler.xml");

        topgen.setArgumentValue("ncomponents", String.valueOf(g.getVertices().size()));
        topgen.setArgumentValue("topologyName", instanceName);
        
        processSignals(g, topgen, loc, toplevel, cgloc, instanceName,
                outsideBinfo);
        
        int id = topologicalSort[0].getISInfo().getId();        
        //String name = Node.replaceDotForUnd(topologicalSort[0].getComponent().getFullInstanceName());
        
        String name = topologicalSort[0].getISInfo().getCGName();
        String componentExecMethod = "extern void " + name + "_odog_init();\n";
        componentExecMethod = componentExecMethod + "extern void " + name + "_odog_compute();\n";
        componentExecMethod = componentExecMethod + "extern void " + name + "_odog_finish();\n";
        
        String fptr = instanceName + "_fptr[" + String.valueOf(id) + "][0] = " + name + "_odog_init;\n";
        fptr += instanceName + "_fptr[" + String.valueOf(id) + "][1] = " + name + "_odog_compute;\n";
        fptr += instanceName + "_fptr[" + String.valueOf(id) + "][2] = " + name + "_odog_finish;\n"; 

        for(int i = 1;i < topologicalSort.length;i++) {
            id = topologicalSort[i].getISInfo().getId();
            
            //name = Node.replaceDotForUnd(topologicalSort[i].getComponent().getFullInstanceName());
            name = topologicalSort[i].getISInfo().getCGName();
            componentExecMethod = componentExecMethod + "extern void " + name + "_odog_init();\n";
            componentExecMethod = componentExecMethod + "extern void " + name + "_odog_compute();\n";
            componentExecMethod = componentExecMethod + "extern void " + name + "_odog_finish();\n";
            
            fptr += instanceName + "_fptr[" + String.valueOf(id) + "][0] = " + name + "_odog_init;\n";
            fptr += instanceName + "_fptr[" + String.valueOf(id) + "][1] = " + name + "_odog_compute;\n";
            fptr += instanceName + "_fptr[" + String.valueOf(id) + "][2] = " + name + "_odog_finish;\n";
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

    private void processSignals(Graph g, FileGenerator topgen, String loc, 
            Topology toplevel, String cgloc, String instanceName, 
            LinkedList<BoundaryData> outsideBinfo) {
        HashMap <ComponentGraphNode, Object> compToInPort = new HashMap<ComponentGraphNode, Object>();
        HashMap <ComponentGraphNode, Object> compToOutPort = new HashMap<ComponentGraphNode, Object>();
        HashMap <ComponentGraphNode, LinkedList<ComponentGraphNode>> inputComponents = 
                new HashMap<ComponentGraphNode, LinkedList<ComponentGraphNode>>();
        HashMap <ComponentGraphNode, LinkedList<ComponentGraphNode>> outputComponents = 
                new HashMap<ComponentGraphNode, LinkedList<ComponentGraphNode>>();

        // 1. Declare all signals that have connections. This includes the 
        // dynamic fifo and a sample rate variable for the associated ports.
        String signalDecl = "";
        String initSignals = "";
        String destroySignals = "";
        Iterator ite = g.getVertices().iterator();
        while(ite.hasNext()) {
            ComponentGraphNode node = (ComponentGraphNode) ite.next();

            LinkedList<ComponentGraphNode> inputcomponents = new LinkedList<ComponentGraphNode>();
            inputComponents.put(node, inputcomponents);
            
            LinkedList<ComponentGraphNode> outputcomponents = new LinkedList<ComponentGraphNode>();
            outputComponents.put(node, outputcomponents);

            // PROCESS IN CONNECTIONS - also include the declaration of the dynamic fifos
            HashMap <VirtualPort, Object> portToConnection = new HashMap <VirtualPort, Object>();
            Iterator inite = node.getInEdges().iterator();
            while(inite.hasNext()) {
                ComponentGraphEdge edge = (ComponentGraphEdge) inite.next();                               
                
                VirtualPort sport = edge.getSinkPort();
                Connection c = edge.getConnection();
                //String signalName = Node.replaceDotForUnd(sport.getFullName() + "_" +
//                   c.getName());
                String signalName = edge.getISInfo().getCGName();

                signalDecl += "dynamic_fifo *" + signalName + ";\n";
                initSignals += signalName + " = init_dynamic_fifo(10);\n";
                destroySignals += "destroy_dynamic_fifo(" + signalName + ");\n";

                if(portToConnection.containsKey(sport)) {
                    LinkedList<String> l = (LinkedList) portToConnection.get(sport);
                    l.add(signalName);
                }
                else {                                        
                    LinkedList<String> connections = new LinkedList<String>();
                    connections.add(signalName);
                    portToConnection.put(sport, connections);
                }                
                
                ComponentGraphNode source = (ComponentGraphNode) edge.getSource();
                if(!inputcomponents.contains(source)) {
                    inputcomponents.add(source);
                }
            }            
            compToInPort.put(node, portToConnection);
            
            // PROCESS OUT CONNECTIONS
            portToConnection = new HashMap <VirtualPort, Object>();            
            HashMap <VirtualPort, LinkedList<VirtualPort>> outPortToInPort = 
                    new HashMap <VirtualPort, LinkedList<VirtualPort>>();
            Iterator outite = node.getOutEdges().iterator();
            while(outite.hasNext()) {
                ComponentGraphEdge edge = (ComponentGraphEdge) outite.next();

                VirtualPort sourcep = edge.getSourcePort();                
                VirtualPort sport = edge.getSinkPort();

                if(portToConnection.containsKey(sourcep)) {
                    LinkedList<ComponentGraphEdge> l = (LinkedList) portToConnection.get(sourcep);
                    l.add(edge);
                }
                else {                                        
                    LinkedList<ComponentGraphEdge> connections = new LinkedList<ComponentGraphEdge>();
                    connections.add(edge);
                    portToConnection.put(sourcep, connections);
                }
                
                ComponentGraphNode dest = (ComponentGraphNode) edge.getDest();
                if(!outputcomponents.contains(dest)) {
                    outputcomponents.add(dest);
                }
            }
            compToOutPort.put(node, portToConnection);
        }

        // PROCESS THE BOUNDARY SIGNALS                
        
        // generate the input initializations        
        String ninputsText = "";
        String ninputConnectionsText = "";        
        String inputFifoText = "";
        String inputSampleRatesText = "";
        String ninputComponentsText = "";
        String inputComponentsText = "";
        for(ComponentGraphNode node : compToInPort.keySet()) {
            HashMap<VirtualPort, Object> portToConnection = (HashMap) compToInPort.get(node);
            int ninputs = portToConnection.keySet().size();
            ninputsText += "ninputs[" + node.getISInfo().getId() + "] = " + ninputs + ";\n";
            
            int nodeId = node.getISInfo().getId();
            
            ninputConnectionsText += "ninputConnections[" + nodeId + "] =" +
                     "(int *) malloc(sizeof(int) * " + ninputs + ");\n";
            inputFifoText += "inputFifo[" + nodeId + "] = " + 
                    "(dynamic_fifo ***) malloc(sizeof(dynamic_fifo **) * " + ninputs + ");\n";
            inputSampleRatesText += "inputSampleRates[" + nodeId + "] =" +
                     "(short **) malloc(sizeof(short *) * " + ninputs + ");\n";
            int portn = 0;
            for(VirtualPort port : portToConnection.keySet()) {
                signalDecl += "short " + Node.replaceDotForUnd(port.getFullName()) + "_rate;\n";
                
                LinkedList<String> connections = (LinkedList) portToConnection.get(port);
                
                ninputConnectionsText += "ninputConnections[" + node.getISInfo().getId() + "][" +
                        portn + "] = " + connections.size() + ";\n";
                
                inputSampleRatesText += "inputSampleRates[" + nodeId + "][" +
                        portn + "] = &" + Node.replaceDotForUnd(port.getFullName()) + "_rate;\n";
                
                inputFifoText += "inputFifo[" + nodeId + "][" + portn + "] = " + 
                    "(dynamic_fifo **) malloc(sizeof(dynamic_fifo *) * " + connections.size() + ");\n";
                for(int i = 0;i < connections.size();i++) {
                    String sname = connections.get(i);
                    inputFifoText += "inputFifo[" + nodeId + "][" + portn + "][" + i + "] = " +
                            sname + ";\n";                    
                }
                portn++;
            }
            LinkedList<ComponentGraphNode> inputcomponents = inputComponents.get(node);
            ninputComponentsText += "ninputComponents[" + nodeId + "] = " + inputcomponents.size() + ";\n";
            inputComponentsText += "inputComponents[" + nodeId + "] = " + 
                    "(int *) malloc(sizeof(int) * " + inputcomponents.size() + ");\n";
            int index = 0;
            for(ComponentGraphNode source : inputcomponents) {
                inputComponentsText +=  "inputComponents[" + nodeId + "][" + index +
                        "] = " + source.getISInfo().getId() + ";\n";
                index++;
            }
        }
        topgen.setArgumentValue("ninputs", ninputsText);
        topgen.setArgumentValue("ninputConnections", ninputConnectionsText);
        topgen.setArgumentValue("ninputs", ninputsText);
        topgen.setArgumentValue("inputFifo", inputFifoText);
        topgen.setArgumentValue("inputSampleRates", inputSampleRatesText);
        topgen.setArgumentValue("ninputComponents", ninputComponentsText);
        topgen.setArgumentValue("inputComponents", inputComponentsText);
       
        // generate the input initializations        
        String noutputsText = "";
        String noutputConnectionsText = "";        
        String outputFifoText = "";
        String outputSampleRatesText = "";
        String noutputComponentsText = "";
        String outputComponentsText = "";
        for(ComponentGraphNode node : compToOutPort.keySet()) {
            HashMap<VirtualPort, Object> portToConnection = (HashMap) compToOutPort.get(node);
            int noutputs = portToConnection.keySet().size();
            
            int nodeId = node.getISInfo().getId();
            
            noutputsText += "noutputs[" + nodeId + "] = " + noutputs + ";\n";
            noutputConnectionsText += "noutputConnections[" + nodeId + "] =" +
                     "(int *) malloc(sizeof(int) * " + noutputs + ");\n";
            outputFifoText += "outputFifo[" + nodeId + "] = " + 
                    "(dynamic_fifo ***) malloc(sizeof(dynamic_fifo **) * " + noutputs + ");\n";
            outputSampleRatesText += "outputSampleRates[" + nodeId + "] =" +
                     "(short ***) malloc(sizeof(short **) * " + noutputs + ");\n";
            int portn = 0;
            for(VirtualPort port : portToConnection.keySet()) {
                LinkedList<ComponentGraphEdge> connections = (LinkedList) portToConnection.get(port);
                
                noutputConnectionsText += "noutputConnections[" + nodeId + "][" +
                        portn + "] = " + connections.size() + ";\n";
                
                outputFifoText += "outputFifo[" + nodeId + "][" + portn + "] = " + 
                    "(dynamic_fifo **) malloc(sizeof(dynamic_fifo *) * " + connections.size() + ");\n";
                outputSampleRatesText += "outputSampleRates[" + nodeId + "][" + portn + "] = " + 
                    "(short **) malloc(sizeof(short *) * " + connections.size() + ");\n";
                
                for(int i = 0;i < connections.size();i++) {
                    ComponentGraphEdge edge = connections.get(i);
                    VirtualPort sport = edge.getSinkPort();
                    Connection c = edge.getConnection();
                    //String signalName = Node.replaceDotForUnd(sport.getFullName() + "_" +
                    //   c.getName());
                    String signalName = edge.getISInfo().getCGName();

                    outputFifoText += "outputFifo[" + nodeId + "][" + portn + "][" + i + "] = " +
                            signalName + ";\n";                    
                    outputSampleRatesText += "outputSampleRates[" + nodeId + "][" +
                        portn + "][" + i + "] = &" + Node.replaceDotForUnd(sport.getFullName()) + "_rate;\n";
                }
                portn++;
            }
            LinkedList<ComponentGraphNode> outputcomponents = outputComponents.get(node);
            noutputComponentsText += "noutputComponents[" + nodeId + "] = " + outputcomponents.size() + ";\n";
            outputComponentsText += "outputComponents[" + nodeId + "] = " + 
                    "(int *) malloc(sizeof(int) * " + outputcomponents.size() + ");\n";
            int index = 0;
            for(ComponentGraphNode source : outputcomponents) {
                outputComponentsText += "outputComponents[" + nodeId + "][" + index +
                        "] = " + source.getISInfo().getId() + ";\n";
                index++;
            }
        }
        topgen.setArgumentValue("noutputs", noutputsText);
        topgen.setArgumentValue("noutputConnections", noutputConnectionsText);
        topgen.setArgumentValue("noutputs", noutputsText);
        topgen.setArgumentValue("outputFifo", outputFifoText);
        topgen.setArgumentValue("outputSampleRates", outputSampleRatesText);
        topgen.setArgumentValue("noutputComponents", noutputComponentsText);
        topgen.setArgumentValue("outputComponents", outputComponentsText);
        
        ///        
        topgen.setArgumentValue("signals", signalDecl);
        topgen.setArgumentValue("initSignals", initSignals);
        topgen.setArgumentValue("destroySignals", destroySignals);
    }
    
    private void generateAtomicComponents(Topology container, String containerName, 
            Graph g, Hver topVersion, String outputDir, String designLocation, 
            HashMap <Attr,Value> attrValueMap, 
            LinkedList<BoundaryData> outsideBinfo) {
        
        Iterator ite = g.getVertices().iterator();
        while(ite.hasNext()) {
            DFComponentGenerator gen = new DFComponentGenerator("Host/DF/FileGenerators/",
                    "atomicComponent.xml");
            ComponentGraphNode node = (ComponentGraphNode) ite.next();
            if(node.getType() != ComponentGraphNode.NodeType.ATOMIC) continue;
            
            buildSignalVarLists("DF", node, attrValueMap, outsideBinfo);

            // 2. signals
            String signals = "";
            for(String sname : inconQueue.values()) {
                signals += "extern dynamic_fifo* " + sname + ";\n";
            }           
            for(LinkedList<String> l : outconQueue.values()) {
                for(int i = 0;i < l.size();i++) {
                    String sname = l.get(i);
                    signals += "extern dynamic_fifo* " + sname + ";\n";
                }
            }

            for(VirtualPort vp : inportsQueue.keySet()) {
                signals += "extern short " + 
                        Node.replaceDotForUnd(vp.getFullName()) + "_rate;\n";
            }
            
            signals += "extern char " + containerName + "_stopExecution;\n";
            gen.setSignals(signals);
            
            generateDFSpecificMethods(gen, containerName);

            generateConnectionMethods(gen, node, outsideBinfo);
            
            super.generateAtomicComponents(gen, containerName, node, outputDir, 
                    designLocation, attrValueMap, outsideBinfo, "default");
        }
    }

    private void generateDFSpecificMethods(DFComponentGenerator gen, 
            String containerName) {        
        String setSampleRateText = "";
        String getSampleRateText = "";
        Iterator ite = inportsQueue.keySet().iterator();
        while(ite.hasNext()) {
            VirtualPort vp = (VirtualPort) ite.next();
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
        
        gen.setStopExecution(containerName + "_stopExecution = 1;\n");
    }

    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
}
