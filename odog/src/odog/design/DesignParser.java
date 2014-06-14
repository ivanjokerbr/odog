/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.design;

import odog.configuration.DTDResolver;
import odog.design.DesignRepository;
import odog.editor.configuration.RuleConfiguration;
import odog.editor.configuration.RuleDescription;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import odog.syntax.Nodes.NonUniqueNameException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import odog.syntax.Parse.MyErrorHandler;
import java.io.FileNotFoundException;
import java.util.Iterator;

/**
 *
 * @author ivan
 */
public class DesignParser extends DefaultHandler {

    public DesignParser(String name, String location, DesignRepository repository) {
        design = new Design();
        design.setDesignLocation(location);
        design.setDesignName(name);
        this.designRepository = repository;
    }
    
    /** Creates a new instance of DesignParser */
    public DesignParser(Design d, DesignRepository repository) {
        design = d;
        this.designRepository = repository;
    }
    
    ///////////////////////  PUBLIC METHODS ///////////////////////////////////
    
    public Design getDesign() {
        return design;
    }
    
    public void startElement(
            String uri, 
            String localName, 
            String qName,
            Attributes attributes) throws SAXException {
        Map attmap = getAttributeMap(attributes);
    
        if(qName.equals("design")) {
            String name = (String) attmap.get("name");
            design.setDesignName(name);
        }
        else
        if(qName.equals("configuration")) {
            String isl = (String) attmap.get("islibrary");
            boolean b = (new Boolean(isl)).booleanValue();

            DesignConfiguration configuration = new DesignConfiguration(b);
            design.setConfiguration(configuration);
        }
        else
        if(qName.equals("ruleConfig")) {
            String name = (String) attmap.get("name");
            String syntaxRules = (String) attmap.get("includeSyntaxRules");
            String cgRules = (String) attmap.get("includeCGRules");
            
            RuleConfiguration rconf = new RuleConfiguration(name);
            if(Boolean.valueOf(syntaxRules)) {
                rconf.setIncludeSyntaxRules(true);
                RuleConfiguration.addSyntaxRules(rconf, designRepository);                
            }
            
            if(Boolean.valueOf(cgRules)) {
                rconf.setIncludeCGRules(true);
                RuleConfiguration.addCGRules(rconf, designRepository);                
            }
            DesignConfiguration dc = design.getConfiguration();
            try {
                dc.addRuleConfiguration(rconf);
            }
            catch(NonUniqueNameException ex) {
                System.out.println(ex);
            }
            currentRuleConfiguration = rconf;
        }
        else
        if(qName.equals("ruleDescription")) {
            String name = (String) attmap.get("name");
            String libraryURL = (String) attmap.get("libraryURL");
            
            RuleDescription rd = new RuleDescription(name, libraryURL);        
            try {
                currentRuleConfiguration.addRuleDescription(rd);
            }
            catch(NonUniqueNameException ex) {
                System.out.println(ex);
            }
        }
        else
        if(qName.equals("artifact")) {
            String name = (String) attmap.get("name");
            String type = (String) attmap.get("type");
            String u = (String) attmap.get("url");
            String d = (String) attmap.get("version");
            Date date = new Date(new Long(d));

            artifact = null;
            try {
                URL url = new URL(u);
                if(type.equals("atomic")) {
                    AtomicComponent aa = new AtomicComponent(name, url, date, 
                            design.getDesignLocation());
                    artifact = aa;
                }
                else
                if(type.equals("composite")) {
                    CompositeComponent ca = new CompositeComponent(name, url, date,
                            design.getDesignLocation(), designRepository);
                    artifact = ca;
                }
                else
                if(type.equals("rule")) {
                    RuleElement r = new RuleElement(name, url, date,
                            design.getDesignLocation());
                    artifact = r;
                }
                else {
                    return;
                }
            }
            catch(MalformedURLException ex) {
                System.out.println("...." + ex.getMessage());
            }
            if(!design.addArtifact(artifact)) {
               System.out.println("Project " + design.getDesignName() +  
                        " already contains artifact " + name +
                            "\n");
            }
        }
        else
        if(qName.equals("summary")) {
            sum = true;
        }
        else
        if(qName.equals("ruleCheckingStatus")) {
            String checked = (String) attmap.get("checked");
            RuleCheckingStatus rstat = new RuleCheckingStatus();
            if(!Boolean.parseBoolean(checked)) {
                ((ComponentArtifact)artifact).setRuleCheckingStatus(rstat);
                return;
            }
            String versionChecked = (String) attmap.get("versionChecked");
            Date date = new Date(new Long(versionChecked));

            // the version is outdated
            if(!artifact.getStatus().getVersion().equals(date)) {
                ((ComponentArtifact)artifact).setRuleCheckingStatus(rstat);
                outdated = true;
                return;
            }
            String passed = (String) attmap.get("passed");
            String ruleConfiguration = (String) attmap.get("ruleConfiguration");
            
            rstat.setVersionChecked(date);
            rstat.setChecked(Boolean.parseBoolean(checked));
            rstat.setPassed(Boolean.parseBoolean(passed));
            rstat.setRuleConfiguration(ruleConfiguration);

            RuleConfiguration rc = design.getConfiguration().getRuleConfiguration(
                    ruleConfiguration);
            Iterator rite = rc.ruleDescriptionsIterator();
            while(rite.hasNext()) {
                RuleDescription rd = (RuleDescription) rite.next();
                rstat.addRule(rd.getName());
            }
            ((ComponentArtifact)artifact).setRuleCheckingStatus(rstat);
        }
        else
        if(qName.equals("report")) {
            if(!outdated) {
                report = true;
                reportString = "";
            }
        }
        else {
            throw new SAXException("Invalid XML element.");
        }
    }
    
