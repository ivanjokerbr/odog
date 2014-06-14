/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.ruleChecker;

import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * Representa a especificacao de um atributo de um dado no
 *
 * @author ivan
 */
public class AttributeSpecification implements TreeNode {
    
    /** Creates a new instance of AttributeSpecification */
    public AttributeSpecification(String attr) {
        attribute = attr;
    }

    ///////////////// PUBLIC METHODS ///////////////////////////////////////////

    public int treeNodeType() {
        return ATTRIBUTE_SPECIFICATION;   
    }

    public String toString() {
        return "[" + attribute + "]";
    }
    
    public String getAttribute() {
        return attribute;
    }
    
    public void setValueAndType(Object value, Class dataType) {
        type = dataType;
        this.value = value;
    }
    
    public Object getValue() {
        return value;
    }
    
    public Class getType() {
        return type;
    }

    ///////////////// PRIVATE METHODS //////////////////////////////////////////

    
    ///////////////// PRIVATE VARIABLES/////////////////////////////////////////
    
    private String attribute;
    
    private Object value;
    private Class type;
}
