/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.syntax.Nodes;

/**
 *
 * @author ivan
 */
public interface Referencia {
    
    public void unlink(Referenciado obj);
    public void update(Referenciado obj);
    
}
