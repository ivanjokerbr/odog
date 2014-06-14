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
public class Logical implements TreeNode, ResultNode {
    
    public static final int NOT = 1;
    public static final int OR = 2;
    public static final int AND = 3;
    public static final int IMPLY = 4;
    public static final int IFONLYIF = 5;
    
    /** Creates a new instance of Logical */
    public Logical(int op) {
        operator = op;
    }
    
    ///////////////// PUBLIC METHODS ///////////////////////////////////////////
    
    public int treeNodeType() {
        return LOGICAL;
    }
    
    public int getOperator() {
        return operator;
    }
    
    public String toString() {
        switch(operator) {
            case NOT: return "!" + "(R = " + result + ")";
            case OR: return "||" + "(R = " + result + ")";
            case AND: return "&&" + "(R = " + result + ")";
            case IMPLY: return "=>" + "(R = " + result + ")";
            case IFONLYIF: return "<=>" + "(R = " + result + ")";
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
