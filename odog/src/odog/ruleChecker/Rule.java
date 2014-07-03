/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.ruleChecker;

import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import java_cup.runtime.Symbol;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.NonExistentAttributeException;
import java.io.File;

/**
 *
 * @author ivan
 */
public class Rule {

    public Rule() {
    }
    
    public Rule(String name, String fname) {
        this.name = name;
        ruleFile = fname;
        parseExpr(fname);
    }

    ///////////////// PUBLIC METHODS ///////////////////////////////////////////

    public Rule clone() {
        return new Rule(name, ruleFile);
    }
    
    public String toString() {
        return name;
    }
    
    public String getRuleFile() {
        return ruleFile;
    }

    public String getExpression() {
        return expr;
    }

    public String getName() {
        return name;
    }
    
    public DefaultMutableTreeNode getSyntaxTree() {
        return tree;
    }

    public List<String> errorMessages() {
        return errorMessages;
    }

    // Para testes de regressao
    public String exportRule() {
        return printSubtree(tree);
    }
    
    public void check(GraphModel model) {
        errorMessages = new LinkedList();
        if(parseError != null) {
            errorMessages.add(parseError);
            result = false;
            return;
        }
        if(DEBUG) {
            System.out.println("Checking " + getName());
        } 
        result = checkFrom(model, new HashMap(), tree);
    }

    public boolean getResult() {
        return result;
    }

    // Metodo feito para testar se um arquivo corretamente implementa uma regra
    // com relacao a sintaxe.
    public static boolean testParse(String ruleFile) {
        return (new Rule()).parseExpr(ruleFile);
    }

    ///////////////// PRIVATE METHODS //////////////////////////////////////////

    private boolean parseExpr(String ruleFile) {
        try {
            File f = new File(ruleFile);
            FileInputStream fi = new FileInputStream(f);
            byte [] data = new byte[(int)f.length()];
            fi.read(data);
            expr = new String(data);

            if(expr.equals("")) {
                parseError = "Rule without expression.";
                return false;
            }            
            FileReader fr = new FileReader(ruleFile);
            Lexer scanner = new Lexer(fr);
            parser p = new parser(scanner);
            
            Symbol s = p.parse();
            tree = (DefaultMutableTreeNode) s.value;
            functionDefs = p.getFunctionDefs();            
        }
        catch(Exception e) { 
            parseError = "PARSE ERROR " + e.getMessage();            
            return false;
        }

        return true;
    }

