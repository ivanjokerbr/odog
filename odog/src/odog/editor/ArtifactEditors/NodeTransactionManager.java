/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors;

import odog.syntax.Nodes.Node;
import java.util.LinkedList;
import java.util.List;
import xmleditorgenerator.BaseFiles.Transaction;
import xmleditorgenerator.BaseFiles.Transaction.TransactionType;
import xmleditorgenerator.BaseFiles.TransactionManager;

/**
 *
 * @author ivan
 */
public class NodeTransactionManager extends TransactionManager {
    
    /** Creates a new instance of NodeTransactionManager */
    public NodeTransactionManager() {
        super();
    }
    
    //////////////////////////// PUBLIC METHODS ////////////////////////////////

    public List<Transaction> getAllTransactions(TransactionType type, int nodeType,
            int from) {

        LinkedList <Transaction> ret = new LinkedList<Transaction>();
        LinkedList <Transaction> list = null;
        switch(type) {            
            case ADD_ELEMENT: {
                list = addElementList;
            } break;
            
            case REMOVE_ELEMENT: {
                list = removeElementList;
            } break;
            
            case CHANGE_ELEMENT: {
                list = changeElementList;
            } break;
        }
        if(from > list.size()) return ret;

        Object [] obj = list.toArray();
        for(int i = from;i < obj.length;i++) {
            Transaction t = (Transaction) obj[i];
            Node target = (Node) t.getTarget();
            if(target.getType() == nodeType) {
                if(stillValid(target)) {
                    ret.add(t);
                }
                else {
                    list.remove(t);
                }
            }
        }

        return ret;
    }
    
    //////////////////////////// PRIVATE METHODS ///////////////////////////////

    // this method will check if the given node is within a component. 
    private boolean stillValid(Node node) {
        if(node.getType() == Node.ACOMP || node.getType() == Node.TOPOLOGY) 
            return true;

        Node ptr = node;
        while(ptr.getContainer() != null) {
            ptr = ptr.getContainer();
        }
        
        if(ptr.getType() != node.ACOMP && ptr.getType() != node.TOPOLOGY) 
            return false;
        
        return true;        
    }
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
}
