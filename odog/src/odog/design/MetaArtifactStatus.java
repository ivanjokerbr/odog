/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.design;

import java.util.Date;
import java.util.LinkedList;

/**
 * This class is responsible for storing transient information regarding the status
 * of a meta-artifact.
 *
 * @author ivan
 */
public class MetaArtifactStatus {
    
    public MetaArtifactStatus(MetaArtifact container) {
        changed = false;
        this.container = container;
        listeners = new LinkedList<>();
    }

    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    public boolean changedStatus() {
        return changed;
    }
    
    public void setVersion(Date version) {
        this.version = version;
    }
    
    public Date getVersion() {
        return version;
    }

    public void signalChanged() {
        version = new Date();
        changed = true;
        for(MetaArtifactStatusListener listener : listeners) {
            listener.statusChanged(this);
        }
    }

    public void resetChanged() {
        changed = false;
    }

    public void addStatusListener(MetaArtifactStatusListener listener) {
        listeners.add(listener);
    }
    
    public void removeStatusListener(MetaArtifactStatusListener listener) {
        listeners.remove(listener);
    }
    
    public MetaArtifact getContainer() {
        return container;
    }
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    protected boolean changed;
    protected Date version;
    
    private MetaArtifact container;
    
    private LinkedList<MetaArtifactStatusListener> listeners;
}
