/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.configuration;

import odog.design.Artifact;
import odog.design.Artifact.ElementType;
import odog.design.Design;
import odog.design.DesignParser;
import odog.design.DesignRepository;
import odog.syntax.Nodes.NonUniqueNameException;
import java.util.Iterator;
import java.util.LinkedList;
import odog.configuration.BaseConfiguration;

/**
 *
 * @author ivan
 */
public class RuleConfiguration {
    
    /** Creates a new instance of RuleConfiguration */
    public RuleConfiguration(String name) {
        this.name = name;
        ruleDescriptions = new LinkedList<RuleDescription>();
        
        syntaxRules = new LinkedList<RuleDescription>();
        includeSyntaxRules = false;
        
        cgRules = new LinkedList<RuleDescription>();
        includeCGRules = false;
    }

    ////////////////////////////// PUBLIC METHODS //////////////////////////////

    public String toString() {
        return name;
    }

    public String exportXML() {
        StringBuffer buf = new StringBuffer();

        buf.append("<ruleConfig name=\"" + name + "\" includeSyntaxRules=\"" +
                includeSyntaxRules + "\" includeCGRules=\"" + 
                includeCGRules + "\">\n");
        for(RuleDescription rd : ruleDescriptions) {
            buf.append(rd.exportXML());            
        }
        buf.append("</ruleConfig>\n");
        
        return buf.toString();
    }
    
    public void setName(String n) {
        name = n;
    }
   
    public String getName() {
        return name;
    }

    public void addRuleDescription(RuleDescription rd) throws NonUniqueNameException {
        for(RuleDescription r : ruleDescriptions) {
            if(r.getName().equals(rd.getName())) {
                throw new NonUniqueNameException("already contains rule description " +
                        "named " + rd.getName());
            }
        }
        ruleDescriptions.add(rd);
    }

    public Iterator ruleDescriptionsIterator() {
        return ruleDescriptions.iterator();
    }
    
    public void removeRuleDescription(RuleDescription rd) {
        ruleDescriptions.remove(rd);
    }

    /////// SYSTEM RULES //////
    public boolean includeSyntaxRules() {
        return includeSyntaxRules;
    }

    public void setIncludeSyntaxRules(boolean value) {
        includeSyntaxRules = value;
    }

    public boolean includeCGRules() {
        return includeCGRules;
    }

    public void setIncludeCGRules(boolean value) {
        includeCGRules = value;
    }

    public int syntaxRulesSize() {
        return syntaxRules.size();
    }

    public int cgRulesSize() {
        return cgRules.size();
    }

    public void addSyntaxRuleDescription(RuleDescription rd) throws NonUniqueNameException {
        for(RuleDescription r : syntaxRules) {
            if(r.getName().equals(rd.getName())) {
                throw new NonUniqueNameException("already contains rule description " +
                        "named " + rd.getName());
            }
        }
        syntaxRules.add(rd);
    }
    
    public void addCGRuleDescription(RuleDescription rd) throws NonUniqueNameException {
        for(RuleDescription r : cgRules) {
            if(r.getName().equals(rd.getName())) {
                throw new NonUniqueNameException("already contains rule description " +
                        "named " + rd.getName());
            }
        }
        cgRules.add(rd);
    }

    public Iterator cgRulesIterator() {
        return cgRules.iterator();
    }
    
    public Iterator syntaxRulesIterator() {
        return syntaxRules.iterator();
    }
    
    public static void addSyntaxRules(RuleConfiguration rd, 
            DesignRepository repository) {
         String loc = System.getenv("ODOG_RULES"); 
         if(loc == null) {
           System.err.println("Variable ODOG_RULES not set");
         }
         loc = BaseConfiguration.appendSlash(loc);        
         
         addRuleDescriptions(rd, "SyntaticRules", loc + "syntaticRules/", repository,
                 true);
    }
    
    public static void addCGRules(RuleConfiguration rd, 
            DesignRepository repository) {
         String loc = System.getenv("ODOG_RULES"); 
         if(loc == null) {
           System.err.println("Variable ODOG_RULES not set");
         }
         loc = BaseConfiguration.appendSlash(loc);        
         
         // 1. Synthatic rules        // 2. isem related rules
         addRuleDescriptions(rd, "cgRules", loc + "cgRules/", repository, false);

         // 3. service availabiblity         
         addRuleDescriptions(rd, "servicesRules", loc + "servicesRules/", 
                 repository, false);
    }
    
    ////////////////////////////// PRIVATE METHODS /////////////////////////////
    
    private static void addRuleDescriptions(RuleConfiguration rc, String designName, 
            String designLoc, DesignRepository repository, boolean isSyntax) {  
       Design d = DesignParser.parseDesign(designName, designLoc, true, true, 
               repository);
       for(Artifact art : d.artifacts()) {
           if(art.getType() == ElementType.RULE) {
               RuleDescription rd = new RuleDescription(art.getName(), 
                       designName + ".xml");
               try {
                   if(isSyntax) {
                       rc.addSyntaxRuleDescription(rd);
                   }
                   else {
                       rc.addCGRuleDescription(rd);
                   }
               }
               catch(NonUniqueNameException ex) {
                   System.out.println(ex);
               }
           }    
       }
    }
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    private String name;
    private LinkedList<RuleDescription> ruleDescriptions;
    
    private boolean includeSyntaxRules;
    private LinkedList<RuleDescription> syntaxRules;

    private boolean includeCGRules;
    private LinkedList<RuleDescription> cgRules;
}
