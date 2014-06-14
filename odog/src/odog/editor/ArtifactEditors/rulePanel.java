/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors;

import odog.ruleChecker.Rule;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import xmleditorgenerator.SpringUtilities;

/**
 *
 * @author ivan
 */
public class rulePanel extends JPanel{
    
    /** Creates a new instance of rulePanel */
    public rulePanel(Rule r, RuleEditor owner, String ruleMsg) {
        rule = r;
        this.ruleMsg = ruleMsg;
        createPanel(owner);
    }
    
    ///////////////// PUBLIC METHODS ///////////////////////////////////////////
    
    public Rule getRule() {
        return rule;
    }

    public String getRuleMsg() {
        return ruleMsg;
    }
    
    //////////////// PRIVATE METHODS ///////////////////////////////////////////    
    
    private void createPanel(final RuleEditor owner) {
        setLayout(new BorderLayout());
        
        JPanel elementsPanel = new JPanel();
        SpringLayout elementsLayout = new SpringLayout();
        elementsPanel.setLayout(elementsLayout);
        
        final JTextArea ruletext = new JTextArea(rule.getExpression(), 35,80);
        JScrollPane sp = new JScrollPane(ruletext);
        JLabel label = new JLabel("Rule expression");
        label.setLabelFor(ruletext);
        elementsPanel.add(label);
        elementsPanel.add(sp);
        
        final JTextArea msgText = new JTextArea(ruleMsg);
        sp = new JScrollPane(msgText);
        label = new JLabel("Rule message text");
        label.setLabelFor(msgText);
        elementsPanel.add(label);
        elementsPanel.add(sp);
        
        final DefaultListModel templates = new DefaultListModel();
        templates.addElement("Conditional expression");
        templates.addElement("Comparison expression");
        templates.addElement("Path expression");
        templates.addElement("Existencial quantifier expression");
        templates.addElement("Universal quantifier expression");
        templates.addElement("Node attributes (all have: name(String) fullName(String)");
        final JList templatesList = new JList(templates);
        sp = new JScrollPane(templatesList);
        elementsPanel.add(sp);
              
        final DefaultListModel parameters = new DefaultListModel();
        final JList parametersList = new JList(parameters);
        sp = new JScrollPane(parametersList);
        elementsPanel.add(sp);
       
        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
              if(e.getClickCount() == 1) {
                  int index = templatesList.locationToIndex(e.getPoint());
                  String s = (String) templates.getElementAt(index);
                  if(s.equals("Conditional expression")) {
                      parameters.clear();
                      parameters.addElement("expression || expression");
                      parameters.addElement("expression && expression");
                      parameters.addElement("expression => expression");
                      parameters.addElement("expression <=> expression");
                      parameters.addElement("~( expression )");
                  }
                  else
                  if(s.equals("Comparison expression")) {
                      parameters.clear();
                      parameters.addElement("node/var[attribute] = node/var[attribute]");
                      parameters.addElement("node/var[attribute] = \"constant\"");
                      parameters.addElement("node/var[attribute] != node/var[attribute]");
                      parameters.addElement("node/var[attribute] != \"constant\"");
                      parameters.addElement("node/var[attribute] > node/var[attribute]");
                      parameters.addElement("node/var[attribute] > \"constant\"");
                      parameters.addElement("node/var[attribute] < node/var[attribute]");
                      parameters.addElement("node/var[attribute] < \"constant\"");
                      parameters.addElement("node/var[attribute] <= node/var[attribute]");
                      parameters.addElement("node/var[attribute] <= \"constant\"");
                      parameters.addElement("node/var[attribute] >= node/var[attribute]");
                      parameters.addElement("node/var[attribute] >= \"constant\"");
                  }   
                  else
                  if(s.equals("Path expression")) {
                      parameters.clear();
                      parameters.addElement("node/var -> node/var");
                      parameters.addElement("node/var ->* node/var");
                  }  
                  else
                  if(s.equals("Existencial quantifier expression")) {
                      parameters.clear();
                      parameters.addElement("EX.dport.var( expression )");
                      parameters.addElement("EX.exportedPort.var( expression )");
                      parameters.addElement("EX.atomicComponent.var( expression )");
                      parameters.addElement("EX.attribute.var( expression )");
                      parameters.addElement("EX.attributeClass.var( expression )");
                      parameters.addElement("EX.topology.var( expression )");
                      parameters.addElement("EX.compInstance.var( expression )");
                      parameters.addElement("EX.atomicVersion.var( expression )");
                      parameters.addElement("EX.topologyVersion.var( expression )");
                      parameters.addElement("EX.value.var( expression )");
                      // parameters.addElement("EX.reqserv.var( expression )");
                      parameters.addElement("EX.method.var( expression )");
                      parameters.addElement("EX.connection.var( expression )");
                      parameters.addElement("EX.defVer.var( expression )");
                  }
                  else
                  if(s.equals("Universal quantifier expression")) {
                      parameters.clear();
                      parameters.addElement("PT.dport.var( expression )");
                      parameters.addElement("PT.exportedPort.var( expression )");
                      parameters.addElement("PT.atomicComponent.var( expression )");
                      parameters.addElement("PT.attribute.var( expression )");
                      parameters.addElement("PT.attributeClass.var( expression )");
                      parameters.addElement("PT.topology.var( expression )");
                      parameters.addElement("PT.compInstance.var( expression )");
                      parameters.addElement("PT.atomicVersion.var( expression )");
                      parameters.addElement("PT.topologyVersion.var( expression )");
                      parameters.addElement("PT.value.var( expression )");
                      // parameters.addElement("PT.reqserv.var( expression )");
                      parameters.addElement("PT.method.var( expression )");
                      parameters.addElement("PT.connection.var( expression )");
                      parameters.addElement("PT.defVer.var( expression )");
                  }
                  else
                  if(s.equals("Node attributes (all have: name(String) fullName(String)")) {
                      parameters.clear();
                      parameters.addElement("dport -> isInput isOutput");
                      parameters.addElement("exportedPort -> ");
                      parameters.addElement("atomicComponent -> ");
                      parameters.addElement("attribute -> ");
                      parameters.addElement("attributeClass -> visible " +
                              "hasData static");
                      parameters.addElement("topology -> ");
                      parameters.addElement("compInstance -> compName " +
                              "libURL instanceName localInstanceName fullInstanceName");
                      parameters.addElement("atomicVersion -> ");
                      parameters.addElement("topologyVersion -> ");
                      parameters.addElement("value -> type valueExpr isDefault");
                      // parameters.addElement("reqserv -> ");
                      parameters.addElement("method -> language");
                      parameters.addElement("connection -> ");
                      parameters.addElement("defVer -> versionName instanceName");
                  }
              }
          }
        };
        templatesList.addMouseListener(mouseListener);
        
        mouseListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
              if(e.getClickCount() == 2) {
                  int index = templatesList.locationToIndex(e.getPoint());
                  String s = (String) templates.getElementAt(index);
                  if(s.equals("Node attributes")) return;
                  
                  index = parametersList.locationToIndex(e.getPoint());
                  s = (String) parameters.getElementAt(index);
                  if(ruletext.getSelectedText() != null) {
                      ruletext.replaceSelection(s);
                  }
                  else {
                      ruletext.insert(s, ruletext.getCaretPosition());
                  }
              }
          }
        };
        parametersList.addMouseListener(mouseListener);
        
        SpringUtilities.makeCompactGrid(elementsPanel, 
            6, 1, 6, 6, 6, 6);

        add(elementsPanel, BorderLayout.CENTER);
        
        final JPanel p = this;
        
        JPanel buttonPanel = new JPanel();        
        JButton commit = new JButton("commit");
        commit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    File f = new File("/tmp/test.rule");
                    FileOutputStream fos = new FileOutputStream(f);
                    PrintStream ps = new PrintStream(fos);
                    
                    ps.print(ruletext.getText());
                    
                    ps.close();
                    fos.close();
                    
                    boolean ok = Rule.testParse("/tmp/test.rule");
                    if(!ok) {
                        JOptionPane.showMessageDialog(p, "Expression syntax error.");
                        return;
                    }
                    
                    f = new File(rule.getRuleFile());
                    fos = new FileOutputStream(f);
                    ps = new PrintStream(fos);

                    ps.print(ruletext.getText());
                    
                    ps.close();
                    fos.close();
                    
                    rule = new Rule(rule.getName(), rule.getRuleFile());
                    
                    ruleMsg = msgText.getText();

                    owner.commit();
                }
                catch(IOException ex) {
                    System.out.println(ex);
                }
            }
        });
           
        buttonPanel.add(commit);

        JButton cancel = new JButton("cancel");        
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                owner.cancel();
            }
        });
        buttonPanel.add(cancel);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    ///////////////// PRIVATE VARIABLES////////////////////////////////////////
    
    private Rule rule;
    private String ruleMsg;
}
