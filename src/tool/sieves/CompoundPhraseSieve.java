/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.sieves;

import java.io.IOException;

import tool.util.HashListMap;
import tool.util.Mention;
import tool.util.Terminology;

/**
 * Compound Phrase Sieve.
 * 
 * @author
 */
public class CompoundPhraseSieve extends Sieve {

    /**
     * Constructor. Calls abstract constructor.
     * 
     * @param standardTerminology
     * @param trainTerminology
     * @param normalizedNameToCuiListMap
     * @throws IOException
     */
    public CompoundPhraseSieve(Terminology standardTerminology, Terminology trainTerminology,
            HashListMap normalizedNameToCuiListMap) {
        super(standardTerminology, trainTerminology, normalizedNameToCuiListMap);
    }

    /**
     * TODO: Just implementing dummy constructor for now. Come back during testing
     * iteration if it makes sense.
     */
    public String apply(Mention mention) {
        return "";
    }

    // public String applyNCBI(String name) {
    // String cui = apply(name);
    // if (!cui.equals("") || (!name.contains(" and ") && !name.contains(" or ")))
    // return cui;

    // String compoundWord = name.contains(" and ") ? "and" : "or";
    // String[] nameTokens = name.split("\\s+");
    // int index = Util.getTokenIndex(nameTokens, compoundWord);

    // if (index == 1) {
    // String replacement1 = nameTokens[0];
    // String replacement2 = nameTokens[2].equals("the") ? nameTokens[2] + " " +
    // nameTokens[3] : nameTokens[2];
    // String phrase = replacement1 + " " + compoundWord + " " + replacement2;
    // replacement2 = nameTokens[2].equals("the") ? nameTokens[3] : nameTokens[2];
    // String cui1 = exactMatch(name.replace(phrase, replacement1));

    // String cui2 = exactMatch(name.replace(phrase, replacement2));
    // if (!cui1.equals("") && !cui2.equals("")) {
    // return trainTerminology.cuiToNameListMap.containsKey(cui2 + "|" + cui1) ?
    // cui2 + "|" + cui1
    // : cui1 + "|" + cui2;
    // }
    // }
    // return "";
    // }

    // public String apply(String name) {
    // String cui = getTerminologyNameCui(trainTerminology.compoundNameToCuiListMap,
    // name);
    // if (!cui.equals("")) {
    // return cui;
    // }

    // return getTerminologyNameCui(standardTerminology.compoundNameToCuiListMap,
    // name);
    // }
}
