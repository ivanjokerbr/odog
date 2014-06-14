/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package xmleditorgenerator.BaseFiles;

/**
 *
 * @author ivan
 */
public class Transaction {
    
    /** Para transacoes de add e remove */
    public Transaction(TransactionType type, Object target) {
        transType = type;
        this.target = target;
    }
    
    // para transacoes de change
    public Transaction(TransactionType type, Object target, String element, 
            Object valueBefore, Object valueAfter) {
        transType = type;
        this.target = target;
        changedElement = element;
        this.valueBefore = valueBefore;
        this.valueAfter = valueAfter;
    }
    
    public enum TransactionType { ADD_ELEMENT, REMOVE_ELEMENT, CHANGE_ELEMENT };
    
    //////////////////////////// PUBLIC METHODS ////////////////////////////////
    
    public TransactionType getType() {
        return transType;
    }
    
    public Object getTarget() {
        return target;
    }
    
    public void setTarget(Object node) {
        target = node;
    }
    
    public String getElement() {
        return changedElement;
    }
    
    public Object getValueBefore() {
        return valueBefore;
    }
    
    public Object getValueAfter() {
        return valueAfter;
    }
    
    //////////////////////////// PRIVATE METHODS ///////////////////////////////
    
    
    /////////////////////////// PRIVATE VARIABLES //////////////////////////////
    
    private TransactionType transType;
    
    // o object inserido ou removido
    private Object target;
    
    // em caso de ser uma modificacao em um objeto, qual o elemento (atributo ou 
    // opcional / obrigatorio )
    private String changedElement; 
    private Object valueBefore;
    private Object valueAfter;
}
