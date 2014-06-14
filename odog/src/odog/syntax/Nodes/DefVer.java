/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.syntax.Nodes;

import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author ivan
 */
public class DefVer extends Node implements Referencia {
    
    /** Creates a new instance of DefVer */
    public DefVer(String name, String instanceName, String versionName) {
        super(name);
        
        this.instanceName = instanceName;
        this.versionName = versionName;
    }
    
    ///////////////// PUBLIC METHODS ///////////////////////////////////////////

    public static boolean checkComponentInstanceLocation(CompInstance ains, Hver
            container) {
        Node ptr = ains.getContainer();
        while(ptr != null && !(ptr instanceof Hver) && !(ptr instanceof Topology)) {
            ptr = ptr.getContainer();
        }

        if(ptr == null) return false;
        
        if(ptr instanceof Topology) {
            // se a instancia foi selecionada da topologia, entao esta ok
            return true;
        }

        if(ptr instanceof Hver) {
            if(((Hver)ptr).equals(container)) {
                return true;
            }
            else {
                return false;
            }
        }
        
        return false;
    }
    
    public String toString() {
        return instanceName + " -> " + versionName;
    }
    
    public int getType() {
        return DEFVER;
    }
    
    public DefVer clone() {
        DefVer ret = new DefVer(name, instanceName, versionName);        
        return ret;
    }
    
    public String exportXML(int ident) {
        String pad = identForXML(ident);
        
        return pad + "<defVer name=\"" + name + "\" instanceName=\"" + 
                instanceName + "\" versionName=\"" + versionName + "\"/>\n";
    }
    
    public void setSelectedInstance(CompInstance ains) {
        if(selectedInstance != null) {
            selectedInstance.removeReference(this);
        }
        selectedInstance = ains;
        selectedInstance.addReference(this);
    }
    
    public CompInstance getSelectedInstance() {
        return selectedInstance;
    }
    
    public void setSelectedVersion(VersionBase ver) {
        selectedVersion = ver;
    }
    
    public VersionBase getSelectedVersion() {
        return selectedVersion;
    }
    
    // Tem que ser o nome completo da instancia
    public void setInstanceName(String name) {
        instanceName = name;
    }
    
    public String getInstanceName() {
        return instanceName;
    }
    
    // O nome da versao, nao o nome completo do no!
    public void setVersionName(String name) {
        versionName = name;
    }
    
    public String getVersionName() {
        return versionName;
    }

    public Iterator getAllConnectedNodes() {
        LinkedList l = new LinkedList();

        if(selectedVersion != null) {
            l.add(selectedVersion);
        }

        return l.iterator();
    }

    public Object getAttributeValue(String attribute) throws NonExistentAttributeException {
        if(attribute.equals("versionName")) {
            return new String(versionName);
        }
        else
        if(attribute.equals("instanceName")) {
            return new String(instanceName);
        }
        else {
            return super.getAttributeValue(attribute);
        }
    }

    public Class getAttributeType(String attribute) throws NonExistentAttributeException {
        if(attribute.equals("versionName") || attribute.equals("instanceName")) {
            return String.class;
        }
        else {
            return super.getAttributeType(attribute);
        }
    }

    //////////////////////////// metodos de referencia ////////////////////////

    public void unlink(Referenciado obj) {
        Hver vb = (Hver) container;
        vb.removeDefVersion(this);
    }

    public void update(Referenciado obj) {
        instanceName = selectedInstance.getFullInstanceName();
    }

    public void removeReferences() {
        selectedInstance.removeReference(this);
    }
    
    ///////////////// PRIVATE METHODS //////////////////////////////////////////


    ///////////////// PRIVATE VARIABLES/////////////////////////////////////////
    
    private String instanceName;
    private String versionName;
    private CompInstance selectedInstance;

    // tem que ser mudado...ao inves de node, uma classe VirtualVer....
    private VersionBase selectedVersion;
}
