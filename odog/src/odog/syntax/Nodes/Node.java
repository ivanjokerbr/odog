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
 * Classe para representar um elemento do grafo que descreve um ator qualquer. Duas 
 * propriedades estao associadas a esta classe basica: nome e o no que a contem.
 * @author Ivan 
 * @version 1.0
 */
public class Node {
    
    public Node(String name) {
        this.name = name;
    }

    // ESSES VALORES TEM QUE BATER COM O DA CLASSE NODETYPE DO CHECK, PORTANTO AO
    // MUDAR AQUI, MUDAR LA TB....
    public static final int UNKNOW = 0;
    public static final int DPORT = 1;
    public static final int ACOMP = 2;
    public static final int ATTR = 3;
    public static final int TOPOLOGY = 4;
    public static final int COMPONENTINSTANCE = 5;
    public static final int ATTRCLASS = 6;
    public static final int VER = 7;
    public static final int HVER = 8;
    public static final int VALUE = 10;
    public static final int REQSERV = 11;
    public static final int METHOD = 12;
    public static final int CONNECTION = 13;
    public static final int EXPORTEDPORT = 14;
    public static final int DEFVER = 15;
    
    /////////////////// PUBLIC METHODS /////////////////////////////////////////
    
    public static String replaceDotForUnd(String s) {        
        String [] sn = s.split("\\.");
        String ret = sn[0]; 
        for(int i = 1;i < sn.length;i++) {
            ret = ret + "_" + sn[i];
        }
        return ret;
    }
    
    public String toString() {
        return getName();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        updateReferences();
    }
    
    public void setContainer(Node n) {
        container = n;
    }
    
    public Node getContainer() {
        return container;
    }

    public String exportXML(int ident) {
        return "";
    }
    
    /* Retorna o nome completo do no, percorrendo a lista de nos pais.
     */
    public String getFullName() {
        if(container == null) {
            return name;
        }
        return container.getFullName() + "." + name;
    }
    
    // Returns the name of the node, up to the first level of instance.
    // If this node is an component instance, it will be ComponentName.InstanceName if not
    // within a version, otherwise it will be ComponentName.VersionName.InstanceName
    // Otherwise, it will be ComponentName.instanceName.nodeName
    public String getLocalName() {
        if(container == null) {
            CompBase abase = (CompBase) this;
            CompInstance ains = abase.getComponentInstance();
            if(ains == null) {
                return name;
            }
            else {
                Node n = ains.getContainer();
                if(n instanceof VersionBase) {
                    Node cont = n.getContainer();
                    return cont.getName() + "." + n.getName() + "." +
                            ains.getInstanceName();
                }
                else {
                    return n.getName() + "." + ains.getInstanceName();    
                }
            }
        }
        
        if(this instanceof CompInstance) {
            if(container instanceof VersionBase) {
                return container.getContainer().getName() + "." + container.getName() + 
                        "." + ((CompInstance) this).getInstanceName();
            }
            else {
                return container.getName() + "." + ((CompInstance) this).getInstanceName();
            }
        }
        return container.getLocalName() + "." + name;
    }
    
    // Esses dois metodos sao usados pelo verificador de regras
    public Object getAttributeValue(String attribute) throws NonExistentAttributeException {
        if(attribute.equals("name")) {
            return name;
        }
        else
        if(attribute.equals("fullName")) {
            return getFullName();
        }
        else
        if(attribute.equals("nodeId")) {
            return new String("" + this.hashCode());
        }
        else {
            throw new NonExistentAttributeException("Attribute " + attribute + 
                    " does not exits in node " + name);
        }
    }
    
    public Class getAttributeType(String attribute) throws NonExistentAttributeException {
        if(attribute.equals("name")) {
            return String.class;
        }
        else
        if(attribute.equals("fullName")) {
            return String.class;
        }
        else
        if(attribute.equals("nodeId")) {
            return String.class;
        }
        else {
            throw new NonExistentAttributeException("Attribute " + attribute + 
                    " does not exits in node " + name);
        }
    }

    // Retorna um iterator para qualquer no associado a este
    public Iterator getAllConnectedNodes() {
        return (new LinkedList()).iterator();
    }

    public int getType() {
        return UNKNOW;
    }

    public boolean containsName(String name, int nodetype) {
        return false;
    }
    
    // metodo basico sobre remocao de referencias
    public void removeReferences() {
        Iterator ite = getAllConnectedNodes();
        while(ite.hasNext()) {
            Node n = (Node) ite.next();
            n.removeReferences();
        }
    }
    
    // metodo basico para atualizacao de referencias
    public void updateReferences() {
        Iterator ite = getAllConnectedNodes();
        while(ite.hasNext()) {
            Node n = (Node) ite.next();
            n.updateReferences();
        }
    }
    
    /////////////////////// Protected Methods  //////////////////////////////////
    
    protected String identForXML(int ident) {
        char [] ch = new char[ident];
        for(int i = 0;i < ident;i++) ch[i] = ' ';
        
        return new String(ch);
    }
    
    ////////////////////////////// Attributes //////////////////////////////////
    
    protected Node container;
    protected String name;
}