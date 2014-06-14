/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.configuration;

import odog.editor.ArtifactEditors.ComponentArtifactEditor;
import odog.editor.LibraryViewerFrame;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import xmleditorgenerator.BaseFiles.*;
import xmleditorgenerator.BaseFiles.Transaction.TransactionType;
import xmleditorgenerator.SpringUtilities;

public class ruleDescriptionPanel extends XMLPanel {
        
    public String [] attributeElements = {"name","libraryURL"};
    public JTextField [] attributeElementsTF = new JTextField[attributeElements.length];

    public JButton libraryViewerButton;
    
    public ruleDescriptionPanel() {
        super();
    }    

    public void setLibraryViewerFrame(LibraryViewerFrame frame) {
        libraryViewerFrame = frame;
    }
    
    public void setDataWarper(Object obj, TransactionManagerContainer editor) {
       artifactEditor = editor;
       dataWarper = new ruleDescriptionDataWarper(obj);
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

        JLabel lab = new JLabel("Library Viewer");        
        libraryViewerButton = new JButton("View");
        libraryViewerButton.addActionListener(new ActionListener() {          
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                libraryViewerFrame.setVisible(true);
                
                if(libraryViewerFrame.isOk()) {
                   attributeElementsTF[0].setText(
                           libraryViewerFrame.getSelectedRuleName());
                   attributeElementsTF[1].setText(
                           libraryViewerFrame.getSelectedLibraryURL());
                }
            }
        });

        lab.setLabelFor(libraryViewerButton);
        elementsPanel.add(lab);
        elementsPanel.add(libraryViewerButton);
        
        // *** Processa os atributos
        for(int i = 0;i < attributeElements.length;i++) {
            JLabel label = new JLabel(attributeElements[i], JLabel.TRAILING);
            attributeElementsTF[i] = new JTextField(dataWarper.getAttribute(attributeElements[i]));

            label.setLabelFor(attributeElementsTF[i]);

            elementsPanel.add(label);
            elementsPanel.add(attributeElementsTF[i]);
        }

        // Faz o layout
        SpringUtilities.makeCompactGrid(elementsPanel, 
            attributeElements.length + 1, 2, 6, 6, 6, 6);       

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
    
    private LibraryViewerFrame libraryViewerFrame;  
    private TransactionManagerContainer artifactEditor;    
}