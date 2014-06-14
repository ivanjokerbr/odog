/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors.Atomic;

import odog.editor.ArtifactEditors.AtomicComponentEditor;
import odog.editor.ArtifactEditors.NodeListCellRenderer;
import xmleditorgenerator.BaseFiles.Transaction;
import xmleditorgenerator.BaseFiles.Transaction.TransactionType;
import xmleditorgenerator.BaseFiles.TransactionManager;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.Ver;
import java.awt.*;
import javax.swing.*;
import java.util.*;
import xmleditorgenerator.BaseFiles.*;

public class atomicComponentPanel extends XMLPanel {
        
    public String [] listElements = {"dport","attribute","version"};
    public Class [] listElementsClass = {dportPanel.class,attributePanel.class,versionPanel.class};
    public String [] listElementsLabels = {"Data Port","Attribute","Version"};
    public JScrollPane [] listElementsSP = new JScrollPane[listElements.length]; 
          
    public atomicComponentPanel() {
        super();
    }

    public void setDataWarper(Object obj, TransactionManagerContainer editor) {
    }
    
    public void setDataWarper(TransactionManagerContainer editor, Object obj, JFrame owner) {
       artifactEditor = editor;
       dataWarper = new atomicComponentDataWarper(obj);
       createPanel(owner);
    }
    
    public void createPanel() {
    }
    
    public int getListElementsSize() {
       return listElements.length;
    }
   
    public int getAttributeElementsSize() {
       return 0;
    }
   
    public int getNecessaryElementsSize() {
       return 0;
    }
     
    public int getOptionalElementsSize() {
        return 0;
    }
   
    // o parametro tem que ser owner para impedir que a variavel owner (JDialog em XMLPanel) 
    // prevaleca
    public void createPanel(final JFrame owner) {
        setLayout(new BorderLayout());

//	JPanel elementsPanel = new JPanel();
        JTabbedPane elementsPanel = new JTabbedPane();
  //      SpringLayout elementsLayout = new SpringLayout();
  //      elementsPanel.setLayout(elementsLayout);
       
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
                    Object obj = dataWarper.newElement(element);
                    // 2. cria o respectivo painel para o elemento
                    try {
                        String msg = null;
                        DataWarper w = null;
                        do {
                            // version must have a separete treatment, since the
                            // object must be included beforehand
                            if(element.equals("version")) {
                                String vname = JOptionPane.showInputDialog("Version name");
                                if(vname == null || vname.matches("[\\s]*")) {
                                    JOptionPane.showMessageDialog(owner, "version must have a" +
                                            "non-empty name");
                                    return;
                                }
                                Ver version = new Ver(vname);

                                msg = dataWarper.addElement(element, version);
                                if(msg != null) {
                                    JOptionPane.showMessageDialog(owner, "Error while adding:\n" +
                                           element + "\nMessage is:\n" + msg);
                                    return;
                                }
                                
                                DefaultListModel model = (DefaultListModel) list.getModel();
                                model.addElement(version);

                                TransactionManager manager = artifactEditor.getTransactionManager();
                                Transaction t = new Transaction(TransactionType.ADD_ELEMENT, 
                                        version);
                                manager.addTransaction(t);
                                return;
                            }
                            
                            final XMLPanel panel = (XMLPanel) elementPanel.newInstance();                        
                            panel.setDataWarper(obj, artifactEditor);
                            
                            // 3. cria o dialog para mostra-lo
                            JDialog dialog = new JDialog((JFrame) owner, true);
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
                                    60 * panel.getAttributeElementsSize() +
                                    50 * panel.getNecessaryElementsSize() +
                                    50 * panel.getOptionalElementsSize());
                            
                            dialog.setVisible(true);

                            // 4. adiciona o elemento editado
                            w = panel.getDataWarper();

                            // apertou cancel
                            if(w.getDataElement() == null) {
                                break;
                            }

                            // Tenta inserir
                            msg = dataWarper.addElement(element, w.getDataElement());
                            if(msg != null) {
                                JOptionPane.showMessageDialog(owner, "Error while adding\n" +
                                        element + "\nMessage:\n" + msg);
                            }
                        } while(msg != null);

                        // atualiza a visao e o manager de transacao
                        if(w.getDataElement() != null) {
                            DefaultListModel model = (DefaultListModel) list.getModel();
                            model.addElement(w.getDataElement());

                            TransactionManager manager = artifactEditor.getTransactionManager();
                            Transaction t = new Transaction(TransactionType.ADD_ELEMENT, 
                                    w.getDataElement());
                            manager.addTransaction(t);
                        }
                    }
                    catch(Exception ex) {
                        System.out.println(ex);
                        ex.printStackTrace();
                    }
                }
            });
            menu.add(item); 

            final String lelement = listElements[i];
            item = new JMenuItem("Edit " + listElementsLabels[i]);
            item.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    Object obj = list.getSelectedValue();
                    if(obj == null) return;
                    try {
                        XMLPanel panel = (XMLPanel) elementPanel.newInstance();
                        panel.setDataWarper(obj, artifactEditor);
                        if(lelement.equals("attribute")) {
                            ((attributePanel) panel).isEditingAttribute = true;
                        }

                        // 3. cria o dialog para mostra-lo
                        JDialog dialog = new JDialog((JFrame)owner, true);
                        dialog.setTitle(element + " Editor");
                        dialog.setContentPane(panel);
                        panel.setOwner(dialog);

                        if(element.equals("version")) {
                            dialog.setBounds(owner.getX() + 10, owner.getY() + 10, 
                                    700, 75 * panel.getListElementsSize() + 
                                    60 * panel.getAttributeElementsSize() +
                                    50 * panel.getNecessaryElementsSize() +
                                    50 * panel.getOptionalElementsSize());
                            ((versionPanel)panel).versionContainer = (Node)
                                    dataWarper.getDataElement();
                        }
                        else {
                            dialog.setBounds(owner.getX() + 10, owner.getY() + 10, 
                                    500, 150 * panel.getListElementsSize() + 
                                    60 * panel.getAttributeElementsSize() +
                                    50 * panel.getNecessaryElementsSize() +
                                    50 * panel.getOptionalElementsSize());
                        }
                        dialog.setVisible(true);
                    }
                    catch(Exception ex) {
                        System.out.println(ex);
                        ex.printStackTrace();
                    }
                }
            });
            menu.add(item);

            final JPanel container = this;
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
                            (Node) obj);
                    manager.addTransaction(t);
                }
            });
            menu.add(item);

            list.setComponentPopupMenu(menu);

            //JLabel label = new JLabel(listElementsLabels[i], JLabel.TRAILING);
            //label.setLabelFor(listElementsSP[i]);
            //elementsPanel.add(label);
            
            elementsPanel.add(listElementsLabels[i], listElementsSP[i]);
        }

        // Faz o layout
        //SpringUtilities.makeCompactGrid(elementsPanel, 
//            listElements.length, 2, 6, 6, 6, 6);       

        add(elementsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();        
        JButton commit = new JButton("commit");
        commit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               ((AtomicComponentEditor)artifactEditor).commit();
            }
        });
        buttonPanel.add(commit);

        JButton cancel = new JButton("cancel");        
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ((AtomicComponentEditor)artifactEditor).cancel();
            }
        });
        buttonPanel.add(cancel);

        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private TransactionManagerContainer artifactEditor;
}