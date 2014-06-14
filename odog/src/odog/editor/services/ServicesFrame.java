/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.editor.services;

import odog.configuration.BaseConfiguration;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import javax.swing.JDialog;

/**
 *
 * @author  ivan
 */
public class ServicesFrame extends JDialog {
    
    /** Creates new form ServicesFrame */
    public ServicesFrame(BaseConfiguration conf) {
        this.setLocation(300,300);
        this.setSize(600,400);
        this.setModal(true);

        loadState(conf);

        servicesPanel sp = new servicesPanel();
        sp.setDataWarper(services, null, this);

        add(sp);
    }

    public void saveState() {
        try {
            File f = new File(file);
            FileOutputStream fos = new FileOutputStream(f);
            PrintStream ps = new PrintStream(fos);
            
            ps.print(services.exportXML());
            
            ps.close();
            fos.close();
        }
        catch(IOException ex) {
            System.out.println(ex);
        }
    }
    
    public void setSelectedService(Service s) {
        selectedService = s;
    }

    public String getSelectedServiceName() {
        if(selectedService == null) return null;
        return selectedService.getName();
    }
            
    public void commit() {
        saveState();
        setVisible(false);
    }
    
    public void cancel() {
        setVisible(false);
    }
    
    ////////////////////////////// PRIVATE METHODS /////////////////////////////
    
    private void loadState(BaseConfiguration configuration) {
        file = configuration.getOdogServices() + "services.xml";

        File f = new File(file);
        if(!f.exists()) {
            services = new Services();
            return;
        }

        services = ServicesParser.parseServices(file);
        if(ServicesParser.errorMessage != null) {
            System.out.println(ServicesParser.errorMessage);
            services = new Services();
        }
    }   
    
    ////////////////////////////// PRIVATE VARIABLES ///////////////////////////

    private servicesPanel sp;
    private Services services;
    private String file;
    
    private Service selectedService;
}