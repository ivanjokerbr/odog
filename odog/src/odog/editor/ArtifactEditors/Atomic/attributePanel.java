/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors.Atomic;

import odog.editor.ArtifactEditors.ComponentArtifactEditor;
import odog.editor.ArtifactEditors.AttributeDisplayList;
import xmleditorgenerator.BaseFiles.Transaction;
import xmleditorgenerator.BaseFiles.Transaction.TransactionType;
import xmleditorgenerator.BaseFiles.TransactionManager;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.AttrClass;
import odog.syntax.Nodes.Node;
import java.awt.*;
import javax.swing.*;
import xmleditorgenerator.BaseFiles.*;
import xmleditorgenerator.SpringUtilities;
        
public class attributePanel extends XMLPanel {
        
    public String [] attributeElements = {"name"};
    public JTextField [] attributeElementsTF = new JTextField[attributeElements.length];
        
    public String [] necessaryElements =  {"attrClassification"};
    public Class  [] necessaryElementsClass = {attrClassificationPanel.class};
    public String [] necessaryElementsLabel = {"attrClassification"};

    public String [] optionalElements = {"value"};
    public String [] optionalElementsLabel = {"default value"};
    public Class  [] optionalElementsClass = {valuePanel.class};
    public JButton [] optionalElementsButton;

    public JCheckBox check;
            
    public boolean isEditingAttribute = false;
    
    public attributePanel() {
        super();
    }    

    public void setDataWarper(Object obj, TransactionManagerContainer editor) {
       artifactEditor = editor;
       dataWarper = new attributeDataWarper(obj);
       createPanel();
    }
 
    public int getListElementsSize() {
       return 0;
    }
   
    public int getAttributeElementsSize() {
       return attributeElements.length;
    }
   
    public int getNecessaryElementsSize() {
       return necessaryElements.length;
    }
     
    public int getOptionalElementsSize() {
        return optionalElements.length;
    }
    
