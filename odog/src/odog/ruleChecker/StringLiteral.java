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
public class StringLiteral implements TreeNode {
    
    /** Creates a new instance of StringLiteral */
    public StringLiteral(String value) {
        literal = value;
    }
    
    ///////////////// PUBLIC METHODS ///////////////////////////////////////////
    
    public int treeNodeType() {
        return STRING_LITERAL;
    }
    
    public String getLiteral() {
        return literal;
    }
    
    public String toString() {
        return literal;
    }
    
    ///////////////// PRIVATE METHODS //////////////////////////////////////////
    
    
    ///////////////// PRIVATE VARIABLES/////////////////////////////////////////
    
    private String literal;
}
