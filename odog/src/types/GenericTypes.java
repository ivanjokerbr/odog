/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package types;

/**
 *
 * @author ivan
 */
public class GenericTypes {
    
   
    public GenericTypes() {
    }
    
    public static String [] availableTypes() {
        
        String [] types = new String[7];
        
        types[0] = "integer";
        types[1] = "boolean";
        types[2] = "float";
        types[3] = "double";
        types[4] = "object";
        types[5] = "byte";
        types[6] = "string";
        
        return types;
    }
    
}
