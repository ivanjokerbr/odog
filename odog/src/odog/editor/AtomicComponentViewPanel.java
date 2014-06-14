/*
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2
 * see LICENSE file
 */
package odog.editor;

import odog.configuration.BaseConfiguration;
import odog.syntax.Nodes.Acomp;
import odog.syntax.Nodes.Attr;
import odog.syntax.Nodes.Dport;
import odog.syntax.Nodes.Method;
import odog.syntax.Nodes.Reqserv;
import odog.syntax.Nodes.Value;
import odog.syntax.Nodes.Ver;
import odog.codegen.util.FileGenerator;
import odog.codegen.util.FileGeneratorParser;
import java.awt.BorderLayout;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

/**
 *
 * @author ivan
 */
public class AtomicComponentViewPanel extends JPanel {
    
    public AtomicComponentViewPanel(String location, Acomp comp, Ver version) {
        super();
        setLayout(new BorderLayout());
        
        final String text = getComponentText(comp, version, location);
        
        final JEditorPane pane = new JEditorPane("text/html", text);
        pane.setEditable(false);
        pane.addHyperlinkListener(new Hyperactive());
        
        JPanel buttomPanel = new JPanel();
        JButton backButton = new JButton("back");
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pane.setContentType("text/html");
                pane.setText(text);       
            }
        });
        buttomPanel.add(backButton);
       
        JScrollPane sp = new JScrollPane(pane);
        add(sp, BorderLayout.CENTER);
        add(buttomPanel, BorderLayout.NORTH);
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    private String getComponentText(Acomp comp, Ver version, String location) {
        String loc = System.getenv("ODOG_WORKSPACE");
        loc = BaseConfiguration.appendSlash(loc) + "/FileGenerators/";
        FileGenerator generator = FileGeneratorParser.parse(loc + "atomicComponentView.xml");
        
        generator.setArgumentValue("compName", comp.getName());
        
        String inputList = "";
        String outputList = "";
        
        Iterator ite = comp.portsIterator();
        while(ite.hasNext()) {
            Dport vp = (Dport) ite.next();
            Attr type = vp.getDataType();
            Value v = Attr.getValueOf(type, version);
            if(v == null) {
                v = type.getDefaultValue();
            }
            if(vp.isInput()) {
                inputList = inputList + "\t" + vp.getName() + " (type " +
                        v.getValueExpr() + ")<br>\n";
            }
            else {
                outputList = outputList + "\t" + vp.getName() + " (type " +
                        v.getValueExpr() + ")<br>\n";
            }
        }
        ite = version.portsIterator();
        while(ite.hasNext()) {
            Dport vp = (Dport) ite.next();
            Attr type = vp.getDataType();
            Value v = Attr.getValueOf(type, version);
            if(v == null) {
                v = type.getDefaultValue();
            }
            if(vp.isInput()) {
                inputList = inputList + "\t" + vp.getName() + " (type " +
                        v.getValueExpr() + ")<br>\n";
            }
            else {
                outputList = outputList + "\t" + vp.getName() + " (type " +
                        v.getValueExpr() + ")<br>\n";
            }
        }
        generator.setArgumentValue("inputPortList", inputList);
        generator.setArgumentValue("outputPortList", outputList);

        String attributeList = "";
        ite = comp.attributesIterator();
        while(ite.hasNext()) {
            Attr at = (Attr) ite.next();
            if(!at.getClassification().isVisible()) continue;
            
            Value v = Attr.getValueOf(at, version);
            if(v == null) {
                v = at.getDefaultValue();
            }
            
            if(at.getClassification().isStatic()) {
                attributeList = attributeList + "\t const ";
            }
            else {
                attributeList = attributeList + "\t ";
            }
            attributeList = attributeList + v.getValueType() + " " +
                    at.getName() + " = " + v.getValueExpr() + "<br>\n";
        }
        
        ite = version.attributesIterator();
        while(ite.hasNext()) {
            Attr at = (Attr) ite.next();
            if(!at.getClassification().isVisible()) continue;
            
            Value v = Attr.getValueOf(at, version);
            if(v == null) {
                v = at.getDefaultValue();
            }
            
            if(at.getClassification().isStatic()) {
                attributeList = attributeList + "\t const ";
            }
            else {
                attributeList = attributeList + "\t ";
            }
            attributeList = attributeList + v.getValueType() + " " +
                    at.getName() + " = " + v.getValueExpr() + "<br>\n";
        }
        generator.setArgumentValue("attributeList", attributeList);
        
        String services = "";
        ite = version.reqservIterator();
        while(ite.hasNext()) {
            Reqserv rq = (Reqserv) ite.next();
            services = rq.getName() + " ";
        }
        services = services + "<br>\n";
        generator.setArgumentValue("serviceList", services);
        
        Method method = version.getMethod("init");
        if(method != null) {
            generator.setArgumentValue("initMethodLink", 
                "<a href=\"file:" + location +  method.getCodeURL() + 
                    "\">Init method</a><br>\n");
        }
        else {
            generator.setArgumentValue("initMethodLink", "");
        }
        
        method = version.getMethod("compute");
        if(method != null) {
            generator.setArgumentValue("computeMethodLink", 
                "<a href=\"file:" + location +  method.getCodeURL() + 
                    "\">Compute method</a><br>\n");
        }
        else {
            generator.setArgumentValue("computeMethodLink", "");
        }
        
        method = version.getMethod("fixpoint");
        if(method != null) {
            generator.setArgumentValue("fixpointMethodLink", 
                "<a href=\"file:" + location +  method.getCodeURL() + 
                    "\">Fixpoint method</a><br>\n");
        }
        else {
            generator.setArgumentValue("fixpointMethodLink", "");
        }
 
        method = version.getMethod("finish");
        if(method != null) {
            generator.setArgumentValue("finishMethodLink", 
                "<a href=\"file:" + location +  method.getCodeURL() + 
                    "\">Finish method</a><br>\n");
        }
        else {
            generator.setArgumentValue("finishMethodLink", "");
        }
        
        return generator.toString();
    }
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////

    class Hyperactive implements HyperlinkListener {
         public void hyperlinkUpdate(HyperlinkEvent e) {
             if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                 JEditorPane pane = (JEditorPane) e.getSource();
                 if (e instanceof HTMLFrameHyperlinkEvent) {
                     HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
                     HTMLDocument doc = (HTMLDocument)pane.getDocument();
                     doc.processHTMLFrameHyperlinkEvent(evt);
                 } else {
                     try {
                         pane.setPage(e.getURL());
                     } catch (Throwable t) {
                         t.printStackTrace();
                     }
                 }
             }
         }
     }
}
