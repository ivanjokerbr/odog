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
import java.util.List;

/**
 *
 * @author ivan
 */
public class ExportedPort extends VirtualPort implements Comparable, Referencia {

    /** Creates a new instance of ExportedPort */
    public ExportedPort(String name) {
        super(name);
        refPorts = new LinkedList<VirtualPort>();
    }

    ///////////////// PUBLIC METHODS ///////////////////////////////////////////

    public Attr getDataType() {
        // all refered ports must have the same type
        return refPorts.get(0).getDataType();
    }
    
    public int compareTo(Object obj) {
        VirtualPort vp = (VirtualPort) obj;
        return getFullName().compareTo(vp.getFullName());
    }

    public int getType() {
        return EXPORTEDPORT;
    }
    
    public boolean isInput() {
        if(refPorts.size() > 0) {
            return refPorts.get(0).isInput();
        }
        return false;
    }
    
    public boolean isOutput() {
        if(refPorts.size() > 0) {
            return refPorts.get(0).isOutput();
        }
        return false;
    }

    public List<VirtualPort> getRefPorts() {
        return refPorts;
    }
    
    public boolean addRefPort(VirtualPort dp) {
        if(refPorts.contains(dp)) return false;

        refPorts.add(dp);
        dp.addReference(this); 

        return true;
    }

    public void removeRefPort(VirtualPort dp) {
        if(refPorts.contains(dp)) {
            dp.removeReference(this);
            refPorts.remove(dp);
        }
    }
    
    public ExportedPort clone() {
        ExportedPort edp = new ExportedPort(name);
        return edp;
    }
    
    public String toString() {
        if(refPorts.size() == 0) return getName() + " exports( )";
        if(refPorts.size() == 1) return getName() + " exports(" + 
                refPorts.get(0).getFullName() +  ")";
        
        String ret = getName() + " exports(" +  refPorts.get(0).getFullName();
        for(int i = 1;i < refPorts.size();i++) {
            ret = ret + ", " + refPorts.get(i).getFullName();    
        }
        ret = ret + ")";
        
        return ret;
    }
    
    public void exportXML(StringBuffer buf, int ident) {
        String pad = identForXML(ident);        
        buf.append(pad + "<exportedPort name=\"" + name + "\">\n");        
        for(int i = 0;i < refPorts.size();i++) {
            buf.append(pad + "  <portRef completePortName=\"" + 
                    refPorts.get(i).getFullName() + "\"/>\n");
        }        
        buf.append(pad + "</exportedPort>\n");
    }

    public Iterator getAllConnectedNodes() {
        LinkedList l = new LinkedList();
        l.addAll(refPorts);
        return l.iterator();
    }

    public Iterator attributesIterator() {
        LinkedList l = new LinkedList();
        for(int i = 0;i < refPorts.size();i++) {
            Iterator ite = refPorts.get(i).attributesIterator();
            while(ite.hasNext()) {
                l.add(ite.next());
            }
        }
        return l.iterator();
    }

    //////////////////////////// metodos de referencia ////////////////////////

    public void unlink(Referenciado obj) {
        if(container instanceof Hver) {
            ((Hver)container).removePort(this);
        }
        else
        if(container instanceof Topology) {
            ((Topology)container).removePort(this);
        }
    }

    // dois casos sao tratados:
    // 2. Uma definicao de versao foi removida, entao eh necessario verificar
    // se o porto referenciado nao eh mais acessivel, isto eh, este porto    
    // exportado dependia desta definicao
    public void update(Referenciado obj) {
        // caso 2 : basta verificar se o porto em questa ainda eh alcansavel por
        // este porto exportado.
        if(container instanceof Topology) return;

        if(container == null) return;  // this happens when an exported port is being inserted in a version
        
        boolean res = VirtualPort.checkPortLocation((VirtualPort) obj, 
                (Hver) container);
        if(!res) {
            ((Hver)container).removePort(this);
        }
    }

    public void removeReferences() {
        for(int i = 0;i < refPorts.size();i++) {
            refPorts.get(i).removeReference(this);
        }        
    }

    ///////////////// PRIVATE METHODS //////////////////////////////////////////
    
    
    ///////////////// PRIVATE VARIABLES/////////////////////////////////////////

    // A list of virtual ports htat are referenced
    private LinkedList<VirtualPort> refPorts;
}
