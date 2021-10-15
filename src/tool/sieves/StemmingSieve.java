/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.sieves;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tool.util.HashListMap;
import tool.util.Mention;
import tool.util.Stemmer;
import tool.util.Terminology;

/**
 * Stemming Sieve.
 * 
 * @author
 */
public class StemmingSieve extends Sieve {

    HashListMap stemmedNormalizedNameToCuiListMap;
    Stemmer stemmer;

    /**
     * Constructor. Calls abstract constructor.
     * 
     * @param standardTerminology
     * @param trainTerminology
     * @param normalizedNameToCuiListMap
     * @throws IOException
     */
    public StemmingSieve(Terminology standardTerminology, Terminology trainTerminology,
            HashListMap normalizedNameToCuiListMap, Stemmer stemmer) throws IOException {
        super(standardTerminology, trainTerminology, normalizedNameToCuiListMap);
        this.stemmer = stemmer;
    }

    /**
     * Checks for an exact match in one of the dictionaries after stemming mention
     * text.
     * 
     * @param mention
     */
    public String apply(Mention mention) {

        List<String> stemmedPermutations = new ArrayList<>();

        // Attempt to stem each name permutation.
        for (String name : mention.namePermutations) {
            stemmedPermutations.add(stemmer.stem(name));
        }
        mention.addPermutationList(stemmedPermutations);

        // Try to link permutations to a CUI in one of the dictionaries.
        return normalize(mention.namePermutations);
    }
}
