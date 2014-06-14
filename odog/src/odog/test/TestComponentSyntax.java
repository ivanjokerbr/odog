/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.test;

import odog.configuration.BaseConfiguration;
import odog.design.DesignRepository;
import odog.syntax.Nodes.Acomp;
import odog.syntax.Nodes.Topology;
import odog.syntax.Parse.AtomicComponentParser;
import odog.syntax.Parse.TopologyParser;
import java.io.*;

public class TestComponentSyntax {

    public TestComponentSyntax(boolean isAtomic) {
        this.isAtomic = isAtomic;
    }

    public boolean test(String compFile, String goldenSyntaxFile) {
        String xml, xmlCloned;
        if(isAtomic) {
            Acomp comp = AtomicComponentParser.parseAtomicComponent(compFile);
            if(AtomicComponentParser.errorMessage != null) {
                System.out.println(AtomicComponentParser.errorMessage);
                return false;
            }
            xml = comp.exportXML(0);
            xmlCloned = comp.clone().exportXML(0);
        }
        else {
            Topology top = TopologyParser.parseTopology(compFile, new DesignRepository(
                    new BaseConfiguration()));
            if(TopologyParser.errorMessage != null) {
                System.out.println(TopologyParser.errorMessage);
                return false;
            }
            xml = top.exportXML(0);
            
            Topology clone = top.clone();
            clone.buildElementTable();
            clone.cloneAssociatedAttributes(top, clone.getAttributeTable());
            xmlCloned = clone.exportXML(0);
        }
        String goldenxml = loadGoldenXML(goldenSyntaxFile);      
        return xml.equals(goldenxml) && xmlCloned.equals(goldenxml);
    }

    private String loadGoldenXML(String goldenSyntaxFile) {
        String goldenxml = null;
        try {
            File f = new File(goldenSyntaxFile);
            long length = f.length();
            FileInputStream fis = new FileInputStream(f);
            int size;
            StringBuffer buf = new StringBuffer();
            while(length > 0) {
                if(length < Integer.MAX_VALUE) {
                    size = (int) length;
                    length = 0;
                }
                else {
                    size = Integer.MAX_VALUE;
                    length -= Integer.MAX_VALUE;
                }
                byte [] b = new byte[size];
                fis.read(b);

                String s = new String(b);
                buf.append(s);
            }
            goldenxml = buf.toString();

            fis.close();
        }
        catch(Exception ex) {
            System.err.println(ex);
        }
        return goldenxml;
    }
    
    private boolean isAtomic;
}