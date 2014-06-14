/*
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2
 * see LICENSE file
 */
package odog.editor;

import java.awt.BorderLayout;
import java.net.URL;
import java.util.Vector;
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
public class HtmlViewer extends JPanel {
    
    public HtmlViewer(URL initialURL) {
        super();
        setLayout(new BorderLayout());
        
        try {
            final JEditorPane pane = new JEditorPane();
            pane.setContentType("text/html");
            pane.setPage(initialURL);
            pane.setEditable(false);
            pane.addHyperlinkListener(new Hyperactive());

            pageStack = new Vector();
            pageStack.add(initialURL);

            JPanel buttomPanel = new JPanel();
            JButton backButton = new JButton("back");
            backButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    pane.setContentType("text/html");
                    try {
                        if(pageStack.size() == 1) {
                            pane.setPage((URL)pageStack.get(0));       
                        }
                        else {
                            pane.setPage((URL)pageStack.remove(pageStack.size() - 1));
                        }
                    }
                    catch(Exception ex) {
                        System.out.println(ex);
                    }
                }
            });
            buttomPanel.add(backButton);

            JScrollPane sp = new JScrollPane(pane);
            add(sp, BorderLayout.CENTER);
            add(buttomPanel, BorderLayout.NORTH);
        }
        catch(Exception ex) {
            System.out.println(ex);
        }    
    }

    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////
    
    
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
                         pageStack.add(e.getURL());
                     } catch (Throwable t) {
                         t.printStackTrace();
                     }
                 }
             }
         }
     }
    
     private Vector pageStack;
}
