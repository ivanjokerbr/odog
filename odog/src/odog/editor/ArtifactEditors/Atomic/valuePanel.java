/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors.Atomic;

import odog.syntax.Nodes.Value;
import odog.configuration.BaseConfiguration;
import odog.editor.ArtifactEditors.ComponentArtifactEditor;
import odog.editor.ArtifactEditors.NodeSelectionDialog;
import odog.syntax.Nodes.CompInstance;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.VersionBase;
import java.awt.*;
import javax.swing.*;
import xmleditorgenerator.BaseFiles.*;
import xmleditorgenerator.BaseFiles.Transaction.TransactionType;
import xmleditorgenerator.SpringUtilities;

public class valuePanel extends XMLPanel {
         
    public String [] attributeElements = {"type", "valueExpr"};
    public JTextField [] attributeElementsTF = new JTextField[attributeElements.length];

    //public JComboBox typeComboBox;

    // the Version or ComponentInstance that contains, or will contain the value
    public Node valueContainer;

    /** Coloquei o objeto para mostrar os atributos em questao do ator, apos os atributos
     ** normais */

    public valuePanel() {
        super();
    }    

    // Um defaultValue nao null indica que esta janela esta editando o valor default de um atributo.
    // Isso significa que o campo associatedAttribute tem que fircar "congelado" com o mesmo nome.
    public void setDefaultValue(boolean value) {
        defaultValue = value;
    }

    public void setDataWarper(Object obj, TransactionManagerContainer editor) {
       artifactEditor = editor;
       dataWarper = new valueDataWarper(obj);
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
            /*if(attributeElements[i].equals("type")) {
                typeComboBox = new JComboBox(BaseConfiguration.dataTypes);
                typeComboBox.setEditable(false);

                typeComboBox.setSelectedIndex(0);
                String s = dataWarper.getAttribute(attributeElements[i]);
                for(int k = 0;k < typeComboBox.getItemCount();k++) {
                    if(s.equals(typeComboBox.getItemAt(k))) {
                        typeComboBox.setSelectedIndex(k);
                        break;
                    }
                }
                
                label.setLabelFor(typeComboBox);
                elementsPanel.add(label);
                elementsPanel.add(typeComboBox);
            }
            else {*/
                attributeElementsTF[i] = new JTextField(
                        dataWarper.getAttribute(attributeElements[i]), 40);
                attributeElementsTF[i].setMaximumSize(
                        attributeElementsTF[i].getPreferredSize());
                
                label.setLabelFor(attributeElementsTF[i]);

                elementsPanel.add(label);
                elementsPanel.add(attributeElementsTF[i]);    
            //}
        }

