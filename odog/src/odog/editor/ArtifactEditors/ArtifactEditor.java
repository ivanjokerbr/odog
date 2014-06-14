/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors;

import odog.configuration.BaseConfiguration;
import odog.design.Design;
import odog.design.DesignRepository;

/**
 *
 * @author ivan
 */
public abstract class ArtifactEditor {
    
    /** Creates a new instance of ArtifactEditor */
    public ArtifactEditor(Design d, String name, EditorType type, 
            BaseConfiguration conf, DesignRepository designRepository) {
        this.name = name;
        editorType = type;
        design = d;
        configuration = conf;
        this.designRepository = designRepository;
        
        initializedOk = true;
    }

    public enum EditorType { ATOMIC, COMPOSITE, RULE  }
     
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public String toString() {
        return name;
    }
    
    public EditorType getType() {
        return editorType;
    }
    
    // mostra na tela com os valores do modelo sendo editado
    public abstract void refreshDisplay();            
    
    public abstract void close();
    
    public Design getDesign() {
        return design;
    }

    public BaseConfiguration getConfiguration() {
        return configuration;
    }
    
    public DesignRepository getDesignRepository() {
        return designRepository;
    }

    public boolean initializedOk() {
        return initializedOk;
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////

    
    ///////////////////////////// PRIVATE VARIABLES ////////////////////////////
    
    protected String name;
    protected EditorType editorType;       
    protected Design design;
    
    protected BaseConfiguration configuration;
    protected DesignRepository designRepository;
    
    protected boolean initializedOk;
}
