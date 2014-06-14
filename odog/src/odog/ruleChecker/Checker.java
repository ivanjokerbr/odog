/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.ruleChecker;

import odog.configuration.BaseConfiguration;
import odog.design.Artifact;
import odog.design.Design;
import odog.design.DesignParser;
import odog.design.RuleElement;
import odog.design.DesignRepository;
import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import odog.syntax.Nodes.Acomp;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.AttrClass;
import odog.syntax.Nodes.CompBase;
import odog.syntax.Nodes.CompInstance;
import odog.syntax.Nodes.Connection;
import odog.syntax.Nodes.DefVer;
import odog.syntax.Nodes.ExportedPort;
import odog.syntax.Nodes.Hver;
import odog.syntax.Nodes.Method;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.Topology;
import odog.syntax.Nodes.Value;
import odog.syntax.Nodes.VersionBase;
import odog.syntax.Nodes.VirtualPort;
import odog.syntax.Parse.AtomicComponentParser;
import odog.syntax.Parse.TopologyParser;

/**
 *
 * @author ivan
 */
public class Checker {

    /** Creates a new instance of Checker */
    public Checker() {
        rules = new LinkedList();
    }

    ///////////////// PUBLIC METHODS ///////////////////////////////////////////

    public void addRule(String rname, String fname) {
        Rule r = new Rule(rname, fname);
        rules.add(r);
    }

    public void addRule(Rule r) {
        rules.add(r);
    }
    
    public LinkedList getRules() {
        return rules;
    }
    
    public boolean checkPass() {
        for(Rule r : rules) {
            if(!r.getResult()) return false;
        }       
        return true;
    }
    
    public void checkAtomicComponent(Acomp comp) {
        model = new GraphModel();
        model.buildModel(comp);

        checkingEngine(model);
    }
    
    public void checkTopology(Topology top) {
        model = new GraphModel();
        model.buildModel(top);
             
        checkingEngine(model);
    }

    public String modelDotGraph() {
        return model.generateDot();
    }
    
    public Rule getRule(String ruleName) {        
        for(Rule r : rules) {
            if(r.getName().equals(ruleName)) {
                return r;
            }
        }
        return null;
    }
    
    public void printResults() {
        System.out.println(generateReport());
    }
    
    public String generateReport() {
        StringBuffer buf = new StringBuffer();
        for(Rule r : rules) {
            if(!r.getResult()) {
                buf.append("Rule " + r.getName() +  " failed!\n");
                for(String em : r.errorMessages()) {
                    buf.append(em);
                }
                buf.append("\n");
            }
        }
        return buf.toString();
    }

    public boolean extraSyntaxChecks(Topology top) {
        return performExtraSyntaxChecks(top);
    }

    public boolean extraCGChecks(Topology top) {
        return performExtraCGChecks(top);
    }
    
    public String getExtraCheckErrorMessage() {
        return extraCheckErrorMsg;
    }

    // Args: ( -l | -r rulefile) (-aa atorAtomic.xml | -tp topologia.xml)
    //         [-show]
    public static void main(String [] args) {
        BaseConfiguration configuration = new BaseConfiguration();
        DesignRepository repository = new DesignRepository(configuration);
        
        if(args.length < 2) {
            printUsage();
            return;
        }
        String extraRules = null;
        int xmlIndex = 0;
        Checker checker = new Checker();

        // a partir das regras contidas em ODOG_LIBRARIES
        if(args[0].equals("-l")) { 
            // carrega todas as regras            
            for(String s : configuration.getOdogLibraries()) {
               File f = new File(s);
               if(!f.isDirectory()) continue; 
               String [] files = f.list();
               for(int j = 0;j < files.length;j++) {
                   Design ret = DesignParser.parseDesign(files[j], s, true, 
                           false, repository);
                   if(ret != null) {
                       for(Artifact art : ret.artifacts()) {
                           if(art instanceof RuleElement) {
                               RuleElement re = (RuleElement) art;
                               Rule r = re.getRule();
                               if(r == null) {
                                   System.out.println(r.getParseError());
                                   continue;
                               }
                               checker.addRule(r);
                           }   
                       }
                   }
               }
            }

            if(args.length < 3) {
                printUsage();
                return;
            }
            xmlIndex = 1;
        }
        else
        if(args[0].equals("-r")) {  // verifica uma regra especifica
            if(args.length < 4) {
                printUsage();
                return;
            }
            checker.addRule("rule_" + new Date().getTime(),
                    System.getProperty("user.dir") + "/" + args[1]);
            xmlIndex = 2;
        }
        else {
            printUsage();
            return;
        }
        
        Acomp comp = null;
        Topology top = null;
        if(args[xmlIndex].equals("-aa")) { // atomico
            comp = AtomicComponentParser.parseAtomicComponent(args[xmlIndex + 1]);
            if(AtomicComponentParser.errorMessage != null) {
                System.out.println(AtomicComponentParser.errorMessage);
                return;
            }
            checker.checkAtomicComponent(comp);
        }
        else
        if(args[xmlIndex].equals("-tp")) {
            top = TopologyParser.parseTopology(args[xmlIndex + 1], repository);
            if(TopologyParser.errorMessage != null) {
                System.out.println(TopologyParser.errorMessage);
                return;
            }
            checker.checkTopology(top);
        }
        else {
            printUsage();
            return;
        }
        String compFile = (new File(args[xmlIndex + 1])).getAbsolutePath();
        
        checker.printResults();        
        
        if(args.length > xmlIndex + 2) {
            if(args[xmlIndex+2].equals("-show")) {
                CheckerResultsFrame frame = new CheckerResultsFrame();
                frame.setComponentFile(compFile);              

                if(comp != null) {                
                    frame.setComponentSyntaxTree(comp.getTreeNode());
                    frame.setComponentName(comp.getFullName());
                    frame.setIsAtomic();
                    frame.setComponentXML(comp.exportXML(0));
                }
                else {
                    frame.setComponentSyntaxTree(top.getTreeNode());
                    frame.setComponentName(top.getFullName());
                    frame.setComponentXML(top.exportXML(0));
                }
                
                frame.setRules(checker.getRules());
                frame.setDotGraph(checker.modelDotGraph());

                frame.setVisible(true); 
                frame.show();
            }
            else {
                printUsage();
                return;
            }
        }
    }

