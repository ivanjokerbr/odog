/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.syntax.Nodes;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Classe para representar uma aresta do grafo que representa um no. 
 *
 *
 * @author ivan
 */
public class Edges {
    
    /** Creates a new instance of Edge */
    public Edges(Node s) {
        source = s;
        nodeList = new LinkedList<Node>();
    }

    ////////////////////////////////////////////////////////////////////////////
    
    public void exportXML(StringBuffer buf, int ident) {
        for(Node n : nodeList) {
            buf.append(n.exportXML(ident));
        }
    }
    
    public void add(Node node) throws NonUniqueNameException {
        if(nameDuplication(node)) {
            throw new NonUniqueNameException("Node " + source.getName() + 
                " already linked to " + node.getName());
        }
        nodeList.add(node);
        node.setContainer(source);
    }

    public void remove(Node node) {
        nodeList.remove(node);
    }
    
    public Iterator iterator() {
        return nodeList.iterator();
    }

    public boolean containsName(String name) {
        for(Node n : nodeList) {
            if(n.getName().equals(name)) return true;
        }
        return false;
    }
    
    public Node find(String name) {
        for(Node n : nodeList) {
            if(n.getName().equals(name)) return n;
        }
        return null;
    }
       
    public List<Node> getList() {
        return nodeList;
    }
    
    ////////////////////////////////////////////////////////////////////////////

    private boolean nameDuplication(Node node) {
        for(Node n : nodeList) {
            if(n.getName().equals(node.getName())) return true;
        }
        return false;
    }
    
    ////////////////////////////////////////////////////////////////////////////

    private List<Node> nodeList;
    // Referencia para o no origem desta aresta
    private Node source;
}
