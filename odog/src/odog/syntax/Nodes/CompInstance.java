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

public class CompInstance extends Node implements Comparable, Referenciado {
    
    // instanceName = nome da instance
    // component name = nome do ator como consta na biblioteca
    // libURL = diretorio da biblioteca, referente a algum diretorio da variavel
    // de configuracao basica
    public CompInstance(String instanceName, String componentName, String libURL) {
        super("ComponentInstance_" + instanceName);
        values = new Edges(this);
        
        this.instanceName = instanceName;
        this.componentName = componentName;
        this.libURL = libURL;

        references = new LinkedList<Referencia>();
    }
    
    ////////////////////////////////////////////////////////////////////////////

    public int compareTo(Object obj) {
        return getFullInstanceName().compareTo(((CompInstance)obj).getFullInstanceName());
    }
    
    public String getFullInstanceName() {
        return container.getFullName() + "." + instanceName;
    }
   
    public String getInstanceName() {
        return instanceName;
    }
    
    public void setInstanceName(String name) {
        instanceName = name;
    }
    
    public int getType() {
        return COMPONENTINSTANCE;
    }

    public CompInstance clone() {
        CompInstance ret = new CompInstance(instanceName, componentName, libURL);
        ret.setComponent(component.clone());
        
        Iterator ite = valuesIterator();
        while(ite.hasNext()) {
            Value v = (Value) ite.next();
            try {
                ret.addValue(v.clone());
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
        
        buf.append(pad + "<compInstance instanceName=\"" + instanceName + "\" compName=\"" + 
                componentName + "\" libraryURL=\"" + libURL + "\">\n");

        values.exportXML(buf, ident + 2);

        buf.append(pad + "</compInstance>\n");

        return buf.toString();
    }

    public String toString() {
        return getFullInstanceName() + "(" + componentName + ", " + libURL + ")";
    }

    public void addValue(Value v) throws NonUniqueNameException {
        values.add(v);
    }

    public void removeValue(Value v) {
        values.remove(v);
        v.removeReferences();
    }

    public Iterator valuesIterator() {
        return values.iterator();
    }

    public Value getValue(String name) {
        return (Value) values.find(name);
    }
    
    public void setComponentName(String name) {
        componentName = name;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponent(CompBase instance) {
        component = instance;
        instance.setComponentInstance(this);
    }

    public CompBase getComponent() {
        return component;
    }

    public void setLibraryURL(String url) {
        libURL = url;
    }
    
    public String getLibraryURL() {
        return libURL;
    }
    
    public Object getAttributeValue(String attribute) throws NonExistentAttributeException {
        if(attribute.equals("compName")) {
            return new String(componentName);
        }
        else
        if(attribute.equals("libURL")) {
            return new String(libURL);
        }
        else
        if(attribute.equals("instanceName")) {
            return new String(instanceName);
        }
        else
        if(attribute.equals("localInstanceName")) {
            return new String(getLocalName());
        }
        else
        if(attribute.equals("fullInstanceName")) {
            return new String(getFullInstanceName());
        }
        else {
            return super.getAttributeValue(attribute);
        }
    }

    public Class getAttributeType(String attribute) throws NonExistentAttributeException {
        if(attribute.equals("compName") || attribute.equals("libURL") ||
                attribute.equals("instanceName") || attribute.equals("fullInstanceName") ||
                attribute.equals("localInstanceName")) {
            return String.class;
        }
        else {
            return super.getAttributeType(attribute);
        }
    }
    
    public Iterator getAllConnectedNodes() {
        List<Node> ret = new LinkedList<Node>();
        ret.addAll(values.getList());

        if(component != null) {
            ret.add((Node)component);
        }

        return ret.iterator();
    }

    public boolean containsName(String name, int nodetype) {
        switch(nodetype) {
            case VALUE: {
                return values.containsName(name);
            } 
        }
        return false; 
    }

    // Return the list of attributes that belongs to the interface of the 
    // referenced component. Those attributes are only visible for this component
    // instance. Used when setting a value within the component instance
    public List<Attr> getInternalInterfaceAttributes() {
        CompInterface intf = (CompInterface) component;
        return intf.getInterfaceAttributes();
    }
    
    // Return the list of attributes that are viewed by the topology that
    // contains this instance.
    public List<Attr> getExternalAttributes(VersionBase ver) {
        return component.getAllRecomputedAttributes(ver);
    }    

    ///////////////////////////// Busca por nos ////////////////////////////////

    public Node findVersion(String name) {        
        Iterator ite = component.versionsIterator();
        while(ite.hasNext()) {
            Node n = (Node) ite.next();
            if(n.getName().equals(name)) return n;
        }
        
        return null;
    }

    ///////////////////////////// Referencias ////////////////////////////////

    public void addReference(Referencia v) {
        if(references.contains(v)) return;
        references.add(v);
    }

    // Esse metodo deve ser chamado pelo objeto que referencia, qdo por exemplo
    // for descartado.
    public void removeReference(Referencia v) {
        references.remove(v);
    }

    // Esse metodo eh chamado quando por algum container dele for eliminado,
    // entao deve-se desfazer todas as referencias
    public void removeReferences() {
        // faz desse jeito, pois ao remover o valor, ele chama o removeRefence
        Object [] objs = references.toArray();
        for(int i = 0;i < objs.length;i++) {
            ((Referencia) objs[i]).unlink(this);
        }
        // Portos virtuais pode ser exportados ou conectados. Entao continua
        // a exporar nos nos contindos....
        super.removeReferences();
    }

    public void updateReferences() {
        for(Referencia v : references) {
            v.update(this);
        }
        super.updateReferences();
    }
   
    ////////////////////////////// PRIVATE VARIABLES ///////////////////////////

     private Edges values;
    
     // Uma copia da estrutura do ator. O nome dele permanece ator. Eh o nome
     // da instancia que muda.
     private CompBase component;

     private String instanceName;
     private String componentName;
     private String libURL;
     
     private LinkedList<Referencia> references;
}