    ///////////////// PRIVATE METHODS //////////////////////////////////////////

    // this method can only be called after the checkTopology method, because
    // the model must be already built.
    private boolean performExtraSyntaxChecks(Topology top) {
        // 1. Type Checking and Connection consistency
        Iterator ite = top.versionsIterator();
        while(ite.hasNext()) {
            VersionBase vb = (VersionBase) ite.next();
            Hashtable<Attr,Value> values = top.buildValuesTable(vb);

            Collection<Node> cons = model.nodeTypeCollection(Node.CONNECTION);
            for(Node n : cons) {
                Connection c = (Connection) n;
                List<VirtualPort> ports = c.getPorts();

                if(ports.size() == 1) continue;
                VirtualPort vp1 = ports.get(0);
                Attr dtype1 = vp1.getDataType();
                Value t1 = values.get(dtype1);
                if(t1 == null) {
                    t1 = dtype1.getDefaultValue();
                }

                for(int i = 1;i < ports.size();i++) {                                        
                    VirtualPort vp2 = ports.get(i);

                    // connection consistency
                    if(vp1 == vp2) {                            
                        extraCheckErrorMsg = "Connection \n" +
                                c.getFullName() + "\n has " +
                                "the port\n" +
                                vp1.getFullName() + "\nlisted more than once.";
                        return false;
                    }
                    Attr dtype2 = vp2.getDataType();                        
                    Value t2 = values.get(dtype2);
                    if(t2 == null) {
                        t2 = dtype2.getDefaultValue();
                    }

                    if( (t1 == null && t2 != null) ||
                        (t1 != null && t2 == null) ||
                        !t1.getValueExpr().equals(t2.getValueExpr())) {

                        extraCheckErrorMsg = "Type check error. \nPorts" +
                                vp1.getFullName() +  " and \n" + vp2.getFullName() +
                                " of connection \n" + c.getFullName() + 
                                "have different types.\n";
                        return false;
                    }                                           
                }
            }
            
            Collection<Node> exps = model.nodeTypeCollection(Node.EXPORTEDPORT);
            for(Node n : exps) {
                ExportedPort ep = (ExportedPort) n;
                
                List<VirtualPort> ports = ep.getRefPorts();
                if(ports.size() == 1) continue;
                
                VirtualPort vp1 = ports.get(0);
                Attr at1 = vp1.getDataType();
                Value t1 = values.get(at1);
                if(t1 == null) {
                    t1 = at1.getDefaultValue();
                }
                
                for(int i = 1;i < ports.size();i++) {
                    VirtualPort vp2 = ports.get(i);
                    Attr at2 = vp2.getDataType();
                    Value t2 = values.get(at2);
                    if(t2 == null) {
                        t2 = at2.getDefaultValue();
                    }
                    
                    if(vp1 == vp2) {                            
                        extraCheckErrorMsg = "Exported port \n" +
                                ep.getFullName() + "\n has " +
                                "the port\n" +
                                vp1.getFullName() + "\nlisted more than once.";
                        return false;
                    }

                    if( (t1 == null && t2 != null) ||
                        (t1 != null && t2 == null) ||
                        !t1.getValueExpr().equals(t2.getValueExpr())) {

                        extraCheckErrorMsg = "Type check error. \nPorts" +
                                vp1.getFullName() +  " and \n" + vp2.getFullName() +
                                " of exported port \n" + ep.getFullName() + 
                                "have different types.\n";
                        return false;
                    }                                           
                }
            }
        }

        return true;
    }

