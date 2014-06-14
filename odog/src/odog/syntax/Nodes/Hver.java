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

public class Hver extends VersionBase {
    
    public Hver(String name) {
        super(name);

        connections = new Edges(this);
        selectedVersions = new Edges(this);
        componentInstances = new Edges(this);
    }

    ////////////////////////////////////////////////////////////////////////////
    
    public int getType() {
        return HVER;
    }

    public String toString() {
        return name;
    }

    public Hver clone(Topology parent, Hashtable<Connection, Connection> connectionsMap,
            Hashtable<ExportedPort, ExportedPort> exportedPortMap) {
        Hver ret = new Hver(name);

        // 1. clona as instancias de componentes
        Iterator ite = componentInstancesIterator();
        while(ite.hasNext()) {
            CompInstance ains = (CompInstance) ite.next();
            try {
                ret.addComponentInstance(ains.clone());
            }
            catch(NonUniqueNameException ex) {
                System.out.println(ex); 
            }
        }
        
        // 2. os atributos
        ite = attributesIterator();
        while(ite.hasNext()) {
            Attr att = (Attr) ite.next();
            try {
                ret.addAttribute(att.clone());
            }
            catch(NonUniqueNameException ex) {
                System.out.println(ex); 
            }
        }
        
        // 3. as definicoes de versao
        ite = defVersionsIterator();
        while(ite.hasNext()) {            
            DefVer v = (DefVer) ite.next();
            DefVer nv = v.clone();
            try {
                ret.addDefVersionTrusted(nv);
            }
            catch(NonUniqueNameException ex) {
                System.out.println(ex); 
            }
        }
        
        ite = valuesIterator();
        while(ite.hasNext()) {
            Value v = (Value) ite.next();
            try {
                ret.addValue(v.clone());
            }
            catch(NonUniqueNameException ex) {
                System.out.println(ex); 
            }   
        }
        
        ite = connectionsIterator();
        while(ite.hasNext()) {
            Connection c = (Connection) ite.next();
            Connection c_cloned = c.clone();
            try {
                ret.addConnection(c_cloned);
            }
            catch(NonUniqueNameException ex) {
                System.out.println(ex); 
            }
            connectionsMap.put(c_cloned, c);
        }

        ite = portsIterator();
        while(ite.hasNext()) {
            ExportedPort ep = (ExportedPort) ite.next();
            ExportedPort newep = ep.clone();
            exportedPortMap.put(newep, ep);
            try {
                ret.addPort(newep);
            }
            catch(NonUniqueNameException ex) {
                System.out.println(ex);
            }
        }
        
        ite = methodsIterator();
        while(ite.hasNext()) {
            Method m = (Method) ite.next();
            try {
                ret.addMethod(m.clone());
            }
            catch(NonUniqueNameException ex) {
                System.out.println(ex); 
            }
        }

        ite = reqservIterator();
        while(ite.hasNext()) {
            Reqserv req = (Reqserv) ite.next();
            try {
                ret.addReqserv(req);
            }
            catch(NonUniqueNameException ex) {
                System.out.println(ex);
            }
        }

        return ret;
    }

    public String exportXML(int ident) {
        StringBuffer buf = new StringBuffer();
        
        String pad = identForXML(ident);

        buf.append(pad + "<version name=\"" + name + "\">\n");
        
        componentInstances.exportXML(buf, ident + 2);
        attributes.exportXML(buf, ident + 2);
        selectedVersions.exportXML(buf, ident + 2);
        values.exportXML(buf, ident + 2);

        Iterator ite = ports.iterator();
        while(ite.hasNext()) {
            ExportedPort dp = (ExportedPort) ite.next();
            dp.exportXML(buf, ident + 2);
        }        

        connections.exportXML(buf, ident + 2);
        
        methods.exportXML(buf, ident + 2);
        reqserv.exportXML(buf, ident + 2);

        buf.append(pad + "</version>\n");

        return buf.toString();
    }
    
   /////////////////////////////// referente aos nos contidos //////////////////
    
    public void addComponentInstance(CompInstance ains) throws NonUniqueNameException {
        componentInstances.add(ains);
    }
    
    public void removeComponentInstance(CompInstance ains) {
        componentInstances.remove(ains);
        ains.removeReferences();
    }
    
    public Iterator componentInstancesIterator() {
        return componentInstances.iterator();
    }
    
    public CompInstance findComponentInstance(String name) {
        Iterator ite = componentInstances.iterator();
        while(ite.hasNext()) {
            CompInstance ains = (CompInstance) ite.next();
            if(name.equals(ains.getFullInstanceName())) return ains;
        }
        return null;
    }
    
    public void addConnection(Connection con) throws NonUniqueNameException {
        connections.add(con);
    }
    
    public void removeConnection(Connection con) {
        connections.remove(con);
        con.removeReferences();
    }
    
    public Iterator connectionsIterator() {
        return connections.iterator();
    }

    // this is a hack. This method should be called only from the CLONE method,
    // since the selectedInstance will be null.
    public void addDefVersionTrusted(DefVer def) throws NonUniqueNameException {
        selectedVersions.add(def);    
    }

    public void addDefVersion(DefVer def) throws NonUniqueNameException {
        Iterator ite = selectedVersions.iterator();
        while(ite.hasNext()) {
            DefVer df = (DefVer) ite.next();
            if(df.getSelectedInstance() == def.getSelectedInstance()) {
                throw new NonUniqueNameException("Version " + getName() +
                        " has already version definiton for instance " +
                        def.getSelectedInstance().getFullInstanceName());
            }
        }
        selectedVersions.add(def);
    }
    
    public Iterator defVersionsIterator() {
        return selectedVersions.iterator();
    }
    
    public void removeDefVersion(DefVer def) {
        selectedVersions.remove(def);

        // serve para verificar se ha alguma referencia a um porto que dependia
        // desta selecao de verao
        def.getSelectedInstance().updateReferences();
        
        def.removeReferences();
    }
    
    public VersionBase getSelectedVersion(CompInstance ains) {
        Iterator ite = selectedVersions.iterator();
        while(ite.hasNext()) {
            DefVer df = (DefVer) ite.next();
            if(df.getSelectedInstance() == ains) {
                return (VersionBase) df.getSelectedVersion();
            }
        }
        return null;
    }
    
    public Iterator getAllConnectedNodes() {
        List<Node> ret = new LinkedList<Node>();
        
        ret.addAll(connections.getList());
        ret.addAll(values.getList());
        ret.addAll(attributes.getList());
        ret.addAll(ports.getList());
        ret.addAll(selectedVersions.getList());
        ret.addAll(methods.getList());
        ret.addAll(componentInstances.getList());
        ret.addAll(reqserv.getList());

        return ret.iterator();
    }
 
    /////////////////////////////////// private methods ////////////////////

    public boolean containsName(String name, int nodetype) {
        switch(nodetype) {
            case COMPONENTINSTANCE: {
                return componentInstances.containsName(name);
            }
            
            case CONNECTION: {
                return connections.containsName(name);
            }
            
            case EXPORTEDPORT: {
                return ports.containsName(name);
            }
            
            case VALUE: {
                return values.containsName(name);
            }
            
            case ATTR: {
                return attributes.containsName(name);
            }
            
            case DEFVER: {
                return selectedVersions.containsName(name);
            }
            
            case METHOD: {
                return methods.containsName(name);
            }

            case REQSERV: {
                return reqserv.containsName(name);
            }
        }

        return false; 
    }
    
    /////////////////////////////////// private attributes ////////////////////
    
    private Edges componentInstances;
    private Edges connections;
    private Edges selectedVersions;
}