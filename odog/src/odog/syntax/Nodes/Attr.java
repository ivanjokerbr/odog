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

public class Attr extends Node implements Comparable, Referenciado {

    public Attr(String name) {
        super(name);
        references = new LinkedList<Referencia>();
    }

    ////////////////////////////////////////////////////////////////////////////

    public int compareTo(Object obj) {
        Attr at2 = (Attr) obj;
        return getFullName().compareTo(at2.getFullName());    
    }
    
    public int getType() {
        return ATTR;
    }
    
    public String toString() {
        return getFullName();
    }
        
    public Attr clone() {
        Attr ret = new Attr(name);

        ret.setClassification(classification.clone());
        if(defaultValue != null) {
            ret.setDefaultValue(defaultValue.clone());
        }

        return ret;
    }
    
    public String exportXML(int ident) {
        StringBuffer buf = new StringBuffer();
        
        String pad = identForXML(ident);
        
        buf.append(pad + "<attribute name=\"" + name + "\">\n");

        buf.append(classification.exportXML(ident + 2));
        if(defaultValue != null) {        
            buf.append(defaultValue.exportXML(ident + 2));
        }
        buf.append(pad + "</attribute>\n");
        
        return buf.toString();
    }

    public void setClassification(AttrClass clas) {
        classification = clas;
        classification.setContainer(this);
    }
    
    public AttrClass getClassification() {
        return classification;
    } 
   
    public void setDefaultValue(Value defaultv) {
        if(defaultValue != null) {
            defaultValue.setContainer(null);
        }
        defaultValue = defaultv;
        
        if(defaultValue != null) {
            defaultValue.setContainer(this);        
            defaultValue.setDefaultValue(true);
        }
    }

    public Value getDefaultValue() {
        return defaultValue;
    }

    public Iterator getAllConnectedNodes() {
        LinkedList ret = new LinkedList();
        if(classification != null) {
            ret.add(classification);
        }
        if(defaultValue != null) {
            ret.add(defaultValue);
        }

        return ret.iterator();
    }

    //******** METODOS RELATIVOS A REFERENCIA QUE UM OBJETO VALUE PODE TER A UM
    //******** ATRIBUTO
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
    }

    public void updateReferences() {
        for(Referencia v : references) {
            v.update(this);
        }
    }
    
    public List<Referencia> references() {
        return references;
    }

    // Retorna um objeto Attr "vazio" (sem valor) para o tipo de um porto de dados
    public static Attr getPortTypeAttribute() {
        Attr ret = new Attr("portType");
        ret.setClassification(new AttrClass(true, true, true));
        
        return ret;
    }
    
    public static Value getValueOf(Attr attribute, VersionBase ver) {
        Value value = attribute.getDefaultValue();

        Iterator ite = ver.valuesIterator();
        while(ite.hasNext()) {
            Value v = (Value) ite.next();
            for(int i = 0;i < v.getAssociatedAttributes().size();i++) {
                if(v.getAssociatedAttributes().get(i) == attribute) {
                    value = v;
                    break;
                }
            }
        }

        return value;
    }
    
    // a value associated with an instance has higher priority than one
    // associated with a version
    public static Value getValueOf(Attr attribute, VersionBase ver, CompInstance
            ains) {
        Value value = attribute.getDefaultValue();

        Iterator ite = ver.valuesIterator();
        while(ite.hasNext()) {
            Value v = (Value) ite.next();
            for(int i = 0;i < v.getAssociatedAttributes().size();i++) {
                if(v.getAssociatedAttributes().get(i) == attribute) {
                    value = v;
                    break;
                }
            }
        }
        
        ite = ains.valuesIterator();
        while(ite.hasNext()) {
            Value v = (Value) ite.next();
            for(int i = 0;i < v.getAssociatedAttributes().size();i++) {
                if(v.getAssociatedAttributes().get(i) == attribute) {
                    value = v;
                    break;
                }
            }
        }

        return value;
    }
    
    ////////////////////////////////////////////////////////////////////////////

    private AttrClass classification;
    private Value defaultValue;
    
    private LinkedList<Referencia> references;
}
