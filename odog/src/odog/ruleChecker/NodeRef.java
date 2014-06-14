/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.ruleChecker;

import odog.syntax.Nodes.Node;

/**
 *
 * Uma referencia a um ou conjunto exatos de no (pelo nome dele)
 *
 * @author ivan
 */
public class NodeRef implements TreeNode {
    
    /** Creates a new instance of NodeRef */
    public NodeRef(String ref) {
        reference = ref;
    }
    
    ///////////////// PUBLIC METHODS ///////////////////////////////////////////
    
    public int treeNodeType() {
        return NODEREF;
    }

    public String toString() {
        return getReference();
    }

    public String getReference() {
        return reference;
    }
    
    // Informacao semantica    
    public void setNode(Node n) {
        node = n;
    }
    
    public Node getNode() {
        return node;
    }
    
    ///////////////// PRIVATE METHODS //////////////////////////////////////////
    

    ///////////////// PRIVATE VARIABLES/////////////////////////////////////////

    private String reference;
    private Node node;
}
