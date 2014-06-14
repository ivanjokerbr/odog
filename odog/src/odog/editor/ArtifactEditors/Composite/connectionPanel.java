/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors.Composite;

import odog.editor.ArtifactEditors.Atomic.attributePanel;
import odog.editor.ArtifactEditors.CompositeComponentEditor;
import odog.editor.ArtifactEditors.NodeListCellRenderer;
import odog.editor.ArtifactEditors.NodeSelectionDialog;
import odog.syntax.Nodes.Connection;
import odog.syntax.Nodes.Hver;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.NonUniqueNameException;
import odog.syntax.Nodes.VirtualPort;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.util.*;
import xmleditorgenerator.BaseFiles.*;
import xmleditorgenerator.BaseFiles.Transaction.TransactionType;
import xmleditorgenerator.SpringUtilities;

public class connectionPanel extends XMLPanel {
        
    public String [] listElements = {"attribute"};
    public Class [] listElementsClass = {attributePanel.class};
    public String [] listElementsLabels = {"attribute"};
    public JScrollPane [] listElementsSP = new JScrollPane[listElements.length]; 
        
    public String [] attributeElements = {"name"};
    public JTextField [] attributeElementsTF = new JTextField[attributeElements.length];

    public JButton outputButton;
    public JTextField outputTextField;

    public JLabel inputLabel;
    public JScrollPane inputPortsSP;

    public boolean fromVersion;
    public Node connectionContainer;

    public connectionPanel() {
        super();
    }    

