/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors;

import odog.configuration.BaseConfiguration;
import odog.editor.ArtifactEditors.ArtifactEditor.EditorType;
import odog.design.Design;
import odog.design.RuleElement;
import odog.design.DesignRepository;
import odog.ruleChecker.Rule;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 *
 * @author ivan
 */
public class RuleEditor extends ArtifactEditor {
    
    /** Creates a new instance of RuleEditor */
    public RuleEditor(Design d, RuleElement rule, JLayeredPane container,
            BaseConfiguration configuration, DesignRepository repository) {
        super(d, "Rule Editor", EditorType.RULE, configuration, repository);
        
        containerPanel = container;
        ruleElement = rule;
        
        initializeFrame();
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public void refreshDisplay() {
        if(!editorFrame.isVisible()) {
            editorFrame.setSize(containerPanel.getWidth(), 
                    containerPanel.getHeight());
            editorFrame.setVisible(true);

            containerPanel.add(editorFrame); 
            try {
                editorFrame.setSelected(true);
            }
            catch(Exception ex) {
                System.out.println(ex);
            }
        }
    }
    
    public void close() {
        containerPanel.remove(editorFrame);
    }
    
    // Chamado pelo painel que esta editando o componente, em resposta ao botao
    // commit dele. Tem que entao, substituir o objeto rule do RuleElement pela
    // versao editada (clone)
    public void commit() {
        ruleElement.setRule(rpanel.getRule());
        ruleElement.setSummary(rpanel.getRuleMsg());
        ruleElement.getStatus().signalChanged();

        design.closeArtifactEditor(this);
        containerPanel.repaint();
    }
    
    // Chamado pelo painel que esta editando o componente, em resposta ao botao
    // cancel dele
    public void cancel() {
        design.closeArtifactEditor(this);
        containerPanel.repaint();
    }

    ////////////////////////////// PRIVATE METHODS /////////////////////////////
    
    // cria toda a infra-estrutura para a edicao, mas ainda nao mostra ela na tela
    private void initializeFrame() {
        editorFrame = new JInternalFrame("Editing " + ruleElement.getName(), true, true, 
                true, true);

        // 1. Cria o painel para o elemento inicial (aactro)
        Object obj = containerPanel.getParent();
        while(!(obj instanceof JFrame)) obj = ((Container) obj).getParent();

        Rule r = ruleElement.getRule();
        if(r == null) {
            initializedOk = false;
            return;
        }
        rpanel = new rulePanel(r, this, ruleElement.getSummary());

        // 5. Adiciona o painel a interface                
        editorFrame.add(rpanel);
        editorFrame.addMouseListener(new FrameMouseListener());
        editorFrame.addInternalFrameListener(new EditorFrameListener());
    }
        
    ////////////////////////////// PRIVATE VARIABLES ///////////////////////////
    
    private Rule clone;
    
    private JLayeredPane containerPanel;
    private RuleElement ruleElement; 
    
     // O Frame que mostra o editor;
    private JInternalFrame editorFrame;
    
    private rulePanel rpanel;
    
    private class FrameMouseListener implements MouseListener {
        
        public void mouseClicked(MouseEvent e) {            
            containerPanel.moveToFront(editorFrame);
        }
          
        public void mouseEntered(MouseEvent e) {
            
            
        }
          
        public void mouseExited(MouseEvent e) {
            
            
        }
          
        public void mousePressed(MouseEvent e) {
            
            
        }
          
        public void mouseReleased(MouseEvent e) {
            
            
        }
    }
    
    private final RuleEditor ed = this;
    
    private class EditorFrameListener implements InternalFrameListener {
        
        public void internalFrameActivated(InternalFrameEvent e) {
            
        }
          
        public void internalFrameClosed(InternalFrameEvent e) {
            design.closeArtifactEditor(ed);
        }
          
        public void internalFrameClosing(InternalFrameEvent e) {
            
            
        }
          
        public void internalFrameDeactivated(InternalFrameEvent e) {
            
            
        }
          
        public void internalFrameDeiconified(InternalFrameEvent e) {
            
            
        }

        public void internalFrameIconified(InternalFrameEvent e) {
          
            
        }
          
        public void internalFrameOpened(InternalFrameEvent e) {
            
        }
    }
}
