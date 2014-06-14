/*
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2
 * see LICENSE file
 */
package odog.editor;

import odog.configuration.BaseConfiguration;
import odog.design.Design;
import odog.design.DesignParser;
import odog.design.DesignRepository;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class TreeState {
    
    public TreeState(String name, JTree tree, BaseConfiguration configuration,
            DesignRepository designRepository) {
        root = new DefaultMutableTreeNode("Projects");
        treeModel = new DefaultTreeModel(root);
        tree.setModel(treeModel);

        designs = new LinkedList<Design>();
        this.designRepository = designRepository;
        
        tree.setCellRenderer(new ProjectTreeRenderer());

        this.configuration = configuration;
        this.name = name;
        this.tree = tree;
    }

    public void reloadTree() {
        treeModel.reload();
    }
    
    public void reloadTree(DefaultMutableTreeNode node) {
        treeModel.reload(node);
    }

    public void repaintTree() {
        treeModel.nodeStructureChanged(root);
        
    }

    public boolean containsDesign(String fullPath) {
        for(Design x : designs) {
            if(new String(x.getDesignLocation() + x.getDesignName() + ".xml").equals(
                    fullPath)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean containsDesign(Design p) {
        for(Design x : designs) {
            if(x.getDesignLocation().equals(p.getDesignLocation())) return true;
        }
        return false;
    }

    public void addNewDesign(Design p) {
        designs.add(p);
        root.add(p.getTreeView());
    }

    public DefaultMutableTreeNode getCurrentSelectedNode() {
        DefaultMutableTreeNode n = (DefaultMutableTreeNode) 
            tree.getSelectionPath().getLastPathComponent();
        return n;            
    }

    public void removeCurrentSelectedDesign() {
        DefaultMutableTreeNode n = (DefaultMutableTreeNode) 
            tree.getSelectionPath().getLastPathComponent();
        root.remove(n);

        Design p = (Design) n.getUserObject();
        designs.remove(p);

        treeModel.reload();
    }

    public void removeDesign(Design p) {
        designs.remove(p);

        Enumeration c = root.children();
        while(c.hasMoreElements()) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) c.nextElement();
            Design d = (Design) n.getUserObject();
            if(d == p) {
                root.remove(n);
                break;
            }
        }
        treeModel.reload();
    }

    public void saveState() {
        // 1. fecha todos os editores dos projetos que estejam abertos
        for(Design d : designs) {
            d.closeAllArtifactEditors();
        }

        // 2. salva o estado de todos os projetos e da arvore de projetos
        String file = configuration.getOdogWorkspace() + name + ".treeState";
        try {
            File f = new File(file);
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            DataOutputStream dos = new DataOutputStream(fos);

            dos.writeUTF("tree_state_file");
            dos.writeInt(designs.size());

            int count = 0;
            for(Design d : designs) {
                dos.writeUTF(d.getDesignName());
                dos.writeUTF(d.getDesignLocation());
                count++;
            }

            dos.close();
            fos.close();

            // Salva cada um dos designs modificados
            for(Design d : designs) {
                if(d.getStatus().changedStatus()) {
                    d.saveDesign();
                }                
            }
        }
        catch(IOException ex) {
            System.out.println(ex);
        }
    }

    public void loadState() {
        String file = configuration.getOdogWorkspace() + "" + name + ".treeState";
        try {
             File f = new File(file);
             FileInputStream fis = new FileInputStream(f);
             DataInputStream dis = new DataInputStream(fis);

             String s = dis.readUTF();
             if(!"tree_state_file".equals(s)) return;

             int size = dis.readInt();
             for(int i = 0;i < size;i++) {
                 String dname = dis.readUTF();
                 String dloc = dis.readUTF();

                 Design d = new Design();

                 d.setDesignName(dname);
                 d.setDesignLocation(dloc);

                 // se nao houver nenhum projeto, pois alguem apagou os arquivos
                 // por fora
                 File fd = new File(d.getDesignLocation() + d.getDesignName() + ".xml");
                 if(!fd.exists()) continue;     

                 // carega o xml do design em si, aqui nao pode haver erro
                 // pois os xml eh produzido pelo save state
                 if(!DesignParser.parseDesign(d, true, designRepository)) {
                     System.out.println(DesignParser.errorMessage);
                 }

                 // tem que vir apos o parse do design, pois na arvore, precisa saber
                 // se eh biblioteca ou sistema
                 addNewDesign(d);
                 designRepository.addDesign(d, d.getDesignLocation() + d.getDesignName() +
                         ".xml");
             }
        }
        catch(FileNotFoundException t) {
            // sem a arvore de projetos, deve ter sido inicializado pela primeira
            // vez
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        treeModel.reload();
    }       
    
    private LinkedList<Design> designs;

    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode root;
    private JTree tree;

    private BaseConfiguration configuration;
    private DesignRepository designRepository;
    
    private String name;
}