    public void setDataWarper(Object obj, TransactionManagerContainer editor) {
       artifactEditor = editor;
       dataWarper = new connectionDataWarper(obj);
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

        ////////////////////// elementos personalizados //////////////////////
        
        // nao sei se este elemento eh novo, ou esta sendo editao.
        final Connection con = (Connection) dataWarper.getDataElement();
        
        // 1. selecao do porto de saida da conexao
        outputTextField = new JTextField(40);
        outputTextField.setMaximumSize(
                outputTextField.getPreferredSize());
        outputTextField.setEditable(false);
        VirtualPort op = con.getOutputPort();
        if(op != null) {
            outputTextField.setText(op.getFullName());
        }

        outputButton = new JButton("Output Port");
        outputButton.addActionListener(new ActionListener() {
           public void actionPerformed(java.awt.event.ActionEvent evt) {
               ((CompositeComponentEditor) artifactEditor).updatePortDisplayList();
               NodeSelectionDialog pd = new NodeSelectionDialog(
                       ((CompositeComponentEditor) artifactEditor).getPortDisplayList(),
                       fromVersion, "Ports");
               pd.setVisible(true);
               
               VirtualPort vp = (VirtualPort) pd.getSelectedElement();
               if(vp == null || !pd.isOk()) return;

               if(!vp.isOutput()) {
                   JOptionPane.showMessageDialog(owner, "Port:\n" + vp.getFullName() +
                           "\nis not an output port.");
                   return;
               }

               if(fromVersion && !VirtualPort.checkPortLocation(vp, (Hver)connectionContainer)) {
                   JOptionPane.showMessageDialog(owner, "Port:\n" + vp.getFullName() + 
                           "\nis not contained in topology nor this version.");
                   return;
               }

               try {
                   con.addPort(vp);
               }
               catch(NonUniqueNameException ex) {
                   JOptionPane.showMessageDialog(owner, ex.toString());
                   return;   
               }

               outputTextField.setText(vp.getFullName());
           }
        });

        attrPanel.add(outputButton);
        attrPanel.add(outputTextField);
        
        // 2. selecao dos portos de entrada da conexao        
        inputLabel = new JLabel("Input Ports");
        
        final DefaultListModel lmodel = new DefaultListModel();
        Iterator ite = con.inputPortsIterator();
        while(ite.hasNext()) {
            lmodel.addElement(ite.next());
        }

        final JList inputList = new JList(lmodel);
        inputList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item = new JMenuItem("Add port");
        item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               ((CompositeComponentEditor) artifactEditor).updatePortDisplayList();
               NodeSelectionDialog pd = new NodeSelectionDialog(
                   ((CompositeComponentEditor) artifactEditor).getPortDisplayList(),
                   fromVersion, "Ports");
               pd.setVisible(true);

               VirtualPort vp = (VirtualPort) pd.getSelectedElement();
               if(vp == null || !pd.isOk()) return;
               
               if(!vp.isInput()) {
                   JOptionPane.showMessageDialog(owner, "Port:\n" + vp.getFullName() +
                           "\nis not an input port.");
                   return;
               }
               if(fromVersion && !VirtualPort.checkPortLocation(vp, (Hver)connectionContainer)) {
                   JOptionPane.showMessageDialog(owner, "Port:\n" + vp.getFullName() + 
                           "\nis not contained in topology nor this version.");
                   return;
               }

               try {
                   con.addPort(vp);
               }
               catch(NonUniqueNameException ex) {
                   JOptionPane.showMessageDialog(owner, ex.toString());
                   return;   
               }

               lmodel.addElement(vp);
            }
        });
        menu.add(item); 

        item = new JMenuItem("Delete port");
        item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Object obj = inputList.getSelectedValue();
                if(obj == null) return;

                int answer = JOptionPane.showConfirmDialog(owner, "Are you sure about " +
                    "removing\n" + obj.toString());
                if(answer == JOptionPane.NO_OPTION ||
                    answer == JOptionPane.CANCEL_OPTION) {
                    return;
                } 

                con.removePort((VirtualPort) obj);
                lmodel.removeElement(obj);
            }
        });
        menu.add(item);

        inputList.setComponentPopupMenu(menu);        
        inputPortsSP = new JScrollPane(inputList);
        inputLabel.setLabelFor(inputPortsSP);
        
        attrPanel.add(inputLabel);
        attrPanel.add(inputPortsSP);
        
        //////////////////////////////////////////////////////// Faz o layout
        
        SpringUtilities.makeCompactGrid(attrPanel, 
            attributeElements.length + 2, 2, 6, 6, 6, 6);       

        elementsPanel.addTab("Connection data", attrPanel);

        for(int i = 0;i < listElements.length;i++) {
  	    DefaultListModel lm = new DefaultListModel();
            ite = dataWarper.elementIterator(listElements[i]);
            while(ite != null && ite.hasNext()) {
                lm.addElement(ite.next());
            }
            final JList list = new JList(lm);
            list.setCellRenderer(new NodeListCellRenderer());
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            listElementsSP[i] = new JScrollPane(list); 

            final String element = listElements[i];
            final Class elementPanel = listElementsClass[i];

            JPopupMenu lmenu = new JPopupMenu();
            JMenuItem litem = new JMenuItem("Add " + listElementsLabels[i]);
            litem.addActionListener(new java.awt.event.ActionListener() {
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
                                JOptionPane.showMessageDialog(owner, "Error while adding\n" +
                                        element + "\nMessage:\n" + msg);
                            }
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
            lmenu.add(litem); 

            litem = new JMenuItem("Edit " + listElementsLabels[i]);
            litem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    Object obj = list.getSelectedValue();
                    if(obj == null) return;
                    
                    try {
                        XMLPanel panel = (XMLPanel) elementPanel.newInstance();
                        ((attributePanel)panel).isEditingAttribute = true;
                        
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
            lmenu.add(litem);

            litem = new JMenuItem("Delete " + listElementsLabels[i]);
            litem.addActionListener(new java.awt.event.ActionListener() {
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
                }
            });
            lmenu.add(litem);

            list.setComponentPopupMenu(lmenu);
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
               
               if(con.getOutputPort() == null) {
                   ok = false;
                   msg = "Connection must have an output port";
               }
               else
               if(con.inputPortsSize() == 0) {
                   ok = false;
                   msg = "Connection must have at least one input port"; 
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