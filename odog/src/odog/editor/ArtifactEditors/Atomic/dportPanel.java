/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors.Atomic;

import odog.editor.ArtifactEditors.NodeListCellRenderer;
import xmleditorgenerator.BaseFiles.Transaction;
import xmleditorgenerator.BaseFiles.Transaction.TransactionType;
import xmleditorgenerator.BaseFiles.TransactionManager;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.Value;
import java.awt.*;
import javax.swing.*;
import java.util.*;
import xmleditorgenerator.BaseFiles.*;
import xmleditorgenerator.SpringUtilities;

/* Modificacoes:
 *
 *  os atributos input e output sao implementdos por JCheckBox
 *
 */

public class dportPanel extends XMLPanel {
        
    public String [] listElements = {"attribute"};
    public Class [] listElementsClass = {attributePanel.class};
    public String [] listElementsLabels = {"attribute"};
    public JScrollPane [] listElementsSP = new JScrollPane[listElements.length];
        
    public String [] attributeElements = {"name","isInput","isOutput"};
    public JTextField [] attributeElementsTF = new JTextField[attributeElements.length];
    
    // adicionado
    public JCheckBox inputCheckBox;
    public JCheckBox outputCheckBox;
        
    public String [] necessaryElements =  {"portType"};
    public Class  [] necessaryElementsClass = {portTypeValuePanel.class};
    public String [] necessaryElementsLabel = {"portType"};
        
    public dportPanel() {
        super();
    }

    public void setDataWarper(Object obj, TransactionManagerContainer editor) {
       artifactEditor = editor;
       dataWarper = new dportDataWarper(obj);
       createPanel();
    }
    
    public int getListElementsSize() {
       return listElements.length;
    }
   
    public int getAttributeElementsSize() {
       return attributeElements.length;
    }
   
    public int getNecessaryElementsSize() {
       return necessaryElements.length;
    }
     
    public int getOptionalElementsSize() {
        return 0;
    }
    
