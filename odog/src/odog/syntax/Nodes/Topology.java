/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.syntax.Nodes;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;

public class Topology extends CompBase implements CompInterface {
    
    public Topology(String name) {
        super(name);
        connections = new Edges(this);
        componentInstances = new Edges(this);
    }

    ///////////////// PUBLIC METHODS ///////////////////////////////////////////

    public int getType() {
        return TOPOLOGY;
    }

    public String toString() {
        return "<Topology> " + name; 
    }

    /////////////////// Visualizacao como uma arvore dos nos contidos ///////////
    
    public void setTreeNode(DefaultMutableTreeNode node) {
        treeNode = node;
    }
    
    public DefaultMutableTreeNode getTreeNode() {
        return treeNode;
    }
    
    /* This clone method is incomplete, in the sense that for the Value nodes,
     * the associated attribute fields are not determined. This is necessary because
     * only when all hierarchy levels are cloned, that this action can be performed. 
     * Therefore, who calls the clone of a topolevel topology must then call 
     * buildElementsTable() and cloneAssociatedAttributes to complete the cloning     
     */
    public Topology clone() {
        Topology ret = new Topology(name);

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
            
            // faz as referencias para as dports. Os nomes sao de componentes instanciados
            Iterator pite = c.portsIterator();
            while(pite.hasNext()) {
                VirtualPort dp = (VirtualPort) pite.next();
                VirtualPort dc = ret.findPort(dp.getLocalName());
                if(dc == null) {
                    System.err.println("topology clone error: did not find " +
                            dp.getFullName());
                }
                try {
                    c_cloned.addPort(dc);
                }
                catch(NonUniqueNameException ex) {
                    System.out.println(ex);
                }
            }
        }       

        ite = portsIterator();
        while(ite.hasNext()) {
            ExportedPort ep = (ExportedPort) ite.next();
            ExportedPort newep = ep.clone();

            List<VirtualPort> ports = ep.getRefPorts();
            for(int i = 0;i < ports.size();i++) {
                VirtualPort dc = ret.findPort(ports.get(i).getLocalName());
                if(dc == null) {
                    System.err.println("topology clone error: did not find " +
                            ports.get(i).getLocalName());
                }
                newep.addRefPort(dc);
            }
            try {
                ret.addPort(newep);
            }
            catch(NonUniqueNameException ex) {
                System.out.println(ex);
            }
        }

        Hashtable <Connection, Connection> connectionsMap = 
                new Hashtable<Connection, Connection>();
        Hashtable <ExportedPort, ExportedPort> exportedPortMap =
                new Hashtable<ExportedPort, ExportedPort>();
        
        ite = versionsIterator();
        while(ite.hasNext()) {
            Hver ver = (Hver) ite.next();
            try {
                ret.addVersion(ver.clone(ret, connectionsMap, exportedPortMap));
            }
            catch(NonUniqueNameException ex) {
                System.out.println(ex); 
            }
        }
                
        ite = ret.versionsIterator();
        while(ite.hasNext()) {
            Hver hv = (Hver) ite.next();
                                    
            // 2. para as definicoes de versao, faz a associacao da instancia selecionada
            // e a versao
            Iterator dfite = hv.defVersionsIterator();
            while(dfite.hasNext()) {
                resolveDefVerReferences(ret, hv, (DefVer) dfite.next());
            }
            
            // 3. para as conecoes da versao, clona os portos da conexao original.
            Iterator cite = hv.connectionsIterator();
            while(cite.hasNext()) {
                Connection c_cloned = (Connection) cite.next();
                Connection c_original = connectionsMap.get(c_cloned);
                
                resolveConnectionReferences(ret, hv, c_cloned, c_original);
            }

            // 4. para os portos exportados, determina a referencia na versao clonada
            Iterator epite = hv.portsIterator();
            while(epite.hasNext()) {
                ExportedPort ep = (ExportedPort) epite.next();
                ExportedPort oldep = exportedPortMap.get(ep);
                resolveExportedPortReferences(ret, hv, ep, oldep);
            }                        
        }
        
        DefaultMutableTreeNode tn = (DefaultMutableTreeNode) treeNode.clone();
        copyChildTreeNodes(tn, treeNode);
        ret.setTreeNode(tn);

