/* 
 * Copyright (c) 2006-2014, Ivan Jeukens
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.design;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * A meta artifact describes an component, or a collection of components, in an abstract way. 
 * For instance,  a state machine, an equation or a template generator are meta-artifacts.
 * Also, a design is a meta artifact. 
 * 
 * A meta artifact can generate another meta artifact or an artifact. Only artifacts can 
 * be code-generated.
 *
 * A meta-artifact has a status.
 *
 * @author ivan
 */
public class MetaArtifact {
    
    public MetaArtifact() {
        status = new MetaArtifactStatus(this);
        children = new LinkedList<>();
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public MetaArtifact getParent() {
        return parent;
    }
    
    public void setParent(MetaArtifact parent) {
        this.parent = parent;
    }

    public List<MetaArtifact> getChildren() {
        return children;
    }
   
    public List<Artifact> artifacts() {
        LinkedList<Artifact> ret = new LinkedList<>();
        
        for(MetaArtifact mart : children) {
            if(mart instanceof Artifact) {
                ret.add((Artifact)mart);
            }
            else {
                ret.addAll(mart.artifacts());    
            }
        }
        return ret;
    }
    
    public String exportXML() {
        return "";
    }
    
    public MetaArtifactStatus getStatus() {
        return status;
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    protected MetaArtifact parent;
    protected LinkedList<MetaArtifact> children;

    protected MetaArtifactStatus status;
}