    public void characters(char [] ch, int start, int length) {
        String s = new String(ch, start, length);
        if(sum) {
            artifact.setSummary(s);
        }
        else
        if(report) {
            reportString = reportString + s;
        }
    }

    public void endElement(
            String uri,
            String localName,
            String qName) {
        
        if(qName.equals("summary")) {
            sum = false;
        }
        else
        if(qName.equals("report")) {
            if(!outdated) {
                ((ComponentArtifact)artifact).getRuleCheckingStatus().setReport(reportString);
            }
            report = false;
        }
        else
        if(qName.equals("artifact")) {
            outdated = false;
        }
        else
        if(qName.equals("design")) {
            design.getStatus().resetChanged();
        }
    }

    public static String errorMessage;

    public static Design parseDesign(String name, String location,
            boolean isLib, boolean addExt, DesignRepository repository) {
        Design d = new Design(name, location, isLib);
        boolean res = parseDesign(d, addExt, repository);
        if(!res) {
            return null;
        }
        return d;
    }
    
    public static boolean parseDesign(Design d, boolean addExt,
            DesignRepository repository) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);

        XMLReader xmlReader = null;
        try {
            SAXParser saxParser = factory.newSAXParser();
            xmlReader = saxParser.getXMLReader();
            xmlReader.setEntityResolver(new DTDResolver());
        } catch (Exception ex) {
            errorMessage = ex.getMessage();
            return false;
        }
        
        DesignParser parser = new DesignParser(d, repository);
        xmlReader.setContentHandler(parser);
        xmlReader.setErrorHandler(new MyErrorHandler(System.err));

        try {
            String file = null;
            if(addExt) {
                file = d.getDesignLocation() + "/" + d.getDesignName() + ".xml";
            }
            else {
                file = d.getDesignLocation() + "/" + d.getDesignName();
            }
            xmlReader.parse(convertToFileURL(file));
        } 
        catch (SAXException se) {
            errorMessage = se.getMessage();
            return false;
        } 
        catch(FileNotFoundException fnf) {
            return false;
        }
        catch (IOException ioe) {
            errorMessage = ioe.getMessage();
            ioe.printStackTrace();
            return false;
        }        
        
        return true;
    }
    
    ///////////////////////  PRIVATE METHODS ///////////////////////////////////
    
     private static String convertToFileURL(String filename) {
        String path = new File(filename).getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file:" + path;
    }
    
    private Map getAttributeMap(Attributes attrs) {
        Map map = new HashMap();
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                map.put(attrs.getQName(i), attrs.getValue(i));
            }
        }
        return map;
    }
    
    ///////////////////////  PRIVATE VARIABLES ////////////////////////////////
    
    private Artifact artifact;

    private DesignRepository designRepository;

    private Design design;
    private boolean sum;
    private boolean report;
    private boolean outdated;
    private String reportString;

    private RuleConfiguration currentRuleConfiguration;
}
