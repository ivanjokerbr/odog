/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.syntax.Nodes;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author ivan
 */
public interface CompInterface {

    public Iterator getPorts();

    public List<Attr> getInterfaceAttributes();
    
    public String getName();
    
    public String getFullName();

}
