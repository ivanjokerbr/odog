/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors.Atomic;

import odog.syntax.Nodes.AttrClass;
import xmleditorgenerator.BaseFiles.Transaction;
import xmleditorgenerator.BaseFiles.Transaction.TransactionType;
import xmleditorgenerator.BaseFiles.TransactionManager;
import java.awt.*;
import javax.swing.*;
import xmleditorgenerator.BaseFiles.*;
import xmleditorgenerator.SpringUtilities;
        
/* Modificacoes
 *
 *  todos os 3 atributos implementados por JCheckBox
 *
 * */
public class attrClassificationPanel extends XMLPanel {
        
    public String [] attributeElements = {"visible","hasData","static"};
    public JCheckBox [] attributeElementsTF = new JCheckBox[attributeElements.length];

    public boolean removedHasData = false;
    public boolean addedHasData = false;
    
    public attrClassificationPanel() {
        super();
    }    

    public void setDataWarper(Object obj, TransactionManagerContainer editor) {
       artifactEditor = editor;
       dataWarper = new attrClassificationDataWarper(obj);
       createPanel();
    }
 
    public int getListElementsSize() {
       return 0;
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
       
        // *** Processa os atributos
        for(int i = 0;i < attributeElements.length;i++) {
            JLabel label = new JLabel(attributeElements[i], JLabel.TRAILING);

            attributeElementsTF[i] = new JCheckBox();
            if(dataWarper.getAttribute(attributeElements[i]).equals("true")) {
                attributeElementsTF[i].setSelected(true);
            }
            else {
                attributeElementsTF[i].setSelected(false);
            }
            label.setLabelFor(attributeElementsTF[i]);

            elementsPanel.add(label);
            elementsPanel.add(attributeElementsTF[i]);
        }

        // Faz o layout
        SpringUtilities.makeCompactGrid(elementsPanel, 
            attributeElements.length, 2, 6, 6, 6, 6);       

        add(elementsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();        
        JButton commit = new JButton("commit");
        commit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               boolean ok = true;
               int i;
               String msg = null;
               for(i = 0;i < attributeElements.length;i++) {
                   String value;
                   if(attributeElementsTF[i].isSelected()) {
                       value = "true";
                   }
                   else {
                       value = "false";
                   }                   
                   Object valueBefore = dataWarper.getAttribute(attributeElements[i]);
                   if(valueBefore.equals(value)) {
                       continue;
                   }
                   
                   if(attributeElements[i].equals("hasData")) {
                       if(value.equals("false")) {
                           int answer = JOptionPane.showConfirmDialog(owner, 
                                   "You have changed this attribute to have no associated value.\n" +
                                   " All values will be removed. Are you sure ?");

                           if(answer == JOptionPane.NO_OPTION) {
                               // nao efetua a modificacao do hasData
                               continue;    
                           }
                           else {
                               // deixa efetuar, e sinaliza na classe
                               removedHasData = true;
                           }
                       }
                       else {
                           addedHasData = true;
                       }
                   }
                   msg = dataWarper.setAttribute(attributeElements[i],
                           value);
                   if(msg != null) {
                       ok = false;
                       break;
                   }                   

                   TransactionManager manager = artifactEditor.getTransactionManager();
                   Transaction t = new Transaction(TransactionType.CHANGE_ELEMENT, 
                        dataWarper.getDataElement(), attributeElements[i], 
                        valueBefore, value);
                   manager.addTransaction(t);
               }

               AttrClass clas = (AttrClass) dataWarper.getDataElement();
               if(!clas.validCombination()) {
                   ok = false;
                   msg = "Invalid Combination of Options.";
                   removedHasData = false;
                   addedHasData = false;
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
}