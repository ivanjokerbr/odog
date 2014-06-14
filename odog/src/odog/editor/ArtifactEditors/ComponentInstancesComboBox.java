/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors;

import odog.syntax.Nodes.CompInstance;
import odog.syntax.Nodes.Hver;
import odog.syntax.Nodes.Topology;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 *
 * @author ivan
 */
public class ComponentInstancesComboBox {
    
    /** Creates a new instance of ComponentInstancesComboBox */
    public ComponentInstancesComboBox(Topology top) {
        topology = top;
        
        allModel = new DefaultComboBoxModel();
        allList = new ArrayList();

        // Percorre as instacias da topologia
        Iterator ite = topology.componentInstancesIterator();
        while(ite.hasNext()) {
            CompInstance ains = (CompInstance) ite.next();
            allList.add(ains);
        }
        Iterator vite = topology.versionsIterator();
        while(vite.hasNext()) {
            Hver ver = (Hver) vite.next();
            Iterator ainsite = ver.componentInstancesIterator();
            while(ainsite.hasNext()) {
                allList.add(ainsite.next());
            }
        }

        Collections.sort(allList);

        for(int i = 0;i < allList.size();i++) {
            allModel.addElement(allList.get(i));
        }
        
        addCounter = 0;
        removeCounter = 0;

        allBox = new JComboBox(allModel);
    }
 
    /////////////////////////////// PUBLIC METHODS /////////////////////////////
    
    public JComboBox getBox() {
         return allBox;
    }

    public void update(NodeTransactionManager manager) {
        allModel = new DefaultComboBoxModel();
        allList = new ArrayList();

        // Percorre as instacias da topologia
        Iterator ite = topology.componentInstancesIterator();
        while(ite.hasNext()) {
            CompInstance ains = (CompInstance) ite.next();
            allList.add(ains);
        }
        Iterator vite = topology.versionsIterator();
        while(vite.hasNext()) {
            Hver ver = (Hver) vite.next();
            Iterator ainsite = ver.componentInstancesIterator();
            while(ainsite.hasNext()) {
                allList.add(ainsite.next());
            }
        }

        Collections.sort(allList);

        for(int i = 0;i < allList.size();i++) {
            allModel.addElement(allList.get(i));
        }
        
        allBox = new JComboBox(allModel);
        
    }

    /////////////////////////////// PRIVATE METHODS ////////////////////////////
    
    
    /////////////////////////////// PRIVATE VARIABLES //////////////////////////
    
    // Inclui todos os portos, de instancias basicas e de versoes, alem de processar
    // os portos adicionados nas versoes destas instancias.
    private JComboBox allBox;
    private DefaultComboBoxModel allModel;
    private ArrayList allList;
    
    private int addCounter;
    private int removeCounter;
    
    private Topology topology;
}
