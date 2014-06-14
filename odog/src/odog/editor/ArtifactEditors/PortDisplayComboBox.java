/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors;

import odog.syntax.Nodes.CompBase;
import odog.syntax.Nodes.CompInstance;
import odog.syntax.Nodes.Hver;
import odog.syntax.Nodes.Topology;
import odog.syntax.Nodes.VersionBase;
import odog.syntax.Nodes.VirtualPort;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 *
 * Nesta versao, ha duas JComboBox. Uma eh utilizada para mostrar os portos quando
 * exportando a partir de uma topologia, e a outra exportando dentro de uma versao
 * de uma topologia. A diferenca eh necessaria devido ao numero diferente de portos
 * exportados vistos.
 *
 * @author ivan
 */
public class PortDisplayComboBox {
    
    /** Creates a new instance of PortDisplayComboBox */
    public PortDisplayComboBox(Topology top) {
       // allModel = new DefaultComboBoxModel();
        //basicInstancesModel = new DefaultComboBoxModel();
        this.top = top;
        
        allList = new ArrayList();
        basicInstancesList = new ArrayList();

        Class [] parameters = new Class[1];
        parameters[0] = Object.class;
        
        Method addToBasicList = null;
        Method addToAllList = null;

        try {
            addToBasicList = this.getClass().getMethod("addToBasicList", parameters);
            addToAllList = this.getClass().getMethod("addToAllList", parameters);
        }
        catch(Exception ex) {
            System.out.println(ex);
        }

        // Percorre as instacias da topologia
        Iterator ite = top.componentInstancesIterator();
        while(ite.hasNext()) {
            CompInstance ains = (CompInstance) ite.next();
            invokeOnVirtualPorts(ains, addToBasicList , addToAllList);    
        }   

        // agora percorre em todas as versoes da topologia, procurando
        // instancias de components e repetindo o processo
        ite = top.versionsIterator();
        while(ite.hasNext()) {
            Hver ver = (Hver) ite.next();
            Iterator aite = ver.componentInstancesIterator();
            while(aite.hasNext()) {
                CompInstance ains = (CompInstance) aite.next();
                invokeOnVirtualPorts(ains, null, addToAllList);
            }        
        }
        
        Collections.sort(allList);
        Collections.sort(basicInstancesList);
        
        allModel = new DefaultComboBoxModel(allList.toArray());
        basicInstancesModel = new DefaultComboBoxModel(basicInstancesList.toArray());
        
        addCounter = 0;
        removeCounter = 0;

        allBox = new JComboBox(allModel);
        basicInstancesBox = new JComboBox(basicInstancesModel);
    }
    
    /////////////////////////////// PUBLIC METHODS /////////////////////////////
    
     public JComboBox getBox(boolean allPorts) {
         if(allPorts) {
             return allBox;
         }
         return basicInstancesBox;
    }
    
    public void updatePorts(NodeTransactionManager manager) {
        allList = new ArrayList();
        basicInstancesList = new ArrayList();

        Class [] parameters = new Class[1];
        parameters[0] = Object.class;
        
        Method addToBasicList = null;
        Method addToAllList = null;

        try {
            addToBasicList = this.getClass().getMethod("addToBasicList", parameters);
            addToAllList = this.getClass().getMethod("addToAllList", parameters);
        }
        catch(Exception ex) {
            System.out.println(ex);
        }

        // Percorre as instacias da topologia
        Iterator ite = top.componentInstancesIterator();
        while(ite.hasNext()) {
            CompInstance ains = (CompInstance) ite.next();
            invokeOnVirtualPorts(ains, addToBasicList , addToAllList);    
        }   

        // agora percorre em todas as versoes da topologia, procurando
        // instancias de components e repetindo o processo
        ite = top.versionsIterator();
        while(ite.hasNext()) {
            Hver ver = (Hver) ite.next();
            Iterator aite = ver.componentInstancesIterator();
            while(aite.hasNext()) {
                CompInstance ains = (CompInstance) aite.next();
                invokeOnVirtualPorts(ains, null, addToAllList);
            }        
        }
        
        Collections.sort(allList);
        Collections.sort(basicInstancesList);
        
        allModel = new DefaultComboBoxModel(allList.toArray());
        basicInstancesModel = new DefaultComboBoxModel(basicInstancesList.toArray());
        
        allBox = new JComboBox(allModel);
        basicInstancesBox = new JComboBox(basicInstancesModel);       
    }

    /////////////////////////////// PRIVATE METHODS ////////////////////////////
    
    // para todos os portos virtuais de uma instancia, executa um metodo dado.
    private void invokeOnVirtualPorts(CompInstance ains, Method m1, Method m2) {
        Object [] args = new Object[1];
        
        CompBase abase = ains.getComponent();
        Iterator pite = abase.getPorts();
        while(pite.hasNext()) {
            VirtualPort vp = (VirtualPort) pite.next();
            args[0] = vp;   
            try {
                if(m1 != null) {
                    m1.invoke(this, args);
                }
                m2.invoke(this, args);
            }
            catch(Exception ex) {
                System.out.println(ex);
            }
        }

        Iterator vite = abase.versionsIterator();
        while(vite.hasNext()) {
            VersionBase v = (VersionBase) vite.next();
            pite = v.portsIterator();
            while(pite.hasNext()) {
                VirtualPort vp = (VirtualPort) pite.next();
                args[0] = vp;
                try {
                    m2.invoke(this, args);
                }
                catch(Exception ex) {
                    System.out.println(ex);
                }
            }
        }
    }

    public void addToBasicList(Object obj) {
        basicInstancesList.add(obj);
    }
    
    public void addToAllList(Object obj) {
        allList.add(obj);
    }

    public void removeFromList(Object obj) {
        VirtualPort vp = (VirtualPort) obj;
        
        basicInstancesModel.removeElement(vp);
        basicInstancesList.remove(vp);
        
        allModel.removeElement(vp);
        allList.remove(vp);
    }
    
    /////////////////////////////// PRIVATE VARIABLES //////////////////////////
    
    // Contem portos de instancias presentes na topologia, independente das versoes,
    // e somente presente em suas interfaces
    private JComboBox basicInstancesBox;
    private DefaultComboBoxModel basicInstancesModel;
    private ArrayList basicInstancesList;

    // Inclui todos os portos, de instancias basicas e de versoes, alem de processar
    // os portos adicionados nas versoes destas instancias.
    private JComboBox allBox;
    private DefaultComboBoxModel allModel;
    private ArrayList allList;
    
    private int addCounter;
    private int removeCounter;

    private Topology top;
}
