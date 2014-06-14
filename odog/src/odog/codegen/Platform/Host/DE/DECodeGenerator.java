/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.codegen.Platform.Host.DE;

import odog.codegen.AtomicComponentGenerator;
import odog.codegen.BoundaryData;
import odog.codegen.ISemGenerator;
import odog.configuration.BaseConfiguration;
import odog.graph.ComponentGraphEdge;
import odog.graph.ComponentGraphNode;
import odog.graph.CompositeComponentGraphNode;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.Hver;
import odog.syntax.Nodes.Method;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.Topology;
import odog.syntax.Nodes.Value;
import odog.codegen.util.FileGenerator;
import odog.codegen.util.FileGeneratorParser;
import edu.uci.ics.jung.graph.Graph;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Performs the code generation for a topology associated with the DE model.
 *
 * The name of an input queue is given by fullnameOfThePort + "_" + connectionName
 *
 * @author ivan
 */
public class DECodeGenerator extends ISemGenerator {
    
    /** Creates a new instance of DECodeGenerator */
    public DECodeGenerator(Hashtable <Attr, Value> attrValueMap) {
        super(attrValueMap);
    }

    ////////////////////////////// PUBLIC METHODS //////////////////////////////

    public boolean codeGenerate(CompositeComponentGraphNode topnode, String designLocation) {
        Graph graph = topnode.getInsideGraph();
        Hver selectedVersion = (Hver) topnode.getSelectedVer();
        // the DE model can only be the toplevel interaction sem
        Topology toplevel = topnode.getComposite().getRootNode();
        
        // 2. Produz o vetor de ordem topologica, associando os ids e determinando
        // o numero de filas de eventos
        if(!topologicalSort(graph)) {
            return false;
        }

        // The DE model can only be used as toplevel.....
        String outputDir = designLocation + selectedVersion.getFullName() + "/";
        File f = new File(outputDir);
        if(!f.exists()) {
            f.mkdir();            
        }
        LinkedList <BoundaryData> outsideBinfo = new LinkedList<BoundaryData>();
        // 3. Produz o modelo a ser gerado para cada ator
        
        generateAtomicComponents(topnode.getISInfo().getCGName(), graph,
                outputDir, designLocation, attrValueMap, outsideBinfo);

        generateCompositeComponents(graph, outputDir, designLocation, attrValueMap, 
                outsideBinfo);
        
        // 4. Creates the module for the scheduler
        generateTopology(topnode.getISInfo().getCGName(), toplevel, selectedVersion, graph, 
                designLocation, outputDir);
        
        // Generate the main.c
        String loc = System.getenv("ODOG_CODEGENERATORS");
        loc = BaseConfiguration.appendSlash(loc) + "Host/DE/FileGenerators/";
        FileGenerator maing = FileGeneratorParser.parse(loc + "main.xml");        
        maing.setArgumentValue("toplevelName", topnode.getISInfo().getCGName());
        try {
            f = new File(outputDir + "main.c");
            FileOutputStream fos = new FileOutputStream(f);
            PrintStream ps = new PrintStream(fos);
            ps.print(maing.toString());

            ps.close();
            fos.close();
        }
        catch(Exception ex) {
            System.out.println(ex);
        }
        return true;
    }

    ///////////////////////////// PROTECTED METHODS //////////////////////////////
    
    // Loads a program file given by the url. Perform all method substituions
    // for the DE model, and return the result.
    protected String performSubstitutions(ComponentGraphNode node, String url) {
        // 3. load the code file
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
        text = text.replaceAll("currentTime\\(\\)", "currentTime");
        text = text.replaceAll("abortSchedule\\(\\)" , "abortSchedule(" + 
                String.valueOf(node.getISInfo().getId()));
        
        return text;
    }
    