    public String getParseError() {
        return parseError;
        
    }
    // O ultimo valor de retorno eh o da expressao
    private boolean checkFrom(GraphModel model, HashMap varNodeTable, 
            DefaultMutableTreeNode root) {

        TreeNode tn = null;
        tn = (TreeNode) root.getUserObject();
        switch(tn.treeNodeType()) {           
            
            case TreeNode.QUANTIFIER: {
                Quantifier quant = (Quantifier) tn;

                DefaultMutableTreeNode child = (DefaultMutableTreeNode)
                    root.getChildAt(0);

                Collection<Node> c = model.nodeTypeCollection(quant.getType());
                if(quant.getQuantifier() == Quantifier.PT) {
                    boolean res = true;
                    for(Node n : c) {
                        varNodeTable.put(quant.getFreeVar(), n);

                        if(DEBUG) {
                            System.out.println("ck : PT " + quant.getFreeVar());    
                        }
                        res = checkFrom(model, varNodeTable, child);

                        if(DEBUG) {
                            System.out.println("-- ck : PT " + quant.getFreeVar() + " =" +
                                    res);
                        }

                        if(errorMessages.size() > 0) {
                            quant.setResult(false);
                            return false;
                        }
                        if(!res) {
                            errorMessages.add("Failed at quantifier " + quant + "\n");
                            errorMessages.add("Free var = " + 
                                    ((Node)varNodeTable.get(quant.getFreeVar())).toString());
                            break;
                        }
                        varNodeTable.remove(quant.getFreeVar());
                    }
                    quant.setResult(res);
                    return res;
                }
                else { // EX quantifier
                    boolean res = false;
                    for(Node n : c) {
                        varNodeTable.put(quant.getFreeVar(), n);

                        if(DEBUG) {
                            System.out.println("ck : EX " + quant.getFreeVar());    
                        }
                        res = checkFrom(model, varNodeTable, child);
                        if(DEBUG) {
                            System.out.println("-- ck : EX " + quant.getFreeVar() + " =" +
                                    res);
                        }
                        // abort
                        if(errorMessages.size() > 0) {
                            quant.setResult(false);
                            return false;
                        }

                        if(res) break;
                        varNodeTable.remove(quant.getFreeVar());
                    }
                    quant.setResult(res);
                    return res;
                }
            }

            case TreeNode.COMPARISON: {
                Comparison cmp = (Comparison) tn;

                DefaultMutableTreeNode child1 = 
                    (DefaultMutableTreeNode) root.getChildAt(0);
                DefaultMutableTreeNode child2 = 
                        (DefaultMutableTreeNode) root.getChildAt(1);

                TreeNode tnchild1 = (TreeNode) child1.getUserObject();
                TreeNode tnchild2 = (TreeNode) child2.getUserObject();

                if(tnchild1.treeNodeType() == TreeNode.ATTRIBUTE_SPECIFICATION) {
                    processAttributeSpecification(child1, model, varNodeTable);
                    // abort
                    if(errorMessages.size() > 0) {
                        cmp.setResult(false);
                        return false;
                    }
                }
                
                if(tnchild2.treeNodeType() == TreeNode.ATTRIBUTE_SPECIFICATION) {
                    processAttributeSpecification(child2, model, varNodeTable);
                    // abort
                    if(errorMessages.size() > 0) {
                        cmp.setResult(false);
                        return false;
                    }
                }
                
                if(DEBUG) {
                    System.out.println("ck : comparison ");
                }
                boolean res = processComparison(cmp, child1, child2);
                if(DEBUG) {
                    System.out.println("-- ck : comparison = " + res);
                }
                if(errorMessages.size() > 0) {
                    errorMessages.add("Failed at comparison " + cmp + "of\n");
                    errorMessages.add(child1.toString() + " \n AND \n " + child2.toString());
                    cmp.setResult(false);
                    return false;
                }
                cmp.setResult(res);
                return res;
            } 

            case TreeNode.LOGICAL: {
                Logical l = (Logical) tn;
                if(l.getOperator() == Logical.NOT) {
                    DefaultMutableTreeNode child = (DefaultMutableTreeNode) 
                        root.getChildAt(0);

                    boolean res = !checkFrom(model, varNodeTable, child);
                    // abort
                    if(errorMessages.size() > 0) {
                        l.setResult(false);
                        return false;
                    }

                    l.setResult(res);
                    return res;
                }
                else {
                    DefaultMutableTreeNode child1 = (DefaultMutableTreeNode) 
                        root.getChildAt(0);
                    DefaultMutableTreeNode child2 = (DefaultMutableTreeNode) 
                        root.getChildAt(1);
                 
                    boolean r1 = checkFrom(model, varNodeTable, child1);
                    // abort
                    if(errorMessages.size() > 0) {
                        l.setResult(false);
                        return false;
                    }

                    boolean res = false;
                    switch(l.getOperator()) {                       
                        case Logical.OR: {
                            if(r1) {
                                res = r1;
                            }
                            else {
                                boolean r2 = checkFrom(model, varNodeTable, child2);
                                // abort
                                if(errorMessages.size() > 0) {
                                    l.setResult(false);
                                    return false;
                                }
                                res = r2;
                            }
                        } break;

                        case Logical.AND: {
                            if(!r1) {
                                res = r1;
                            }
                            else {
                                boolean r2 = checkFrom(model, varNodeTable, child2);
                                // abort
                                if(errorMessages.size() > 0) {
                                    l.setResult(false);
                                    return false;        
                                }
                                res = r2;
                            }
                        } break;

                        case Logical.IMPLY: {
                            if(!r1) {
                                res = true;
                            }
                            else {
                                boolean r2 = checkFrom(model, varNodeTable, child2);
                                // abort
                                if(errorMessages.size() > 0) {
                                    l.setResult(false);
                                    return false;
                                }                  
                                res = r2;
                            }
                        } break;

                        case Logical.IFONLYIF: {
                            boolean r2 = checkFrom(model, varNodeTable, child2);
                                // abort
                            if(errorMessages.size() > 0) {
                                l.setResult(false);
                                return false;
                            }
                            
                            res = (r1 && r2) || (!r1 && !r2);
                        } break;
                    }

                    l.setResult(res);
                    return res;
                }
            }

            case TreeNode.GRAPH_CONNECTION: {
                GConnection gcon = (GConnection) tn;
                
                DefaultMutableTreeNode child1 = 
                    (DefaultMutableTreeNode) root.getChildAt(0);
                DefaultMutableTreeNode child2 = 
                        (DefaultMutableTreeNode) root.getChildAt(1);

                TreeNode tnchild1 = (TreeNode) child1.getUserObject();
                TreeNode tnchild2 = (TreeNode) child2.getUserObject();

                processNodeRef(tnchild1, model, varNodeTable);
                // abort
                if(errorMessages.size() > 0) {
                    // so da para colocar aqui esse add no error messages, pq preciso
                    // saber o no default mutable tree
                    errorMessages.add(printSubtree(root));
                    gcon.setResult(false);
                    return false;
                }
                processNodeRef(tnchild2, model, varNodeTable);
                // abort
                if(errorMessages.size() > 0) {
                    errorMessages.add(printSubtree(root));
                    gcon.setResult(false);
                    return false;
                }

                NodeRef c1 = (NodeRef) tnchild1;
                NodeRef c2 = (NodeRef) tnchild2;
                if(gcon.getOperator() == GConnection.AR) {
                    try {
                        gcon.setResult(model.hasEdge(c1.getNode(),
                               c2.getNode()));
                    }
                    catch(InvalidNodeException ex)  {
                        errorMessages.add("Edge connection ...\n");
                        errorMessages.add(ex.toString() + "\n");
                        errorMessages.add(printSubtree(root));
                        
                        gcon.setResult(false);
                        return false;
                    }
                }
                else { // caminho
                    try {
                        gcon.setResult( model.hasPath(c1.getNode(), 
                               c2.getNode()));
                    }
                    catch(InvalidNodeException ex)  {
                        errorMessages.add("Path connection ...\n");
                        errorMessages.add(ex.toString() + "\n");
                        errorMessages.add(printSubtree(root));

                        gcon.setResult(false);
                        return false;
                    }
                }
                return gcon.getResult();
            }   
            
            case TreeNode.FUNCTION_CALL: {
                Function funcCall = (Function) tn;
                Function funcDef = functionDefs.get(funcCall.getName());

                String [] arguments = funcCall.getArguments();
                String [] parameters = funcDef.getArguments();

                HashMap parametersTable = new HashMap();
                for(int j = 0;j < arguments.length;j++) {
                    Node node = (Node) varNodeTable.get(arguments[j]);
                    if(node == null) {
                        try {
                            node = model.findNode(arguments[j]);                          
                        }
                        catch(InvalidNodeException ex) {
                            errorMessages.add("Error while retrieving node " +
                                arguments[j] + " from model.");
                            errorMessages.add(ex.toString() + "\n");
                            funcCall.setResult(false);

                            return false;
                        }
                    }
                    parametersTable.put(parameters[j], node);
                }                
                boolean res = checkFrom(model, parametersTable, funcDef.getExpr());
                if(errorMessages.size() > 0) {
                    funcCall.setResult(false);
                    return false;
                }
                
                funcCall.setResult(res);
                return res;
            }
        }
        
        // System.out.println("PASSSSSSSSSSSEIIIIIIIIIIIII");
        return false;
    }