    private boolean performExtraCGChecks(Topology top) {
        // 1. check that every attribute has a value
        // 2. check the valid combination of IS values
        Iterator ite = top.versionsIterator();
        while(ite.hasNext()) {
            VersionBase vb = (VersionBase) ite.next();
            Hashtable<Attr,Value> values = top.buildValuesTable(vb);
            
            String isem;
            Attr at = top.getAttribute("ISEM");
            if(at == null) {
                isem = null;
            }
            else {
                Value v = values.get(at);
                if(v == null) {
                    v = at.getDefaultValue();
                }
                if(v == null) {                    
                    isem = null;
                }
                else {
                    isem = v.getValueExpr();
                }
            }

            boolean res = checkAttrValues(top, vb, values, isem);
            if(res == false) return false;
        }
        
        return true;
    }

    private boolean checkAttrValues(Topology top, VersionBase vb, 
            Hashtable<Attr, Value> values, String currentISemValue) {

        Iterator ite = top.attributesIterator();
        while(ite.hasNext()) {
            Attr at = (Attr) ite.next();
            AttrClass clas = at.getClassification();
            if(!clas.hasData()) continue;
            
            if(!values.containsKey(at)) {
                if(at.getDefaultValue() == null) {
                    extraCheckErrorMsg = "Attribute \n" +
                        at.getFullName() + " does not have an associated value.";
                    return false;
                }                
            }
        }

        ite = vb.attributesIterator();
        while(ite.hasNext()) {
            Attr at = (Attr) ite.next();
            AttrClass clas = at.getClassification();
            if(!clas.hasData()) continue;
            
            if(!values.containsKey(at)) {
                if(at.getDefaultValue() == null) {
                    extraCheckErrorMsg = "Attribute \n" +
                            at.getFullName() + " does not have an associated value.";
                    return false;
                }                
            }
        }

        ite = ((Hver) vb).defVersionsIterator();
        while(ite.hasNext()) {
            DefVer df = (DefVer) ite.next();
            CompInstance ains = df.getSelectedInstance();
            CompBase cb = ains.getComponent();

            if(cb instanceof Topology) {
                VersionBase version = df.getSelectedVersion();
                Topology t = (Topology) cb;
                
                String isem;
                Attr at = t.getAttribute("ISEM");
                if(at == null) {
                    isem = null;
                }
                else {
                    Value v = values.get(at);
                    if(v == null) {
                        v = at.getDefaultValue();
                    }   
                    if(v == null) {
                        isem = null;
                    }
                    else {
                        isem = v.getValueExpr();
                    }
                }                
                
                if(currentISemValue != null && isem != null) {
                    boolean r = checkValidISemComb(currentISemValue, isem);
                    if(!r) {
                        extraCheckErrorMsg = "Cannot have the current combination of" +
                                " interaciton semantics: " + currentISemValue + " " +
                                isem;
                        return false;
                    } 
                }
                
                boolean res;
                if(isem != null) {
                    res = checkAttrValues((Topology) cb, version, values, isem);
                }
                else {
                    res = checkAttrValues((Topology) cb, version, values, currentISemValue);
                }

                if(res == false) {
                    return false;
                }
            }
            else {
                // check for the presence of non supported methods                
                VersionBase aver = df.getSelectedVersion();
                Iterator mite = aver.methodsIterator();
                while(mite.hasNext()) {
                    Method m = (Method) mite.next();
                    
                    if((currentISemValue.equals("DE") ||
                          currentISemValue.equals("DDF")) && m.getName().equals("fixedPoint")) {
                     
                        extraCheckErrorMsg = "Method fixedPoint is not supported by the " +
                                currentISemValue + " interaction semantics.";

                        return false;                        
                    }
                }
            }
        }
        
        return true;
    }
    
    private boolean checkValidISemComb(String parent, String child) {
        if(!parent.equals("DE") && child.equals("DE")) return false;
        
        if(parent.equals("DF") && !child.equals("DF")) return false;
        
        if(parent.equals("SR") && !child.equals("SR")) return false;
        
        if(parent.equals("DE") && child.equals("DF")) return false;

        return true;
    }
    
    private void checkingEngine(GraphModel model) {
        for(Rule r : rules) {
            r.check(model);
        }
    } 

    private static void printUsage() {
        System.out.println("Correct usage: checker (-l | -r rulefile) " +
                "(-aa atomicComponent.xml | -tp topology.xml) [-show]");
    }
    
    ///////////////// PRIVATE VARIABLES/////////////////////////////////////////
    
    private LinkedList<Rule> rules;
    private GraphModel model; 
    
    private String extraCheckErrorMsg;
}