/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.design;

import odog.editor.ArtifactEditors.ArtifactEditor;
import odog.editor.ArtifactEditors.ArtifactEditor.EditorType;
import odog.ruleChecker.Rule;
import odog.syntax.Nodes.Acomp;
import odog.syntax.Nodes.Topology;
import odog.syntax.Parse.AtomicComponentParser;
import odog.syntax.Parse.TopologyParser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *  Design is a group of artifacts. A design can be a library or a project. The difference is
 *  that code generation can not be applied to a library.
 *
 *  A design has associated information:
 *
 *      * configuration
 *      * status
 *      * a view as a tree
 *      * name and the directory were all its data are located in the machine
 *
 *  At any time, a design has a list of the artifacts being edited and viewed.
 *
 * @author ivan
 */
public class Design extends MetaArtifact implements MetaArtifactStatusListener {

    public Design() {
        super();
        artifactsBeingEdited = new Hashtable<Artifact, ArtifactEditor>();
        tree = new DefaultMutableTreeNode(this);

        parent = null;
    }

    public Design(boolean isLib) {
        this();
        configuration = new DesignConfiguration(isLib);
    }

    /** Name is the name of the project. Locations is the directory. The xml containing
     *  de persistent data is location/name.xml */
    public Design(String name, String location, boolean isLib) {
        this(isLib);
        designName = name;
        designLocation = location;
    }

    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    // In several situations, the path of an object must be contained witin a 
    // based path (prefix to it). This method will test if the absolute path
    // respects this rule, and return the remaining of the path. Therefore
    // basePath + return = absolutePath
    public static String subtractPath(String absolutePath, String basePath) {
        if (!absolutePath.startsWith(basePath)) {
            return null;
        }
        return absolutePath.substring(basePath.length(), absolutePath.length());
    }

    @Override
    public String toString() {
        return designName;
    }

    @Override
    public String exportXML() {
        StringBuffer buf = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE design PUBLIC \"-//ODOGDESIGN//DTD//EN\" " +
                "\"\">");

        buf.append("<design name=\"" + designName + "\">\n");
        buf.append(configuration.exportXML());
        for (MetaArtifact a : children) {
            buf.append(a.exportXML());
        }
        buf.append("</design>\n");

