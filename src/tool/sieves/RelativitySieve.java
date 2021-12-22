/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.sieves;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tool.util.Mention;
import tool.util.Terminology;

/**
 * Relativity Sieve.
 * 
 * @author
 */
public class RelativitySieve extends Sieve {

    List<String> increasingSynonyms;

    /**
     * Constructor. Calls abstract constructor.
     * 
     * @param standardTerminology
     * @param trainTerminology
     * @throws IOException
     */
    public RelativitySieve(Terminology standardTerminology, Terminology trainTerminology) {
        super(standardTerminology, trainTerminology);
        increasingSynonyms = Arrays.asList("copious", "elevated", "increase", "increased", "increasing", "raised",
                "rising", "copious", "spiked", "spiking");
    }

    /**
     * Checks for an exact match in one of the dictionaries after permuting the
     * mention text adding or removing hyphens.
     * 
     * @param mention
     */
    public void apply(Mention mention) {

        List<String> allPermutations = new ArrayList<>();

        for (String name : mention.namePermutations) {
            for (var increasingSynonym : increasingSynonyms) {
                if (name.contains(increasingSynonym)) {
                    for (var replacement : increasingSynonyms) {
                        var permutation = name.replaceAll(name, replacement).trim();
                        allPermutations.add(permutation);
                    }
                }
            }
        }

        // Append unique permutations to the mention object.
        mention.addPermutationList(allPermutations);

        // Try to link permutations to a CUI in one of the dictionaries.
        normalize(mention);
    }
}