    // Associa a o no da arvore NodeRef, o no do grafo em questao. Ele pode ser
    // especificado pelo nome completo, ou por uma variavel de um quantificador
    
    // this method alters the error message list
    private void processNodeRef(TreeNode tn, GraphModel model, HashMap varNodeTable) {
        NodeRef ref = (NodeRef) tn;
        
        // Procura primeiro na tabela de variaveis livres de quantificadores
        Node graphnode = (Node) varNodeTable.get(ref.getReference());
        if(graphnode != null) {
            ref.setNode(graphnode);
            return;
        }

        try {
            graphnode = model.findNode(ref.getReference());
            ref.setNode(graphnode);
        }
        catch(InvalidNodeException ex) {
            errorMessages.add("Error while retrieving node " +
                ref.getReference() + " from model.");
            errorMessages.add(ex.toString() + "\n");
        }
    }
    
    // Primeiro determina o no Determina o valor e tipo de um atributo

    // this method alters the errorMessage list
    private void processAttributeSpecification(DefaultMutableTreeNode attrN, GraphModel model,
            HashMap varNodeTable) {
        DefaultMutableTreeNode child = (DefaultMutableTreeNode) attrN.getChildAt(0);
        AttributeSpecification ats = (AttributeSpecification) attrN.getUserObject();       
        TreeNode tnchild = (TreeNode) child.getUserObject();

        processNodeRef(tnchild, model, varNodeTable);
        // abort
        if(errorMessages.size() > 0) {
            errorMessages.add(printSubtree(attrN));
            return;
        }

        NodeRef ref = (NodeRef) tnchild;
        Node nref = ref.getNode();

        try {
            ats.setValueAndType(
                    model.getAttributeValue(nref, ats.getAttribute()),
                    model.getAttributeType(nref, ats.getAttribute()) );
        }
        catch(NonExistentAttributeException ex) {
            errorMessages.add("process attribute...\n");
            errorMessages.add(ex.toString());
            errorMessages.add(printSubtree(attrN));
        }   
        catch(InvalidNodeException ex2) {
            errorMessages.add("process attribute...\n");
            errorMessages.add(ex2.toString());
            errorMessages.add(printSubtree(attrN));
        }
    }
    
