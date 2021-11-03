/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.sieves;

import java.util.Arrays;
import java.util.List;

import tool.util.Mention;
import tool.util.Terminology;

/**
 * Exact Match Sieve
 * 
 * @author
 */
public class RemoveStopwordsSieve extends Sieve {

    List<String> stopwords;

    /**
     * Constructor. Calls abstract constructor.
     * 
     * @param standardTerminology
     * @param trainTerminology
     */
    public RemoveStopwordsSieve(Terminology standardTerminology, Terminology trainTerminology) {
        super(standardTerminology, trainTerminology);
        stopwords = Arrays.asList("a", "an", "the", "his", "her", "&apos;s", "'s", "this", "these", "any", "patient");
    }

    /**
     * Checks for an exact match in one of the dictionaries after removing stopwords
     * from name.
     */
    public void apply(Mention mention) {
        var swRemoved = "";
        for (var token : mention.name.split("\\s")) {
            token = token.trim();
            if (!stopwords.contains(token)) {
                swRemoved += token + " ";
            } else {
                // Store removed stopword for analysis
                mention.keyPhrase = token;
            }
        }
        swRemoved = swRemoved.trim();
        mention.addPermutation(swRemoved);
        mention.cui = exactMatch(mention, swRemoved);

        // Normalized if exactly one CUI was returned, not a list.
        if (!mention.cui.equals("") && !mention.cui.contains(",")) {
            mention.normalized = true;
        }
    }
}