    public void createPanel() {
        setLayout(new BorderLayout());

        JTabbedPane elementsPanel = new JTabbedPane();
	
        //JPanel elementsPanel = new JPanel();
        //SpringLayout elementsLayout = new SpringLayout();
        //elementsPanel.setLayout(elementsLayout);
                      
        JPanel attrPanel = new JPanel();
        SpringLayout attrLayout = new SpringLayout();
        attrPanel.setLayout(attrLayout);        

        // *** elementos individuais necessarios
        // O unico elemento necessario eh o tipo do porto. Nao mostro a janela de 
        // attributo, pois seu nome e classificacao sao fixas. Mostro direto a janela
        // de valores
        for(int i = 0;i < necessaryElements.length;i++) {
            JLabel label = new JLabel(necessaryElementsLabel[i], JLabel.TRAILING);
            JButton button = new JButton(necessaryElements[i]);

            final String element = necessaryElements[i];
            final Class elementPanel = necessaryElementsClass[i];
          
            button.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    // 1. recupera o elemento ou cria ele
                    Attr attrib = (Attr) dataWarper.getElement(element);
                    Value v;
		    if(attrib == null) {
                        attrib = (Attr) dataWarper.newElement(element);
                        v = new Value("string", "");
                    }
                    else {
                        v = attrib.getDefaultValue();
                        if(v == null) {
                            v = new Value("string", "");
                        }
                    }
                    // 2. cria o respectivo painel para o elemento
                    try {
                        portTypeValuePanel panel = (portTypeValuePanel) elementPanel.newInstance();
                        panel.setDataWarper(v, artifactEditor, attrib.getFullName());

                    // 3. cria o dialog para mostra-lo
                        JDialog dialog = new JDialog(owner, true);
                        dialog.setTitle(element + " Editor");
                        dialog.setContentPane(panel);
                        panel.setOwner(dialog);
                        
                        if(element.equals("portType")) {
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
                            // ** particularizacao ** //
                            TransactionManager manager = artifactEditor.getTransactionManager();
                            Transaction t = new Transaction(TransactionType.CHANGE_ELEMENT, 
                                attrib, "value", 
                                attrib.getDefaultValue(), w.getDataElement());
                            manager.addTransaction(t);
                            
                            attrib.setDefaultValue((Value)w.getDataElement());
                            
                            t = new Transaction(TransactionType.CHANGE_ELEMENT, 
                                (Node) dataWarper.getDataElement(), element, 
                                dataWarper.getElement(element) ,attrib);
                            manager.addTransaction(t);
                            
                            dataWarper.setElement(element, attrib);
                        }
                    }
                    catch(Exception ex) {
                        System.out.println(ex);
                        ex.printStackTrace();
                    }
                }
            });            

            label.setLabelFor(button);

            attrPanel.add(label);
            attrPanel.add(button);
        }        

        // *** Processa os atributos
        for(int i = 0;i < attributeElements.length;i++) {
            JLabel label = new JLabel(attributeElements[i], JLabel.TRAILING);
            if(attributeElements[i].equals("isInput")) {
                inputCheckBox = new JCheckBox();                

                if(dataWarper.getAttribute(attributeElements[i]).equals("true")) {
                    inputCheckBox.setSelected(true);
                }
                else {
                    inputCheckBox.setSelected(false);
                }
                
                label.setLabelFor(inputCheckBox);
                attrPanel.add(label);
                attrPanel.add(inputCheckBox);
            }
            else
            if(attributeElements[i].equals("isOutput")) {
                outputCheckBox = new JCheckBox();
                if(dataWarper.getAttribute(attributeElements[i]).equals("true")) {
                    outputCheckBox.setSelected(true);
                }
                else {
                    outputCheckBox.setSelected(false);
                }

                label.setLabelFor(outputCheckBox);
                attrPanel.add(label);
                attrPanel.add(outputCheckBox);
            }
            else {
                attributeElementsTF[i] = new JTextField(
                        dataWarper.getAttribute(attributeElements[i]), 40);
                attributeElementsTF[i].setMaximumSize(
                        attributeElementsTF[i].getPreferredSize());
                label.setLabelFor(attributeElementsTF[i]);
                attrPanel.add(label);
                attrPanel.add(attributeElementsTF[i]);
            }
        }

        // Faz o layout
        SpringUtilities.makeCompactGrid(attrPanel, 
            attributeElements.length + necessaryElements.length, 
                2, 6, 6, 6, 6);

        elementsPanel.addTab("Data port data", attrPanel);        
        
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
                                    (Node)w.getDataElement());
                                manager.addTransaction(t);
  /*                          
                                // como eh um atributo, tem que resolver a questao do nome
                                // do valor default
                                Attr attr = (Attr) w.getDataElement();
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
                            (Node) obj);
                    manager.addTransaction(t);
                }
            });
            menu.add(item);

            list.setComponentPopupMenu(menu);

            /*JLabel label = new JLabel(listElementsLabels[i], JLabel.TRAILING);
            label.setLabelFor(listElementsSP[i]);
            elementsPanel.add(label);                                   
            elementsPanel.add(listElementsSP[i]);*/
            
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
               
               if(!inputCheckBox.isSelected() && !outputCheckBox.isSelected()) {
                   ok = false;
                   JOptionPane.showMessageDialog(owner, "Port must be input or output.");
                   return;
               }
               
               for(i = 0;i < attributeElements.length;i++) {
                   if(attributeElements[i].equals("name")) {
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
                   else 
                   if(attributeElements[i].equals("isInput") ||
                      attributeElements[i].equals("isOutput")) {
                       String value;
                       if(attributeElements[i].equals("isInput")) {
                           if(inputCheckBox.isSelected()) {
                               value = "true";
                           }
                           else {
                               value = "false";
                           }
                       }
                       else   {
                           if(outputCheckBox.isSelected()) {
                               value = "true";
                           }
                           else {
                               value = "false";
                           }    
                       }
                           
                       Object valueBefore = dataWarper.getAttribute(attributeElements[i]);
                       msg = dataWarper.setAttribute(attributeElements[i],
                                   value);
                       if(msg != null) {
                           ok = false;
                           break;
                       }
                       else 
                       if(!valueBefore.equals(value)) {
                           TransactionManager manager = artifactEditor.getTransactionManager();
                           Transaction t = new Transaction(TransactionType.CHANGE_ELEMENT, 
                                (Node) dataWarper.getDataElement(), attributeElements[i],
                                  valueBefore, value);
                            manager.addTransaction(t);
                       }
                   }
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