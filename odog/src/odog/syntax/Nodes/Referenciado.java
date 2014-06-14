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
public interface Referenciado {
    
    public void addReference(Referencia no);
    public void removeReference(Referencia no);

}
