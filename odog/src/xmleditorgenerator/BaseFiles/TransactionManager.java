/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package xmleditorgenerator.BaseFiles;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author ivan
 */
public class TransactionManager {
    
    
    public TransactionManager() {
        addElementList = new LinkedList<Transaction>();
        removeElementList = new LinkedList<Transaction>();
        changeElementList = new LinkedList<Transaction>();
    }
    
    //////////////////////////// PUBLIC METHODS ////////////////////////////////
    
    public void addTransaction(Transaction trans) {
        switch(trans.getType()) {            
            
            case ADD_ELEMENT : {
                addElementList.add(trans);
            } break ;
            
            case REMOVE_ELEMENT: {
                removeElementList.add(trans);
            } break;
            
            case CHANGE_ELEMENT: {
                changeElementList.add(trans);
            } break;
        }
    }
    
    public int lastAddTransaction() {
        return addElementList.size();
    }
    
    public int lastRemoveTransaction() {
        return removeElementList.size();
    }
    
    public int lastChangeTransaction() {
        return changeElementList.size();
    }
    
    //////////////////////////// PRIVATE METHODS ///////////////////////////////
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    protected LinkedList<Transaction> addElementList;    
    protected LinkedList<Transaction> removeElementList;    
    protected LinkedList<Transaction> changeElementList;
}
