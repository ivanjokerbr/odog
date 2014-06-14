/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.ruleChecker;

/**
 *
 * @author ivan
 */
public class GConnection implements TreeNode, ResultNode {
    
    public static final int AR = 1;
    public static final int CM = 2;
    
    /** Creates a new instance of GConnection */
    public GConnection(int op) {
        operator = op;
    }
    
    ///////////////// PUBLIC METHODS ///////////////////////////////////////////
    
    public int treeNodeType() {
        return GRAPH_CONNECTION;
    }
    
    public int getOperator() {
        return operator;
    }
    
    public String toString() {
        if(operator == AR) {
            return "->" + "(R = " + result + ")";
        }
        else {
            return "->*" + "(R = " + result + ")";
        }
    }
    
    public void setResult(boolean value) {
        result = value;
    }
    
    public boolean getResult() {
        return result;
    }
    
    ///////////////// PRIVATE METHODS //////////////////////////////////////////
    
    
    
    ///////////////// PRIVATE VARIABLES/////////////////////////////////////////

    private int operator;
    private boolean result;
}
