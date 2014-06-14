/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.ruleChecker;

import java.util.Collection;
import java.util.Iterator;
import odog.syntax.Nodes.Node;

/**
 *
 *  Representa um no da arvore que eh uma especificao com quantificador de tipo de no
 *
 * @author ivan
 */
public class Quantifier implements TreeNode, ResultNode {
    
    public static final int PT = 1;
    public static final int EX = 2;
    
    public Quantifier(int quant, int type, String var) {
       quantifier = quant;
       nodeType = type;
       freeVar = var;
    }
    
    ///////////////// PUBLIC METHODS ///////////////////////////////////////////
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if(quantifier == PT) {
            buf.append("PT.");           
        }
        else {
            buf.append("EX.");
        }
        
        switch(nodeType) {
            case Node.DPORT : buf.append("DPORT."); break;
            case Node.ACOMP: buf.append("ACOMP."); break;
            case Node.ATTR: buf.append("ATTR."); break;
            case Node.TOPOLOGY: buf.append("TOPOLOGY."); break;
            case Node.COMPONENTINSTANCE: buf.append("COMPINSTANCE."); break;
            case Node.ATTRCLASS: buf.append("ATTRCLASS."); break;
            case Node.VER: buf.append("VER."); break;
            case Node.HVER: buf.append("HVER."); break;
            case Node.VALUE: buf.append("VALUE."); break;
            case Node.REQSERV: buf.append("REQSERV."); break;
            case Node.METHOD: buf.append("METHOD."); break;
            case Node.CONNECTION: buf.append("CONNECTION."); break;
            case Node.EXPORTEDPORT: buf.append("EXPORTED PORT."); break;
            case Node.DEFVER: buf.append("DEF VER."); break;
            default: buf.append("UNKNOWN NODE");
        }
        buf.append(freeVar + " (= " + result + ")");
        
        
        return buf.toString();
    }
    
    public int getType() {
        return nodeType;
    }

    public int treeNodeType() {
        return QUANTIFIER;
    }

    public void setNodes(Collection col) {
        nodes = col;
    }
    
    public Iterator getIterator() {
        return nodes.iterator();
    }
    
    public int getQuantifier() {
        return quantifier;
    }
    
    public String getFreeVar() {
        return freeVar;
    }
    
    public void setResult(boolean value) {
        result = value;
    }
    
    public boolean getResult() {
        return result;
    }

    ///////////////// PRIVATE METHODS //////////////////////////////////////////
  
    ///////////////// PRIVATE VARIABLES/////////////////////////////////////////
    
    private int quantifier;
    private int nodeType;
    private String freeVar;
    
    private Collection nodes;
    
    private boolean result;   
}