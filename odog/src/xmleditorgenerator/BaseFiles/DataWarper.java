/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package xmleditorgenerator.BaseFiles;

import java.util.Iterator;

public abstract class DataWarper {

   // utilizado qdo nao eh lista
   public abstract Object getElement(String type);

   public abstract void setElement(String type, Object value);

   public abstract Object newElement(String type);

   public abstract Iterator elementIterator(String type);

   public abstract String addElement(String type, Object obj);

   public abstract void removeElement(String type, Object obj);

   public abstract String setAttribute(String attr, String value);

   public abstract String getAttribute(String attr);


   public Object getDataElement() {
       return dataElement;
   }

   public void setDataElement(Object obj) {
       dataElement = obj;
   }

   // tem que botar o elemento de dado
   protected Object dataElement;
}
