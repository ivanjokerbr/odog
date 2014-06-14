/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor;

import odog.configuration.BaseConfiguration;
import odog.design.Artifact;
import odog.design.Artifact.ElementType;
import odog.design.Design;
import odog.design.DesignRepository;
import odog.design.MetaArtifactStatus;
import odog.design.MetaArtifactStatusListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * The implementation of this class in no good, since it reloads all designs for 
 * their respective tree , each time it is created. It would be better to create
 * this object only once, and each time it is required to be visible, update the
 * trees of the libraries. This wold need a synchronization with the designs being
 * edited by the OdogEditor object.
 *
 * @author  ivan
 */
public class LibraryViewerFrame extends javax.swing.JDialog implements 
        MetaArtifactStatusListener {
    
    /** Creates new form LibraryViewerFrame */
    public LibraryViewerFrame(BaseConfiguration configuration, DesignRepository repository,
            List<Design> environmentLibs) {
        treeSelectionModel = new DefaultTreeSelectionModel();
        treeSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        initComponents();

        setModal(true);
        urlDesignMap = new Hashtable<String, Design>();
        ok = false;

        artifactTree.setCellRenderer(new ProjectTreeRenderer());
        this.configuration = configuration;

        displayingTreeMap = new Hashtable<Design,DefaultMutableTreeNode>();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) artifactTree.getModel().getRoot();
        for(Design lib : environmentLibs) {
            DefaultMutableTreeNode n = copyTree(lib.getTreeView());
            root.add(n);
            displayingTreeMap.put(lib, n);
            lib.getStatus().addStatusListener(this);
        }
        
        designRepository = repository;
    }

    public void statusChanged(MetaArtifactStatus status) {        
        Design design = (Design) status.getContainer();
        DefaultMutableTreeNode old = displayingTreeMap.get(design);

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) artifactTree.getModel().getRoot();
        root.remove(old);

        DefaultMutableTreeNode n = copyTree(design.getTreeView());
        root.add(n);
        displayingTreeMap.put(design, n);

        ((DefaultTreeModel)artifactTree.getModel()).reload();
        repaint();
    }
    
    // For each design under the projects three, its directory must be also
    // included in the list.
    public void addDesignDirectory(String dir) {
        addURLDir(dir);
    }

    public void removeDesignDirectory(String dir) {
        remURLDir(dir);
    }

    // This method will retrive the libraries added via this frame, so that their
    // artifact tree is shown in the frame.
    public void loadState() {
        String file = configuration.getOdogWorkspace() + "libraryIndex.dat";
        try {
             File f = new File(file);
             FileInputStream fis = new FileInputStream(f);
             DataInputStream dis = new DataInputStream(fis);

             DefaultListModel lm = (DefaultListModel) libraryURLList.getModel();
             DefaultMutableTreeNode root = (DefaultMutableTreeNode) artifactTree.getModel().getRoot();

             int size = dis.readInt();
             for(int i = 0;i < size;i++) {
                  String dname = dis.readUTF();
 
                  Design d = designRepository.loadDesignFromFile(dname, true);
                  if(d == null) continue;

                  lm.addElement(dname);
                  DefaultMutableTreeNode n = copyTree(d.getTreeView());
                  root.add(n);
                  d.getStatus().addStatusListener(this);
                  displayingTreeMap.put(d, n);
                  urlDesignMap.put(dname, d);

                  String dir = (new File(dname)).getParentFile().toString();        
                  designRepository.addExtraDir(BaseConfiguration.appendSlash(dir));
             }
         }
         catch(FileNotFoundException t) {
                // sem a arvore de projetos, deve ter sido inicializado pela primeira
                // vez
         }
        catch(Exception ex) {
            System.out.println(ex);
        }
        ((DefaultTreeModel)artifactTree.getModel()).reload();
    }

    // Save the url of each library added by the user in this frame
    public void saveState() {
        String file = configuration.getOdogWorkspace() + "libraryIndex.dat";
        try {
             File f = new File(file);
             FileOutputStream fos = new FileOutputStream(f);
             DataOutputStream dos = new DataOutputStream(fos);
             
             DefaultListModel lm = (DefaultListModel) libraryURLList.getModel();
             
             dos.writeInt(lm.size());             
             for(int i = 0;i < lm.size();i++) {
                dos.writeUTF((String)lm.getElementAt(i));
             }

             dos.close();
             fos.close();
        }
        catch(IOException ex) {
            System.out.println(ex);
        }
    }
    
    public boolean isOk() {
        return ok;
    }
    
    public String getSelectedComponentName() {
        if(!ok) return null;
        
        TreePath path = artifactTree.getSelectionPath();
        if(path == null) return null;
        
        DefaultMutableTreeNode last = (DefaultMutableTreeNode) path.getLastPathComponent();
        
        Object obj = last.getUserObject();
        if(obj == null || !(obj instanceof Artifact)) return null;
        
        Artifact a = (Artifact) obj;
        if(a.getType() != ElementType.RULE) {
            return a.getName();
        }
        else {
            JOptionPane.showMessageDialog(this, "Selected artifact is not an component.");
        }

        return null;
    }
    
    public String getSelectedRuleName() {
        if(!ok) return null;
        
        TreePath path = artifactTree.getSelectionPath();
        if(path == null) return null;
        
        DefaultMutableTreeNode last = (DefaultMutableTreeNode) path.getLastPathComponent();
        
        Object obj = last.getUserObject();
        if(obj == null || !(obj instanceof Artifact)) return null;
        
        Artifact a = (Artifact) obj;
        if(a.getType() == ElementType.RULE) {
            return a.getName();
        }
        else {
            JOptionPane.showMessageDialog(this, "Selected artifact is not a rule.");
        }
        return null;
    }
    
    public String getSelectedLibraryURL() {
        if(!ok) return null;

        TreePath path = artifactTree.getSelectionPath();
        if(path == null) return null;
        
        DefaultMutableTreeNode last = (DefaultMutableTreeNode) path.getLastPathComponent();
        
        Object obj = last.getUserObject();
        if(obj == null || !(obj instanceof Artifact)) return null;
        
        Artifact a = (Artifact) obj;
        // the parent of an artifact is allways a design
        Design d = (Design) a.getParent();
        
        return d.getDesignName() + ".xml";  
    }
    
    ////////////////////////////// PRIVATE METHODS /////////////////////////////    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        libraryURLList = new javax.swing.JList();
        addLibButton = new javax.swing.JButton();
        remLibButton = new javax.swing.JButton();
        browseLibButton = new javax.swing.JButton();
        libraryURLTextField = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        artifactTree = new javax.swing.JTree();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        okButton.setText("Ok");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jSplitPane1.setDividerLocation(500);
        libraryURLList.setModel(new DefaultListModel());
        libraryURLList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(libraryURLList);

        addLibButton.setText("Add library");
        addLibButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addLibButtonActionPerformed(evt);
            }
        });

        remLibButton.setText("Remove library");
        remLibButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                remLibButtonActionPerformed(evt);
            }
        });

        browseLibButton.setText("Browse");
        browseLibButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLibButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addLibButton, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(remLibButton)
                .addContainerGap(58, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(browseLibButton)
                .addGap(18, 18, 18)
                .addComponent(libraryURLTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(browseLibButton)
                    .addComponent(libraryURLTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addLibButton)
                    .addComponent(remLibButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 384, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jSplitPane1.setRightComponent(jPanel1);

        artifactTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Artifacts")));
        artifactTree.setSelectionModel(treeSelectionModel);
        jScrollPane3.setViewportView(artifactTree);

        jSplitPane1.setLeftComponent(jScrollPane3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(368, Short.MAX_VALUE)
                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton)
                .addGap(315, 315, 315))
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 837, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void remLibButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_remLibButtonActionPerformed
        String url = (String) libraryURLList.getSelectedValue();
        if(url == null || url.equals("")) return;

        remURLDir(url);
    }//GEN-LAST:event_remLibButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        ok = false;
    }//GEN-LAST:event_formWindowClosing

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        setVisible(false);
        ok = true;
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);
        ok = false;
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void addLibButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addLibButtonActionPerformed
        String url = (String) libraryURLTextField.getText();
        if(url == null || url.equals("") || url.matches("[\\s]*")) return;

        addURLDir(url);
    }//GEN-LAST:event_addLibButtonActionPerformed

    private void addURLDir(String url) {
        Design d = designRepository.loadDesignFromFile(url, true);
        if(d == null) {
            JOptionPane.showMessageDialog(this, "URL " + url +
                    "\ndoes not contains a valid library file.");
            return;
        }

        DefaultListModel lm = (DefaultListModel) libraryURLList.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) artifactTree.getModel().getRoot();
        
        // 1. add the url of the library in the list box
        lm.addElement(url);
        
        // 2. add the tree of the design to the lib tree
        DefaultMutableTreeNode n = copyTree(d.getTreeView());
        root.add(n);        
        displayingTreeMap.put(d, n);
        
        // 3. associates the url with the design
        urlDesignMap.put(url, d);
        
        // 4. adds this frame as a listener of changes of the design
        d.getStatus().addStatusListener(this);

        // 5. adds the directory of the design to the repository
        String dir = (new File(url)).getParentFile().toString();        
        designRepository.addExtraDir(BaseConfiguration.appendSlash(dir));

        libraryURLTextField.setText("");
        ((DefaultTreeModel)artifactTree.getModel()).reload();
    }

    private void remURLDir(String url) {
        // 1. Remove the library path from the list of paths
        DefaultListModel lm = (DefaultListModel) libraryURLList.getModel();
        lm.removeElement(url);

        // 2. remove the design tree
        Design d = urlDesignMap.get(url);
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) artifactTree.getModel().getRoot();
        
        DefaultMutableTreeNode old = displayingTreeMap.get(d);
        root.remove(old);
        d.getStatus().removeStatusListener(this);
        
        // 3. update the url -> design map
        urlDesignMap.remove(url);

        // 4. remove the directory associated with the file from the repository
        String dir = (new File(url)).getParentFile().toString();        
        designRepository.removeExtraDir(BaseConfiguration.appendSlash(dir));

        ((DefaultTreeModel)artifactTree.getModel()).reload();
    }
    
    private void browseLibButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseLibButtonActionPerformed
        JFileChooser chooser = new JFileChooser(configuration.getOdogWorkspace());
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().toString();
            libraryURLTextField.setText(path);
        }
    }//GEN-LAST:event_browseLibButtonActionPerformed
         
    private DefaultMutableTreeNode copyTree(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode ret = (DefaultMutableTreeNode) root.clone();
        Enumeration children = root.children();
        while(children.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            ret.add(copyTree(child));
        }
        return ret;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addLibButton;
    private javax.swing.JTree artifactTree;
    private javax.swing.JButton browseLibButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JList libraryURLList;
    private javax.swing.JTextField libraryURLTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JButton remLibButton;
    // End of variables declaration//GEN-END:variables

    private BaseConfiguration configuration;
    private Hashtable<String, Design> urlDesignMap;
    private Hashtable<Design, DefaultMutableTreeNode> displayingTreeMap;
    private DesignRepository designRepository;    
    private DefaultTreeSelectionModel treeSelectionModel;

    private boolean ok;
}