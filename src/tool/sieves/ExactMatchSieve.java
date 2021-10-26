/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.sieves;

import tool.util.HashListMap;
import tool.util.Mention;
import tool.util.Terminology;

/**
 * Exact Match Sieve
 * 
 * @author
 */
public class ExactMatchSieve extends Sieve {

    /**
     * Constructor. Calls abstract constructor.
     * 
     * @param standardTerminology
     * @param trainTerminology
     * @param normalizedNameToCuiListMap
     */
    public ExactMatchSieve(Terminology standardTerminology, Terminology trainTerminology,
            HashListMap normalizedNameToCuiListMap) {
        super(standardTerminology, trainTerminology, normalizedNameToCuiListMap);
    }

    /**
     * Checks for an exact match in one of the dictionaries for the raw (spelling
     * corrected) mention text.
     */
    public void apply(Mention mention) {
        mention.cui = exactMatch(mention, mention.name);
        if (!mention.cui.equals("")) {
            mention.normalized = true;
        }
    }
}
