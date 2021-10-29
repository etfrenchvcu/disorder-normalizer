/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.sieves;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tool.util.Mention;
import tool.util.Terminology;
import tool.util.Util;

/**
 * Partial Match Sieve.
 * 
 * @author
 */
public class PartialMatchSieve extends Sieve {

    List<String> stopwords;

    /**
     * Constructor. Calls abstract constructor.
     * 
     * @param standardTerminology
     * @param trainTerminology
     * @param stopwords
     * @throws IOException
     */
    public PartialMatchSieve(Terminology standardTerminology, Terminology trainTerminology, List<String> stopwords) {
        super(standardTerminology, trainTerminology);

        this.stopwords = stopwords;
    }

    /**
     * Returns a CUI if exactly one distinct CUI is associated with the set of
     * tokens in the name.
     * 
     * @param mention
     */
    public void apply(Mention mention) {
        String name = mention.nameExpansion != null && mention.nameExpansion.equals("") ? mention.nameExpansion
                : mention.name;
        var cuis = new HashMap<String, Integer>();
        var names = new ArrayList<String>();

        for (String token : name.split("\\s+")) {
            // Skip stopwords.
            if (!stopwords.contains(token)) {

                // Add CUIs from trainTerminology.
                if (trainTerminology.tokenToNameListMap.containsKey(token)) {
                    addCUIsWithToken(token, trainTerminology, cuis, names);
                }

                // Add CUIs from standardTerminology.
                if (standardTerminology.tokenToNameListMap.containsKey(token)) {
                    addCUIsWithToken(token, standardTerminology, cuis, names);
                }

                // TODO: Add CUIs from normalizedNameToCuiListMap?
            }
        }

        // return getBestMatchCui(cuis);
        if (cuis.keySet().size() > 0) {
            mention.cui = String.join(",",cuis.keySet());
            mention.normalized = cuis.keySet().size() == 1;

            // if (mention.normalized)
            //     normalizedNameToCuiListMap.addKeyPair(names.get(0), mention.cui);
        }
    }

    // /**
    //  * Gets the CUI with the associated with the most tokens in the name.
    //  * 
    //  * @param cuis
    //  * @return
    //  */
    // private String getBestMatchCui(HashMap<String, Integer> cuis) {
    //     Map.Entry<String, Integer> bestMatch = null;

    //     for (Map.Entry<String, Integer> entry : cuis.entrySet()) {
    //         if (bestMatch == null || entry.getValue().compareTo(bestMatch.getValue()) > 0) {
    //             // Initialize or when entry has most CUI hits.
    //             bestMatch = entry;
    //         } else if (entry.getValue().compareTo(bestMatch.getValue()) == 0
    //                 && entry.getKey().length() < bestMatch.getKey().length()) {
    //             // Same # CUI hits, but entry is shorter than previous best match.
    //             bestMatch = entry;
    //         }
    //     }

    //     return bestMatch == null ? "" : bestMatch.getKey();
    // }

    /**
     * Add CUIs to the list which correspond to a name in the terminology containing
     * the given token.
     * 
     * @param token
     * @param terminology
     * @param cuis
     * @param names
     */
    private void addCUIsWithToken(String token, Terminology terminology, HashMap<String, Integer> cuis,
            List<String> names) {

        for (var candidateName : terminology.tokenToNameListMap.get(token)) {
            var cui = terminology.nameToCuiListMap.get(candidateName).get(0);
            Util.addUnique(names, candidateName);

            // Initialize map if necessary.
            if (!cuis.keySet().contains(cui))
                cuis.put(cui, 0);

            // Increment cui count.
            cuis.put(cui, cuis.get(cui) + 1);
        }
    }
}
