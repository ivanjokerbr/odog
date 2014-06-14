/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors.Atomic;

import odog.editor.ArtifactEditors.NodeListCellRenderer;
import odog.syntax.Nodes.Node;
import java.awt.*;
import javax.swing.*;
import java.util.*;
import xmleditorgenerator.BaseFiles.*;
import xmleditorgenerator.BaseFiles.Transaction.TransactionType;
import xmleditorgenerator.SpringUtilities;
        
public class versionPanel extends XMLPanel {
        
    // with the reqserv object....
    //public String [] listElements = {"dport","value","attribute","method","reqserv"};
    //public Class [] listElementsClass = {dportPanel.class,valuePanel.class,attributePanel.class,
    //      methodPanel.class, reqservPanel.class};
    //public String [] listElementsLabels = {"Data Port","Value","Attribute","Method",
    //    "Required Services"};
    
    public String [] listElements = {"dport","value","attribute","method" };
    public Class [] listElementsClass = {dportPanel.class,valuePanel.class,attributePanel.class,
          methodPanel.class };
    public String [] listElementsLabels = {"Data Port","Value","Attribute","Method" };
    public JScrollPane [] listElementsSP = new JScrollPane[listElements.length];

    public String [] attributeElements = {"name"};
    public JTextField [] attributeElementsTF = new JTextField[attributeElements.length];
    
    // the Acomp node that contains or will contain this version
    public Node versionContainer;
    
    public versionPanel() {
        super();
    }

    public void setDataWarper(Object obj, TransactionManagerContainer editor) {
        artifactEditor = editor;
        dataWarper = new versionDataWarper(obj);
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
        /*
	JPanel elementsPanel = new JPanel();
        SpringLayout elementsLayout = new SpringLayout();
        elementsPanel.setLayout(elementsLayout);
       */
       
        JTabbedPane elementsPanel = new JTabbedPane();

        JPanel attrPanel = new JPanel();
        SpringLayout attrLayout = new SpringLayout();
        attrPanel.setLayout(attrLayout);
        
        // *** Processa os atributos
        for(int i = 0;i < attributeElements.length;i++) {
            JLabel label = new JLabel(attributeElements[i], JLabel.TRAILING);
            attributeElementsTF[i] = new JTextField(
                    dataWarper.getAttribute(attributeElements[i]), 40);
            attributeElementsTF[i].setMaximumSize(
                    attributeElementsTF[i].getPreferredSize());

            label.setLabelFor(attributeElementsTF[i]);

            attrPanel.add(label);
            attrPanel.add(attributeElementsTF[i]);
        }

        // Faz o layout
        SpringUtilities.makeCompactGrid(attrPanel, 
            attributeElements.length, 2, 6, 6, 6, 6);       

        elementsPanel.addTab("Version data", attrPanel);
        
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
            
            if(listElements[i].equals("value")) {
                valueListModel = lm;
            }

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
                            
                            if(element.equals("value")) {
                                ((valuePanel)panel).valueContainer = 
                                        (Node) dataWarper.getDataElement();
                            }

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
                            
                            if(element.equals("reqserv")) {
                                dialog.setBounds(owner.getX() + 10, owner.getY() + 10, 
                                        500, 100);                                   
                            }
                            else
                            if(element.equals("value")) {
                                dialog.setBounds(owner.getX() + 10, owner.getY() + 10, 
                                        500, 200);
                            }
                            else {
                                dialog.setBounds(owner.getX() + 10, owner.getY() + 10, 
                                        500, 150 * panel.getListElementsSize() + 
                                        60 * panel.getAttributeElementsSize() +
                                        50 * panel.getNecessaryElementsSize() +
                                        50 * panel.getOptionalElementsSize());
                            }
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
                            
                            /*
                            if(element.equals("attribute") && msg == null) {
                                // como eh um atributo, tem que resolver a questao do nome
                                // do valor default
                                Attr attr = (Attr) w.getDataElement();
                                Value v = attr.getDefaultValue();
                                if(v != null) {
                                    v.setAssociatedAttributeName(attr.getFullName());
                                }
                            }*/
                            
                        } while(msg != null);

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

            item = new JMenuItem("Edit " + listElementsLabels[i]);
            item.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    Object obj = list.getSelectedValue();
                    if(obj == null) return;
                    
                    try {
                        XMLPanel panel = (XMLPanel) elementPanel.newInstance();

                        if(element.equals("attribute")) {
                            ((attributePanel)panel).isEditingAttribute = true;
                        }
                        else
                        if(element.equals("value")) {
                            ((valuePanel)panel).valueContainer = 
                                    (Node) dataWarper.getDataElement();
                        }

                        panel.setDataWarper(obj, artifactEditor);
                        
                        // 3. cria o dialog para mostra-lo
                        JDialog dialog = new JDialog(owner, true);
                        dialog.setTitle(element + " Editor");
                        dialog.setContentPane(panel);
                        panel.setOwner(dialog);

                        if(element.equals("reqserv")) {
                            dialog.setBounds(owner.getX() + 10, owner.getY() + 10, 
                                    500, 100);                                   
                        }
                        else
                        if(element.equals("value")) {
                            dialog.setBounds(owner.getX() + 10, owner.getY() + 10, 
                                500, 200);
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
                    
                    TransactionManager manager = artifactEditor.getTransactionManager();
                    Transaction t = new Transaction(TransactionType.REMOVE_ELEMENT, 
                            obj);
                    manager.addTransaction(t);

                    // se foi removido um atributo, entao recoloca a lista dos valores
                    // pois algum pode ter sido removido.
                    if(element.equals("attribute")) {
                        Iterator ite = dataWarper.elementIterator("value");
                        valueListModel.clear();
                        while(ite != null && ite.hasNext()) {
                            valueListModel.addElement(ite.next());
                        }           
                    }
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

    private DefaultListModel valueListModel;
    private TransactionManagerContainer artifactEditor;
}