    // this method alters the error message list
    private boolean processComparison(Comparison cmp, DefaultMutableTreeNode child1,
            DefaultMutableTreeNode child2) {        
        TreeNode tnchild1 = (TreeNode) child1.getUserObject();        
        TreeNode tnchild2 = (TreeNode) child2.getUserObject();
        boolean result;
        
        if(tnchild1.treeNodeType() == TreeNode.STRING_LITERAL) {
            StringLiteral c1 = (StringLiteral) tnchild1;                        
            // String literal + String literal
            if(tnchild2.treeNodeType() == TreeNode.STRING_LITERAL) {
                StringLiteral c2 = (StringLiteral) tnchild2;

                errorMessages.add("Rule makes no sense. Comparison between " +
                        " two string literals " + c1.getLiteral() +
                        " " + c2.getLiteral());
                return false;
            }
            else 
            // String literal + attribute
            if(tnchild2.treeNodeType() == TreeNode.ATTRIBUTE_SPECIFICATION) {
                AttributeSpecification c2 = (AttributeSpecification) tnchild2;

                Class type = getTypeOfLiteral(c1.getLiteral());
                Class t2 = c2.getType();
                if(type != t2) {
                    errorMessages.add("Types doesn't match between " +
                            c1.getLiteral() + " and " + c2.getAttribute());
                    cmp.setResult(false);
                    return false;
                }
                Object value = getValueOfLiteral(c1.getLiteral(), type);
                
                result = compareValues(cmp.getOperator(),
                    type, value, c2.getValue());

                cmp.setResult(result);
            }                           
        }
        else 
        if(tnchild1.treeNodeType() == TreeNode.ATTRIBUTE_SPECIFICATION) {
            AttributeSpecification c1 = (AttributeSpecification) tnchild1;
            // Attribute + string literal
            if(tnchild2.treeNodeType() == TreeNode.STRING_LITERAL) {
                StringLiteral c2 = (StringLiteral) tnchild2;

                Class type = getTypeOfLiteral(c2.getLiteral());
                Class t2 = c1.getType();
                if(type != t2) {
                    errorMessages.add("Types doesn't match between " +
                            c2.getLiteral() + " and " + c1.getAttribute());
                    cmp.setResult(false);
                    return false;
                }

                Object value = getValueOfLiteral(c2.getLiteral(), type);
                result = compareValues(cmp.getOperator(), 
                    type, value, c1.getValue());

                cmp.setResult(result);
            }
            else 
            // Attribute + attribute
            if(tnchild2.treeNodeType() == TreeNode.ATTRIBUTE_SPECIFICATION) {
                AttributeSpecification c2 = (AttributeSpecification) tnchild2;

                Class type = c1.getType();
                Class t2 = c2.getType();
                if(type != t2) {
                    errorMessages.add("Types doesn't match between " +
                            c1.getAttribute() + " and " + c2.getAttribute());
                    cmp.setResult(false);
                    return false;
                }

                Object value = c1.getValue();
                result = compareValues(cmp.getOperator(),
                    type, value, c2.getValue());

                cmp.setResult(result);
            }   
        }

        return cmp.getResult();
    }

    private Class getTypeOfLiteral(String literal) {
        // Booleano ?
        if(literal.equalsIgnoreCase("true") ||
           literal.equalsIgnoreCase("false")) {
            return Boolean.class;
        }

        // Inteiro ?        
        try {
            Integer i = new Integer(literal);
            return Integer.class;
        } 
        catch(NumberFormatException ex) { }
       
        // PF ?
        try {
            Double d = new Double(literal);
            return Double.class;
        }
        catch(NumberFormatException ex) { }
        
        return String.class;
    }
 
    private Object getValueOfLiteral(String literal, Class type) {
        if(type == Integer.class) {
            return new Integer(literal);
        }
        else
        if(type == Double.class) {
            return new Double(literal);
        }
        else
        if(type == Boolean.class) {
            if(literal.equalsIgnoreCase("true")) {
                return new Boolean(true);
            }
            else {
                return new Boolean(false);
            }
        }
        return new String(literal);
    }