        return buf.toString();
    }

    public void setDesignName(String dname) {
        designName = dname;
    }

    public String getDesignName() {
        return designName;
    }

    public void setDesignLocation(String dloc) {
        designLocation = dloc;
    }

    public String getDesignLocation() {
        return designLocation;
    }

    public void statusChanged(MetaArtifactStatus statusChanged) {
        status.signalChanged();
    }
    
    // Adds a component or rule ro the list of meta artifacts of this design
    public boolean addArtifact(Artifact art) {
        for (MetaArtifact at : children) {
            if (!(at instanceof Artifact)) {
                continue;
            }
            if (((Artifact) at).getName().equals(art.getName())) {
                return false;
            }
        }
        children.add(art);
        art.setParent(this);

        DefaultMutableTreeNode n = new DefaultMutableTreeNode(art);
        tree.add(n);

        status.signalChanged();
        art.getStatus().addStatusListener(this);

        return true;
    }

    // The artifact must be directly linked to this design
    public void removeArtifact(Artifact art) {
        // if this artifact was being edited, then the respective editor
        // must already be destroyed

        // 1. atualiza a arvore removendo o no desse artefato
        Enumeration tnodes = tree.children();
        while (tnodes.hasMoreElements()) {
            DefaultMutableTreeNode ch = (DefaultMutableTreeNode) tnodes.nextElement();
            if (ch.getUserObject() == art) {
                tree.remove(ch);
            }
        }

        // 2. remove da lista
        children.remove(art);
        status.signalChanged();
    }

    public boolean containsArtifact(String name) {
        for (MetaArtifact art : children) {
            if (art instanceof Artifact) {
                Artifact artifact = (Artifact) art;
                if (artifact.getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Artifact getArtifact(String name) {
        for (MetaArtifact art : children) {
            if (art instanceof Artifact) {
                Artifact artifact = (Artifact) art;
                if (artifact.getName().equals(name)) {
                    return artifact;
                }
            }
        }
        return null;
    }

    public DefaultMutableTreeNode getTreeView() {
        return tree;
    }

    public void closeAllArtifactEditors() {
        Enumeration<Artifact> en = artifactsBeingEdited.keys();
        while (en.hasMoreElements()) {
            Artifact a = en.nextElement();
            closeArtifactEditor(a);
        }
    }

    public void closeArtifactEditor(ArtifactEditor ed) {
        Enumeration<Artifact> en = artifactsBeingEdited.keys();
        while (en.hasMoreElements()) {
            Artifact a = en.nextElement();
            if (artifactsBeingEdited.get(a).equals(ed)) {
                closeArtifactEditor(a);
            }
        }
    }

    public void closeArtifactEditor(Artifact art) {
        // 1. remove a janela do editor de artefato correspondente
        ArtifactEditor aed = artifactsBeingEdited.get(art);
        if (aed == null) {
            return;
        }

        aed.close();
        artifactsBeingEdited.remove(art);
    }

    public boolean isEditingArtifact(Artifact art) {
        return artifactsBeingEdited.containsKey(art);
    }

    public ArtifactEditor getArtifactEditor(Artifact art) {
        return artifactsBeingEdited.get(art);
    }

    public void addArtifactEditor(Artifact art, ArtifactEditor aed) {
        artifactsBeingEdited.put(art, aed);
    }

    public DesignConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(DesignConfiguration conf) {
        configuration = conf;
    }

    public void saveDesign() {
        try {
            File f = new File(designLocation + designName + ".xml");
            FileOutputStream fos = new FileOutputStream(f);
            PrintStream ps = new PrintStream(fos);

            ps.print(exportXML());

            ps.close();
            fos.close();

            for (Artifact a : artifacts()) {
                if (a.getStatus().changedStatus()) {
                    saveArtifact(a);
                }
            }

            getStatus().resetChanged();
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
    }

    /////////////////////////////// PROTECTED VARIABLES ////////////////////////
    
    // This is the only method that can reset the stauts of a meta-artifact
    private final void saveArtifact(Artifact artifact) throws IOException {
        StringBuffer buf = new StringBuffer();
        if (artifact.getEditorType() == EditorType.ATOMIC) {
            AtomicComponent aa = (AtomicComponent) artifact;
            Acomp comp = aa.getRootNode();
            if (comp == null) {
                System.out.println(AtomicComponentParser.errorMessage);
                return;
            }
            buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE atomicComponent PUBLIC \"-//ODOGSYNTAXATOMIC//DTD//EN\" " +
                    "\"\">");
            buf.append(comp.exportXML(0));
        } 
        else 
        if (artifact.getEditorType() == EditorType.COMPOSITE) {
            CompositeComponent ca = (CompositeComponent) artifact;
            Topology top = ca.getRootNode();
            if (top == null) {
                System.out.println(TopologyParser.errorMessage);
                return;
            }
            buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE topology PUBLIC \"-//ODOGSYNTAXTOPOLOGY//DTD//EN\" " +
                    "\"\">");
            buf.append(top.exportXML(0));
        } 
        else {
            RuleElement re = (RuleElement) artifact;
            Rule r = re.getRule();
            buf.append(r.getExpression());
        }

        File f = new File(designLocation + artifact.getURL().getFile());
        FileOutputStream fos = new FileOutputStream(f);
        PrintStream ps = new PrintStream(fos);

        ps.print(buf.toString());

        ps.close();
        fos.close();

        artifact.getStatus().resetChanged();
    }
    /////////////////////////////// PROTECTED VARIABLES ////////////////////////
    
    // uma associacao dos artefatos que estao sendo editados     
    private Hashtable<Artifact, ArtifactEditor> artifactsBeingEdited;
    
    // no que visualiza os elementos deste design na janela da interface
    private DefaultMutableTreeNode tree;
    
    private DesignConfiguration configuration;
    private String designName;
    private String designLocation;
}
