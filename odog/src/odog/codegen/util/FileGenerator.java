/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.codegen.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 *
 * Responsavel por produzir um arquivo a partir de uma sequencia de elementos.
 * Um elemento pode ser um atributo ou um texto. Texto eh mandado para a saida.
 * Atributo e substituido pelo valor associado a ele. Usa-se o metodo setArgumentValue
 * para tal. Essa classe deve tipicamente ser generada pelo FileGeneratorParser.
 *
 * @author ivan
 */
public class FileGenerator {
    
    /** Creates a new instance of TextFileGenerator */
    public FileGenerator() {
        elements = new LinkedList();
        arguments = new HashMap();
    }
    
    ///////////////// PUBLIC METHODS ///////////////////////////////////////////
    
    public void addElement(String value, boolean isArgument) {
        String [] data = new String[2];
        data[0] = value;
        data[1] = (new Boolean(isArgument)).toString();

        elements.add(data);
    }
    
    public void setArgumentValue(String argument, String value) {
        arguments.put(argument, value);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        Iterator ite = elements.iterator();
        while(ite.hasNext()) {
            String [] data = (String []) ite.next();
            if(data[1].equals("false")) {
                buf.append(data[0]);
            }    
            else {
                buf.append((String) arguments.get(data[0]));
            }
        }
        
        return buf.toString();
    }

    ///////////////// PRIVATE METHODS //////////////////////////////////////////
        
    
    ///////////////// PRIVATE VARIABLES/////////////////////////////////////////
    
    // Os elementos que podem ser texto puro ou argumentos.
    private LinkedList elements;

    private HashMap arguments;
}
