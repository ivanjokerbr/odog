/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.configuration;

import odog.design.DesignConfiguration;
import odog.design.DesignRepository;
import odog.editor.LibraryViewerFrame;
import java.awt.*;
import javax.swing.*;
import java.util.*;
import xmleditorgenerator.BaseFiles.*;
import xmleditorgenerator.BaseFiles.Transaction.TransactionType;
import xmleditorgenerator.SpringUtilities;

public class configurationPanel extends XMLPanel {
        
    public String [] listElements = {"ruleConfig"};
    public Class [] listElementsClass = {ruleConfigPanel.class};
    public String [] listElementsLabels = {"ruleConfig"};
    public JScrollPane [] listElementsSP = new JScrollPane[listElements.length]; 
        
    public JComboBox islibrary = new JComboBox();

    public configurationPanel(DesignRepository repository) {
        super();
        designRepository = repository;
    }    

    public void setLibraryViewerFrame(LibraryViewerFrame frame){
        libraryViewerFrame = frame;
    }
    
    public void setDataWarper(Object obj, TransactionManagerContainer editor) {
       artifactEditor = editor;
       dataWarper = new configurationDataWarper(obj);
       commited = false;
       createPanel();
    }
 
    public boolean commited() {
        return commited;
    }
    
    public int getListElementsSize() {
       return listElements.length;
    }
   
    public int getAttributeElementsSize() {
       return 0;
    }
    
    public int getOptionalElementsSize() {
        return 0;
    }

    public int getNecessaryElementsSize() {
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
                            ((ruleConfigPanel)panel).setLibraryViewerFrame(libraryViewerFrame);
                            ((ruleConfigPanel)panel).setDesignRepository(designRepository);
                            
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
                        ((ruleConfigPanel)panel).setLibraryViewerFrame(libraryViewerFrame);
                        
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

        // is library
        islibrary.addItem("true");
        islibrary.addItem("false");
        islibrary.setMaximumSize(islibrary.getPreferredSize());
        
        if(((DesignConfiguration)dataWarper.getDataElement()).isLibrary()) {
            islibrary.setSelectedIndex(0);
        }
        else {
            islibrary.setSelectedIndex(1);
        }
        JLabel lab = new JLabel("Is library");
        lab.setLabelFor(islibrary);
        
        elementsPanel.add(lab);
        elementsPanel.add(islibrary);
        
        // Faz o layout
        SpringUtilities.makeCompactGrid(elementsPanel, 
            listElements.length + 1, 2, 6, 6, 6, 6);       

        add(elementsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();        
        JButton commit = new JButton("commit");
        commit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               boolean ok = true;
               int i;
               String msg = null;

               dataWarper.setAttribute("islibrary",(String)islibrary.getSelectedItem());
               if(ok) {
                   owner.dispose();
                   commited = true;
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
  
    private boolean commited;
    private LibraryViewerFrame libraryViewerFrame;
    private TransactionManagerContainer artifactEditor;
    
    private DesignRepository designRepository;
}