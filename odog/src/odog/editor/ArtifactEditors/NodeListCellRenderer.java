/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.ArtifactEditors;

import odog.syntax.Nodes.CompInstance;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.AttrClass;
import odog.syntax.Nodes.Connection;
import odog.syntax.Nodes.DefVer;
import odog.syntax.Nodes.Dport;
import odog.syntax.Nodes.ExportedPort;
import odog.syntax.Nodes.Method;
import odog.syntax.Nodes.Node;
import odog.syntax.Nodes.Value;
import odog.syntax.Nodes.VirtualPort;
import java.awt.Color;
import java.awt.Component;
import java.util.Iterator;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author ivan
 */
public class NodeListCellRenderer extends JLabel implements ListCellRenderer {
    
    /** Creates a new instance of NodeListCellRenderer */
    public NodeListCellRenderer() {
        super();
        setOpaque(true);
    }
    
    public Component getListCellRendererComponent(JList list,
                                                   Object value,
                                                   int index,
                                                   boolean isSelected,
                                                   boolean cellHasFocus) {
        Node n = (Node) value;
        String text = n.getName();

        switch(n.getType()) {
            
            case Node.COMPONENTINSTANCE: {                
                CompInstance ains = (CompInstance) n;
                text = ains.getInstanceName();
                text = text + " (instance of " + ains.getComponent().getName() +
                        ") (library " + ains.getLibraryURL() + ")";
            } break;

            case Node.ATTR: {
                Attr at = (Attr) n;
                AttrClass atclass = at.getClassification();
                Value v = at.getDefaultValue();
                text = text + "(";
                
                if(atclass.isVisible()) {
                    text = text + "visible, ";                    
                }
                else {
                    text = text + "invisible, ";
                }
                
                if(atclass.hasData()) {
                    text = text + "has data, ";    
                }
                else {
                    text = text + "no data, ";
                }
                
                if(atclass.isStatic()) {
                    text = text + "static)";
                }
                else {
                    text = text + "not static)";
                }
                
                if(v == null) {
                    text = text + " no default value";
                }
                else {
                    text = text + " default value = " + v.getValueExpr();
                }
            } break;
            
            case Node.DPORT : {                
                Dport dp = (Dport) n;
                Attr type = dp.getDataType();
                Value v = type.getDefaultValue();
                if(dp.isInput()) {
                    text = text + " (type " + v.getValueExpr() + " ,input)";
                }
                else 
                if(dp.isOutput()) {
                    text = text + " (type " + v.getValueExpr() + " ,output)";
                } 
            } break;
            
            case Node.CONNECTION: {
                Connection c = (Connection) n;
                if(c.getOutputPort() == null) {
                    text = text + " { NULL => ";
                }
                else {
                    text = text + " { " + c.getOutputPort().getFullName() + " => ";
                }
                Iterator ite = c.inputPortsIterator();
                while(ite.hasNext()) {
                    text = text + ((VirtualPort)ite.next()).getFullName() + " ";
                }
                text = text + "}";
            } break;
            
            case Node.DEFVER: {
                DefVer df = (DefVer) n;
                text = df.getInstanceName() + " <= " + df.getVersionName(); 
            } break;
            
            case Node.EXPORTEDPORT: {
                ExportedPort ep = (ExportedPort) n;
                if(ep.getRefPorts().size() == 1) {
                    text = ep.getName() + " exports( " + ep.getRefPorts().get(0) +
                            " )";
                }
                else {
                    text = ep.getName() + " exports( " + ep.getRefPorts().get(0);
                    for(int i = 1;i < ep.getRefPorts().size();i++) {
                        text = text + ", " + ep.getRefPorts().get(i);
                    }
                    text = text + ")";
                }
            } break;
            
            case Node.VALUE: {
                Value v = (Value) n;
                if(v.isDefaultValue()) {
                    text = "(attr) " + v.getContainer().getFullName() + " <- " +
                        v.getValueExpr() + " (" + v.getValueType() + ")";
                }
                else 
                if(v.getAssociatedAttributes().size() == 1) {                
                    text = "(attr) " + v.getAssociatedAttributes().get(0).getFullName() + 
                            " <- " + v.getValueExpr() + " (" + v.getValueType() + ")";
                }
                else
                if(v.getAssociatedAttributes().size() < 1) {
                    System.out.println("Value " + v + " with zero");
                }
                else {
                    text = "(attr) " + v.getAssociatedAttributes().get(0).getFullName();
                    for(int i = 1;i < v.getAssociatedAttributes().size();i++) {
                        text = text + ", " + v.getAssociatedAttributes().get(i).getFullName();
                    }
                    text = text + " <- " + v.getValueExpr() + " (" + v.getValueType() + ")";
                }
                
            } break;

            case Node.METHOD: {
                Method m = (Method) n;
                text = text + "(" + m.getLanguage() + ")";
            } break;
        }
        setText(text);
        
        if(isSelected) {
            setBackground(new Color(125, 214, 255));
        }
        else {
            setBackground(Color.WHITE);
        }

        return this;
    }
    
}
