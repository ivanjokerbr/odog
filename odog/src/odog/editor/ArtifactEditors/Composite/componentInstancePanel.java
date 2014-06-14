/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors.Composite;

import odog.design.Artifact;
import odog.design.Artifact.ElementType;
import odog.editor.ArtifactEditors.ArtifactEditor;
import odog.editor.ArtifactEditors.ComponentArtifactEditor;
import odog.editor.ArtifactEditors.Atomic.valuePanel;
import odog.editor.ArtifactEditors.CompositeComponentEditor;
import odog.editor.ArtifactEditors.NodeListCellRenderer;
import odog.design.Design;
import odog.editor.LibraryViewerFrame;
import odog.design.AtomicComponent;
import odog.design.CompositeComponent;
import odog.design.DesignRepository;
import odog.syntax.Nodes.Acomp;
import odog.syntax.Nodes.CompInstance;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.Topology;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.util.*;
import xmleditorgenerator.BaseFiles.*;
import xmleditorgenerator.BaseFiles.Transaction.TransactionType;
import xmleditorgenerator.SpringUtilities;

public class componentInstancePanel extends XMLPanel {
        
    public String [] listElements = {"value"};
    public Class [] listElementsClass = {valuePanel.class};
    public String [] listElementsLabels = {"value"};
    public JScrollPane [] listElementsSP = new JScrollPane[listElements.length]; 
        
    public String [] attributeElements = {"instanceName","compName","libraryURL"};
    public JTextField [] attributeElementsTF = new JTextField[attributeElements.length];

    public JButton libraryViewerButton;
   
    public componentInstancePanel() {
        super();
    }    

    public void setDataWarper(Object obj, TransactionManagerContainer editor) {
       artifactEditor = editor;
       dataWarper = new componentInstanceDataWarper(obj);
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
        
        JLabel lab = new JLabel("Library Viewer");        
        libraryViewerButton = new JButton("View");
        libraryViewerButton.setMaximumSize(
                libraryViewerButton.getPreferredSize());
        libraryViewerButton.addActionListener(new ActionListener() {          
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LibraryViewerFrame lf = ((ComponentArtifactEditor) artifactEditor).getLibraryViewer();
                lf.setVisible(true);
                
                if(lf.isOk()) {
                   attributeElementsTF[1].setText(lf.getSelectedComponentName());
                   attributeElementsTF[2].setText(lf.getSelectedLibraryURL());
                }
            }
        });

        lab.setLabelFor(libraryViewerButton);
        attrPanel.add(lab);
        attrPanel.add(libraryViewerButton);

        // *** Processa os atributos
        for(int i = 0;i < attributeElements.length;i++) {
            attributeElementsTF[i] = new JTextField(
                    dataWarper.getAttribute(attributeElements[i]), 40);
            attributeElementsTF[i].setMaximumSize(
                    attributeElementsTF[i].getPreferredSize());
            
            JLabel label = new JLabel(attributeElements[i], JLabel.TRAILING);
            label.setLabelFor(attributeElementsTF[i]);    
            attrPanel.add(label);
            attrPanel.add(attributeElementsTF[i]);
        }

        // Faz o layout
        SpringUtilities.makeCompactGrid(attrPanel, 
            attributeElements.length + 1, 2, 6, 6, 6, 6);       

        elementsPanel.addTab("Component instance date", attrPanel);
        
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
                            
                            if(element.equals("value")) {
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
            
            menu.add(item); 

            item = new JMenuItem("Edit " + listElementsLabels[i]);
            item.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    Object obj = list.getSelectedValue();
                    if(obj == null) return;

                    try {
                        XMLPanel panel = (XMLPanel) elementPanel.newInstance();
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

                        if(element.equals("value")) {
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
               boolean changed = false;
               
               // test for auto-instantiation
               String aname = attributeElementsTF[1].getText();
               String libloc = attributeElementsTF[2].getText();
                       
               DesignRepository rep = ((ArtifactEditor)artifactEditor).getDesignRepository();
               Design d = rep.findAndParseLib(libloc);
               if(d == null) {
                   JOptionPane.showMessageDialog(owner, "Could not find library:\n" + libloc);
                   return;
               }
               Design d2 = ((CompositeComponentEditor) artifactEditor).getDesign();
               CompositeComponent ca = ((CompositeComponentEditor) artifactEditor).getComponentBeingEdited();
               if(d.getDesignName().equals(d2.getDesignName()) &&
                  d.getDesignLocation().equals(d2.getDesignLocation()) &&
                  aname.equals(ca.getRootNode().getName())) {
                   ok = false;
                   msg = "Cannot instanciate itself!";
               }
  
               if(ok) {
                   for(i = 0;i < attributeElements.length;i++) {
                       if(attributeElementsTF[i].getText().matches("[\\s]*")) {
                          ok = false;
                          msg =  new String("Attribute " + attributeElements[i] +
                           " has incorrect value = " + attributeElementsTF[i].getText()); 
                          break;
                       }
                       Object valueBefore = dataWarper.getAttribute(attributeElements[i]);
                       if(valueBefore != null && valueBefore.equals(attributeElementsTF[i].getText())) {
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

                       if(attributeElements[i].equals("compName") || 
                               attributeElements[i].equals("libraryURL")) {
                           changed = true;
                       }
                   }
               }

               if(ok && changed) {
                   // Estando tudo correto, falta carregar a copia do ator da
                   // instancia, e associala com o setComponent
                   CompInstance ains = (CompInstance) dataWarper.getDataElement();

                   d = rep.findAndParseLib(ains.getLibraryURL());
                   if(d == null) {
                       msg = "Could not find library " + ains.getLibraryURL();
                       ok = false;
                   }
                   else {
                       Artifact art = d.getArtifact(ains.getComponentName());
                       if(art == null) {
                           ok = false;
                           msg = "Library at " + ains.getLibraryURL() + " does not " +
                                   "contain component " + ains.getComponentName();
                       }
                       else {
                           if(art.getType() == ElementType.ATOMICCOMP) {
                               Acomp comp = ((AtomicComponent)art).getNewInstance();
                               ains.setComponent(comp);
                           }
                           else {
                               Topology top = ((CompositeComponent)art).getNewInstance();
                               ains.setComponent(top);
                           }
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
 
    private String getLibName(String location) {
        StringTokenizer tk = new StringTokenizer(location, "/");
        String file = null;
        while(tk.hasMoreTokens()) {
            file = tk.nextToken();
        }
        return file;
    }
    
    private TransactionManagerContainer artifactEditor;    
}