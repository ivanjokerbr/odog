/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors.Atomic;

import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.Value;
import odog.configuration.BaseConfiguration;
import odog.editor.ArtifactEditors.ArtifactEditor;
import odog.editor.ArtifactEditors.NodeListCellRenderer;
import odog.design.Design;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import java.util.*;
import xmleditorgenerator.BaseFiles.*;
import xmleditorgenerator.BaseFiles.Transaction.TransactionType;
import xmleditorgenerator.SpringUtilities;
        
public class methodPanel extends XMLPanel {
        
    public String [] listElements = {"attribute"};
    public Class [] listElementsClass = {attributePanel.class};
    public String [] listElementsLabels = {"attribute"};
    public JScrollPane [] listElementsSP = new JScrollPane[listElements.length]; 

    public String [] attributeElements = {"name", "language", "codeURL"};
    public JTextField [] attributeElementsTF = new JTextField[attributeElements.length];

    public JComboBox languageComboBox;
    public JComboBox nameComboBox;
    public JButton browseURL;
    public JButton editURL;

    public methodPanel() {
        super();
    }    
    
    public void setDataWarper(Object obj, TransactionManagerContainer editor) {
       artifactEditor = editor;
       designLocation = ((ArtifactEditor)artifactEditor).getDesign().getDesignLocation();
       dataWarper = new methodDataWarper(obj);       
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

        JTabbedPane elementsPanel = new JTabbedPane();
        
        /*
	JPanel elementsPanel = new JPanel();
        SpringLayout elementsLayout = new SpringLayout();
        elementsPanel.setLayout(elementsLayout);
          */
        
        JPanel attrPanel = new JPanel();
        SpringLayout attrLayout = new SpringLayout();
        attrPanel.setLayout(attrLayout);        

        // *** Processa os atributos
        for(int i = 0;i < attributeElements.length;i++) {
            JLabel label = new JLabel(attributeElements[i], JLabel.TRAILING);
            if(attributeElements[i].equals("language")) {
                languageComboBox = new JComboBox(BaseConfiguration.componentLanguages);
                languageComboBox.setEditable(false);
                languageComboBox.setMaximumSize(
                        languageComboBox.getPreferredSize());
                
                languageComboBox.setSelectedIndex(0);
                String s = dataWarper.getAttribute(attributeElements[i]);
                for(int k = 0;k < languageComboBox.getItemCount();k++) {
                    if(s.equals(languageComboBox.getItemAt(k))) {
                        languageComboBox.setSelectedIndex(k);
                        break;
                    }
                }

                label.setLabelFor(languageComboBox);
                attrPanel.add(label);
                attrPanel.add(languageComboBox);
            }
            else
            if(attributeElements[i].equals("name")) {
                nameComboBox = new JComboBox(BaseConfiguration.executionMethods);
                nameComboBox.setEditable(false);
                nameComboBox.setMaximumSize(
                        nameComboBox.getPreferredSize());
                
                nameComboBox.setSelectedIndex(0);
                String s = dataWarper.getAttribute(attributeElements[i]);
                for(int k = 0;k < nameComboBox.getItemCount();k++) {
                    if(s.equals(nameComboBox.getItemAt(k))) {
                        nameComboBox.setSelectedIndex(k);
                        break;
                    }
                }
                
                label.setLabelFor(nameComboBox);
                attrPanel.add(label);
                attrPanel.add(nameComboBox);
            }
            else
            if(attributeElements[i].equals("codeURL")) {
                attributeElementsTF[i] = new JTextField(
                        dataWarper.getAttribute(attributeElements[i]), 40);
                attributeElementsTF[i].setMaximumSize(
                        attributeElementsTF[i].getPreferredSize());
                attributeElementsTF[i].setEditable(false);
                
                final JTextField tf = attributeElementsTF[i];
                browseURL = new JButton("codeURL");
                browseURL.addActionListener(new java.awt.event.ActionListener() {
                    
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                       JFileChooser chooser = new JFileChooser(designLocation);
                       chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                       int returnVal = chooser.showOpenDialog(owner);
                       if(returnVal == JFileChooser.APPROVE_OPTION) {
                           String path = chooser.getSelectedFile().getAbsolutePath();
                           String relative = Design.subtractPath(path, designLocation);
                           if(relative == null) {
                               JOptionPane.showMessageDialog(owner, "Path must be " +
                                       "within project");
                               return;
                           }
                           File f = new File(path);
                           if(!f.exists()) {
                               try {
                                   f.createNewFile();
                               }
                               catch(IOException ioex) {
                                   System.out.println(ioex);
                               }
                           }
                           tf.setText(relative);
                       }
                    }
                    
                });

                JLabel lab1 = new JLabel("Edit code");
                editURL = new JButton("edit");
                editURL.setMaximumSize(editURL.getPreferredSize());
                editURL.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        try {
                            Runtime.getRuntime().exec("jedit " + designLocation + 
                                    tf.getText());
                        }
                        catch(IOException ex) {
                            System.out.println(ex);
                        }
                    }
                });
                lab1.setLabelFor(editURL);
                
                attrPanel.add(browseURL);
                attrPanel.add(attributeElementsTF[i]);

                attrPanel.add(lab1);
                attrPanel.add(editURL);
            }
        }

        // Faz o layout
        SpringUtilities.makeCompactGrid(attrPanel, 
            attributeElements.length + 1,
            2, 6, 6, 6, 6);              
        elementsPanel.addTab("Method data", attrPanel);
        
        for(int i = 0;i < listElements.length;i++) {
  	    DefaultListModel lm = new DefaultListModel();
            Iterator ite = dataWarper.elementIterator(listElements[i]);
            while(ite != null && ite.hasNext()) {
                lm.addElement(ite.next());
            }
            final JList list = new JList(lm);
            list.setCellRenderer(new NodeListCellRenderer());
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
                            final XMLPanel panel = (XMLPanel) elementPanel.newInstance();                        
                            panel.setDataWarper(obj, artifactEditor);
                            
                            // 3. cria o dialog para mostra-lo
                            JDialog dialog = new JDialog(owner, true);
                            dialog.setTitle(element + " Editor");
                            dialog.setContentPane(panel);
                            panel.setOwner(dialog);
                            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                                public void windowClosing(java.awt.event.WindowEvent evt) {
                                    DataWarper dw = panel.getDataWarper();
                                    dw.setDataElement(null);
                                }
                            });
                            
                            dialog.setBounds(owner.getX() + 10, owner.getY() + 10, 
                                    500, 150 * panel.getListElementsSize() + 
                                    50 * panel.getAttributeElementsSize() +
                                    50 * panel.getNecessaryElementsSize() +
                                    50 * panel.getOptionalElementsSize());
                            dialog.setVisible(true);

                            // 4. adiciona o elemento editado
                            w = panel.getDataWarper();

                            // apertou cancel
                            if(w.getDataElement() == null) break;

                            // Tenta inserir
                            msg = dataWarper.addElement(element, w.getDataElement());
                            if(msg != null) {
                                JOptionPane.showMessageDialog(owner, "Error while adding:\n" +
                                        element + "\nMessage:\n" + msg);
                            }
                            else {
                                TransactionManager manager = artifactEditor.getTransactionManager();
                                Transaction t = new Transaction(TransactionType.ADD_ELEMENT, 
                                    w.getDataElement());
                                manager.addTransaction(t);                                
                                
                                // como eh um atributo, tem que resolver a questao do nome
                                // do valor default
                                /*Attr attr = (Attr) w.getDataElement();
                                Value v = attr.getDefaultValue();
                                if(v != null) {
                                    v.setAssociatedAttributeName(attr.getFullName());
                                }*/
                            }
                        } while(msg != null);

                        if(w.getDataElement() != null) {
                            DefaultListModel model = (DefaultListModel) list.getModel();
                            model.addElement(w.getDataElement());
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
                        ((attributePanel) panel).isEditingAttribute = true;
                        
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
                        "removing:\n" + obj.toString());
                    if(answer == JOptionPane.NO_OPTION ||
                        answer == JOptionPane.CANCEL_OPTION) {
                        return;
                    }

                    dataWarper.removeElement(element, obj);
                    DefaultListModel lm = (DefaultListModel) list.getModel();
                    lm.removeElement(obj);

                    TransactionManager manager = artifactEditor.getTransactionManager();
                    Transaction t = new Transaction(TransactionType.REMOVE_ELEMENT, 
                            obj);
                    manager.addTransaction(t);
                }
            });
            menu.add(item);

            list.setComponentPopupMenu(menu);

            /*
            JLabel label = new JLabel(listElementsLabels[i], JLabel.TRAILING);
            label.setLabelFor(listElementsSP[i]);

            elementsPanel.add(label);                                   
            elementsPanel.add(listElementsSP[i]);
            */
            
            elementsPanel.add(listElementsLabels[i], listElementsSP[i]);
        }
        
        add(elementsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();        
        JButton commit = new JButton("commit");
        commit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               boolean ok = true;
               int i;
               String msg = null;
               for(i = 0;i < attributeElements.length;i++) {
                   if(attributeElements[i].equals("name")) {
                       Object valueBefore = dataWarper.getAttribute(attributeElements[i]);
                       if(valueBefore.equals((String)nameComboBox.getSelectedItem())) {
                           continue;
                       }
                       msg = dataWarper.setAttribute(attributeElements[i],
                               (String)nameComboBox.getSelectedItem());
                       if(msg != null) {
                           ok = false;
                           break;
                       }

                      TransactionManager manager = artifactEditor.getTransactionManager();
                      Transaction t = new Transaction(TransactionType.CHANGE_ELEMENT, 
                            dataWarper.getDataElement(), attributeElements[i], 
                            valueBefore, nameComboBox.getSelectedItem());
                      manager.addTransaction(t);                  
                   }
                   else
                   if(attributeElements[i].equals("codeURL")) {      
                       if(attributeElementsTF[i].getText().matches("[\\s]*")) {
                          ok = false;
                          msg =  new String("Attribute " + attributeElements[i] +
                           " has incorrect value = " + attributeElementsTF[i].getText()); 
                          break;
                       }
                       Object valueBefore = dataWarper.getAttribute(attributeElements[i]);
                       msg = dataWarper.setAttribute(attributeElements[i],
                               attributeElementsTF[i].getText());  
                       if(msg != null) {
                           ok = false;
                           break;
                       }
                       else 
                       if(!valueBefore.equals(attributeElementsTF[i].getText())) {
                          TransactionManager manager = artifactEditor.getTransactionManager();
                          Transaction t = new Transaction(TransactionType.CHANGE_ELEMENT, 
                                dataWarper.getDataElement(), attributeElements[i], 
                                valueBefore, attributeElementsTF[i].getText());
                          manager.addTransaction(t);
                       }
                   }
                   else 
                   if(attributeElements[i].equals("language")) {
                       Object valueBefore = dataWarper.getAttribute(attributeElements[i]);
                       msg = dataWarper.setAttribute(attributeElements[i], 
                               (String)languageComboBox.getSelectedItem());
                       if(msg != null) {
                           ok = false;
                           break;
                       }
                       if(!valueBefore.equals((String)languageComboBox.getSelectedItem())) {
                          TransactionManager manager = artifactEditor.getTransactionManager();
                          Transaction t = new Transaction(TransactionType.CHANGE_ELEMENT, 
                                dataWarper.getDataElement(), attributeElements[i], 
                                valueBefore, (String)languageComboBox.getSelectedItem());
                          manager.addTransaction(t);
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

    private String designLocation;
    private TransactionManagerContainer artifactEditor; 
}