    protected void generateCommunicationMethods(ComponentGraphNode node, 
            AtomicComponentGenerator gen, Hashtable <Attr, Value> attrValueMap,
            String containerName, LinkedList<BoundaryData> outsideBinfo) {
        String loc = System.getenv("ODOG_CODEGENERATORS");
        loc = BaseConfiguration.appendSlash(loc) + "Host/DE/FileGenerators/";      

        // Communication methods on input channels        
        FileGenerator receiveGenerator = FileGeneratorParser.parse(loc + "receive.xml");
        FileGenerator canReceiveGenerator = FileGeneratorParser.parse(loc + "canReceive.xml");
        String receiveText = "";
        String canReceiveText = "";

        Iterator ite = node.getInEdges().iterator();
        while(ite.hasNext()) {
            ComponentGraphEdge edge = (ComponentGraphEdge) ite.next();
            String pname = edge.getConnection().getName();

            receiveText = receiveText + "if(strcmp(p1,\"" + pname + "\") == 0) {\n";
            canReceiveText = canReceiveText + "if(strcmp(p1,\"" + pname + "\") == 0) {\n" + "return ";
            
            //String qname = edge.getSignalName();
            String qname = edge.getISInfo().getCGName();
            // RECEIVE
            receiveGenerator.setArgumentValue("arg1", "p2");                
            receiveGenerator.setArgumentValue("arg0", qname);
            receiveGenerator.setArgumentValue("arg2", "p3");
            receiveText = receiveText + "\t" + receiveGenerator.toString() + "\n"; 

            // CANRECEIVE
            canReceiveGenerator.setArgumentValue("arg0", qname);
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
        
        // Communication methods on output ports
        FileGenerator sendGenerator = FileGeneratorParser.parse(loc + "send.xml");
        FileGenerator sendTimedGenerator = FileGeneratorParser.parse(loc + "sendTimed.xml");
        String sendText = "";
        String sendDelayedText = "";

        ite = outconQueue.keySet().iterator();
        while(ite.hasNext()) {
            String pname = (String) ite.next();

            sendText = sendText + "if(strcmp(p1,\"" + pname + "\") == 0) {\n";
            sendDelayedText = sendDelayedText + "if(strcmp(p1,\"" + pname + "\") == 0) {\n";
            
            LinkedList<String> l = outconQueue.get(pname);
            for(int i = 0;i < l.size();i++) {      
                // SEND
                String qname = l.get(i);
                sendGenerator.setArgumentValue("arg0", qname);
                sendGenerator.setArgumentValue("arg1", "p2");
                sendGenerator.setArgumentValue("arg2", "length");
                sendText = sendText + "\t"+ sendGenerator.toString() + "\n";
            
                sendTimedGenerator.setArgumentValue("arg0", qname);
                sendTimedGenerator.setArgumentValue("arg1", "p2");
                sendTimedGenerator.setArgumentValue("arg2", "length");
                sendTimedGenerator.setArgumentValue("arg3", "p3");
                sendTimedGenerator.setArgumentValue("componentID", 
                        componentIdTable.get(qname).toString());
                sendDelayedText = sendDelayedText + "\t\t"+ sendTimedGenerator.toString() + "\n";
            }
            sendText += "\tDE_zeroTimeEvents = TRUE;\n}\n";
            sendDelayedText += "}\n";
            if(ite.hasNext()) {
                sendText += "else\n";
                sendDelayedText += "else\n";
            }
        }
        gen.setSend(sendText);
        gen.setSendDelayed(sendDelayedText);        
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////

    private void generateTopology(String instanceName, Topology toplevel, 
            Hver topVersion, Graph g, String designLocation, String outputDir) {
        String loc = System.getenv("ODOG_CODEGENERATORS");
        loc = BaseConfiguration.appendSlash(loc) + "Host/DE/FileGenerators/";
        FileGenerator topgen = FileGeneratorParser.parse(loc + "scheduler.xml");

        topgen.setArgumentValue("ncomponents", String.valueOf(g.getVertices().size()));
        topgen.setArgumentValue("topologyName", instanceName);

        int maxqueues = 0;
        String setQueues = "";

        LinkedList<String> inputQueues = new LinkedList<String>();
        Iterator ite = g.getVertices().iterator();
        while(ite.hasNext()) {
            ComponentGraphNode node = (ComponentGraphNode) ite.next();
            int index = 0;
            Iterator inite = node.getInEdges().iterator();
            while(inite.hasNext()) {
                ComponentGraphEdge edge = (ComponentGraphEdge) inite.next();
                //String qname = Node.replaceDotForUnd(edge.getSinkPort().getFullName() + "_" +
//                   edge.getConnection().getName());
                String qname = edge.getISInfo().getCGName();
                inputQueues.add(qname);

                setQueues = setQueues + "queues[" + String.valueOf(node.getISInfo().getId()) +
                        "][" + String.valueOf(index) + "] = " + qname + ";\n";
                index++;
            }
            if(node.getInEdges().size() > maxqueues) {
                maxqueues = node.getInEdges().size();
            }
        }
        topgen.setArgumentValue("maxQueues", String.valueOf(maxqueues));
        topgen.setArgumentValue("setQueues", setQueues);

        String queuesDecl = ""; 
        String initQueues = "";
        String destroyQueues = "";
        for(String inputQ : inputQueues) {
            queuesDecl = queuesDecl + "calendarQueue_t * " + inputQ + ";\n";
            initQueues = initQueues + inputQ +  " = calendar_newQueue(FALSE);\n";
            destroyQueues = destroyQueues + "calendar_destroyQueue(" + inputQ +
                    ");\n";
        }
        topgen.setArgumentValue("inputQueues", queuesDecl);
        topgen.setArgumentValue("initializeQueues", initQueues);
        topgen.setArgumentValue("destroyQueues", destroyQueues);

        int id = topologicalSort[0].getISInfo().getId();
        String topOrder = String.valueOf(id);

        //String name = Node.replaceDotForUnd(topologicalSort[0].getComponent().getFullInstanceName());
        String name = topologicalSort[0].getISInfo().getCGName();
        String componentExecMethod = "extern void " + name + "_init();\n";
        componentExecMethod = componentExecMethod + "extern void " + name + "_compute();\n";
        componentExecMethod = componentExecMethod + "extern void " + name + "_finish();\n";
        
        String fptr = instanceName + "_fptr[" + String.valueOf(id) + "][0] = " + name + "_init;\n";
        fptr += instanceName + "_fptr[" + String.valueOf(id) + "][1] = " + name + "_compute;\n";
        fptr += instanceName + "_fptr[" + String.valueOf(id) + "][2] = " + name + "_finish;\n"; 

        for(int i = 1;i < topologicalSort.length;i++) {
            id = topologicalSort[i].getISInfo().getId();
            topOrder = topOrder + "," + String.valueOf(id);
            
            //name = Node.replaceDotForUnd(topologicalSort[i].getComponent().getFullInstanceName());
            name = topologicalSort[i].getISInfo().getCGName();
            componentExecMethod = componentExecMethod + "extern void " + name + "_init();\n";
            componentExecMethod = componentExecMethod + "extern void " + name + "_compute();\n";
            componentExecMethod = componentExecMethod + "extern void " + name + "_finish();\n";
            
            fptr += instanceName + "_fptr[" + String.valueOf(id) + "][0] = " + name + "_init;\n";
            fptr += instanceName + "_fptr[" + String.valueOf(id) + "][1] = " + name + "_compute;\n";
            fptr += instanceName + "_fptr[" + String.valueOf(id) + "][2] = " + name + "_finish;\n";
        }
        topgen.setArgumentValue("topologicalOrder", topOrder);
        topgen.setArgumentValue("componentsExecMethods", componentExecMethod);
        topgen.setArgumentValue("initializeFptr", fptr);
        
        String selfSched = "-1.0";
        for(int i = 1;i < g.getVertices().size();i++) {
            selfSched = selfSched + ", -1.0";
        }
        topgen.setArgumentValue("selfSchedule", selfSched);

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

    private void generateAtomicComponents(String containerName, Graph g,
            String outputDir, String designLocation, 
            Hashtable <Attr, Value> attrValueMap,
            LinkedList<BoundaryData> outsideBinfo) {
        
        Iterator ite = g.getVertices().iterator();
        while(ite.hasNext()) {
            DEComponentGenerator gen = new DEComponentGenerator();
            ComponentGraphNode node = (ComponentGraphNode) ite.next();
            if(node.getType() != ComponentGraphNode.NodeType.ATOMIC) continue;
            
            buildSignalVarLists("DE", node, attrValueMap, outsideBinfo);
            
            // 2. signals
            String signals = "";
            Iterator conite = node.getInEdges().iterator();
            while(conite.hasNext()) {
                ComponentGraphEdge edge = (ComponentGraphEdge) conite.next();                
                //signals = signals + "extern calendarQueue_t* " + edge.getSignalName() + ";\n";
                signals = signals + "extern calendarQueue_t* " + 
                        edge.getISInfo().getCGName() + ";\n";
            }
            
            conite = node.getOutEdges().iterator();
            while(conite.hasNext()) {
                ComponentGraphEdge edge = (ComponentGraphEdge) conite.next();                
                //signals = signals + "extern calendarQueue_t* " + edge.getSignalName() + ";\n";
                signals = signals + "extern calendarQueue_t* " + 
                        edge.getISInfo().getCGName() + ";\n";
            }
            gen.setSignals(signals);

            generateDESpecificMethods(containerName, node, gen);

            generateConnectionMethods(gen, node, outsideBinfo);
                   
            super.generateAtomicComponents(gen, containerName, node, outputDir, 
                    designLocation, attrValueMap, outsideBinfo, "default");                    
        }
    }

    private void generateDESpecificMethods(String containerName, ComponentGraphNode node, 
            DEComponentGenerator gen) {
        String loc = System.getenv("ODOG_CODEGENERATORS");
        if(loc == null) {
            System.err.println("ODOG_CODEGENERATORS must be set");
        }
        loc = BaseConfiguration.appendSlash(loc) + "Host/DE/FileGenerators/";

        FileGenerator scheduleMe = FileGeneratorParser.parse(loc + "scheduleMe.xml");
        scheduleMe.setArgumentValue("arg0", "p1");
        scheduleMe.setArgumentValue("componentID", String.valueOf(node.getISInfo().getId()));
        scheduleMe.setArgumentValue("name", containerName);

        gen.setScheduleMe(scheduleMe.toString());
    }
        
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    
}
