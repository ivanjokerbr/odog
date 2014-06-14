/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors;

import odog.editor.LibraryViewerFrame;
import odog.editor.services.ServicesFrame;
import odog.syntax.Nodes.CompInstance;
import odog.syntax.Nodes.VersionBase;

/**
 *
 * @author ivan
 */
public interface ComponentArtifactEditor {
    
    public AttributeDisplayList getAttributeDisplayList();
    
    public void updateAttributeDisplayList(CompInstance ains, VersionBase ver);
    
   // public void resolveValueReference(Value v);
    
    public void setServicesFrame(ServicesFrame frame);
    
    public ServicesFrame getServicesFrame();

    public void setLibraryViewer(LibraryViewerFrame frame);
    
    public LibraryViewerFrame getLibraryViewer();
    
}
