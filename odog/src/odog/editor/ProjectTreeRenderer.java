/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor;

import odog.design.Artifact;
import odog.design.Design;
import odog.design.AtomicComponent;
import odog.design.CompositeComponent;
import odog.design.RuleCheckingStatus;
import odog.design.RuleElement;
import java.awt.Component;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *  Renderer for the tree of meta-artifacts and artifacts. See class OdogEditor
 * @author ivan
 */
public class ProjectTreeRenderer extends DefaultTreeCellRenderer {
   
    public ProjectTreeRenderer() {
        baseFont = getFont();
    }

    public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
                row, hasFocus);
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object obj = node.getUserObject();
        setText(obj.toString());
        
        setFont(baseFont);

        if(obj instanceof String) {
            if(expanded) {
                setIcon(openRootIcon);
            }
            else {
                setIcon(closedRootIcon);
            }
        }
        else
        if(obj instanceof Design) {
            Design d = (Design) obj;
            if(d.getConfiguration().isLibrary()) {
                setIcon(libraryIcon);   
            }
            else {
                setIcon(projectIcon);
            }
            
            if(d.getStatus().changedStatus()) {
                setFont(new Font(getFont().getName(), Font.ITALIC + Font.BOLD, 
                        getFont().getSize()));
            }
        }
        else
        if(obj instanceof Artifact) {
            Artifact a = (Artifact) obj;
            String s = a.toString();

            if(a.getStatus().changedStatus()) {
                setFont(new Font(getFont().getName(), Font.ITALIC + Font.BOLD, 
                        getFont().getSize()));
            }

            if(a instanceof AtomicComponent) {
                setIcon(atomicComponentIcon);
                RuleCheckingStatus rs = ((AtomicComponent)a).getRuleCheckingStatus();
                if(rs.wasChecked()) {
                    if(rs.hasPassed()) {
                        s = s + " (check passed)";
                    }
                    else {
                        s = s + " (check fail)";
                    }
                }
                else {
                    s = s + " (not checked)";
                }
            }
            else
            if(a instanceof CompositeComponent) {
                setIcon(compositeComponentIcon);
                RuleCheckingStatus rs = ((CompositeComponent)a).getRuleCheckingStatus();
                if(rs.wasChecked()) {
                    if(rs.hasPassed()) {
                        s = s + " (check passed)";
                    }
                    else {
                        s = s + " (check fail)";
                    }
                }
                else {
                    s = s + " (not checked)";
                }
            }
            else
            if(a instanceof RuleElement) {
                setIcon(ruleIcon);
            }
            setText(s);
        }
        return this;
    } 

    private Font baseFont;

    private final ImageIcon closedRootIcon = new ImageIcon(getClass().getResource(
            "/odog/icons/folder.png"));
    private final ImageIcon openRootIcon = new ImageIcon(getClass().getResource(
            "/odog/icons/openfolder.png"));
    private final ImageIcon projectIcon = new ImageIcon(getClass().getResource(
            "/odog/icons/project.png"));
    private final ImageIcon atomicComponentIcon = new ImageIcon(getClass().getResource(
            "/odog/icons/atomiccomponent.png"));
    private final ImageIcon compositeComponentIcon = new ImageIcon(getClass().getResource(
            "/odog/icons/compositecomponent.png"));
    private final ImageIcon ruleIcon = new ImageIcon(getClass().getResource(
            "/odog/icons/ruleicon.png"));
    private final ImageIcon libraryIcon = new ImageIcon(getClass().getResource(
            "/odog/icons/libraryicon.png"));
}
