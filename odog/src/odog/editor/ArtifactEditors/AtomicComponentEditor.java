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
import odog.editor.ArtifactEditors.Atomic.atomicComponentPanel;
import odog.design.Design;
import odog.editor.LibraryViewerFrame;
import odog.editor.services.ServicesFrame;
import odog.design.AtomicComponent;
import odog.syntax.Nodes.Acomp;
import odog.syntax.Nodes.CompInstance;
import odog.syntax.Nodes.VersionBase;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import xmleditorgenerator.BaseFiles.TransactionManagerContainer;

/**
 * Responsavel por efetuar as transacoes entre o frame de edicao, e o modelo sendo
 * editado
 *
 * @author ivan
 */
public class AtomicComponentEditor extends ArtifactEditor implements TransactionManagerContainer,
        ComponentArtifactEditor {
    
    public AtomicComponentEditor(Design d, AtomicComponent comp, JLayeredPane container,
            BaseConfiguration configuration) {
        super(d, "Atomic Component Editor", EditorType.ATOMIC, configuration, null);
        
        this.component = comp;
        containerPanel = container;

        initializeFrame();
    }

    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    // mostra na interface a visao do modelo
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
    
    public void processTransaction() {
        
    }
    
    public void undoLastTransaction() {
        
    }
    
    public void setServicesFrame(ServicesFrame frame) {
        servicesFrame = frame;
    }
    
    public ServicesFrame getServicesFrame() {
        return servicesFrame;
    }
    
    public void setLibraryViewer(LibraryViewerFrame lf) {
        libraryViewer = lf;
    }
    
    public LibraryViewerFrame getLibraryViewer() {
        return libraryViewer;
    }
    
    // Se estiver visivel, remove
    public void close() {
        containerPanel.remove(editorFrame);
        //editorFrame.dispose();
    }

    // Chamado pelo painel que esta editando o componente, em resposta ao botao
    // commit dele. Tem que entao, substituir o objeto acomp do AtomicComponent pela
    // versao editadao
    public void commit() {
        clone.buildElementTable();
        
        component.setRootNode(clone);
        component.getStatus().signalChanged();

        design.closeArtifactEditor(this);
        containerPanel.repaint();
    }
    
    // Chamado pelo painel que esta editando o componente, em resposta ao botao
    // cancel dele
    public void cancel() {
        design.closeArtifactEditor(this);
        containerPanel.repaint();
    }
    
    public AttributeDisplayList getAttributeDisplayList() {
        return attributeList;
    }

    public void updateAttributeDisplayList(CompInstance ains, VersionBase ver) {
        attributeList.updateAttributes(transactionManager, ains, ver);
    }
    
    public NodeTransactionManager getTransactionManager() {
        return transactionManager;
    }
    
    /*
    public void resolveValueReference(Value v) {
        Attr r = (Attr) clone.getNode(v.getAssociatedAttributeName());      
        // r nao pode dar null, pois so mostro os atributos que existem.
        v.setAssociatedAttribute(r);
    }
*/
    ////////////////////////////// PRIVATE METHODS /////////////////////////////

    // cria toda a infra-estrutura para a edicao, mas ainda nao mostra ela na tela
    private void initializeFrame() {
        editorFrame = new JInternalFrame("Editing " + component.getName(), true, true, 
                true, true);

        // 1. Cria o painel para o elemento inicial (aactro)
        Object obj = containerPanel.getParent();
        while(!(obj instanceof JFrame)) obj = ((Container) obj).getParent();

        atomicComponentPanel panel = new atomicComponentPanel();
                
        // 2. pega o conteudo, clonando o ator a ser ediado
        clone = component.getRootNode().clone();
        if(clone == null) {
            initializedOk = false;
            return;
        }
        panel.setDataWarper(this, clone, (JFrame) obj);
        
        // 3. Cria e inicializa o manager de transacoes
        
        // 4. cria o objeto que lista todos os atributos
        attributeList = new AttributeDisplayList(clone);
        
        // 5. Cria o manager de transacoes
        transactionManager = new NodeTransactionManager();
        
        // 5. Adiciona o painel a interface                
        editorFrame.add(panel);        
        editorFrame.addMouseListener(new FrameMouseListener());
        editorFrame.addInternalFrameListener(new EditorFrameListener());
    }
        
    ////////////////////////////// PRIVATE VARIABLES ///////////////////////////

    // O element sendo editado
    private AtomicComponent component;

    // Elemento temporario para ser editado.
    private Acomp clone;
    
    // O Painel onde o JInternalFrame deste editor vai ser posto
    private JLayeredPane containerPanel;
    
    // O Frame que mostra o editor;
    private JInternalFrame editorFrame;
    
    // ComboBox personalizado para listar, de maneira atualizada durante a edicao
    // os atributos disponiveis
    private AttributeDisplayList attributeList;
    
    // Registra as transacoes de edicao deste artefato
    private NodeTransactionManager transactionManager;

    private ServicesFrame servicesFrame;
    
    private LibraryViewerFrame libraryViewer;
    
    ////////////////////////////// INNER CLASSES ///////////////////////////////
    
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
    
    private final AtomicComponentEditor ed = this;
    
    private class EditorFrameListener implements InternalFrameListener {
        
        @Override
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