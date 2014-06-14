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

public class Ver extends VersionBase {
    
    public Ver(String name) {
        super(name);
    }
    
    /////////////////////////////// metodos publicos ///////////////////////////
    
    public int getType() {
        return VER;
    }
    
    public String toString() {
        return name;
    }
    
    public Ver clone() {
        Ver ret = new Ver(name);

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

        ports.exportXML(buf, ident + 2);
        values.exportXML(buf, ident + 2);
        attributes.exportXML(buf, ident + 2);
        methods.exportXML(buf, ident + 2);
        reqserv.exportXML(buf, ident + 2);

        buf.append(pad + "</version>\n");
        
        return buf.toString();
    }

    public Iterator getAllConnectedNodes() {
        List<Node> ret = new LinkedList<Node>();

        ret.addAll(ports.getList());
        ret.addAll(values.getList());
        ret.addAll(attributes.getList());
        ret.addAll(methods.getList());
        ret.addAll(reqserv.getList());

        return ret.iterator();
    }

    public boolean containsName(String name, int nodetype) {
        switch(nodetype) {
            case DPORT: {
                return ports.containsName(name);
            }
            
            case VALUE: {
                return values.containsName(name);
            }
            
            case ATTR: {
                return attributes.containsName(name);
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
 
    ////////////////// private methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\

}
