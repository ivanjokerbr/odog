/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.ruleChecker;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author ivan
 */
public class Function implements TreeNode, ResultNode {
    
    /** Creates a new instance of FunctionCall */
    public Function(int type, String name, Object[] arguments, 
            DefaultMutableTreeNode node) {
        this.type = type;
        this.name = name;        
        this.arguments = new String[arguments.length];
        for(int i = 0;i < arguments.length;i++) {
            this.arguments[i] = (String) arguments[i];
        }
        this.node = node;
    }
    
    ///////////////// PUBLIC METHODS ///////////////////////////////////////////
    
    public int treeNodeType() {
        return type;
    }
    
    public void setResult(boolean value) {
        result = value;
    }

    public boolean getResult() {
        return result;
    }

    public String getName() {
        return name;
    }
   
    public String[] getArguments() {
        return arguments;
    }
    
    public DefaultMutableTreeNode getExpr() {
        return node;
    }
    
    public String toString() {
        if(type == TreeNode.FUNCTION_CALL) {
            return "CALL " + name + " " + arguments;
        }
        else {
            return "DEF " + name + " "  + arguments + "\n" + node.toString();
        }
    }

    ///////////////// PRIVATE METHODS //////////////////////////////////////////
    
    ///////////////// PRIVATE VARIABLES/////////////////////////////////////////

    private int type;
    private boolean result;
    private String name;
    private String[] arguments;
    private DefaultMutableTreeNode node;
}
