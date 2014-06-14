/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors.Atomic;

// AN SPECIAL VALUE PANEL CLASS FOR SETTING THE PORT TYPE.

import java.awt.*;
import javax.swing.*;
import types.GenericTypes;
import xmleditorgenerator.BaseFiles.*;
import xmleditorgenerator.BaseFiles.Transaction.TransactionType;
import xmleditorgenerator.SpringUtilities;
        
public class portTypeValuePanel extends XMLPanel {

    public String [] attributeElements = {"associatedAttribute", "valueExpr"};
    public JTextField [] attributeElementsTF = new JTextField[attributeElements.length];

    //public JComboBox selectedTypeCBox;
    
    /** Coloquei o objeto para mostrar os atributos em questao do ator, apos os atributos
     ** normais */

    public portTypeValuePanel() {
        super();                
    }    

    public void setDataWarper(Object obj, TransactionManagerContainer editor) {
    }

    public void setDataWarper(Object obj, TransactionManagerContainer editor, String attr) {
       artifactEditor = editor;
       dataWarper = new valueDataWarper(obj);
       associatedAttribute = attr;
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
            if(attributeElements[i].equals("type")) {
                attributeElementsTF[i] = new JTextField("string");
                attributeElementsTF[i].setEditable(false);
            }
            else
            if(attributeElements[i].equals("associatedAttribute")) {
                attributeElementsTF[i] = new JTextField(associatedAttribute, 40);            
                attributeElementsTF[i].setEditable(false);
            }         
            else
            if(attributeElements[i].equals("valueExpr")) {                
                attributeElementsTF[i] = new JTextField(
                        dataWarper.getAttribute(attributeElements[i]), 40);                
            }
            attributeElementsTF[i].setMaximumSize(
                        attributeElementsTF[i].getPreferredSize());
            
            label.setLabelFor(attributeElementsTF[i]);
            elementsPanel.add(label);
            elementsPanel.add(attributeElementsTF[i]);
        }
        
        //selectedTypeCBox = new JComboBox(GenericTypes.availableTypes());

        /*String value = (String) dataWarper.getAttribute("valueExpr");
        selectedTypeCBox.setSelectedItem(value);
        
        JLabel lab = new JLabel();
        lab.setLabelFor(selectedTypeCBox);
        elementsPanel.add(lab);
        elementsPanel.add(selectedTypeCBox);*/

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
               //String selectedType = (String) selectedTypeCBox.getSelectedItem();
               for(i = 0;i < attributeElements.length;i++) {
                   if(attributeElements[i].equals("valueExpr")) {
                       if(attributeElementsTF[i].getText().matches("[\\s]*")) {
                          ok = false;
                          msg =  new String("Attribute " + attributeElements[i] +
                           " has incorrect value = " + attributeElementsTF[i].getText()); 
                          break;
                       }
                       Object valueBefore = dataWarper.getAttribute("valueExpr");
                       msg = dataWarper.setAttribute("valueExpr",
                          attributeElementsTF[i].getText());               
                       if(msg != null) {
                           ok = false;
                       }
                       else 
                       if(!valueBefore.equals(attributeElementsTF[i].getText())) {
                          TransactionManager manager = artifactEditor.getTransactionManager();
                          Transaction t = new Transaction(TransactionType.CHANGE_ELEMENT, 
                                dataWarper.getDataElement(), "valueExpr", 
                                valueBefore, attributeElementsTF[i].getText());
                          manager.addTransaction(t);
                       }        
                   }
               }               

               if(ok) {
                   // nao ha necessidade de resolver aqui o associatedAttribute do valor,
                   // pois qdo chamar o metodo setDefaultValue, na classe dportPanel
                   // isso sera resolvido
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
 
    private String associatedAttribute;
    private TransactionManagerContainer artifactEditor;
    private String defaultAttribute = null;
}