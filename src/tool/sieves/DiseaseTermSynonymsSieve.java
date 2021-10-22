/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.sieves;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tool.util.HashListMap;
import tool.util.Mention;
import tool.util.Terminology;

/**
 * Disease Term Synonym Sieve.
 * 
 * @author
 */
public class DiseaseTermSynonymsSieve extends Sieve {

    private final List<String> SINGULAR_DISORDER_TERMS = Arrays.asList("disease", "disorder", "condition", "syndrome",
            "symptom", "abnormality", "NOS", "event", "episode", "issue", "impairment");
    private final List<String> PLURAL_DISORDER_TERMS = Arrays.asList("diseases", "disorders", "conditions", "syndromes",
            "symptoms", "abnormalities", "events", "episodes", "issues", "impairments");

    /**
     * Constructor. Calls abstract constructor.
     * 
     * @param standardTerminology
     * @param trainTerminology
     * @param normalizedNameToCuiListMap
     * @throws IOException
     */
    public DiseaseTermSynonymsSieve(Terminology standardTerminology, Terminology trainTerminology,
            HashListMap normalizedNameToCuiListMap) {
        super(standardTerminology, trainTerminology, normalizedNameToCuiListMap);
    }

    /**
     * Checks for an exact match in one of the dictionaries after permuting the
     * mention text by substituting, appending, and removing disorder terms.
     * 
     * @param mention
     */
    public void apply(Mention mention) {
        List<String> allPermutations = new ArrayList<>();

        // Concatenate lists of disorder terms, plural first.
        var synonyms = new ArrayList<String>(PLURAL_DISORDER_TERMS);
        synonyms.addAll(SINGULAR_DISORDER_TERMS);

        for (String name : mention.namePermutations) {
            var nameTokens = Arrays.asList(name.split("\\s+"));
            var term = findTermInTokens(nameTokens, synonyms);
            if (term != null) {
                // Substitute all disorder term synonyms for the term found.
                for (String synonym : synonyms) {
                    if (!synonym.equals(term))
                        allPermutations.add(name.replace(term, synonym));
                }

                // If term is the last token, drop it from name.
                if (nameTokens.get(nameTokens.size() - 1).equals(term)) {
                    allPermutations.add(String.join(" ", nameTokens.subList(0, nameTokens.size() - 1)));
                }
            } else {
                // No term found, so append one to the end.
                for (String disorderTerm : synonyms) {
                    allPermutations.add(name + " " + disorderTerm);
                }
            }
        }

        // Append unique permutations to the mention object.
        mention.addPermutationList(allPermutations);

        // Try to link permutations to a CUI in one of the dictionaries.
        normalize(mention);
    }

    /**
     * Check if the given list of name tokens contains any of the disorder terms.
     * Returns the first disorder term found, otherwise NULL.
     * 
     * @param nameTokens
     * @param disorderTerms
     * @return
     */
    private String findTermInTokens(List<String> nameTokens, List<String> disorderTerms) {
        String match = null;
        for (String term : disorderTerms) {
            if (nameTokens.contains(term))
                return term;
        }
        return match;
    }

}
