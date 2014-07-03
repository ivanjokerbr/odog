/*
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2
 * see LICENSE file
 */
package odog.design;

import odog.configuration.BaseConfiguration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * This class stores all designs parsed within the editor. Every time an object
 * needs a design, it must ask to this object. This ensures that the places where
 * a design can be modified, and the places where it is referenced, are synchronized.
 *
 * There are three ways of introducing new designs in this repository:
 *
 *  * addDesign : this method should be called if the design is created, i.e, by
 *   the OdogEditor class.
 *
 *  * findAndParseLib: when the relative name of a library is given, and it must be
 *  searched in the ODOG_LIBRARIES var.
 *
 *  * loadDesignFromFile: when the full filename of the design is known, and it must
 *  be parsed.
 *
 * * @author ivan
 */
public class DesignRepository {

    public DesignRepository(BaseConfiguration configuration) {
        designs = new LinkedList<Design>();
        designsFullPath = new HashMap<String,Design>();

        this.configuration = configuration;
        extraDirs = new LinkedList<String>();
    }

    ///////////////////////////////// PUBLIC METHODS ///////////////////////////
    
    public void addDesign(Design d, String fullPath) {
        if(!designsFullPath.containsKey(fullPath)) {
            designs.add(d);
            designsFullPath.put(fullPath, d);
        }
    }

    public boolean contains(String fullPath) {
        return designsFullPath.containsKey(fullPath);
    }

    public void addExtraDir(String dir) {
        if(!extraDirs.contains(dir)) {
            extraDirs.add(dir);
        }
    }
    
    public void removeExtraDir(String dir) {
        extraDirs.remove(dir);
    }

    // given a relativeURL (name + ".xml") of a design that is a library, look
    // in all ODOG_LIBRARIES dirs to see if it can be found. First, check if its
    // already parsed, by joining the url with the path of a dir.
    public Design findAndParseLib(String relativeLibURL) {
        for(String dir : configuration.getOdogLibraries()) {
            Design d = designsFullPath.get(dir + relativeLibURL);
            if(d != null) {
                return d;
            }
        }

        for(String dir : extraDirs) {
            Design d = designsFullPath.get(dir + relativeLibURL);
            if(d != null) {
                return d;
            }
        }
        
        // not parsed yet. Find the correct library dir
        for(String dir : configuration.getOdogLibraries()) {
            Design d = fromFile(dir + relativeLibURL, true);
            if(d != null) {
                designs.add(d);
                designsFullPath.put(dir + relativeLibURL, d);
                return d;
            }
        }
        return null;
    } 
        
    public Design loadDesignFromFile(String fname, boolean isLib) {
        Design d = designsFullPath.get(fname);
        if(d == null) {
            d = fromFile(fname, isLib);
            if(d != null) {
                designs.add(d);
                designsFullPath.put(fname, d);
                return d;
            }
            else {
                return null;
            }
        }
        return d;
    }
    
    ///////////////////////////////// PRIVATE METHODS ///////////////////////////
    
    private Design fromFile(String fname, boolean isLib) {
        Design ret = null;
       
        StringTokenizer tk = new StringTokenizer(fname, "/");
        StringBuffer buf = new StringBuffer("/");
        String file = null;
        while(true) {
            String s = tk.nextToken();
            if(!tk.hasMoreTokens()) {
                file = s;
                break;
            }
            else {                
                buf.append(s + "/");
            }
        }
        String dir = buf.toString();
        ret = DesignParser.parseDesign(file, dir, isLib, false, 
            this);
        
        return ret;
    }
    
    ///////////////////////////////// PRIVATE VARIABLES /////////////////////////
    
    // this is the list of parsed designs.
    private LinkedList<Design> designs;

    // associates each design with its fullPath
    private HashMap <String, Design> designsFullPath;

    private LinkedList<String> extraDirs;
    
    private BaseConfiguration configuration;
}
