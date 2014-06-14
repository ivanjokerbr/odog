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
public interface TreeNode {
    
    public static final int NODEREF = 1;
    public static final int QUANTIFIER = 2;
    public static final int ATTRIBUTE_SPECIFICATION = 3;
    public static final int COMPARISON = 4;
    public static final int STRING_LITERAL = 5;
    public static final int GRAPH_CONNECTION = 6;
    public static final int LOGICAL = 7;
    public static final int FUNCTION_CALL = 8;
    public static final int FUNCTION_DEF = 9;
    
    public int treeNodeType();
}
