/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package xmleditorgenerator.BaseFiles;

import javax.swing.*;

public abstract class XMLPanel extends JPanel {

   public XMLPanel() {
      super();
   }
   
   public abstract void setDataWarper(Object obj, TransactionManagerContainer editor);
             
   public DataWarper getDataWarper() {
       return dataWarper;
   }

   public abstract void createPanel();

   public abstract int getListElementsSize();
   public abstract int getAttributeElementsSize();
   public abstract int getNecessaryElementsSize();
   public abstract int getOptionalElementsSize();
   
   public void setOwner(JDialog ow) {
       owner = ow;       
   }

   protected DataWarper dataWarper; 
   protected JDialog owner;
}
