/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.sieves;

import tool.util.HashListMap;
import tool.util.Mention;
import tool.util.Terminology;

/**
 * Abbreviation Expansion Sieve
 * @author
 */
public class AbbreviationExpansionSieve extends Sieve {

    /**
     * Constructor. Calls abstract constructor.
     * 
     * @param standardTerminology
     * @param trainTerminology
     * @param normalizedNameToCuiListMap
     */
    public AbbreviationExpansionSieve(Terminology standardTerminology, Terminology trainTerminology,
            HashListMap normalizedNameToCuiListMap) {
        super(standardTerminology, trainTerminology, normalizedNameToCuiListMap);
    }

    /**
     * Checks for an exact match in one of the dictionaries after expanding abbrevations in the mention text.
     */
    public String apply(Mention mention) {
        return exactMatch(mention.getNameExpansion());
    }
}
