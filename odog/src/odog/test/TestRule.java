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
import odog.ruleChecker.Checker;
import odog.syntax.Nodes.Acomp;
import odog.syntax.Nodes.Topology;
import odog.syntax.Parse.AtomicComponentParser;
import odog.syntax.Parse.TopologyParser;
import java.io.*;

public class TestRule {

    public TestRule(boolean isAtomic) {
        this.isAtomic = isAtomic;
    }

    public boolean test(String compFile, String ruleFile, String ruleName, String goldenFile) {
        Checker checker = new Checker();
        checker.addRule(ruleName, ruleFile);

        if(isAtomic) {
            Acomp comp = AtomicComponentParser.parseAtomicComponent(compFile);
            checker.checkAtomicComponent(comp);
        }
        else {
            Topology top = TopologyParser.parseTopology(compFile, 
                    new DesignRepository(new BaseConfiguration()));
            checker.checkTopology(top);
        }

        String ruleXML = checker.getRule(ruleName).exportRule();
        
        String goldenRuleXML = null;
        try {
            File f = new File(goldenFile);
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
            goldenRuleXML = buf.toString();

            fis.close();
        }
        catch(Exception ex) {
            System.err.println(ex);
        }

        return ruleXML.equals(goldenRuleXML);
    }

    private boolean isAtomic;
}