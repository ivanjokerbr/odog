/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.graph;

import odog.design.CompositeComponent;
import odog.graph.ComponentGraphNode.NodeType;
import odog.syntax.Nodes.Acomp;
import odog.syntax.Nodes.CompBase;
import odog.syntax.Nodes.CompInstance;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.Connection;
import odog.syntax.Nodes.Dport;
import odog.syntax.Nodes.ExportedPort;
import odog.syntax.Nodes.Hver;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.Topology;
import odog.syntax.Nodes.Value;
import odog.syntax.Nodes.Ver;
import odog.syntax.Nodes.VersionBase;
import odog.syntax.Nodes.VirtualPort;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import edu.uci.ics.jung.graph.impl.SparseGraph;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author ivan
 */
public class TopologyGraph {
    
    public TopologyGraph() {
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    /* generates a graph view of the topology */
    public Graph generateViewGraph(Topology topology, Hver version) {
        DirectedSparseGraph graph = new DirectedSparseGraph();

        Hashtable <VirtualPort, Vertex> vpTable = new 
                Hashtable <VirtualPort, Vertex>();
        
        // 1. add the nodes from the topology. The virtual ports of the component
        // are put on a hash table. This table is used when connecting the ports
        Iterator ite = topology.componentInstancesIterator();
        while(ite.hasNext()) {
            CompInstance ains = (CompInstance) ite.next();
            CompBase comp = ains.getComponent();
            VersionBase vb = version.getSelectedVersion(ains);

            ComponentViewVertex v = new ComponentViewVertex(comp);
            graph.addVertex(v);
            
            Iterator pite = comp.portsIterator();
            while(pite.hasNext()) {
                VirtualPort vp = (VirtualPort)  pite.next();
                VirtualPortViewVertex vpv = new VirtualPortViewVertex(vp);
                vpTable.put(vp, vpv);
                
                graph.addVertex(vpv);
                if(vp.isInput()) {
                    graph.addEdge(new DirectedSparseEdge(vpv, v));
                }
                else {
                    graph.addEdge(new DirectedSparseEdge(v, vpv));    
                }
            }
            
            pite = vb.portsIterator();
            while(pite.hasNext()) {
                VirtualPort vp = (VirtualPort)  pite.next();
                VirtualPortViewVertex vpv = new VirtualPortViewVertex(vp);
                vpTable.put(vp, vpv);
                
                graph.addVertex(vpv);
                if(vp.isInput()) {
                    graph.addEdge(new DirectedSparseEdge(vpv, v));
                }
                else {
                    graph.addEdge(new DirectedSparseEdge(v, vpv));    
                }
            }
        }
        
        // ... now for the component instances of the selected version
        ite = version.componentInstancesIterator();
        while(ite.hasNext()) {
            CompInstance ains = (CompInstance) ite.next();
            CompBase comp = ains.getComponent();
            VersionBase vb = version.getSelectedVersion(ains);

            ComponentViewVertex v = new ComponentViewVertex(comp);
            graph.addVertex(v);
            
            Iterator pite = comp.portsIterator();
            while(pite.hasNext()) {
                VirtualPort vp = (VirtualPort)  pite.next();
                VirtualPortViewVertex vpv = new VirtualPortViewVertex(vp);
                vpTable.put(vp, vpv);
                
                graph.addVertex(vpv);
                if(vp.isInput()) {
                    graph.addEdge(new DirectedSparseEdge(vpv, v));
                }
                else {
                    graph.addEdge(new DirectedSparseEdge(v, vpv));    
                }
            }
            
            pite = vb.portsIterator();
            while(pite.hasNext()) {
                VirtualPort vp = (VirtualPort)  pite.next();
                VirtualPortViewVertex vpv = new VirtualPortViewVertex(vp);
                vpTable.put(vp, vpv);
                
                graph.addVertex(vpv);
                if(vp.isInput()) {
                    graph.addEdge(new DirectedSparseEdge(vpv, v));
                }
                else {
                    graph.addEdge(new DirectedSparseEdge(v, vpv));    
                }
            }
        }
        
        // 2. add the edges between virtual ports
        ite = topology.connectionsIterator();
        while(ite.hasNext()) {
             Connection c = (Connection) ite.next();
             VirtualPort vps = c.getOutputPort();
             VirtualPortViewVertex source = (VirtualPortViewVertex) vpTable.get(vps);
             
             Iterator cite = c.inputPortsIterator();
             while(cite.hasNext()) {
                 VirtualPort vpd = (VirtualPort) cite.next();
                 VirtualPortViewVertex sink = (VirtualPortViewVertex) vpTable.get(vpd);
                 
                 graph.addEdge(new DirectedSparseEdge(source, sink));
             }
        }
        
        ite = version.connectionsIterator();
        while(ite.hasNext()) {
             Connection c = (Connection) ite.next();
             VirtualPort vps = c.getOutputPort();             
             VirtualPortViewVertex source = (VirtualPortViewVertex) vpTable.get(vps);
             
             Iterator cite = c.inputPortsIterator();
             while(cite.hasNext()) {
                 VirtualPort vpd = (VirtualPort) cite.next();
                 VirtualPortViewVertex sink = (VirtualPortViewVertex) vpTable.get(vpd);
                 
                 graph.addEdge(new DirectedSparseEdge(source, sink));
             }
        }
            
        return graph; 
    }
    
    /* produz um grafo onde os nos sao os components, passando pelos niveis de hierarquia
     * em um mesmo modelo isemname. Components hierarquicos de outros modelos nao sao
     * explorados. 
     */
    public CompositeComponentGraphNode generateISemGraph(CompositeComponent comp, 
            Hver version, Hashtable <Attr, Value> attrValueMap)  {
        Topology toplevel = comp.getRootNode();

        compIndex = 0;
        edgeIndex = 0;
        
        // 1. Discover the isem. The checks ensure that none of the values are null
        Attr isem = toplevel.getAttribute("ISEM");
        Value v = attrValueMap.get(isem);
        if(v == null) {
            v = isem.getDefaultValue();
        }
        String type = v.getValueType();        
        String isemValue = v.getValueExpr();
        
        CompositeComponentGraphNode ret = new CompositeComponentGraphNode(comp, null, 
                isemValue);
        BaseISemInfo bi = ret.getISInfo();
        bi.setCGName("comp_" + compIndex++);

        ret.setSelectedVer(version);
        ret.setInsideGraph(generateGraph(toplevel, version, isemValue, attrValueMap));

        return ret;
    }

    ///////////////////////////// PRIVATE METHODS //////////////////////////////

    private Graph generateGraph(Topology top, Hver version, String isemname,
            Hashtable <Attr, Value> attrValueMap) {
        Graph graph = new SparseGraph();
        Hashtable <CompInstance, ComponentGraphNode> nodeTable = new 
                Hashtable <CompInstance, ComponentGraphNode>();

        addNodes(top, version, nodeTable, graph, isemname, attrValueMap);
        addEdges(top, version, nodeTable, graph, attrValueMap);
        
        return graph;
    }

    private void addEdges(Topology top, Hver ver, 
            Hashtable<CompInstance, ComponentGraphNode> table, Graph graph,
            Hashtable<Attr, Value> attrValueMap) {

         // 1. procura em todas as conexoes da topologia em questao
         Iterator ite = top.connectionsIterator();
         while(ite.hasNext()) {
             Connection c = (Connection) ite.next();
             VirtualPort vps = c.getOutputPort();

             // output ports always have only one port associated
             Vector v = new Vector();
             List<CompInstance> ainsl = getOwner(vps, table, v);
             CompInstance ains = ainsl.get(0);
             vps = (VirtualPort) v.get(0);
             
             boolean delayed = false;
             Iterator attrIte = vps.attributesIterator();
             while(attrIte.hasNext()) {
                 Attr at = (Attr) attrIte.next();
                 if(at.getName().equalsIgnoreCase("delayed")) {
                     delayed = true;
                     break;
                 }
             }
             ComponentGraphNode source = table.get(ains);

             Iterator cite = c.inputPortsIterator();
             while(cite.hasNext()) {
                 VirtualPort vpd = (VirtualPort) cite.next();
                 v.clear();
                 ainsl = getOwner(vpd, table, v);
                 for(int i = 0;i < ainsl.size();i++) {
                     vpd = (VirtualPort) v.get(i);
                     ains = ainsl.get(i);
                     ComponentGraphNode sink = table.get(ains);

                     ComponentGraphEdge edge = new ComponentGraphEdge(source, sink, vps, 
                             vpd, c, attrValueMap);
                     BaseEdgeISemInfo bi = edge.getISInfo();
                     bi.setCGName("edge_" + edgeIndex++);
                     if(delayed) {
                        bi.breakCycle(true);
                     }
                     
                     // for the SR ISem
                     if(sink.getType() == NodeType.ATOMIC) {
                         CompInstance cins = sink.getComponent();
                         Acomp acomp = (Acomp) cins.getComponent();
                         if(acomp.getAttribute("nonstrict") != null) {
                             bi.breakCycle(true);
                         }
                     }
                     
                     graph.addEdge(edge);
                     source.addedOutputEdge(edge);
                     sink.addedInputEdge(edge);
                 }                                                                    
             }
         }
         
         // 2. procura em todas as conexoes da versao escolhida
         ite = ver.connectionsIterator();
         while(ite.hasNext()) {
             Connection c = (Connection) ite.next();
             VirtualPort vps = c.getOutputPort();
             
             Vector v = new Vector();
             List<CompInstance> ainsl = getOwner(vps, table, v);
             CompInstance ains = ainsl.get(0);
             vps = (VirtualPort) v.get(0);
             
             boolean delayed = false;
             Iterator attrIte = vps.attributesIterator();
             while(attrIte.hasNext()) {
                 Attr at = (Attr) attrIte.next();
                 if(at.getName().equalsIgnoreCase("delayed")) {
                     delayed = true;
                     break;
                 }
             }             
             ComponentGraphNode source = table.get(ains);
             
             Iterator cite = c.inputPortsIterator();
             while(cite.hasNext()) {
                 VirtualPort vpd = (VirtualPort) cite.next();
                 v.clear();
                 ainsl = getOwner(vpd, table, v);
                 for(int i = 0;i < ainsl.size();i++) {
                     vpd = (VirtualPort) v.get(i);
                     ains = ainsl.get(i);
                     ComponentGraphNode sink = table.get(ains);

                     ComponentGraphEdge edge = new ComponentGraphEdge(source, sink, vps, 
                             vpd, c, attrValueMap);
                     BaseEdgeISemInfo bi = edge.getISInfo();
                     bi.setCGName("edge_" + edgeIndex++);
                     if(delayed) {
                        bi.breakCycle(true);
                     }
                     graph.addEdge(edge);
                     source.addedOutputEdge(edge);
                     sink.addedInputEdge(edge);
                 }                                                                    
             }
         }
         
         // 3. For all composites that don't have a isem associated, generate the edges
         // of their connections recursively
         ite = top.componentInstancesIterator();
         while(ite.hasNext()) {
             CompInstance ains = (CompInstance) ite.next();
             if(!table.containsKey(ains)) {
                 Topology t = (Topology) ains.getComponent();
                 Hver selectedver = (Hver) ver.getSelectedVersion(ains);   
                 addEdges(t, selectedver, table, graph, attrValueMap);
             } 
         }

         // 4. repete para a versao selecionada
         ite = ver.componentInstancesIterator();
         while(ite.hasNext()) {
             CompInstance ains = (CompInstance) ite.next();
             if(!table.containsKey(ains)) {
                 Topology t = (Topology) ains.getComponent();
                 Hver selectedver = (Hver) ver.getSelectedVersion(ains);   
                 addEdges(t, selectedver, table, graph, attrValueMap);
             } 
         }
    }

    // The realNode vector is used to return the virtual port associated with the
    // destination instance. In the case of an atomic component, will the a dport. In
    // the case of a composite component with diferent isem, the exported port.
    private List<CompInstance> getOwner(VirtualPort vp, 
            Hashtable<CompInstance, ComponentGraphNode> table, Vector realNode)  {
        
        if(vp instanceof Dport) {
            Node owner = vp.getContainer();
            if(owner instanceof Ver) {
                owner = owner.getContainer();
            }
            Acomp comp = (Acomp) owner;
            realNode.add(vp);

            LinkedList<CompInstance> ret = new LinkedList<CompInstance>();
            ret.add(comp.getComponentInstance());
            
            return ret;
        }
        else {// eh um ExportedPort
            ExportedPort ep = (ExportedPort) vp;
            Node owner = ep.getContainer();
            if(owner instanceof Hver) {
                owner = owner.getContainer();
            }
            Topology top = (Topology) owner;
            CompInstance ains = top.getComponentInstance();
            
            ComponentGraphNode anode = table.get(ains);
            if(anode != null) {   // eh um ator COMPOSITE com ISEM diferente
                realNode.add(vp);

                LinkedList<CompInstance> ret = new LinkedList<CompInstance>();
                ret.add(ains);

                return ret;
            }       

            LinkedList<CompInstance> ret = new LinkedList<CompInstance>();
            for(int i = 0;i < ep.getRefPorts().size();i++) {
                List instances = getOwner(ep.getRefPorts().get(i), table, realNode);
                ret.addAll(instances);
            }

            return ret;
        }
    }

    private void addNodes(Topology top, Hver ver, 
            Hashtable<CompInstance, ComponentGraphNode> table, Graph graph, 
            String isemname, Hashtable <Attr, Value> attrValueMap) {

        Iterator ite = top.componentInstancesIterator();
        while(ite.hasNext()) {
            CompInstance ains = (CompInstance) ite.next();
            addNodesProcessComponentInstance(ains, top, ver, table, graph, isemname,
                    attrValueMap);
        }
        
        ite = ver.componentInstancesIterator();
        while(ite.hasNext()) {
            CompInstance ains = (CompInstance) ite.next();
            addNodesProcessComponentInstance(ains, top, ver, table, graph, isemname,
                    attrValueMap);
        }
    }
    
    private void addNodesProcessComponentInstance(CompInstance ains, Topology top, Hver ver, 
            Hashtable<CompInstance, ComponentGraphNode> table, Graph graph, 
            String isemname, Hashtable <Attr, Value> attrValueMap) {

        CompBase comp = ains.getComponent();
        if(comp instanceof Acomp) {
            ComponentGraphNode anode = new ComponentGraphNode(ains,
                    NodeType.ATOMIC);
            BaseISemInfo bi = anode.getISInfo();
            bi.setCGName("comp_" + compIndex++);
            
            VersionBase vb = ver.getSelectedVersion(ains);
            anode.setSelectedVer(vb);

            graph.addVertex(anode);
            table.put(ains, anode);
        }
        else {
            Topology t = (Topology) comp;
            Hver selectedver = (Hver) ver.getSelectedVersion(ains);

            Attr isem = t.getAttribute("ISEM");
            if(isem == null) { // eh o mesmo modelo
                addNodes(t, selectedver, table, graph, isemname, attrValueMap);
            }
            else {
                Value value = attrValueMap.get(isem);
                if(value == null) {
                    value = isem.getDefaultValue();
                }
                String isemValue = value.getValueExpr();
                if(isemValue == null || isemValue.equals(isemname)) {
                    addNodes(t, selectedver, table, graph, isemname, attrValueMap);
                }
                else  {  
                    // in this case, is a composite with another isem. Create
                    // a CompositeComponentGraphNode
                    CompositeComponentGraphNode ret = new CompositeComponentGraphNode(null,
                            ains, isemValue);
                    ret.setSelectedVer(selectedver);
                    BaseISemInfo bi = ret.getISInfo();
                    bi.setCGName("comp_" + compIndex++);

                    ret.setInsideGraph(generateGraph(t, selectedver, isemValue, attrValueMap));

                    graph.addVertex(ret);
                    table.put(ains, ret);
                }
            }
        }
    }
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    private int compIndex;
    private int edgeIndex;

}
