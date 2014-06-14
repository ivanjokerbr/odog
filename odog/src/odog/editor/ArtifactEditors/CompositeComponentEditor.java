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
import odog.editor.ArtifactEditors.Composite.topologyPanel;
import odog.design.Design;
import odog.editor.LibraryViewerFrame;
import odog.editor.services.ServicesFrame;
import odog.design.CompositeComponent;
import odog.design.DesignRepository;
import odog.syntax.Nodes.CompInstance;
import odog.syntax.Nodes.Topology;
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
 *
 * @author ivan
 */
public class CompositeComponentEditor extends ArtifactEditor implements TransactionManagerContainer,
        ComponentArtifactEditor {
    
    /** Creates a new instance of CompositeComponentEditor */
    public CompositeComponentEditor(Design d, CompositeComponent comp, JLayeredPane container,
            BaseConfiguration configuration, DesignRepository repository) {
        super(d, "Composite Component Editor", EditorType.COMPOSITE, configuration,
                repository);
        
        this.component = comp;
        containerPanel = container;
        
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
    
    public NodeTransactionManager getTransactionManager() {
        return transactionManager;
    }
/*
    public void resolveValueReference(Value v) {
        Attr r = (Attr) clone.getLocalAttribute(v.getAssociatedAttributeName());
        // r nao pode dar null, pois so mostro os atributos que existem.
        v.setAssociatedAttribute(r);
    }
*/
    public AttributeDisplayList getAttributeDisplayList() {
        return attributeList;
    }
    
    public void updateAttributeDisplayList(CompInstance ains, VersionBase ver) {
        attributeList.updateAttributes(transactionManager,ains,ver);
    }

    public PortDisplayList getPortDisplayList() {
        return portList;
    }
    
    public void updatePortDisplayList() {
        portList.updatePorts(transactionManager);
    }
    
    public ComponentInstancesComboBox getComponentInstancesComboBox() {
        return instancesCBox;
    }
    
    public void updateComponentInstancesComboBox() {
        instancesCBox.update(transactionManager);
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
    
    public CompositeComponent getComponentBeingEdited() {
        return component;
    }
    
    ////////////////////////////// PRIVATE METHODS /////////////////////////////

    // cria toda a infra-estrutura para a edicao, mas ainda nao mostra ela na tela
    private void initializeFrame() {
        editorFrame = new JInternalFrame("Editing " + component.getName(), true, true, 
                true, true);

        // 1. Cria o painel para o elemento inicial (aactro)
        Object obj = containerPanel.getParent();
        while(!(obj instanceof JFrame)) obj = ((Container) obj).getParent();

        topologyPanel panel = new topologyPanel();
        
        // 2. pega o conteudo, clonando o ator a ser ediado        
        // must use this method, because when parsing, checks will be made
        Topology top = (Topology) component.getRootNode();
        clone = top.clone();
        if(clone == null) {
            System.err.println("Could not create instance of " + component.getName());
            initializedOk = false;
            return;
        }
        clone.buildElementTable();
        clone.cloneAssociatedAttributes(top, clone.getAttributeTable());
        
        panel.setDataWarper(this, clone, (JFrame) obj);
        
        // 3. cria o objeto que lista todos os atributos
        attributeList = new AttributeDisplayList(clone);        
        portList = new PortDisplayList(clone);        
        instancesCBox = new ComponentInstancesComboBox(clone);

        // 5. Cria o manager de transacoes
        transactionManager = new NodeTransactionManager();
        
        // 5. Adiciona o painel a interface                
        editorFrame.add(panel);        
        editorFrame.addMouseListener(new FrameMouseListener());
        editorFrame.addInternalFrameListener(new EditorFrameListener());
    }
    
    ////////////////////////////// PRIVATE VARIABLES ///////////////////////////
    
    // O element sendo editado
    private CompositeComponent component;

    // O Painel onde o JInternalFrame deste editor vai ser posto
    private JLayeredPane containerPanel;
 
    // O Frame que mostra o editor;
    private JInternalFrame editorFrame;
    
    private Topology clone;

        // Registra as transacoes de edicao deste artefato
    private NodeTransactionManager transactionManager;
    
    // ComboBox personalizado para listar, de maneira atualizada durante a edicao
    // os atributos disponiveis
    private AttributeDisplayList attributeList;
    private PortDisplayList portList;
    private ComponentInstancesComboBox instancesCBox;
    
    private ServicesFrame servicesFrame;
 
    private LibraryViewerFrame libraryViewer;
    
    ////////////////////////// INNER CLASSES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    
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
    
    private final CompositeComponentEditor ed = this;
    
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