        if(defaultValue) {
            JLabel label = new JLabel("associatedAttribute", JLabel.TRAILING);
                        
            JTextField tf = new JTextField("");
            tf.setEditable(false);
            
            label.setLabelFor(tf);
            
            elementsPanel.add(label);
            elementsPanel.add(tf);

            // Faz o layout
            SpringUtilities.makeCompactGrid(elementsPanel, 
                attributeElements.length + 1, 2, 6, 6, 6, 6);    
        }
        else {
            JLabel associatedAttrLabel= new JLabel("Associated Attributes");
            final Value value = (Value) dataWarper.getDataElement();
            
            final DefaultListModel lmodel = new DefaultListModel();
            for(int j = 0;j < value.getAssociatedAttributes().size();j++) {
                lmodel.addElement(value.getAssociatedAttributes().get(j));
            }

            final JList attrList = new JList(lmodel);
            attrList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JPopupMenu menu = new JPopupMenu();
            JMenuItem item = new JMenuItem("Add associated attribute");
            item.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                   if(valueContainer instanceof CompInstance) {
                       ((ComponentArtifactEditor) artifactEditor).updateAttributeDisplayList(
                               (CompInstance)valueContainer, null);
                   }
                   else {
                       ((ComponentArtifactEditor) artifactEditor).updateAttributeDisplayList(
                               null, (VersionBase) valueContainer);
                   }
                   NodeSelectionDialog pd = new NodeSelectionDialog(
                       ((ComponentArtifactEditor) artifactEditor).getAttributeDisplayList(),
                       false, "Attributes");
                   pd.setVisible(true);

                   Attr attr = (Attr) pd.getSelectedElement();
                   if(attr == null || !pd.isOk()) return;
                   
                   /*if(!Value.checkAttributeLocation(attr, valueContainer)) {
                       JOptionPane.showMessageDialog(owner, "Selected attribute " + attr.getFullName() +
                           "does not belong to the same version or component " +
                           "of this value");
                   }
                   else {*/
                       if(value.addAssociatedAttribute(attr)) {
                           lmodel.addElement(attr);
                       }
                   /*} */
                }
            });
            menu.add(item); 

            item = new JMenuItem("Delete associated attribute");
            item.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    Object obj = attrList.getSelectedValue();
                    if(obj == null) return;

                    int answer = JOptionPane.showConfirmDialog(owner, "Are you sure about " +
                        "removing:\n" + obj.toString());
                    if(answer == JOptionPane.NO_OPTION ||
                        answer == JOptionPane.CANCEL_OPTION) {
                        return;
                    } 

                    value.removeAssociatedAttribute((Attr) obj);
                    lmodel.removeElement(obj);
                }
            });
            menu.add(item);

            attrList.setComponentPopupMenu(menu);        
            JScrollPane attrSP = new JScrollPane(attrList);
            associatedAttrLabel.setLabelFor(attrSP);

            elementsPanel.add(associatedAttrLabel);
            elementsPanel.add(attrSP);
            
            SpringUtilities.makeCompactGrid(elementsPanel, 
                attributeElements.length + 1, 2, 6, 6, 6, 6);    
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
                   /*if(attributeElements[i].equals("type")) {
                       Object valueBefore = dataWarper.getAttribute("type");                       
                       msg = dataWarper.setAttribute(attributeElements[i], 
                               (String)typeComboBox.getSelectedItem());
                       if(msg != null) {
                           ok = false;
                           break;
                       }
                       else 
                       if(!valueBefore.equals(typeComboBox.getSelectedItem())) {
                          TransactionManager manager = artifactEditor.getTransactionManager();
                          Transaction t = new Transaction(TransactionType.CHANGE_ELEMENT, 
                                dataWarper.getDataElement(), "type", 
                                valueBefore, typeComboBox.getSelectedItem());
                          manager.addTransaction(t);
                       }    
                   }
                   else {*/
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
                   //}
               }

               if(!defaultValue && ok) {
                   Value v = (Value) dataWarper.getDataElement();
                   if(v.getAssociatedAttributes().size() == 0) {
                       msg = "No associated attribute selected.";
                       ok = false;
                   }
               }
               
               /*if(!defaultValue && ok) {
                   Attr at = (Attr) box.getSelectedItem();                                                         
                   if(at == null) { // nao houve selecao
                       msg = "No associated attribute selected.";
                       ok = false;
                   }
                   else {                       
                       // Para o campo de associated attribute, tem que associar ao objeto
                       // Attr do atributo em questao. Foi colocado so o nome dele.
                       ok = Value.checkAttributeLocation(at, valueContainer);
                       if(!ok) {
                           msg = "Selected attribute " + at.getFullName() +
                                   "does not belong to the same version or component " +
                                   "of this value";
                       }
                       else {
                           Object valueBefore = dataWarper.getAttribute("associatedAttribute");
                           msg = dataWarper.setAttribute("associatedAttribute",
                                   at.getFullName());
                           if(msg != null) {
                               ok = false;
                           }
                           else
                           if(!valueBefore.equals(box.getSelectedItem())) {
                              TransactionManager manager = artifactEditor.getTransactionManager();
                              Transaction t = new Transaction(TransactionType.CHANGE_ELEMENT, 
                                    dataWarper.getDataElement(), "associatedAttribute", 
                                    valueBefore, box.getSelectedItem());
                              manager.addTransaction(t);
                           }
                           ((Value)dataWarper.getDataElement()).setAssociatedAttribute(
                               at);
                       }
                   }
               }*/
               
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
     
    //////////////////////////////////////////////////////////////////////
    
    private TransactionManagerContainer artifactEditor;
    private boolean defaultValue;
}