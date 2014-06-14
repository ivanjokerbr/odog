/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.codegen.languages.C;

/**
 *
 *
 * @author ivan
 */
public class Type {
    
    /** Creates a new instance of Type */
    public Type() {
    }
  
    
    public static String convertToCType(String genericType) {
        if(genericType.equals("integer")) {
            return "int";   
        }
        else
        if(genericType.equals("boolean")) {
            return "short";
        }
        else
        if(genericType.equals("float")) {
            return "float";
        }
        else
        if(genericType.equals("double")) {
            return "double";
        }
        else                
        if(genericType.equals("object")) {
            return "void *";
        }
        else
        if(genericType.equals("byte")) {
            return "char";
        }
        else
        if(genericType.equals("string")) {
            return "char *";
        }
            
        return null;
    }    
}