        return ret;
    }
    
    // A buildElementsTable must have been called before this method!!
    public void cloneAssociatedAttributes(Topology originalTopology, 
            Hashtable<String, Attr> attributeTable) {
        Iterator ite = componentInstancesIterator();
        while(ite.hasNext()) {
            CompInstance ins = (CompInstance) ite.next();
            CompInstance originalInstance = originalTopology.findComponentInstance(
                    ins.getFullInstanceName());

            Iterator vite = ins.valuesIterator();
            while(vite.hasNext()) {
                Value v = (Value) vite.next();
                Value originalValue = originalInstance.getValue(v.getName());

                for(int i = 0;i < originalValue.getAssociatedAttributes().size();i++) {
                    Attr att = attributeTable.get(
                            originalValue.getAssociatedAttributes().get(i).getFullName());
                    if(att == null) {
                        System.err.println("cloneAssociatedAttributes error: did not find atribute " +
                            originalValue.getAssociatedAttributes().get(i).getFullName());
                    }
                    v.addAssociatedAttribute(att);
                }
            }

            CompBase abase = ins.getComponent();
            if(abase instanceof Topology) {
                ((Topology) abase).cloneAssociatedAttributes(
                        (Topology) originalInstance.getComponent(), attributeTable);
            }
        }

        ite = versionsIterator();
        while(ite.hasNext()) {
            Hver hv = (Hver) ite.next();
            Hver originalVersion = (Hver) originalTopology.getVersion(hv.getName());

            // 1. efetua as associacoes entre um valor incluido na versao, e o 
            // atributo associado
            Iterator valuesite = hv.valuesIterator();
            while(valuesite.hasNext()) {
                Value v = (Value) valuesite.next();
                Value originalValue = originalVersion.getValue(v.getName());

                for(int i = 0;i < originalValue.getAssociatedAttributes().size();i++) {
                    Attr att = attributeTable.get(
                            originalValue.getAssociatedAttributes().get(i).getFullName());
                    if(att == null) {
                        System.err.println("cloneAssociatedAttributes error: did not find version atribute " +
                            originalValue.getAssociatedAttributes().get(i).getFullName());
                    }
                    v.addAssociatedAttribute(att);
                }
            }            
            
            Iterator aite = hv.componentInstancesIterator();
            while(aite.hasNext()) {
                CompInstance ins = (CompInstance) aite.next();
                CompInstance originalInstance = originalVersion.findComponentInstance(
                        ins.getFullInstanceName());

                Iterator vite = ins.valuesIterator();
                while(vite.hasNext()) {
                    Value v = (Value) vite.next();
                    Value originalValue = originalInstance.getValue(v.getName());

                    for(int i = 0;i < originalValue.getAssociatedAttributes().size();i++) {                    
                        Attr att = attributeTable.get(
                                originalValue.getAssociatedAttributes().get(i).getFullName());
                        if(att == null) {
                            System.err.println("cloneAssociatedAttributes error: did not find atribute " +
                                originalValue.getAssociatedAttributes().get(i).getFullName());
                        }
                        v.addAssociatedAttribute(att);
                    }
                }

                CompBase abase = ins.getComponent();
                if(abase instanceof Topology) {
                    ((Topology) abase).cloneAssociatedAttributes(
                            (Topology) originalInstance.getComponent(), attributeTable);
                }
            }
        }
    }

    public String exportXML(int ident) {
        StringBuffer buf = new StringBuffer();
        String pad = identForXML(ident);

        buf.append(pad + "<topology name=\"" + name + "\">\n");

        componentInstances.exportXML(buf, ident + 2);
        attributes.exportXML(buf, ident + 2);
        
        Iterator ite = ports.iterator();
        while(ite.hasNext()) {
            ExportedPort ep = (ExportedPort) ite.next();
            ep.exportXML(buf, ident + 2);
        }
        
        connections.exportXML(buf, ident + 2);
        versions.exportXML(buf, ident + 2);

        buf.append(pad + "</topology>\n");

        return buf.toString();
    }

    //////////////////////////// Manipulacao dos nos contidos //////////////////

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

    /////////////////////// Procurando nos especificos /////////////////////////

    // Esse metodo eh usado para achar uma referencia a um porto a partir de uma
    // topologia. Nao pode passar para outros niveis de hierarquia, entao considera
    // somente as instancias de ator na dada topologia. Dessa forma, o nome do porto
    // especificado eh apartir da topologia em questao. 
    public VirtualPort findPort(String portName) {
        Iterator ite = componentInstances.iterator();
        while(ite.hasNext()) {
            CompInstance ains = (CompInstance) ite.next();
            CompInterface ai = (CompInterface) ains.getComponent();

            Iterator portsite = ai.getPorts();
            while(portsite.hasNext()) {
                VirtualPort vp = (VirtualPort) portsite.next();
                if(vp.getFullName().equals(portName)) return vp;
            }
        }

        return null;
    }

    // Usado pelos objetos de definicao de versao
    public CompInstance findComponentInstance(String compInstanceName) {
        Iterator ite = componentInstances.iterator();
        while(ite.hasNext()) {
            CompInstance ains = (CompInstance) ite.next();
            if(compInstanceName.equals(ains.getFullInstanceName())) {
                return ains;
            }
        }

        return null;
    }
    
    // Tem que ser sobreposta a da classe Node devido a instancia
    public String getFullName() {
        String fullName;
        if(compInstance == null) {
            fullName = name;
        }
        else {
            fullName = compInstance.getFullInstanceName();
        }
        return fullName;
    }
        
    public Iterator getAllConnectedNodes() {
        List<Node> l = new LinkedList<Node>();

        l.addAll(componentInstances.getList());
        l.addAll(attributes.getList());
        l.addAll(connections.getList());
        l.addAll(versions.getList());
        l.addAll(ports.getList());

        return l.iterator();
    }
   
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
                      
            case ATTR: {
                return attributes.containsName(name);
            }
            
            case HVER: {
                return versions.containsName(name);
            }
        }

        return false; 
    }

    // retorna os atributos associados aos elementos da interface deste ator (ator e portos)
    public List<Attr> getInterfaceAttributes() {
        LinkedList<Attr> ret = new LinkedList<Attr>();
    
        // Attributes of the component and of its ports
        ret.addAll(super.getInterfaceAttributes());
                
        Iterator ite = connections.iterator();
        while(ite.hasNext()) {
            Connection c = (Connection) ite.next();
            Iterator aite = c.attributesIterator();
            while(aite.hasNext()) {
                ret.add((Attr)aite.next());
            }
        }

        return ret;
    }        

    // returns all attributes of this topology. It will recompute the table
    // each time this method is called.
    // Ver must be a version of this topology
    public List<Attr> getAllRecomputedAttributes(VersionBase version) {
        LinkedList<Attr> ret = new LinkedList<Attr>();

        Hver ver = (Hver) version;
        Iterator ite = attributesIterator();
        while(ite.hasNext()) {
            Attr at = (Attr) ite.next();
            ret.add(at);
        }

        ite = connectionsIterator();
        while(ite.hasNext()) {
            Connection c = (Connection) ite.next();
            Iterator atite = c.attributesIterator();
            while(atite.hasNext()) {
                Attr at = (Attr) atite.next();
                ret.add(at);
            }
        }

        Iterator atite = ver.attributesIterator();
        while(atite.hasNext()) {
            Attr at = (Attr) atite.next();
            ret.add(at);
        }

        Iterator mite = ver.methodsIterator();
        while(mite.hasNext()) {
           Method m = (Method) mite.next();
           atite = m.attributesIterator();
           while(atite.hasNext()) {
               Attr at = (Attr) atite.next();
               ret.add(at);
           }
        }

        Iterator cite = ver.connectionsIterator();
        while(cite.hasNext()) {
            Connection c = (Connection) cite.next();
            atite = c.attributesIterator();
            while(atite.hasNext()) {
                Attr at = (Attr) atite.next();
                ret.add(at);
            }
        }

        Iterator dfite = ver.defVersionsIterator();
        while(dfite.hasNext()) {
             DefVer df = (DefVer) dfite.next();
             CompInstance ains = df.getSelectedInstance();
             ret.addAll(ains.getExternalAttributes(
                     df.getSelectedVersion()));
        }
        return ret; 
    }        
            
    public void buildElementTable() {
        buildAttributeTable();
    }
    
    /* Can be called only after the values are pointing to the associated attributes.
     * For each value, adds the respective associated attributes if not already in the
     * table. If this happens, it means that a value at a higher level of hierarchy contains
     * a value pointing to it. 
     * Default values are not processed. If one looks at the table and does not find the 
     * value for the attribute, the default value must be considered.
     */
    public Hashtable<Attr, Value> buildValuesTable(VersionBase ver) {
        valuesTable = new Hashtable<Attr, Value>();

        LinkedList<CompBase> process = new LinkedList<CompBase>();
        process.add(this);
        LinkedList<VersionBase> versions = new LinkedList<VersionBase>();
        versions.add(ver);

        while(process.size() > 0) {
            CompBase abase = process.remove(0);
            VersionBase vbase = versions.remove(0);
            
            // Must be in this order
            Iterator ite = vbase.valuesIterator();
            while(ite.hasNext()) {
                Value v = (Value) ite.next();
                for(int i = 0;i < v.getAssociatedAttributes().size();i++) {
                    Attr at = v.getAssociatedAttributes().get(i);                    
                    if(!valuesTable.containsKey(at)) {
                        valuesTable.put(at, v);
                    }
                }
            }
            
            if(abase instanceof Topology) {
                Topology top = (Topology) abase;
                Hver hver = (Hver) vbase;
                
                Iterator aite = top.componentInstancesIterator();
                while(aite.hasNext()) {
                    CompInstance ains = (CompInstance) aite.next();
                    
                    ite = ains.valuesIterator();
                    while(ite.hasNext()) {
                        Value v = (Value) ite.next();
                        for(int i = 0;i < v.getAssociatedAttributes().size();i++) {
                            Attr at = v.getAssociatedAttributes().get(i);
                            if(!valuesTable.containsKey(at)) {
                                valuesTable.put(at, v);
                            }
                        }
                    }
                    
                    VersionBase vb = hver.getSelectedVersion(ains);                    
                    process.add(ains.getComponent());
                    versions.add(vb);
                }
                
                aite = hver.componentInstancesIterator();
                while(aite.hasNext()) {
                    CompInstance ains = (CompInstance) aite.next();
                    
                    ite = ains.valuesIterator();
                    while(ite.hasNext()) {
                        Value v = (Value) ite.next();
                        for(int i = 0;i < v.getAssociatedAttributes().size();i++) {
                            Attr at = v.getAssociatedAttributes().get(i);
                            if(!valuesTable.containsKey(at)) {
                                valuesTable.put(at, v);
                            }
                        }
                    }
                    
                    VersionBase vb = hver.getSelectedVersion(ains);                    
                    process.add(ains.getComponent());
                    versions.add(vb);
                }
            }
        }
        
        return valuesTable;
    }       

    /////////////// PRIVATE METHODS ////////////////////////////////////////////    
    
    // Esse metodo necessita que os portos tenham sido resolvidos antes
    private void buildAttributeTable() {
        attributeTable = new Hashtable<String, Attr>();

        Iterator ite = attributesIterator();
        while(ite.hasNext()) {
            Attr at = (Attr) ite.next();
            attributeTable.put(at.getFullName(), at);
        }

        ite = connectionsIterator();
        while(ite.hasNext()) {
            Connection c = (Connection) ite.next();
            Iterator atite = c.attributesIterator();
            while(atite.hasNext()) {
                Attr at = (Attr) atite.next();
                attributeTable.put(at.getFullName(), at);
            }
        }
        
        ite = componentInstancesIterator();
        while(ite.hasNext()) {
            CompInstance ains = (CompInstance) ite.next();
            CompBase abase = ains.getComponent();

            abase.buildElementTable();
            attributeTable.putAll(abase.getAttributeTable());
        }

        ite = versionsIterator();
        while(ite.hasNext()) {
            Hver ver = (Hver) ite.next();

            Iterator atite = ver.attributesIterator();
            while(atite.hasNext()) {
                Attr at = (Attr) atite.next();
                attributeTable.put(at.getFullName(), at);
            }

            Iterator mite = ver.methodsIterator();
            while(mite.hasNext()) {                     
               Method m = (Method) mite.next();
               atite = m.attributesIterator();
               while(atite.hasNext()) {
                   Attr at = (Attr) atite.next();
                   attributeTable.put(at.getFullName(), at);
               }
            }

            Iterator cite = ver.connectionsIterator();
            while(cite.hasNext()) {
                Connection c = (Connection) cite.next();
                atite = c.attributesIterator();
                while(atite.hasNext()) {
                    Attr at = (Attr) atite.next();
                    attributeTable.put(at.getFullName(), at);
                }
            }

            Iterator aite = ver.componentInstancesIterator();
            while(aite.hasNext()) {
                CompInstance ains = (CompInstance) aite.next();
                CompBase abase = ains.getComponent();

                abase.buildElementTable();
                attributeTable.putAll(abase.getAttributeTable());
            }
        }
    }
        
    private void copyChildTreeNodes(DefaultMutableTreeNode tnew, 
            DefaultMutableTreeNode toCopy) {
        
        Enumeration c = toCopy.children();
        while(c.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) c.nextElement();
            DefaultMutableTreeNode newchild = (DefaultMutableTreeNode) child.clone();
            tnew.add(newchild);
            
            copyChildTreeNodes(newchild, child);
        }
    }           

    private void resolveDefVerReferences(Topology parent, Hver ret, DefVer nv) {
        // procura na topologia pela instancia do ator
        CompInstance ains = parent.findComponentInstance(nv.getInstanceName());
        if(ains == null) { // procura nessa versao
            Iterator aite = ret.componentInstancesIterator();
            while(aite.hasNext()) {
                ains = (CompInstance) aite.next();
                if(ains.getFullInstanceName().equals(nv.getInstanceName())) {
                    break;
                }
            }
        }

        // ains nao pode ser null aqui, pois no clone, se assume que foi corretamente
        // construido o modelo
        nv.setSelectedInstance(ains);

        CompBase abase = ains.getComponent();
        VersionBase ver = abase.getVersion(nv.getVersionName());
        nv.setSelectedVersion(ver);
    }
 
    private void resolveExportedPortReferences(Topology parent, Hver ver, ExportedPort newep,
            ExportedPort oldep) {
        for(int i = 0;i < oldep.getRefPorts().size();i++) {
            VirtualPort toclone = oldep.getRefPorts().get(i);
            VirtualPort dc = parent.findPort(toclone.getLocalName());
            if(dc == null) { 
                dc = findPortInVersionComponentInstance(ver, toclone.getLocalName());
                if(dc == null) {
                    dc = findPortInVersions(ver, toclone.getLocalName());
                    if(dc == null) {
                        System.err.println("hver exportedPort clone error: did not find " +
                            toclone.getLocalName());
                    }
                }                
            }
            newep.addRefPort(dc);
        }        
    }
   
    private void resolveConnectionReferences(Topology parent, Hver ver, Connection c_cloned, 
            Connection c)  {
        // faz as referencias para as dports
        Iterator pite = c.portsIterator();
        while(pite.hasNext()) {
            VirtualPort dp = (VirtualPort) pite.next();
            // procura o porto em todas as intancias da topologia onde
            // a versao esta inserida
            VirtualPort dc = parent.findPort(dp.getLocalName());
           
            // Procura nas instancias adicionadas nessa versao
            if(dc == null) {
                dc = findPortInVersionComponentInstance(ver, dp.getLocalName());
                // considera todas as versoes definidas aqui, e procura nelas
                // por portos adicionais
                if(dc == null) {        
                    dc = findPortInVersions(ver, dp.getLocalName());
                    if(dc == null) {
                        System.err.println("resolveConnectionReferences clone error: did not find " +
                                dp.getLocalName());
                    }
                }    
            }
            try {
                c_cloned.addPort(dc);
            }
            catch(NonUniqueNameException ex) {
                System.out.println(ex);
            }
        }
    }

    private VirtualPort findPortInVersionComponentInstance(Hver ver, 
            final String portName) {
        VirtualPort dc = null;
        
        Iterator aite = ver.componentInstancesIterator();
        while(aite.hasNext()) {
            CompInstance ains = (CompInstance) aite.next();
            CompInterface ai = (CompInterface) ains.getComponent();

            Iterator portsite = ai.getPorts();
            while(portsite.hasNext()) {
                dc = (VirtualPort) portsite.next();
                if(portName.equals(dc.getFullName())) {
                    return dc;
                }
            }
        }
        return null;
    }
    
    private VirtualPort findPortInVersions(final Hver ret, final String portName) {
        VirtualPort dc = null;       
        Iterator dfite = ret.defVersionsIterator();
        while(dfite.hasNext()) {
            DefVer df = (DefVer) dfite.next();
            VersionBase insv = (VersionBase) df.getSelectedVersion();
            dc = insv.findPort(portName);
            if(dc != null) {
                break;
            }
        }
        return dc;
    }
    
    /////////////// PRIVATE VARIABLES //////////////////////////////////////////

    private Edges componentInstances;
    private Edges connections;

    private DefaultMutableTreeNode treeNode;

    private Hashtable<Attr, Value> valuesTable;
}