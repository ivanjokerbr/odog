/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.codegen;

import odog.configuration.BaseConfiguration;
import odog.codegen.util.FileGenerator;
import odog.codegen.util.FileGeneratorParser;
import java.util.LinkedList;

/** 
 *
 * @author ivan
 */
public class AtomicComponentGenerator {
    
    /** Creates a new instance of AtomicComponentGenerator */
    public AtomicComponentGenerator(String location, String file) {
        String loc = System.getenv("ODOG_CODEGENERATORS");
        if(loc == null) {
            System.err.println("ODOG_CODEGENERATORS must be set");
        }
        loc = BaseConfiguration.appendSlash(loc) + location;
        fileGenerator =  FileGeneratorParser.parse(loc + file);
     
        attributes = new LinkedList<String>();

        signals = "";
        initModule = "";
        computeModule = "";
        finishModule = "";
        fixpointModule = "";

        numberOfConnections = "";
    }
    
    ////////////////////////////// PUBLIC METHODS //////////////////////////////
    
    // Assigns the name of the associated instance of this component, and add
    // the defines for name substituion of the execution methods
    public void setComponentInstanceName(String name) {
        String methods = "#define init() " + name + "_init()\n" +
                "#define compute() " + name + "_compute()\n" +
                "#define finish() " + name + "_finish()\n" +
                "#define compute_wraper(a) " + name + "_compute_wraper(a)\n" +
                "#define fixpoint() " + name + "_fixpoint()\n\n";
        
        fileGenerator.setArgumentValue("instanceExecutionMethods", methods);
    }

    // Only the name of the input queue
    public void setSignals(String sig) {
        signals = sig;
    }

    public void addAttributeLine(String line) {
        attributes.add(line);
    }
    
    public void setInitModule(String m) {
        initModule = m;
    }
    
    public void setComputeModule(String m) {
        computeModule = m;
    }
    
    public void setFinishModule(String m) {
        finishModule = m;
    }
    
    public void setFixpointModule(String m) {
        fixpointModule = m;
    }
    
    public void setNumberOfConnections(String s) {
        numberOfConnections = s;
    }
     
    public void setNameOfConnection(String text) {
        nameOfConnection = text;
    }
      
    public void setReceive(String text) {
        receive = text;
    }
    
    public void setSend(String text) {
        send = text;
    }
    
    public void setSendDelayed(String text) {
        sendDelayed = text;
    }
    
    public void setCanReceive(String text) {
        canReceive = text;
    }
       
    public void setCanSend(String text) {
        canSend = text;
    }
    
    public void setContainerName(String text) {
        containerName = text;
    }

    public void setCname(String text) {
        cname = text;
    }
    
    public String getContainerName() {
        return containerName;
    } 
    
    public String getComponentText() {
        // Os defines ja foram introduzidos no metodo setComponentInstanceName
        
        fileGenerator.setArgumentValue("signals", signals);
        
        // 3. adiciona todos os atributos ao ator
        String attr =  "";
        for(String s : attributes) {
            attr = attr + s + "\n";          
        }
        attr = attr + "\n";
        fileGenerator.setArgumentValue("attributes", attr);
        
        // 4. Adiciona os modulos do ator: init, execute e finish
        fileGenerator.setArgumentValue("initModule", initModule);
        fileGenerator.setArgumentValue("computeModule", computeModule);
        fileGenerator.setArgumentValue("finishModule", finishModule);
        fileGenerator.setArgumentValue("fixpointModule", fixpointModule);

        // 5. especifica o corpo do numberOfConnections
        fileGenerator.setArgumentValue("numberOfConnectionsBody", 
                numberOfConnections);
        fileGenerator.setArgumentValue("nameOfConnectionBody", 
                nameOfConnection);

        // 7. Communication methods body
        fileGenerator.setArgumentValue("receiveBody", receive);
        fileGenerator.setArgumentValue("sendBody", send);       
        fileGenerator.setArgumentValue("sendDelayedBody", sendDelayed);
        fileGenerator.setArgumentValue("canSendBody", canSend);      
        fileGenerator.setArgumentValue("canReceiveBody", canReceive);

        fileGenerator.setArgumentValue("containerName", containerName);
        fileGenerator.setArgumentValue("cname", cname);
        
        return fileGenerator.toString();
    }    
    
    ///////////////////////////// PRIVATE METHODS //////////////////////////////

    /////////////////////////// PRIVATE VARIABLES //////////////////////////////

    private String signals;
    private LinkedList<String> attributes;

    private String initModule;
    private String computeModule;
    private String finishModule;
    private String fixpointModule;
    private String numberOfConnections;
    private String nameOfConnection;
        
    private String receive = "";
    private String send = "";
    private String sendDelayed = "";    
    private String canReceive = "";
    private String canSend = "";
    
    private String containerName = "";
    private String cname = "";
   
    
    protected FileGenerator fileGenerator;
}
