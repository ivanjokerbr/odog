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
public abstract class VirtualPort extends Node implements Referenciado {

    public VirtualPort(String name) {
        super(name);   
        references = new LinkedList<Referencia>();
    }

    /////////////////////////////// public methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    
    // verifica se o port vp esta na mesma versao que container, ou na mesma topologia,
    // ou na versao selecionada pela versao container, de algum ator
    public static boolean checkPortLocation(VirtualPort vp, Hver container) {
        Node ptr = vp.getContainer();
        while(ptr != null && !(ptr instanceof Hver) && !(ptr instanceof Topology) &&
                !(ptr instanceof Acomp) && !(ptr instanceof Ver)) {
            ptr = ptr.getContainer();
        }

        if(ptr == null) return false;
        
        if(ptr instanceof Topology || ptr instanceof Acomp) {
            CompBase abase = (CompBase) ptr;
            CompInstance ains = abase.getComponentInstance(); 
            Node ptr2 = ains.getContainer();
            if(ptr2 instanceof Hver) {
                if(((Hver)ptr2).equals(container)) {
                    return true;  // eh mesma versao
                }
                else {
                    return false;
                }
            }
            else {
                // se o pai for uma topologia, obrigatoriamente eh a mesma.
                return true;
            }
        }
        
        // como eh versao de ator atomico, so pode ser de ator instanciado
        // na topologia da versao. Tem que verificar se a versao eh a selecionada
        if(ptr instanceof Ver || ptr instanceof Hver) {
            VersionBase ver = (VersionBase) ptr;
            CompBase component = (CompBase) ptr.getContainer();
            CompInstance ains = component.getComponentInstance();

            Iterator ite = container.defVersionsIterator();
            while(ite.hasNext()) {
                DefVer defver = (DefVer) ite.next();
                if(defver.getSelectedInstance().equals(ains)) {
                    if(defver.getVersionName().equals(ver.getName())) {
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            }

            // nao achou a definicao de versao para o ator atomico, ou a versao
            // era outra
            return false;
        }
        
        return false;
    }
    
    public abstract Attr getDataType();
    
    public abstract Iterator attributesIterator();
    
    public abstract boolean isInput();
    
    public abstract  boolean isOutput();
    
    ///////////////////////////// SOBRE as referencias /////////////////////////
    
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
        super.removeReferences();
    }

    public void updateReferences() {
        Object [] objs = references.toArray();
        for(int i = 0;i < objs.length;i++) {
            ((Referencia) objs[i]).update(this);
        }
        super.updateReferences();
    }

    ////////////////////////////// PRIVATE VARIABLES ///////////////////////////
    
    private LinkedList<Referencia> references;    
}
