/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.syntax.Nodes;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * Uma classe base para componentes atomicos e topologias.
 *
 * @author ivan
 */
public abstract class CompBase extends Node implements Attributable, Instanciable {
    
    /** Creates a new instance of CompBase */
    public CompBase(String name) {
        super(name);

        ports = new Edges(this);
        attributes = new Edges(this);
        versions = new Edges(this);
    }
    
    //////////////////////////////// public methods ////////////////////////////
    
    ////////////////////// REFERENTE A Interface ///////////////////////////////
    
    public CompBase clone() {
        return null;
    }
    
    public abstract List<Attr> getAllRecomputedAttributes(VersionBase ver);
    
    public void setComponentInstance(final CompInstance instance) {
        compInstance = instance;
    }

    public CompInstance getComponentInstance() {
        return compInstance;
    }
    
    public Iterator getPorts() {
        return ports.iterator();
    }

    public Iterator getAttributes() {
        return attributes.iterator();
    }    
    
    public VersionBase getVersion(String name) {
        return (VersionBase) versions.find(name);
    }
    
    public void addPort(VirtualPort port) throws NonUniqueNameException {
        ports.add(port);
    }
    
    public void removePort(VirtualPort port) {
        ports.remove(port);
        port.removeReferences();
    }
    
    public Iterator portsIterator() {
        return ports.iterator();
    }

    public void addAttribute(Attr attribute) throws NonUniqueNameException {
        attributes.add(attribute);
    }
    
    public void removeAttribute(Attr attribute) {
        attributes.remove(attribute);
        attribute.removeReferences();
    }
    
    public Iterator attributesIterator() {
        return attributes.iterator();
    }
    
    public Attr getAttribute(String name) {
        return (Attr)attributes.find(name);
    }
 
    public void addVersion(VersionBase version) throws NonUniqueNameException {
        versions.add(version);
    }

    public void removeVersion(VersionBase version) {
        versions.remove(version);
        version.removeReferences();
    }
    
    public Iterator versionsIterator() {
        return versions.iterator();
    }       
    
    // retorna os atributos associados aos elementos da interface deste ator (ator e portos)
    public List<Attr> getInterfaceAttributes() {
        LinkedList<Attr> ret = new LinkedList<Attr>();
        
        List<Node> l = attributes.getList();
        for(int i = 0;i < l.size();i++) {
            ret.add((Attr)l.get(i));
        }
        
        Iterator ite = ports.iterator();
        while(ite.hasNext()) {
            VirtualPort dp = (VirtualPort) ite.next();
            Iterator dpite = dp.attributesIterator();
            while(dpite.hasNext()) {
                ret.add((Attr)dpite.next());
            }
            ret.add((Attr) dp.getDataType());
        }

        return ret;
    }        
    
    public abstract void buildElementTable();
    
    public void printElementTable() {
        Iterator keys = attributeTable.keySet().iterator();
        while(keys.hasNext()) {
            String s = (String) keys.next();
            System.out.println("at -> " + s);
        }
    }
    
    public Hashtable<String, Attr> getAttributeTable() {
        return attributeTable;
    }
    
    public Attr getAttributeFromTable(String completeName) {
        if(attributeTable.containsKey(completeName)) {
            return (Attr) attributeTable.get(completeName);
        }
        return null;
    }
    
    /////////////////////// private methods ////////////////////////////////////
   
            
    ///////////////////////////// private attributes ///////////////////////////

    protected Edges ports;
    protected Edges attributes;
    protected Edges versions;

    protected CompInstance compInstance;

    // Nome Completo a partir do Acomp -> Att
    protected Hashtable<String, Attr> attributeTable;
}
