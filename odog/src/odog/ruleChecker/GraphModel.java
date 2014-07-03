/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.ruleChecker;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import odog.syntax.Nodes.Acomp;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.NonExistentAttributeException;
import odog.syntax.Nodes.Topology;
import odog.syntax.Nodes.Value;
import odog.syntax.Nodes.VirtualPort;

/**
 * @author ivan
 */
public class GraphModel {

    /** Creates a new instance of GraphModel */
    public GraphModel() {
        nodeTypeTables = new HashMap[TOTAL_TABLES];
        for(int i = 0;i < TOTAL_TABLES;i++) {
            nodeTypeTables[i] = new HashMap();
        }
        hasEdgeTables = new HashMap();
        hasPathTables = new HashMap();
    }

    ///////////////// PUBLIC METHODS ///////////////////////////////////////////

    public void buildModel(Acomp comp) {
        StringBuffer buf = new StringBuffer();
        buf.append("digraph " + comp.getName() + "{\n");
        
        modelNode(comp, buf);
        
        buf.append("}\n");
        dotGraph = buf.toString();
    }

    public void buildModel(Topology top) {
        StringBuffer buf = new StringBuffer();
        buf.append("digraph " + top.getName() + "{\n");
        
        modelNode(top, buf);
        
        buf.append("}\n");
        dotGraph = buf.toString();
    }

    public Node findNode(String completeName) throws InvalidNodeException {
        for(int i = 0;i < TOTAL_TABLES;i++) {
            Node n = (Node) nodeTypeTables[i].get(completeName);
            if(n != null) return n;
        }
        throw new InvalidNodeException("Cannot find node " + completeName); 
    }

    // O valor do type tem que ser as constantes definidas em NodeType
    public Collection<Node> nodeTypeCollection(int type) {
        return nodeTypeTables[type].values();
    }
   
    public Object getAttributeValue(Node n, String attribute) 
            throws NonExistentAttributeException, InvalidNodeException {
        Node node = findNode(n.getFullName());
        return node.getAttributeValue(attribute);
    }
    
    public Class getAttributeType(Node n, String attribute) 
            throws NonExistentAttributeException, InvalidNodeException {
        Node node = findNode(n.getFullName());
        return node.getAttributeType(attribute);
    }
    
    public boolean hasEdge(Node from, Node to) throws InvalidNodeException {
        HashMap tab = (HashMap) hasEdgeTables.get(from);
        if(tab == null) {
            throw new InvalidNodeException("Node " + from + " invalid");           
        }
        return tab.containsKey(to);
    }
    
    public boolean hasPath(Node from, Node to) throws InvalidNodeException {                       
        if(hasEdge(from, to)) {
            return true;
        }        
        //System.out.println("No direct edge from " + from + " to " + to);        
        
        HashMap tab = (HashMap) hasPathTables.get(from);
        if(tab == null) {
            throw new InvalidNodeException("Node " + from + " invalid");
        }
        if(tab.containsKey(to)) {
            return true;
        }

        // percorre todos os nos conectados, vendo se ha caminho
        HashMap tab2 = (HashMap) hasEdgeTables.get(from);
        Iterator ite = tab2.keySet().iterator();
        while(ite.hasNext()) {
            Node n = (Node) ite.next();
            if(hasPath(n, to)) {
                tab.put(to, to);
                return true;
            }
        }

        return false;
    }
    
    public String generateDot() {
        return dotGraph.toString();
    }
    
    ///////////////// PRIVATE METHODS //////////////////////////////////////////
    
    // Essa rotina nao entra em loop porque o AllconnectedNodes evita ciclos
    private void modelNode(Node n, StringBuffer buf) {
        // devido a nos que tem referencias para outros, como conexao que tem para portos,
        // porto exportado para o porto, etc, um no pode ser aparecer mais de uma vez.
        if(nodeTypeTables[n.getType()].containsKey(n.getFullName())) return;

        nodeTypeTables[n.getType()].put(n.getFullName(), n);
        HashMap tab = new HashMap();

        Iterator ite = n.getAllConnectedNodes();
        while(ite.hasNext()) {
            Node nc = (Node) ite.next();
            tab.put(nc, nc);            
            modelNode(nc, buf);
                       
            if(n.getType() == Node.CONNECTION && (nc instanceof VirtualPort)) {
                buf.append("\"" + n.getFullName() + "\" -> \"" + nc.getFullName() +
                     "\"[color=blue];\n");
            }
            else
            if(n.getType() == Node.EXPORTEDPORT) {
                buf.append("\"" + n.getFullName() + "\" -> \"" + nc.getFullName() +
                     "\"[color=green];\n");
            }
            else
            if(n.getType() == Node.DEFVER) {
                buf.append("\"" + n.getFullName() + "\" -> \"" + nc.getFullName() +
                     "\"[color=cyan];\n");
            }
            else
            if(n.getType() == Node.VALUE) {
                String name = n.getContainer().getFullName() + "_value_";
                Value v = (Value) n;
                for(int i = 0;i < v.getAssociatedAttributes().size();i++) {
                    name = name + v.getAssociatedAttributes().get(i).getFullName();
                }
                buf.append("\"" + name + "\" -> \"" + nc.getFullName() +
                     "\"[color=red];\n");
            }
            else {
                String ncname = nc.getFullName();
                if(nc instanceof Value) {
                    Value v = (Value) nc;
                    ncname = nc.getContainer().getFullName() + "_value_";
                    for(int i = 0;i < v.getAssociatedAttributes().size();i++) {
                        ncname = ncname + v.getAssociatedAttributes().get(i).getFullName();
                    }
                }
                buf.append("\"" + n.getFullName() + "\" -> \"" + ncname +
                     "\";\n");
            }
        }

        hasEdgeTables.put(n, tab);
        hasPathTables.put(n, new HashMap());

/*
        if(n.getType() == Node.VALUE) {
            Value value = (Value) n;
            if(!value.isDefaultValue()) {
               for(int i = 0;i < value.getAssociatedAttributes().size();i++) {
                   buf.append("\"" + n.getFullName() + "\" -> \"" + 
                           value.getAssociatedAttributes().get(i).getFullName() + 
                        "\"[color=red];\n");                        
               } 
            }
        }
 */
    }

    ///////////////// PRIVATE VARIABLES/////////////////////////////////////////

    // Tabela de hash para cada um dos tipos de nos. Indexado por NodeType.type
    private HashMap [] nodeTypeTables;
    private static final int TOTAL_TABLES = 16;

    // Uma tabela de hash contendo uma entrada para cada no do grafo. O valor associado
    // eh outra tabela de hash, indicando os nos a ele diretamente conectados.
    private HashMap hasEdgeTables;

    // Uma table de hash contende uma entrada para cada no do grafo. O valor associado
    // eh outra tabela de hash, indicando os nos que tem caminho para o tal no.
    private HashMap hasPathTables;
    
    private String dotGraph;
}
