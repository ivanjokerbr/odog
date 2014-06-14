/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor;

import odog.design.DesignConfiguration;
import odog.editor.configuration.RuleConfiguration;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import xmleditorgenerator.SpringUtilities;

/**
 *
 * @author ivan
 */
public class PickRuleConfigurationPanel extends JPanel {
    
    /** Creates a new instance of PickRuleConfigurationPanel */
    public PickRuleConfigurationPanel(JDialog owner, DesignConfiguration conf) {
        super();
        ok = false;
        
        createPanel(owner, conf);
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////

    public boolean isOk() {
        return ok;
    }
    
    public RuleConfiguration getSelectedRuleConfiguration() {
        return (RuleConfiguration) list.getSelectedValue();
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    private void createPanel(final JDialog owner, DesignConfiguration conf) {
        setLayout(new BorderLayout());

	JPanel elementsPanel = new JPanel();
        SpringLayout elementsLayout = new SpringLayout();
        elementsPanel.setLayout(elementsLayout); 
                
        list = new JList(conf.ruleConfigurationArray());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JLabel label = new JLabel("rule configurations");
        label.setLabelFor(list);
        
        elementsPanel.add(label);
        elementsPanel.add(list);
     
        // Faz o layout
        SpringUtilities.makeCompactGrid(elementsPanel,1, 2, 6, 6, 6, 6);       

        add(elementsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();        
        JButton commit = new JButton("commit");
        commit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               ok = true;
               owner.dispose();
            }
        });
        buttonPanel.add(commit);

        JButton cancel = new JButton("cancel");        
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ok = false;
                owner.dispose();
            }
        });
        buttonPanel.add(cancel);

        add(buttonPanel, BorderLayout.SOUTH);   
    }
    
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    private boolean ok;
    private JList list;
}
