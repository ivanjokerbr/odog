/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package xmleditorgenerator;

import com.conradroche.matra.decl.ElementType;
import com.conradroche.matra.dtdparser.DTDParser;
import com.conradroche.matra.exception.DTDException;
import com.conradroche.matra.io.DTDFile;
import com.conradroche.matra.tree.DTDTree;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 *
 * @author ivan
 */
public class Main {
    
    /** Creates a new instance of Main */
    public Main() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        boolean onlygui = false;
        String fname = null;
        String pkgname = null;
        
        if(args.length == 3) {
            if(args[0].equals("-onlygui")) {
                onlygui = true;
                pkgname = args[1];
                fname = args[2];
            }
        }
        else
        if(args.length == 2) {
            pkgname = args[0];
            fname = args[1];
        }
        else {
            System.out.println("Usage: generateXMLEditor [-onlygui] pkgname filename.dtd");    
            return;
        }
        
        Generator g = new Generator(fname, pkgname, onlygui);
        
        g.generateXMLEditor();
    }   
}
