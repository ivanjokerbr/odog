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
 *
 * @author ivan
 */
public class Comparison implements TreeNode, ResultNode {
    
    public final static int EQ = 1;
    public final static int DIFF = 2;
    public final static int LT = 3;
    public final static int GT = 4;
    public final static int LE = 5;
    public final static int GE = 6;
    
    /** Creates a new instance of Comparison */
    public Comparison(int type) {
        operator = type;
    }
    
    ///////////////// PUBLIC METHODS ///////////////////////////////////////////
    
    public int treeNodeType() {
        return COMPARISON;
    }
    
    public int getOperator() {
        return operator;
    }
    
    public String toString() {
        switch(operator) {
            case EQ: return "=" + "(R = " + result + ")"; 
            case DIFF: return "!=" + "(R = " + result + ")";
            case LT: return "<" + "(R = " + result + ")";
            case GT: return ">" + "(R = " + result + ")";
            case LE: return "<=" + "(R = " + result + ")";
            case GE: return ">=" + "(R = " + result + ")";
        }
        return "";
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
