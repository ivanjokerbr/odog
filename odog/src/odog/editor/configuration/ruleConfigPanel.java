/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.configuration;

import odog.design.DesignRepository;
import odog.editor.LibraryViewerFrame;
import java.awt.*;
import javax.swing.*;
import java.util.*;
import xmleditorgenerator.BaseFiles.*;
import xmleditorgenerator.BaseFiles.Transaction.TransactionType;
import xmleditorgenerator.SpringUtilities;

public class ruleConfigPanel extends XMLPanel {
        
    public String [] listElements = {"ruleDescription"};
    public Class [] listElementsClass = {ruleDescriptionPanel.class};
    public String [] listElementsLabels = {"ruleDescription"};
    public JScrollPane [] listElementsSP = new JScrollPane[listElements.length]; 
        
    public String [] attributeElements = {"name"};
    public JTextField [] attributeElementsTF = new JTextField[attributeElements.length]; 
    
    public JCheckBox includeSyntaxRules;
    public JCheckBox includeCGRules;
    
    public ruleConfigPanel() {
        super();
    }    

    public void setLibraryViewerFrame(LibraryViewerFrame frame) {
        libraryViewerFrame = frame;
    }

    public void setDesignRepository(DesignRepository repository) {
        designRepository = repository;
    }

    public void setDataWarper(Object obj, TransactionManagerContainer editor) {
       artifactEditor = editor;
       dataWarper = new ruleConfigDataWarper(obj);
       createPanel();
    }
 
    public int getListElementsSize() {
       return listElements.length;
    }
   
    public int getAttributeElementsSize() {
       return attributeElements.length;
    }
   
    public int getNecessaryElementsSize() {
       return 0;
    }
     
    public int getOptionalElementsSize() {
        return 0;
    }
    
