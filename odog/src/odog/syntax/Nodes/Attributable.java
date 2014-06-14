/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.syntax.Nodes;

import java.util.Iterator;

/**
 *
 * @author ivan
 */
public interface Attributable {
    
    public void addAttribute(Attr at) throws NonUniqueNameException;
    
    public void removeAttribute(Attr at);
    
    public Iterator attributesIterator();
    
    public Attr getAttribute(String name);
}
