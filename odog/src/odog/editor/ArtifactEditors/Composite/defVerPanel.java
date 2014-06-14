/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors.Composite;

import odog.editor.ArtifactEditors.ComponentInstancesComboBox;
import odog.editor.ArtifactEditors.CompositeComponentEditor;
import odog.syntax.Nodes.CompBase;
import odog.syntax.Nodes.CompInstance;
import odog.syntax.Nodes.DefVer;
import odog.syntax.Nodes.Hver;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.VersionBase;
import java.awt.*;
import java.util.Iterator;
import javax.swing.*;
import xmleditorgenerator.BaseFiles.*;
import xmleditorgenerator.BaseFiles.Transaction.TransactionType;
import xmleditorgenerator.SpringUtilities;

public class defVerPanel extends XMLPanel {

    // personalizada
    public JTextField nameTF;

    public Node defVerContainer;  
    
    public defVerPanel() {
        super();
    }    

    public void setDataWarper(Object obj, TransactionManagerContainer editor) {
       artifactEditor = editor;
       dataWarper = new defVerDataWarper(obj);
       createPanel();
    }
 
    public int getListElementsSize() {
       return 0;
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
    
    public void createPanel() {
        setLayout(new BorderLayout());

	JPanel elementsPanel = new JPanel();
        SpringLayout elementsLayout = new SpringLayout();
        elementsPanel.setLayout(elementsLayout);
        
        // 1. O nome da defver

        nameTF = new JTextField(dataWarper.getAttribute("name"), 40);
        nameTF.setMaximumSize(nameTF.getPreferredSize());
        JLabel label = new JLabel("name", JLabel.TRAILING);
        label.setLabelFor(nameTF);
        
        elementsPanel.add(label);
        elementsPanel.add(nameTF);
        
        // 2. Combo box de selecao de instancias

        label = new JLabel("Component Instance", JLabel.TRAILING);
        ((CompositeComponentEditor) artifactEditor).updateComponentInstancesComboBox();
        ComponentInstancesComboBox ainscombo = 
                ((CompositeComponentEditor) artifactEditor).getComponentInstancesComboBox();        
        final JComboBox box = ainscombo.getBox();
        box.setMaximumSize(box.getPreferredSize());
        box.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CompBase ccomp = ((CompInstance) box.getSelectedItem()).getComponent();
                if(lmodel == null) return;

                lmodel.clear();
                Iterator vite = ccomp.versionsIterator();
                while(vite.hasNext()) {
                    lmodel.addElement(vite.next());
                }
            }
        });

        String name = (String) dataWarper.getAttribute("instanceName");
        for(int i = 0;i < box.getItemCount();i++) {
            CompInstance ai = (CompInstance) box.getItemAt(i);
            if(ai.getFullInstanceName().equals(name)) {
                box.setSelectedItem(ai);
                break;
            }
        }
        CompInstance selectedInstance = (CompInstance) box.getSelectedItem();
        CompBase ccomp = null;
        if(selectedInstance != null) {
            ccomp = selectedInstance.getComponent();
        }
        label.setLabelFor(box);
        elementsPanel.add(label);
        elementsPanel.add(box);
        
        // 3. Lista de versoes de uma instancia selecionada
        label = new JLabel("Versions", JLabel.TRAILING);
        
        String selectedVersion = (String) dataWarper.getAttribute("versionName");
        VersionBase selectedV = null;
        
        lmodel = new DefaultListModel();
        if(ccomp != null) {
            Iterator vite = ccomp.versionsIterator();
            while(vite.hasNext()) {
                VersionBase vb = (VersionBase) vite.next();
                if(selectedVersion != null && vb.getName().equals(selectedVersion)) {
                    selectedV = vb;
                }
                lmodel.addElement(vb);
            }
        }

        final JList list = new JList(lmodel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        if(selectedV != null) {
            list.setSelectedValue(selectedV, true);
        }

        JScrollPane sp = new JScrollPane(list);
        label.setLabelFor(sp);

        elementsPanel.add(label);
        elementsPanel.add(sp);
        
        // Faz o layout
        SpringUtilities.makeCompactGrid(elementsPanel, 3, 2, 6, 6, 6, 6);       

        add(elementsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();        
        JButton commit = new JButton("commit");
        commit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String msg = null;
                
                if(nameTF.getText().matches("[\\s]*")) {
                    msg =  new String("Attribute name has incorrect value :\n" +
                            nameTF.getText()); 
                    JOptionPane.showMessageDialog(owner, msg);  
                    return;
                }
                
                Object valueBefore = dataWarper.getAttribute("name");
                if(!valueBefore.equals(nameTF.getText())) {
                    msg = dataWarper.setAttribute("name",
                            nameTF.getText());
                    if(msg != null) {
                        JOptionPane.showMessageDialog(owner, msg);  
                        return;
                    }
                    TransactionManager manager = artifactEditor.getTransactionManager();
                    Transaction t = new Transaction(TransactionType.CHANGE_ELEMENT, 
                       dataWarper.getDataElement(), "name", 
                       valueBefore, nameTF.getText());
                    manager.addTransaction(t);
                } 

                DefVer defver = (DefVer) dataWarper.getDataElement();
                CompInstance ains = (CompInstance) box.getSelectedItem();

                if(!DefVer.checkComponentInstanceLocation(ains, (Hver) defVerContainer)) {
                    JOptionPane.showMessageDialog(owner, "Component instance:\n" +
                            ains.getFullName() + "\nnot in version\n" + 
                            defVerContainer.getFullName());
                    return;
                }
                VersionBase vbase = (VersionBase) list.getSelectedValue();

                if(ains == null) {
                    JOptionPane.showMessageDialog(owner, "No component instance selected");
                    return;
                }
                
                if(vbase == null) {
                    JOptionPane.showMessageDialog(owner, "No component instance selected");
                    return;
                }

                defver.setSelectedInstance(ains);
                defver.setSelectedVersion(vbase);

                defver.setInstanceName(ains.getFullInstanceName());
                defver.setVersionName(vbase.getName());

                owner.dispose();
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

    private DefaultListModel lmodel;
    private TransactionManagerContainer artifactEditor;
}