    public void createPanel() {
        setLayout(new BorderLayout());

	JPanel elementsPanel = new JPanel();
        SpringLayout elementsLayout = new SpringLayout();
        elementsPanel.setLayout(elementsLayout);
       
        // *** elementos individuais necessarios
        for(int i = 0;i < necessaryElements.length;i++) {
            JLabel label = new JLabel(necessaryElementsLabel[i], JLabel.TRAILING);
            JButton button = new JButton(necessaryElements[i]);

            final String element = necessaryElements[i];
            final Class elementPanel = necessaryElementsClass[i];

            button.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    // 1. recupera o elemento ou cria ele
                    Object obj = dataWarper.getElement(element);
		    if(obj == null) {
                        obj = dataWarper.newElement(element);
                    }
                    // 2. cria o respectivo painel para o elemento
                    try {
                        XMLPanel panel = (XMLPanel) elementPanel.newInstance();
                        panel.setDataWarper(obj, artifactEditor);

                    // 3. cria o dialog para mostra-lo
                        JDialog dialog = new JDialog(owner, true);
                        dialog.setTitle(element + " Editor");
                        dialog.setContentPane(panel);
                        panel.setOwner(dialog);
                        
                        dialog.setBounds(owner.getX() + 10, owner.getY() + 10, 
                                500, 150 * panel.getListElementsSize() + 
                                50 * panel.getAttributeElementsSize() +
                                50 * panel.getNecessaryElementsSize() +
                                50 * panel.getOptionalElementsSize());
                        dialog.setVisible(true);                        

                      // 4. atribui o elemento editado
                        DataWarper w = panel.getDataWarper();
                        if(w.getDataElement() != null) {
                            TransactionManager manager = artifactEditor.getTransactionManager();
                            Transaction t = new Transaction(TransactionType.CHANGE_ELEMENT, 
                                dataWarper.getDataElement(), element, 
                                dataWarper.getElement(element), w.getDataElement());
                            manager.addTransaction(t);
                            
                            dataWarper.setElement(element, w.getDataElement());
                        }
                        
                        if(((attrClassificationPanel)panel).removedHasData) {
                            removedHasData = true;
                            addedHasData = false;
                        }
                        else
                        if(((attrClassificationPanel)panel).addedHasData) {
                            removedHasData = false;
                            addedHasData = true;
                        }
                        
                        AttrClass clas = (AttrClass) dataWarper.getElement("attrClassification");
                        if(clas != null && clas.hasData()) {
                            check.setEnabled(true);
                            optionalElementsButton[0].setEnabled(true);
                        }
                        else {
                            check.setEnabled(false);
                            optionalElementsButton[0].setEnabled(false);
                        }
                    }
                    catch(Exception ex) {
                        System.out.println(ex);
                        ex.printStackTrace();
                    }
                }
            });            

            label.setLabelFor(button);

            elementsPanel.add(label);
            elementsPanel.add(button);
        }        

        optionalElementsButton = new JButton[optionalElements.length];
        // *** elementos individuais opcionais
        for(int i = 0;i < optionalElements.length;i++) {
            final String element = optionalElements[i];
            final Class elementPanel = optionalElementsClass[i];
            
            check = new JCheckBox("Has " + optionalElementsLabel[i]);
            optionalElementsButton[i] = new JButton(optionalElements[i]);
            
            Object obj = dataWarper.getElement(element);
            if(obj == null) {
                check.setSelected(false);
                optionalElementsButton[i].setEnabled(false);
            }
            else {
                check.setSelected(true);
                optionalElementsButton[i].setEnabled(true);
            }
            
            // Finds out if the attribute is classified as hasDAta
            final AttrClass clas = (AttrClass) dataWarper.getElement("attrClassification");
            if(clas == null || !clas.hasData()) {
                check.setEnabled(false);
            }
            
            final JButton jb = optionalElementsButton[i];
            check.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if(check.isSelected()) {
                        jb.setEnabled(true);
                    }
                    else {
                        jb.setEnabled(false);
                    }
                }
            });           

            optionalElementsButton[i].addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    // 1. recupera o elemento ou cria ele
                    Object obj = dataWarper.getElement(element);
		    if(obj == null) {
                        obj = dataWarper.newElement(element);
                    }
                    // 2. cria o respectivo painel para o elemento
                    try {
                        XMLPanel panel = (XMLPanel) elementPanel.newInstance();
                        ((valuePanel) panel).setDefaultValue(true);
                        panel.setDataWarper(obj, artifactEditor);

                    // 3. cria o dialog para mostra-lo
                        JDialog dialog = new JDialog(owner, true);
                        dialog.setTitle(element + " Editor");
                        dialog.setContentPane(panel);
                        panel.setOwner(dialog);

                        if(element.equals("value")) {
                            dialog.setBounds(owner.getX() + 10, owner.getY() + 10, 
                                        500, 200);
                        }
                        else {
                            dialog.setBounds(owner.getX() + 10, owner.getY() + 10, 
                                    500, 150 * panel.getListElementsSize() + 
                                    50 * panel.getAttributeElementsSize() +
                                    50 * panel.getNecessaryElementsSize() +
                                    50 * panel.getOptionalElementsSize());
                        }
                        dialog.setVisible(true);

                      // 4. atribui o elemento editado
                        DataWarper w = panel.getDataWarper();
                        if(w.getDataElement() != null) {
                            TransactionManager manager = artifactEditor.getTransactionManager();
                            Transaction t = new Transaction(TransactionType.CHANGE_ELEMENT, 
                                dataWarper.getDataElement(), element, 
                                dataWarper.getElement(element), w.getDataElement());
                            manager.addTransaction(t);
                            
                            dataWarper.setElement(element, w.getDataElement());
                        }
                    }
                    catch(Exception ex) {
                        System.out.println(ex);
                        ex.printStackTrace();
                    }
                }
            });
            elementsPanel.add(check);
            elementsPanel.add(optionalElementsButton[i]);
        }

        // *** Processa os atributos
        for(int i = 0;i < attributeElements.length;i++) {
            JLabel label = new JLabel(attributeElements[i], JLabel.TRAILING);
            attributeElementsTF[i] = new JTextField(dataWarper.getAttribute(attributeElements[i]),
                    40);
            attributeElementsTF[i].setMaximumSize(attributeElementsTF[i].getPreferredSize());
            
            label.setLabelFor(attributeElementsTF[i]);

            elementsPanel.add(label);
            elementsPanel.add(attributeElementsTF[i]);
        }

        // Faz o layout
        SpringUtilities.makeCompactGrid(elementsPanel, 
            attributeElements.length + necessaryElements.length +
            optionalElements.length, 2, 6, 6, 6, 6);       

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
                   
                  TransactionManager manager = artifactEditor.getTransactionManager();
                  Transaction t = new Transaction(TransactionType.CHANGE_ELEMENT, 
                        dataWarper.getDataElement(), attributeElements[i], 
                        valueBefore, attributeElementsTF[i].getText());
                  manager.addTransaction(t);
               }

               if(!ok) {
                   JOptionPane.showMessageDialog(owner, msg);  
                   return;
               }
               
               // 2. verifica se todos os elementos obrigatorios foram editados
               for(i = 0;i < necessaryElements.length;i++) {
                   Object obj = dataWarper.getElement(necessaryElements[i]);
                   if(obj == null) {
                       ok = false;
                       msg = new String("Element " + necessaryElements[i] + 
                           " must be edited.");
                       break;
                   }
               }
               
               // 3. Verifica que elemento opcional foi selecionado. Os que nao foram
               // sao apagados.
               for(i = 0;i < optionalElements.length;i++) {
                   if(!optionalElementsButton[i].isEnabled() && 
                       dataWarper.getElement(optionalElements[i]) != null) {
                        TransactionManager manager = artifactEditor.getTransactionManager();
                        Transaction t = new Transaction(TransactionType.CHANGE_ELEMENT, 
                            (Node) dataWarper.getDataElement(), optionalElements[i], 
                            dataWarper.getElement(optionalElements[i]), null);
                        manager.addTransaction(t); 
                        
                        dataWarper.setElement(optionalElements[i], null);
                   }
               }
               
               // 4. verifica se ha um valor default
               /*
               Value v = (Value) dataWarper.getElement("value");
               if(v != null) {
                   // nao da para adicionar o nome do atributo associado, pois
                   // tem que ser completo, e o atributo ainda nao foi inserido
                   // modelo. Cada objeto que tenha atributos, tem que verificar
                   // se tem valor default                   
                   Attr at = (Attr) dataWarper.getDataElement();
                   
                   TransactionManager manager = artifactEditor.getTransactionManager();
                   Transaction t = new Transaction(TransactionType.CHANGE_ELEMENT, 
                       at, "value", at.getDefaultValue(), v);
                   manager.addTransaction(t);
                   
                   at.setDefaultValue(v);
               }
*/
               if(ok) {
                 // Ao final, o status de hasData esta false, entao elimina as referencias
                 // e atualiza a attributeCBox. Somente para o caso de editar o attributo
                 if(isEditingAttribute) {
                     if(removedHasData) {
                         Attr at = (Attr) dataWarper.getDataElement();
                         at.removeReferences();
                     }   
                 }
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
  
    private boolean removedHasData;
    private boolean addedHasData;
    private TransactionManagerContainer artifactEditor;
}