/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.design;

import odog.editor.ArtifactEditors.ArtifactEditor.EditorType;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author ivan
 */
public class Artifact extends MetaArtifact {

    /** Creates a new instance of Artifact */
    public Artifact(String name, URL url, Date version, String baseDir,
            EditorType editortype, ElementType etype) {
        super();

        this.name = name;
        this.url = url;
        this.baseDir = baseDir;

        status.setVersion(version);

        editorType = editortype;
        elementType = etype;
        
        dependsOn = new LinkedList<>();
        dependsVersions = new HashMap<>();
    }

    public enum ElementType {
        RULE, ATOMICCOMP, COMPOSITECOMP
    }

    /////////////////////// PUBLIC METHODS ////////////////////////////////////

    public String toString() {
        return name;
    }

    public String exportXML() {
        return "";
    }

    public EditorType getEditorType() {
        return editorType;
    }

    public URL getURL() {
        return url;
    }

    public String getName() {
        return name;
    }

    public void setSummary(String s) {
        summary = s;
    }

    public String getSummary() {
        return summary;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public ElementType getType() {
        return elementType;
    }
    
    // There is not an removeArtifactDependency, since, it would be necessary to
    // track when an artifact does not depends anymore. What may happens is that
    // an update may trigger a new parse of the artifact, and than this false
    // dependency is removed. Both dependsOn and dependsVersions must be 
    // cleared by the specific subclasses.
    public void addArtifactDependency(Artifact art) {
        if(!dependsOn.contains(art)) {
            dependsOn.add(art);
            Date d = art.getStatus().getVersion();
            dependsVersions.put(art, d);
        }
    }

    /////////////////////// PRIVATE METHODS ////////////////////////////////////

    protected boolean anyArtifactHasChanged() {
        for(Artifact art : dependsOn) {
            if(art.getStatus().changedStatus()) {
                return true;
            }
            else {
                Date d = dependsVersions.get(art);
                if(!d.equals(art.getStatus().getVersion())) {
                    return true;
                }
            }

            if(art.anyArtifactHasChanged()) return true;
        }

        return false;
    }

    /////////////////////// PRIVATE VARIABLES //////////////////////////////////

    protected ElementType elementType;
    protected EditorType editorType;
    protected String name;
    protected URL url;
    protected String baseDir;
    protected String summary;

    // stores a list of artifacts that this artifact depends. In particular,
    // for a composite component, the artifacts of the instantiated components
    protected LinkedList<Artifact> dependsOn;
    
    protected HashMap<Artifact, Date> dependsVersions;
}
