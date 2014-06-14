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

public class Acomp extends CompBase implements CompInterface {

    public Acomp(String name) {
        super(name);
    }

    //////////////////////////////// public methods ////////////////////////////

    public int getType() {
        return ACOMP;
    }
    
    public Acomp clone() {
        Acomp ret = new Acomp(name);

        Iterator ite = portsIterator();
        while(ite.hasNext()) {
            Dport dp = (Dport) ite.next();
            try {
                ret.addPort(dp.clone());
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

        ite = versionsIterator();
        while(ite.hasNext()) {
            Ver v = (Ver) ite.next();
            try {
                ret.addVersion(v.clone());
            }
            catch(NonUniqueNameException ex) {
                System.out.println(ex); 
            }   
        }
       
        DefaultMutableTreeNode tn = (DefaultMutableTreeNode) treeNode.clone();
        copyChildTreeNodes(tn, treeNode);
        ret.setTreeNode(tn);

        ret.buildAttributeTable();
        Hashtable <String, Attr> retTable = ret.getAttributeTable();
        
        ite = ret.versionsIterator();
        while(ite.hasNext()) {
            Ver v = (Ver) ite.next();
            Ver originalVersion = (Ver) getVersion(v.getName());
            
            Iterator vite = v.valuesIterator();
            while(vite.hasNext()) {
                Value value = (Value) vite.next();
                Value originalValue = originalVersion.getValue(value.getName());
                
                for(int i = 0;i < originalValue.getAssociatedAttributes().size();i++) {
                    Attr target = originalValue.getAssociatedAttributes().get(i);
                    String name = getLocalAttrName(target);

                    Attr attribute = retTable.get(name);
                    if(attribute == null) {
                        System.out.println("Acomp clone : did not find attribute " +
                                name);
                    }
                    value.addAssociatedAttribute(attribute);            
                }
            }
        }
        return ret;
    }        

    public String exportXML(int ident) {
        StringBuffer buf = new StringBuffer();
        
        String pad = identForXML(ident);
        
        buf.append(pad + "<atomicComponent name=\"" + name + "\">\n");

        ports.exportXML(buf, ident + 2);
        attributes.exportXML(buf, ident + 2);
        versions.exportXML(buf, ident + 2);

        buf.append(pad + "</atomicComponent>\n");

        return buf.toString();
    }
    
    public String toString() {
        return "<Atomic Component> " + name;
    }

    ////////////////////// REFERENTE A Interface ///////////////////////////////
    
    public void buildElementTable() {
       buildAttributeTable();
    }

    // O parametro para esse metodo tem que ser o nome composto do no, onde a 
    // primeira parte do nome eh o nome do ator (nao instancias)
    public Node getNode(String completeName) {
        if(attributeTable.containsKey(completeName)) {
            return (Node) attributeTable.get(completeName);
        }
        return null;
    }
    
    public void setTreeNode(DefaultMutableTreeNode node) {
        treeNode = node;
    }
    
    public DefaultMutableTreeNode getTreeNode() {
        return treeNode;
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

    // returns all attributes of this component. Every time this method is called
    // it will recompute the table
    public List<Attr> getAllRecomputedAttributes(VersionBase version) {
        LinkedList<Attr> ret = new LinkedList<Attr>();
        
        Ver ver = (Ver) version;
        
        Iterator ite = attributesIterator();
        while(ite.hasNext()) {
            Attr at = (Attr) ite.next();
            ret.add(at);
        }

        ite = portsIterator();
        while(ite.hasNext()) {
            Dport dp = (Dport) ite.next();
            Iterator atite = dp.attributesIterator();
            while(atite.hasNext()) {
                Attr at = (Attr) atite.next();
                ret.add(at);                
            }
            Attr at = dp.getDataType();
            if(at != null) {
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

        Iterator dite = ver.portsIterator();
        while(dite.hasNext()) {
            Dport dp = (Dport) dite.next();

            Iterator atdite = dp.attributesIterator();
            while(atdite.hasNext()) {
                Attr at = (Attr) atdite.next();
                ret.add(at);
            }

            Attr at = dp.getDataType();
            if(at != null) {
                ret.add(at);                
            }   
        }

        return ret;
    }
    
    public boolean containsName(String name, int nodetype) {
        switch(nodetype) {
            case DPORT: {
                return ports.containsName(name);
            }
            
            case ATTR: {
                return attributes.containsName(name);
            }
            
            case VER: {
                return versions.containsName(name);
            }
        }

        return false; 
    }

    public Iterator getAllConnectedNodes() {
        List<Node> ret = new LinkedList<Node>();
        
        ret.addAll(ports.getList());
        ret.addAll(attributes.getList());
        ret.addAll(versions.getList());
        
        return ret.iterator();
    }
    
    /////////////////////// private methods ////////////////////////////////////
    
    // Coloca todos os atributos do ator em uma tabela indexada pela nome comleto dele.
    // O nome dos atributos nessa tabela nao eh o completo, eh a partir do ator
    private void buildAttributeTable() {
        attributeTable = new Hashtable<String, Attr>();
        
        Iterator ite = attributesIterator();
        while(ite.hasNext()) {
            Attr at = (Attr) ite.next();
            attributeTable.put(at.getFullName(), at);
        }
        
        ite = portsIterator();
        while(ite.hasNext()) {
            Dport dp = (Dport) ite.next();
            Iterator atite = dp.attributesIterator();
            while(atite.hasNext()) {
                Attr at = (Attr) atite.next();
                attributeTable.put(at.getFullName(), at);
            }
            Attr at = dp.getDataType();
            if(at != null) {
                attributeTable.put(at.getFullName(), at);
            }
        }

        ite = versionsIterator();
        while(ite.hasNext()) {
            Ver v = (Ver) ite.next();
            Iterator atite = v.attributesIterator();
            while(atite.hasNext()) {
                Attr at = (Attr) atite.next();
                attributeTable.put(at.getFullName(), at);
            }

            Iterator mite = v.methodsIterator();
            while(mite.hasNext()) {
               Method m = (Method) mite.next();
               atite = m.attributesIterator();
               while(atite.hasNext()) {
                   Attr at = (Attr) atite.next();
                   attributeTable.put(at.getFullName(), at);
               }
            }
            
            Iterator dite = v.portsIterator();
            while(dite.hasNext()) {
                Dport dp = (Dport) dite.next();
                
                Iterator atdite = dp.attributesIterator();
                while(atdite.hasNext()) {
                    Attr at = (Attr) atdite.next();
                    attributeTable.put(at.getFullName(), at);
                }
                
                Attr at = dp.getDataType();
                if(at != null) {
                    attributeTable.put(at.getFullName(), at);
                }   
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

    private String getLocalAttrName(Attr at) {
        String name = at.getName();
        Node n = at;
        while(!(n instanceof Acomp)) {
            n = n.getContainer();
            name = n.getName() + "." + name;        
        }
        return name;
    }

    ///////////////////////////// private attributes ///////////////////////////

    private DefaultMutableTreeNode treeNode;
}