/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors;

import odog.syntax.Nodes.Acomp;
import odog.syntax.Nodes.CompInstance;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.Hver;
import odog.syntax.Nodes.Topology;
import odog.syntax.Nodes.Ver;
import odog.syntax.Nodes.VersionBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import javax.swing.DefaultListModel;
import javax.swing.JList;

/**
 *  When editing an atomic component, or topology, will mantain a list of available attributes.
 *
 * @author ivan
 */
public class AttributeDisplayList implements NodeDisplayList {
    
    public AttributeDisplayList(Acomp comp) {
        /*model = new DefaultListModel();
        dataList = new ArrayList();
        
        Iterator ite = comp.getAllRecomputedAttributes();
        while(ite.hasNext()) {
            Attr at = (Attr) ite.next();
            if(at.getName().equals("portType") && (at.getContainer() instanceof Dport)) {
                continue;
            }
            if(!at.getClassification().isVisible() || !at.getClassification().hasData()) {
                continue;
            }
            dataList.add(at);
        }
        Collections.sort(dataList);

        for(int i = 0;i < dataList.size();i++) {
            model.addElement(dataList.get(i));
        }

        addCounter = 0;
        removeCounter = 0;
        list = new JList(model);
        */
        isTopology = false;
        this.component = comp;
    }
    
    public AttributeDisplayList(Topology comp) {
        /*model = new DefaultListModel();
        dataList = new ArrayList();
        
        Iterator ite = comp.getAllRecomputedAttributes();
        while(ite.hasNext()) {            
            Attr at = (Attr) ite.next();
            if(at.getName().equals("portType") && (at.getContainer() instanceof Dport)) {
                continue;
            }
            if(!at.getClassification().hasData()) {
                continue;
            }
            dataList.add(at);
        }

        Collections.sort(dataList);
        
        for(int i = 0;i < dataList.size();i++) {
            model.addElement(dataList.get(i));
        }
        
        addCounter = 0;
        removeCounter = 0;
        //box = new JComboBox(model);
        list = new JList(model);*/
        
        isTopology = true;
        top = comp;
    }

    /////////////////////////////// PUBLIC METHODS /////////////////////////////
    
    public JList getList(boolean flag) {
        return list;
    }

    public void updateAttributes(NodeTransactionManager manager, 
            CompInstance ains, VersionBase version) {
        if(isTopology) {  
            model = new DefaultListModel();
            dataList = new ArrayList();

            Iterator ite;
            if(ains == null) {
                ite = top.getAllRecomputedAttributes((Hver)version).iterator();
            }
            else {
                ite = ains.getInternalInterfaceAttributes().iterator();
            }
            
            while(ite.hasNext()) {            
                Attr at = (Attr) ite.next();            
                // Changed to allow for changing the type of a port by a version
                //if(at.getName().equals("portType") && (at.getContainer() instanceof Dport)) {
                //    continue;
                //}
                if(!at.getClassification().hasData()) {
                    continue;
                }
                dataList.add(at);
            }

            Collections.sort(dataList);

            for(int i = 0;i < dataList.size();i++) {
                model.addElement(dataList.get(i));
            }
            list = new JList(model);
        }
        else {
            model = new DefaultListModel();
            dataList = new ArrayList();

            Iterator ite;
            if(ains == null) {
                ite = component.getAllRecomputedAttributes((Ver) version).iterator();
            }
            else {
                ite = ains.getInternalInterfaceAttributes().iterator();
            }
            while(ite.hasNext()) {
                Attr at = (Attr) ite.next();
                //if(at.getName().equals("portType") && (at.getContainer() instanceof Dport)) {
                //    continue;
               // }
                if(!at.getClassification().hasData()) {
                    continue;
                }
                dataList.add(at);
            }
            Collections.sort(dataList);

            for(int i = 0;i < dataList.size();i++) {
                model.addElement(dataList.get(i));
            }
            list = new JList(model);
        }

        // due to bugs, this code is unavailable       
        /*
        List <Transaction> added = manager.getAllTransactions(TransactionType.ADD_ELEMENT, 
                Node.ATTR, addCounter);
        List <Transaction> removed = manager.getAllTransactions(TransactionType.REMOVE_ELEMENT, 
                Node.ATTR, removeCounter);

        // 1. primeiro remove da lista de adicionados os removidos. Percorre primeiro essa
        // lista, pois ela eh menor que a de atributos do box.
        int rmcounter = removed.size();
        for(Transaction rm : removed) {
            for(Transaction add : added) {
                if(rm.getTarget() == add.getTarget()) {
                    add.setTarget(null); // marca como apagado 
                    rm.setTarget(null);
                    rmcounter--;
                    break;
                }
            }
        }

        // 2. se ainda existirem remocoes que nao foram processadas, verifica
        // a lista inicial de atributos.
        if(rmcounter > 0) {
            for(Transaction rm : removed) {
                if(rm.getTarget() == null) continue;

                model.removeElement(rm.getTarget());
                dataList.remove(rm.getTarget());
            }
        }

        // 3. para os elementos adicionados e nao removidos, coloca na lista de atributos
        for(Transaction add : added) {
            if(add.getTarget() != null) {
                Attr at = (Attr) add.getTarget();
                if(at.getClassification().isVisible() && at.getClassification().hasData()) {
                    dataList.add(add.getTarget());                                        
                }
            }
        }
        
        Collections.sort(dataList);
        
        for(Transaction add : added) {
            if(add.getTarget() != null) {
                Attr at = (Attr) add.getTarget();
                if(at.getClassification().isVisible() && at.getClassification().hasData()) {
                    box.insertItemAt(at,dataList.indexOf(at));
                }
            }
        }
                    
        addCounter = manager.lastAddTransaction();
        removeCounter = manager.lastRemoveTransaction();
         */
    }
  
    /*
    public void removeAttribute(Attr attribute) {
        model.removeElement(attribute);
        dataList.remove(attribute);
    }
    
    public void addAttribute(Attr attribute) {
        if(!model.contains(attribute)) {
            dataList.add(attribute);
            Collections.sort(dataList);            
            model.add(dataList.indexOf(attribute), attribute);
        }
    }
*/
    /////////////////////////////// PRIVATE METHODS ////////////////////////////
    
    
    /////////////////////////////// PRIVATE VARIABLES //////////////////////////

    //private JComboBox box;
    private JList list;
    
    private DefaultListModel model;
    //private DefaultComboBoxModel model;
    private ArrayList dataList;

    private int addCounter;
    private int removeCounter;
 
    private boolean isTopology;
    private Acomp component;
    private Topology top;

}