/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.services;

import odog.configuration.BaseConfiguration;
import java.awt.*;
import java.io.File;
import javax.swing.*;
import xmleditorgenerator.BaseFiles.*;
import xmleditorgenerator.SpringUtilities;

public class methodPanel extends XMLPanel {

    public String [] attributeElements = {"interface"};
    public JTextField [] attributeElementsTF = new JTextField[attributeElements.length];

    public JButton browseURL;
    public JComboBox languageComboBox;
    
    public methodPanel() {
        super();
    }    

    public void setDataWarper(Object obj, TransactionManagerContainer editor) {
       dataWarper = new methodDataWarper(obj);
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
            attributeElementsTF[i] = new JTextField(dataWarper.getAttribute(attributeElements[i]));
            
            if(attributeElements[i].equals("codeURL")) {
                final JTextField tf = attributeElementsTF[i];
                tf.setText(System.getenv("ODOG_SERVICES"));
                JLabel label = new JLabel(attributeElements[i], JLabel.TRAILING);
                
                label.setLabelFor(tf);
                
                //elementsPanel.add(browseURL);
                elementsPanel.add(label);
                elementsPanel.add(attributeElementsTF[i]);
            }
            else
            if(attributeElements[i].equals("language")) {
                JLabel label = new JLabel(attributeElements[i], JLabel.TRAILING);
                languageComboBox = new JComboBox(BaseConfiguration.componentLanguages);
                languageComboBox.setEditable(false);
                
                languageComboBox.setSelectedIndex(0);
                String s = dataWarper.getAttribute(attributeElements[i]);                
                for(int k = 0;k < languageComboBox.getItemCount();k++) {                    
                    if(s.equals(languageComboBox.getItemAt(k))) {                        
                        languageComboBox.setSelectedIndex(k);
                        break;
                    }
                }
                
                label.setLabelFor(languageComboBox);
                elementsPanel.add(label);
                elementsPanel.add(languageComboBox);
            }
            else {
                JLabel label = new JLabel(attributeElements[i], JLabel.TRAILING);
                label.setLabelFor(attributeElementsTF[i]);
                elementsPanel.add(label);
                elementsPanel.add(attributeElementsTF[i]);
            }
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
                   if(attributeElements[i].equals("language")) {
                       msg = dataWarper.setAttribute(attributeElements[i],
                               (String)languageComboBox.getSelectedItem());
                       if(msg != null) {
                           ok = false;
                           break;
                       }
                   }
                   else {                   
                       if(attributeElementsTF[i].getText().matches("[\\s]*")) {
                          ok = false;
                          msg =  new String("Attribute " + attributeElements[i] +
                           " has incorrect value = " + attributeElementsTF[i].getText()); 
                          break;
                       }
                       
                       // verifica a existencia do arquivo
                       if(attributeElements[i].equals("codeURL")) {
                           File f = new File( attributeElementsTF[i].getText());
                           if(!f.exists()) {
                               ok = false;
                               msg = new String("File " + attributeElementsTF[i].getText() + " does " +
                                       "not exists");
                               break;
                           }
                       }
                       
                       msg = dataWarper.setAttribute(attributeElements[i],
                               attributeElementsTF[i].getText());  
                       if(msg != null) {
                           ok = false;
                           break;
                       }
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
}