    public void createPanel() {
        setLayout(new BorderLayout());

	JPanel elementsPanel = new JPanel();
        SpringLayout elementsLayout = new SpringLayout();
        elementsPanel.setLayout(elementsLayout);
       
        for(int i = 0;i < listElements.length;i++) {
  	    DefaultListModel lm = new DefaultListModel();
            Iterator ite = dataWarper.elementIterator(listElements[i]);
            while(ite != null && ite.hasNext()) {
                lm.addElement(ite.next());
            }
            final JList list = new JList(lm);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            listElementsSP[i] = new JScrollPane(list); 

            final String element = listElements[i];
            final Class elementPanel = listElementsClass[i];

            JPopupMenu menu = new JPopupMenu();
            JMenuItem item = new JMenuItem("Add " + listElementsLabels[i]);
            item.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    // 1. recupera o elemento ou cria ele
                    Object obj = dataWarper.newElement(element);

                    // 2. cria o respectivo painel para o elemento
                    try {
                        String msg = null;
                        DataWarper w = null;
                        do {
                            XMLPanel panel = (XMLPanel) elementPanel.newInstance();                        
                            panel.setDataWarper(obj, artifactEditor);
                            
                            // 3. cria o dialog para mostra-lo
                            JDialog dialog = new JDialog(owner, true);
                            dialog.setTitle(element + " Editor");
                            dialog.setContentPane(panel);
                            panel.setOwner(dialog);
                            ((ruleDescriptionPanel)panel).setLibraryViewerFrame(libraryViewerFrame);
                            
                            dialog.setBounds(owner.getX() + 10, owner.getY() + 10, 
                                    500, 150 * panel.getListElementsSize() + 
                                    50 * panel.getAttributeElementsSize() +
                                    50 * panel.getNecessaryElementsSize() +
                                    50 * panel.getOptionalElementsSize() + 50);
                            dialog.setVisible(true);

                            // 4. adiciona o elemento editado
                            w = panel.getDataWarper();

                            // apertou cancel
                            if(w.getDataElement() == null) break;

                            // Tenta inserir
                            msg = dataWarper.addElement(element, w.getDataElement());
                            if(msg != null) {
                                JOptionPane.showMessageDialog(owner, "Error while adding\n" +
                                        element + "\nMessage:\n" + msg);
                            }
                        } while(msg != null);

                        if(w.getDataElement() != null) {
                            DefaultListModel model = (DefaultListModel) list.getModel();
                            model.addElement(w.getDataElement());

                            if(artifactEditor != null) {
                                TransactionManager manager = artifactEditor.getTransactionManager();
                                Transaction t = new Transaction(TransactionType.ADD_ELEMENT, 
                                        w.getDataElement());
                                manager.addTransaction(t);
                            }
                        }
                    }
                    catch(Exception ex) {
                        System.out.println(ex);
                        ex.printStackTrace();
                    }
                }
            });
            menu.add(item); 

            item = new JMenuItem("Edit " + listElementsLabels[i]);
            item.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    Object obj = list.getSelectedValue();
                    if(obj == null) return;
                    
                    try {
                        XMLPanel panel = (XMLPanel) elementPanel.newInstance();
                        panel.setDataWarper(obj, artifactEditor);                        

                        // 3. cria o dialog para mostra-lo
                        JDialog dialog = new JDialog(owner, true);
                        dialog.setTitle(element + " Editor");
                        dialog.setContentPane(panel);
                        panel.setOwner(dialog);
                        ((ruleDescriptionPanel)panel).setLibraryViewerFrame(libraryViewerFrame);
                        
                        dialog.setBounds(owner.getX() + 10, owner.getY() + 10, 
                                500, 150 * panel.getListElementsSize() + 
                                50 * panel.getAttributeElementsSize() +
                                50 * panel.getNecessaryElementsSize() +
                                50 * panel.getOptionalElementsSize() + 50);
                        dialog.setVisible(true);                        
                    }
                    catch(Exception ex) {
                        System.out.println(ex);
                        ex.printStackTrace();
                    }
                }
            });
            menu.add(item);

            item = new JMenuItem("Delete " + listElementsLabels[i]);
            item.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    Object obj = list.getSelectedValue();
                    if(obj == null) return;
                    
                    int answer = JOptionPane.showConfirmDialog(owner, "Are you sure about " +
                        "removing\n" + obj.toString());
                    if(answer == JOptionPane.NO_OPTION ||
                        answer == JOptionPane.CANCEL_OPTION) {
                        return;
                    } 

                    dataWarper.removeElement(element, obj);
                    DefaultListModel lm = (DefaultListModel) list.getModel();
                    lm.removeElement(obj);
                    
                    if(artifactEditor != null) {
                        TransactionManager manager = artifactEditor.getTransactionManager();
                        Transaction t = new Transaction(TransactionType.REMOVE_ELEMENT, 
                                obj);
                        manager.addTransaction(t);
                    }
                }
            });
            menu.add(item);

            list.setComponentPopupMenu(menu);

            JLabel label = new JLabel(listElementsLabels[i], JLabel.TRAILING);
            label.setLabelFor(listElementsSP[i]);

            elementsPanel.add(label);                                   
            elementsPanel.add(listElementsSP[i]);
        }

        // *** Processa os atributos
        for(int i = 0;i < attributeElements.length;i++) {
            JLabel label = new JLabel(attributeElements[i], JLabel.TRAILING);
            attributeElementsTF[i] = new JTextField(dataWarper.getAttribute(attributeElements[i]));
            attributeElementsTF[i].setMaximumSize(attributeElementsTF[i].getPreferredSize());
            
            label.setLabelFor(attributeElementsTF[i]);

            elementsPanel.add(label);
            elementsPanel.add(attributeElementsTF[i]);
        }

        includeSyntaxRules = new JCheckBox();
        RuleConfiguration rconf = (RuleConfiguration) dataWarper.getDataElement();
        includeSyntaxRules.setSelected(rconf.includeSyntaxRules());
        JLabel inclabel = new JLabel("Include syntax rules");
        inclabel.setLabelFor(includeSyntaxRules);

        elementsPanel.add(inclabel);
        elementsPanel.add(includeSyntaxRules);
        
        includeCGRules = new JCheckBox();
        includeCGRules.setSelected(rconf.includeCGRules());
        JLabel cglabel = new JLabel("Include code generation rules");
        cglabel.setLabelFor(includeCGRules);

        elementsPanel.add(cglabel);
        elementsPanel.add(includeCGRules);

        // Faz o layout
        SpringUtilities.makeCompactGrid(elementsPanel, 
            listElements.length + attributeElements.length + 2, 2, 6, 6, 6, 6);

        add(elementsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();        
        JButton commit = new JButton("commit");
        commit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               boolean ok = true;
               int i;
               String msg = null;
               for(i = 0;i < attributeElements.length;i++) {
                   if(attributeElementsTF[i].getText().matches("[\\s]*")) {
                      ok = false;
                      msg =  new String("Attribute " + attributeElements[i] +
                       " has incorrect value = " + attributeElementsTF[i].getText()); 
                      break;
                   }
                   Object valueBefore = dataWarper.getAttribute(attributeElements[i]);
                   if(valueBefore.equals(attributeElementsTF[i].getText())) {
                       continue;
                   }
                   msg = dataWarper.setAttribute(attributeElements[i],
                           attributeElementsTF[i].getText());

                   if(msg != null) {
                       ok = false;
                       break;
                   }

                   if(artifactEditor != null) {
                      TransactionManager manager = artifactEditor.getTransactionManager();
                      Transaction t = new Transaction(TransactionType.CHANGE_ELEMENT, 
                            dataWarper.getDataElement(), attributeElements[i], 
                            valueBefore, attributeElementsTF[i].getText());
                      manager.addTransaction(t);
                   }
               }
               
               RuleConfiguration rd = (RuleConfiguration) dataWarper.getDataElement();
               if(includeSyntaxRules.isSelected()) {
                   rd.setIncludeSyntaxRules(true);
                   if(rd.syntaxRulesSize() == 0) {
                       RuleConfiguration.addSyntaxRules(rd, designRepository);
                   }
               }
               else {
                   rd.setIncludeSyntaxRules(false);
               }
               
               if(includeCGRules.isSelected()) {
                   rd.setIncludeCGRules(true);
                   if(rd.cgRulesSize() == 0) {
                       RuleConfiguration.addCGRules(rd, designRepository);
                   }
               }
               else {
                   rd.setIncludeCGRules(false);
               }

               if(ok) {
                 owner.dispose();
               }
               else {
                   JOptionPane.showMessageDialog(owner, msg);  
               }
            }
        });
        buttonPanel.add(commit);

        JButton cancel = new JButton("cancel");        
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataWarper.setDataElement(null);               
                owner.dispose();
            }
        });
        buttonPanel.add(cancel);

        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private TransactionManagerContainer artifactEditor;
    private LibraryViewerFrame libraryViewerFrame;   
    private DesignRepository designRepository;
}