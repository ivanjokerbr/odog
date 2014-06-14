/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors.Composite;

import odog.editor.ArtifactEditors.CompositeComponentEditor;
import odog.editor.ArtifactEditors.NodeSelectionDialog;
import odog.syntax.Nodes.ExportedPort;
import odog.syntax.Nodes.Hver;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.VirtualPort;
import java.awt.*;
import javax.swing.*;
import xmleditorgenerator.BaseFiles.*;
import xmleditorgenerator.BaseFiles.Transaction.TransactionType;
import xmleditorgenerator.SpringUtilities;

public class exportedPortPanel extends XMLPanel {

    public String [] attributeElements = {"name"};
    public JTextField [] attributeElementsTF = new JTextField[attributeElements.length];

    public boolean fromVersion;
    public JList plist;
    public Node exportedPortContainer;

    public JLabel refPortsLabel;
    public JScrollPane refPortsSP;

    public exportedPortPanel() {
        super();
    }

    public void setDataWarper(Object obj, TransactionManagerContainer editor) {
       artifactEditor = editor;
       dataWarper = new exportedPortDataWarper(obj);
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
            if(attributeElements[i].equals("name")) {
                attributeElementsTF[i] = new JTextField(
                        dataWarper.getAttribute(attributeElements[i]), 40);
                attributeElementsTF[i].setMaximumSize(
                        attributeElementsTF[i].getPreferredSize());
                label.setLabelFor(attributeElementsTF[i]);

                elementsPanel.add(label);
                elementsPanel.add(attributeElementsTF[i]);
            }
        }

        // 2. selecao dos portos de entrada da conexao        
        final ExportedPort port = (ExportedPort) dataWarper.getDataElement();                
        refPortsLabel = new JLabel("Export ports");
        
        final DefaultListModel lmodel = new DefaultListModel();
        for(int i = 0;i < port.getRefPorts().size();i++) {                
            lmodel.addElement(port.getRefPorts().get(i));
        }

        final JList portList = new JList(lmodel);
        portList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
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
               
               if(fromVersion && !VirtualPort.checkPortLocation(vp, 
                       (Hver)exportedPortContainer)) {
                   JOptionPane.showMessageDialog(owner, "Port:\n" + vp.getFullName() + 
                           "\nis not contained in topology nor this version.");
                   return;
               }

               if(port.addRefPort(vp)) {
                   lmodel.addElement(vp);
               }
            }
        });
        menu.add(item); 

        item = new JMenuItem("Delete port");
        item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Object obj = portList.getSelectedValue();
                if(obj == null) return;

                int answer = JOptionPane.showConfirmDialog(owner, "Are you sure about " +
                    "removing\n" + obj.toString());
                if(answer == JOptionPane.NO_OPTION ||
                    answer == JOptionPane.CANCEL_OPTION) {
                    return;
                } 

                port.removeRefPort((VirtualPort) obj);
                lmodel.removeElement(obj);
            }
        });
        menu.add(item);

        portList.setComponentPopupMenu(menu);        
        refPortsSP = new JScrollPane(portList);
        refPortsLabel.setLabelFor(refPortsSP);
        
        elementsPanel.add(refPortsLabel);
        elementsPanel.add(refPortsSP);
        
        //////////////////////////////////////////////////////// Faz o layout
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
               }

               if(port.getRefPorts().size() == 0) {
                   ok = false;
                   msg = "Must export at least one port.";                   
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