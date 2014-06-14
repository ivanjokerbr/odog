/*
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2
 * see LICENSE file
 */
package odog.editor.ArtifactEditors;

import odog.syntax.Nodes.Node;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import xmleditorgenerator.SpringUtilities;

public class NodeSelectionDialog extends JDialog {

    public NodeSelectionDialog(NodeDisplayList list, boolean fver, String nodeLabel) {
        nodeList = list;
        setModal(true);
        setBounds(200,200, 500, 400);

        fromVersion = fver;

        JPanel elementsPanel = new JPanel();
        SpringLayout elementsLayout = new SpringLayout();
        elementsPanel.setLayout(elementsLayout);

        JLabel label = new JLabel(nodeLabel);
        JList plist = list.getList(fromVersion);
        plist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        label.setLabelFor(plist);

        elementsPanel.add(label);
        elementsPanel.add(new JScrollPane(plist));

        JButton ok = new JButton("Commit");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               isOk = true;                   
               dispose();
            }
        });

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               isOk = false;
               dispose();
            }
        });

        elementsPanel.add(ok);
        elementsPanel.add(cancel);

        SpringUtilities.makeCompactGrid(elementsPanel, 
            2, 2, 6, 6, 6, 6);
        add(elementsPanel);
    }

    public boolean isOk() {
        return isOk;
    }

    public Node getSelectedElement() {
        JList list = nodeList.getList(fromVersion);
        return (Node) list.getSelectedValue();
    }

    private NodeDisplayList nodeList;
    private boolean fromVersion;
    private boolean isOk;
}