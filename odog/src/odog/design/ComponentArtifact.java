/* 
 * Copyright (c) 2006-2014, Ivan "Joker"
 * All rights reserved.
 *
 * This software is open-source under GNU General Public License, version 2 
 * see LICENSE file
 */
package odog.design;

/**
 *
 * @author ivan
 */
public interface ComponentArtifact {
    
    public RuleCheckingStatus getRuleCheckingStatus();
    
    public void setRuleCheckingStatus(RuleCheckingStatus status);
    
}
