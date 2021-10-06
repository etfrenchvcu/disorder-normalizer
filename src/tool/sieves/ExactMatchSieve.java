/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.sieves;

import tool.util.HashListMap;
import tool.util.Terminology;

/**
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
}