    private boolean compareValues(int operator, Class type, Object value1, 
            Object value2) {
        if(DEBUG) {
            switch(operator) {
                case Comparison.EQ: System.out.println("ck : " + value1 + " = " +
                        value2); break;                        
                case Comparison.DIFF: System.out.println("ck : " + value1 + " != " +
                        value2); break;
                case Comparison.LT: System.out.println("ck : " + value1 + " < " +
                        value2); break;
                case Comparison.GT: System.out.println("ck : " + value1 + " > " +
                        value2); break;
                case Comparison.LE: System.out.println("ck : " + value1 + " <= " +
                        value2); break;
                case Comparison.GE: System.out.println("ck : " + value1 + " >= " +
                        value2); break;
            }            
        }
        
        if(type == Integer.class) {
            int i1 = ((Integer) value1).intValue();
            int i2 = ((Integer) value2).intValue();
            switch(operator) {
                case Comparison.EQ: return i1 == i2;
                case Comparison.DIFF: return i1 != i2;
                case Comparison.LT: return i1 < i2;
                case Comparison.GT: return i1 > i2;
                case Comparison.LE: return i1 <= i2;
                case Comparison.GE: return i1 >= i2;
            }
        }
        else
        if(type == Double.class) {
            double d1 = ((Double) value1).doubleValue();
            double d2 = ((Double) value2).doubleValue();
            switch(operator) {
                case Comparison.EQ: return d1 == d2;
                case Comparison.DIFF: return d1 != d2;
                case Comparison.LT: return d1 < d2;
                case Comparison.GT: return d1 > d2;
                case Comparison.LE: return d1 <= d2;
                case Comparison.GE: return d1 >= d2;
            }
        }
        else
        if(type == Boolean.class) {
            boolean b1 = ((Boolean) value1).booleanValue();
            boolean b2 = ((Boolean) value2).booleanValue();

            switch(operator) {
                case Comparison.EQ: return b1 == b2;
                case Comparison.DIFF: return b1 != b2;
                default: return false;
            }
        }
        else
        if(type == String.class) {
            String s1 = (String) value1;
            String s2 = (String) value2;
            
            switch(operator) {
                case Comparison.EQ: return s1.equals(s2);
                case Comparison.DIFF: return !s1.equals(s2);
                default: return false;
            }
        }
        return false;
    }
 
    private String printSubtree(DefaultMutableTreeNode root) {
        StringBuffer buf = new StringBuffer();

        Enumeration en = tree.preorderEnumeration();
        while(en.hasMoreElements()) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) en.nextElement();
            String pad = padFor(2*n.getLevel());
            
            TreeNode tn = (TreeNode) n.getUserObject();

            switch(tn.treeNodeType()) {
                
                case TreeNode.QUANTIFIER: {
                    Quantifier type = (Quantifier) tn;                    
                    buf.append(pad + type.toString() + "\n");
                } break;

                case TreeNode.NODEREF: {
                    NodeRef ref = (NodeRef) tn;
                    buf.append(pad + ref.toString() + "\n");
                } break;
                
                case TreeNode.ATTRIBUTE_SPECIFICATION: {
                    AttributeSpecification ats = (AttributeSpecification) tn;
                    buf.append(pad + ats.toString() + "\n");
                } break;
                
                case TreeNode.STRING_LITERAL: {
                    StringLiteral st = (StringLiteral) tn;
                    buf.append(pad + st.toString() + "\n");
                } break;
                
                case TreeNode.COMPARISON: {
                    Comparison cmp = (Comparison) tn;
                    buf.append(pad + cmp.toString() + "\n");
                } break;
                
                case TreeNode.LOGICAL: {
                    Logical l = (Logical) tn;
                    buf.append(pad + l.toString() + "\n");
                } break;
                
                case TreeNode.GRAPH_CONNECTION: {
                    GConnection gcon = (GConnection) tn;
                    buf.append(pad + gcon.toString() + "\n");
                } break;
            }
        }
        return buf.toString();
    }

    private String padFor(int length) {
        char [] ch = new char[length];
        for(int i = 0;i < length;i++) ch[i] = ' ';
        
        return new String(ch);
    }
    
    ///////////////// PRIVATE VARIABLES/////////////////////////////////////////

    private String name;
    private String expr = "";
 
    private String ruleFile;

    // Indica o resultado da verificacao com relacao ao ultimo modelo
    private boolean result;
    
    private DefaultMutableTreeNode tree;
    // for the current syntax tree, the associated table of function definitions
    private HashMap<String, Function> functionDefs;
    
    private LinkedList<String> errorMessages;
    
    private String parseError;
    
    private static final boolean DEBUG = false;
}