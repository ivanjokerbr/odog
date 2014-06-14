/* 
 * Copyright (c) 2006-2008, Ivan Jeukens
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.codegen.Platform.Multicore.DF;

import edu.uci.ics.jung.graph.Graph;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import odog.codegen.AtomicComponentGenerator;
import odog.codegen.BoundaryData;
import odog.codegen.ISemGenerator;
import odog.codegen.util.FileGenerator;
import odog.codegen.util.FileGeneratorParser;
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

/**
 *
 * @author ivan
 */
public class MulticoreDFCodeGenerator extends ISemGenerator {

    public MulticoreDFCodeGenerator(Hashtable <Attr, Value> attrValueMap) {
        super(attrValueMap);
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public boolean codeGenerate(CompositeComponentGraphNode topnode, String outputDir,
            String designLocation) {
        LinkedList<BoundaryData> outsideBinfo = new LinkedList<BoundaryData>();

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
            loc = BaseConfiguration.appendSlash(loc) + "Multicore/DF/FileGenerators/";
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
    
    /////////////////////////// PROTECTED METHODS //////////////////////////////

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
            AtomicComponentGenerator gen, Hashtable <Attr, Value> attrValueMap,
            String containerName, LinkedList<BoundaryData> outsideBinfo) {
        String loc = System.getenv("ODOG_CODEGENERATORS");
        loc = BaseConfiguration.appendSlash(loc) + "Multicore/DF/FileGenerators/";
        
        // verificar a implementacao desse port queue 
        Hashtable <String, String> portQueueTable = new Hashtable<String, String>();
      
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
            receiveGenerator.setArgumentValue("arg1", findSourceComponent(qname,
                    node));
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
    
    private String findSourceComponent(String qname, ComponentGraphNode n) {
        
        for(int i = 0;i < n.numberInputPorts();i++) {
            List <ComponentGraphEdge> l = n.getInputConnections(i);
            for(ComponentGraphEdge edge : l) {
                if(edge.getISInfo().getCGName().equals(qname)) {
                    ComponentGraphNode source = (ComponentGraphNode) edge.getSource();
                    return String.valueOf(source.getISInfo().getId());                    
                }
            }
        }

        return null;
    }
    
    private void generateTopology(String instanceName, Topology toplevel, 
            Hver topVersion, Graph g, String designLocation, String outputDir,
            Hashtable <Attr, Value> attrValueMap, 
            LinkedList<BoundaryData> outsideBinfo) {        
        String cgloc = System.getenv("ODOG_CODEGENERATORS");
        String loc = BaseConfiguration.appendSlash(cgloc) + "Multicore/DF/FileGenerators/";
        FileGenerator topgen = FileGeneratorParser.parse(loc + "scheduler.xml");

        topgen.setArgumentValue("ncomponents", String.valueOf(g.getVertices().size()));
        topgen.setArgumentValue("topologyName", instanceName);
        
        processSignals(g, topgen, loc, toplevel, cgloc, instanceName,
                outsideBinfo);

        int id = topologicalSort[0].getISInfo().getId();

        String name = topologicalSort[0].getISInfo().getCGName();
        String componentExecMethod = "extern void " + name + "_init();\n";        
        componentExecMethod += "extern void " + name + "_finish();\n";
        componentExecMethod += "extern void * " + name + "_compute_wraper(void *);\n";

        String fptr = instanceName + "_fptr[" + String.valueOf(id) + "][0] = " + name + "_init;\n";        
        fptr += instanceName + "_fptr[" + String.valueOf(id) + "][1] = " + name + "_finish;\n"; 
        fptr += instanceName + "thread_ids[0] = 0;\n";
        
        String createThreadText = "\tpthread_create(&" + instanceName + "components[0], " +
                "&" + instanceName + "attr, " + name + "_compute_wraper, (void *)&" + instanceName +
                "thread_ids[0]);\n";        
        
        for(int i = 1;i < topologicalSort.length;i++) {
            id = topologicalSort[i].getISInfo().getId();
            
            name = topologicalSort[i].getISInfo().getCGName();
            componentExecMethod += "extern void " + name + "_init();\n";            
            componentExecMethod += "extern void " + name + "_finish();\n";
            componentExecMethod += "extern void * " + name + "_compute_wraper(void *);\n";

            fptr += instanceName + "_fptr[" + String.valueOf(id) + "][0] = " + name + "_init;\n";
            fptr += instanceName + "_fptr[" + String.valueOf(id) + "][1] = " + name + "_finish;\n";
            fptr += instanceName + "thread_ids[" + i + "] = " + i + ";\n";

            createThreadText += "\tpthread_create(&" + instanceName + "components[" + i + 
                    "], &" + instanceName + "attr, " + name + "_compute_wraper, (void *)&" +
                    instanceName + "thread_ids[" + i + "]);\n";            
        }
        topgen.setArgumentValue("componentsExecMethods", componentExecMethod);
        topgen.setArgumentValue("initializeFptr", fptr);        
        topgen.setArgumentValue("createThreads", createThreadText);        

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

    private void generateAtomicComponents(Topology container, String containerName, 
            Graph g, Hver topVersion, String outputDir, String designLocation, 
            Hashtable <Attr,Value> attrValueMap, 
            LinkedList<BoundaryData> outsideBinfo) {
        
        Iterator ite = g.getVertices().iterator();
        while(ite.hasNext()) {
            MulticoreDFComponentGenerator gen = new MulticoreDFComponentGenerator(
                    "Multicore/DF/FileGenerators/", "atomicComponent.xml");
            ComponentGraphNode node = (ComponentGraphNode) ite.next();
            if(node.getType() != ComponentGraphNode.NodeType.ATOMIC) continue;
            
            buildSignalVarLists("DF", node, attrValueMap, outsideBinfo);

            gen.setNumberOfComponents(String.valueOf(g.getVertices().size()));
            gen.setComponentId(String.valueOf(node.getISInfo().getId()));
            
            // 2. signals
            String signals = "";
            String yieldTestText = "";
            String inputTestText = "";

            for(String sname : inconQueue.values()) {
                signals += "extern dynamic_fifo_safe * " + sname + ";\n";
                inputTestText += "\tcanRead_dynamic_fifo_safe(" + sname + 
                    ", &" + containerName + "numberOfWriteBlocked, &"+
                    containerName + "numberOfReadBlocked, " +
                    String.valueOf(g.getVertices().size()) + ", " +
                    containerName + "_wakeAllWriteBlocked, &" +
                    containerName + "stateMutex);\n";
            }
            
            if(outconQueue.size() == 0) {
                yieldTestText += "goto _execute;";   
            }
            else {
                for(LinkedList<String> l : outconQueue.values()) {
                    for(int i = 0;i < l.size();i++) {
                        String sname = l.get(i);
                        signals += "extern dynamic_fifo_safe * " + sname + ";\n";
                        yieldTestText += "\tif(isDeferrable_dynamic_fifo_safe(" +
                                sname + ") == 0) goto _execute;\n";
                    }
                }
            }
            
            signals += "extern pthread_mutex_t " + containerName + "_stopMutex;\n";
            signals += "extern pthread_cond_t "  + containerName + "_stopCond;\n";
            
            gen.setSignals(signals);
            gen.setYieldTest(yieldTestText);
            gen.setInputTest(inputTestText);
            
            generateDFSpecificMethods(gen, containerName);

            generateConnectionMethods(gen, node, outsideBinfo);

            super.generateAtomicComponents(gen, containerName, node, outputDir, 
                    designLocation, attrValueMap, outsideBinfo, "default");
        }
    }
    
    private void processSignals(Graph g, FileGenerator topgen, String loc, 
            Topology toplevel, String cgloc, String instanceName, 
            LinkedList<BoundaryData> outsideBinfo) {
        // 1. Declare all signals that have connections. This includes the 
        // dynamic fifo and a sample rate variable for the associated ports.
        String signalDecl = "";
        String initSignals = "";
        String destroySignals = "";
        Iterator ite = g.getVertices().iterator();
        while(ite.hasNext()) {
            ComponentGraphNode node = (ComponentGraphNode) ite.next();           
            Iterator inite = node.getInEdges().iterator();
            while(inite.hasNext()) {
                ComponentGraphEdge edge = (ComponentGraphEdge) inite.next();                               
                
                VirtualPort sport = edge.getSinkPort();
                Connection c = edge.getConnection();
                String signalName = edge.getISInfo().getCGName();

                signalDecl += "dynamic_fifo_safe *" + signalName + ";\n";
                initSignals += signalName + " = init_dynamic_fifo_safe(10);\n";
                destroySignals += "destroy_dynamic_fifo_safe(" + signalName + ");\n";
            }                        
        }
        
        topgen.setArgumentValue("signals", signalDecl);
        topgen.setArgumentValue("initSignals", initSignals);
        topgen.setArgumentValue("destroySignals", destroySignals);
    }

    private void generateDFSpecificMethods(MulticoreDFComponentGenerator gen,
            String containerName) {
        String setSampleRateText = "";
        String getSampleRateText = "";        
        Iterator ite = inconQueue.keySet().iterator();
        while(ite.hasNext()) {
            String pname = (String) ite.next();            
            setSampleRateText += "if(strcmp(p1,\"" + pname + "\") == 0) {\n";
            getSampleRateText += "if(strcmp(p1,\"" + pname + "\") == 0) {\n";
            String qname = inconQueue.get(pname);
            
            setSampleRateText += "\tsetSampleRate_dynamic_fifo_safe(" + qname +
                    ", p2);\n";
            getSampleRateText += "\treturn getSampleRate_dynamic_fifo_safe(" + qname +
                    ");\n";

            setSampleRateText += "}\n";
            getSampleRateText += "}\n";
            if(ite.hasNext()) {
                setSampleRateText += "else\n";
                getSampleRateText += "else\n";
            }
        }
        gen.setSetSampleRate(setSampleRateText);        
        gen.setGetSampleRate(getSampleRateText);
        
        String stopExecText = "pthread_mutex_lock(&" + containerName + "_stopMutex);\n";
        stopExecText += "pthread_cond_signal(&" + containerName + "_stopCond);\n";
        stopExecText += "pthread_mutex_unlock(&" + containerName + "_stopMutex);\n";
        gen.setStopExecution(stopExecText);
    }
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
}
