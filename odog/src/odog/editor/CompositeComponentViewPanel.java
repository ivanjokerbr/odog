/*
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2
 * see LICENSE file
 */
package odog.editor;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.contrib.KKLayout;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 *
 * @author ivan
 */
public class CompositeComponentViewPanel extends JPanel {
    
    public CompositeComponentViewPanel(Graph graph) {
        final VisualizationViewer vv = new VisualizationViewer(
            new CompositeComponentLayout(graph),
            new CompositeComponentViewRenderer());
     
        final ScalingControl scaler = new CrossoverScalingControl();
        JButton plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1.1f, vv.getCenter());
            }
        });
        JButton minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1/1.1f, vv.getCenter());
            }
        });
        
        final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
        vv.setGraphMouse(graphMouse);

        JComboBox modeBox = graphMouse.getModeComboBox();
        modeBox.addItemListener(((DefaultModalGraphMouse)vv.getGraphMouse()).getModeListener());
        
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        add(vv, BorderLayout.CENTER);
     
        JPanel bottomControls = new JPanel();
        bottomControls.add(plus);
        bottomControls.add(minus);
        bottomControls.add(modeBox);
        add(bottomControls, BorderLayout.NORTH);
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
}
